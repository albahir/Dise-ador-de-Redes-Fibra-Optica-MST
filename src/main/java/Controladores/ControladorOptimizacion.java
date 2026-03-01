package Controladores;

import Modelo.Algoritmos.IMSTStrategy;
import Modelo.dominio.Enlace;
import Modelo.dominio.Nodo;
import Modelo.dominio.TipoNodo;
import Modelo.dominio.TopologiaRed;
import Vista.VentanaPrincipal;
import Util.UI.PaletaTema;
import java.awt.Color;

import javax.swing.*;
import java.util.List;

public class ControladorOptimizacion {
    private final TopologiaRed modelo;
    private final VentanaPrincipal vista;
    

    public ControladorOptimizacion(TopologiaRed modelo, VentanaPrincipal vista) {
        this.modelo = modelo;
        this.vista = vista;
        
        initListeners();

        // PASO 2.1: Suscripción en tiempo real
        // Cada vez que el modelo llame a 'notificarCambios()', se ejecutará el cálculo
        this.modelo.addTopologiaListener(() -> actualizarMetricasGlobales());
        
        // Carga inicial de datos
        actualizarMetricasGlobales();
    }
    private void initListeners() {
       vista.getBtnEjecutarPrim().addActionListener(e -> ejecutarAlgoritmo(new Modelo.Algoritmos.PrimStrategy()));
        
        vista.getBtnLimpiarMapa().addActionListener(e -> limpiarConexiones());
    }
    private void actualizarMetricasGlobales() {
        SwingUtilities.invokeLater(() -> {
       int totalNodos = (int) modelo.getNodos().stream()
                .filter(n -> n.getTipo() != Modelo.dominio.TipoNodo.CLIENTE)
                .count();
        
        // Contar clientes usando el Enum TipoNodo para mayor precisión
        int totalClientes = (int) modelo.getNodos().stream()
                .filter(n -> n.getTipo() == Modelo.dominio.TipoNodo.CLIENTE)
                .count();
        int clientesConectados = (int) modelo.getNodos().stream()
                .filter(n -> n.getTipo() == Modelo.dominio.TipoNodo.CLIENTE)
                .filter(cliente -> modelo.getEnlacesActivos().stream()
                        .anyMatch(e -> e.getOrigen().equals(cliente) || e.getDestino().equals(cliente)))
                .count();
        // CÁLCULO DE COSTOS Y DISTANCIA (Resuelve el problema del PanelCostos)
        double costoTotal = modelo.getEnlacesActivos().stream().mapToDouble(Enlace::getCosto).sum();
        double longitudTotalMetros = modelo.getEnlacesActivos().stream().mapToDouble(Enlace::getDistancia).sum();
        
       // 1. Limpiamos todas las alertas visuales antes de iniciar la nueva auditoría
      // 1. Limpiamos todas las alertas visuales antes de iniciar la nueva auditoría
        modelo.getNodos().forEach(Nodo::limpiarAlerta);
        modelo.getEnlaces().forEach(Enlace::limpiarAlerta);

        Nodo olt = modelo.getNodos().stream().filter(n -> n.getTipo() == Modelo.dominio.TipoNodo.CENTRAL_OLT).findFirst().orElse(null);
        double peorAtenuacion = 0.0;

        // ========================================================
        // PRE-CÁLCULO: Detectar quiénes son los VERDADEROS Huérfanos
        // ========================================================
        long clientesHuerfanos = 0;
        long napsHuerfanas = 0;
        long splittersHuerfanos = 0;

        for (Nodo n : modelo.getNodos()) {
            // ¿El algoritmo Prim evaluó este nodo y le trazó cables virtuales?
            boolean fueEvaluado = modelo.getEnlaces().stream()
                    .anyMatch(e -> e.getOrigen().equals(n) || e.getDestino().equals(n));
            
            // ¿El algoritmo Prim finalmente lo conectó con un cable real (verde)?
            boolean tieneCableActivo = modelo.getEnlacesActivos().stream()
                    .anyMatch(e -> e.getOrigen().equals(n) || e.getDestino().equals(n));
            
            // Solo es huérfano si Prim lo vio, pero lo dejó por fuera
            if (fueEvaluado && !tieneCableActivo) {
                if (n.getTipo() == Modelo.dominio.TipoNodo.CLIENTE) clientesHuerfanos++;
                else if (n.getTipo() == Modelo.dominio.TipoNodo.CAJA_NAP) napsHuerfanas++;
                else if (n.getTipo() == Modelo.dominio.TipoNodo.SPLITTER_N1) splittersHuerfanos++;
            }
        }

        // ========================================================
        // AUDITORÍA PROFUNDA DE NODOS (ESCENARIO A)
        // ========================================================
        for (Nodo n : modelo.getNodos()) {
            if (n.equals(olt)) continue; 

            boolean fueEvaluado = modelo.getEnlaces().stream()
                    .anyMatch(e -> e.getOrigen().equals(n) || e.getDestino().equals(n));
            boolean tieneCableActivo = modelo.getEnlacesActivos().stream()
                    .anyMatch(e -> e.getOrigen().equals(n) || e.getDestino().equals(n));

            // ESCENARIO A.1: Nodos sin conexión
            if (!tieneCableActivo) {
                if (!fueEvaluado) {
                    // ¡CORRECCIÓN! Recién agregado al mapa -> GRIS (Neutral)
                    n.setColorAlerta(new Color(100, 100, 100)); 
                } else {
                    // Prim intentó conectarlo pero no habían puertos -> AMARILLO (Huérfano)
                    n.setColorAlerta(new Color(255, 215, 0)); 
                }
                continue;
            }

            double atenuacion = modelo.calcularAtenuacionHasta(n);

            // ESCENARIO A.2: Falla de origen (Cables conectados pero sin OLT)
            if (atenuacion == -1.0) {
                n.setColorAlerta(new Color(255, 215, 0)); 
                continue;
            }

            // ESCENARIO A.3: Problemas de Atenuación Óptica
            double umbralMaximo = Modelo.dominio.ConfiguracionGlobal.getInstance().getUmbralSensibilidad();

            if (n.getTipo() == Modelo.dominio.TipoNodo.CLIENTE) {
                if (atenuacion > peorAtenuacion) peorAtenuacion = atenuacion;
                
                if (atenuacion > umbralMaximo) n.setColorAlerta(PaletaTema.NEON_ROJO);
            } 
            else if (n.getTipo() == Modelo.dominio.TipoNodo.CAJA_NAP) {
                // Las NAPs acumulan mucha pérdida interna, se deben evaluar con el umbral máximo de la red
                if (atenuacion > umbralMaximo) n.setColorAlerta(PaletaTema.NEON_ROJO);
            }
        }

        // ========================================================
        // ESCENARIO B: SATURACIÓN INTELIGENTE (Culpables)
        // ========================================================
        for (Nodo n : modelo.getNodos()) {
            if (n.getTipo() == Modelo.dominio.TipoNodo.CLIENTE|| n.getTipo() == Modelo.dominio.TipoNodo.EMPALME)  continue;

            long conexionesActivas = modelo.getEnlacesActivos().stream()
                    .filter(e -> e.getOrigen().equals(n) || e.getDestino().equals(n))
                    .count();

            int limiteConexiones = n.getMaxPuertosIn() + n.getMaxPuertosOut();

            // Si el equipo está lleno al 100%
            if (conexionesActivas >= limiteConexiones) {
                
                boolean hayDemandaInsatisfecha = false;
                
                // Verificamos si la saturación de este equipo está dejando a alguien por fuera
                if (n.getTipo() == Modelo.dominio.TipoNodo.CAJA_NAP && clientesHuerfanos > 0) hayDemandaInsatisfecha = true;
                if (n.getTipo() == Modelo.dominio.TipoNodo.SPLITTER_N1 && napsHuerfanas > 0) hayDemandaInsatisfecha = true;
                if (n.getTipo() == Modelo.dominio.TipoNodo.CENTRAL_OLT && (splittersHuerfanos > 0 || napsHuerfanas > 0)) hayDemandaInsatisfecha = true;

                // Solo alumbra naranja si está lleno Y es el culpable de que alguien esté huérfano
                if (hayDemandaInsatisfecha && n.getColorAlerta() != PaletaTema.NEON_ROJO) {
                    n.setColorAlerta(new Color(255, 165, 0)); 
                }
            }
        }
        // ========================================================
        // ESCENARIO C: EL CABLE ASESINO
        // ========================================================
        for (Enlace e : modelo.getEnlacesActivos()) {
            // Un tramo único de fibra GPON no debería perder más de 4 dB por sí solo (Aprox 13 KM)
            if (e.getPerdidaDB() > 4.0 || e.getDistancia() > 20000) {
                e.setColorAlerta(PaletaTema.NEON_ROJO); // La línea se vuelve roja
            }
        }
        double umbralSensibilidad = Modelo.dominio.ConfiguracionGlobal.getInstance().getUmbralSensibilidad();
        // DETERMINACIÓN DEL ESTADO LÓGICO
        String estado = "ÓPTIMA";
        Color colorEstado = PaletaTema.NEON_VERDE;
        
        if (totalNodos == 0) {
            estado = "SIN EQUIPOS";
            colorEstado = PaletaTema.TEXTO_GRIS;
        } else if (modelo.getEnlacesActivos().isEmpty()) {
            estado = "DESCONECTADA";
            colorEstado = PaletaTema.TEXTO_GRIS;
        } else if (peorAtenuacion > umbralSensibilidad) {
            estado = "CRÍTICA (Pérdida)";
            colorEstado = PaletaTema.NEON_ROJO;
        }else if (clientesConectados < totalClientes) {
           
            estado = "SATURADA";
            colorEstado = new Color(255, 165, 0); 
        }

        // ACTUALIZACIÓN DE PANELES (UI)
        if (vista.getPanelTelemetria() != null) {
            vista.getPanelTelemetria().actualizarTelemetria(
                totalNodos,clientesConectados,totalClientes, peorAtenuacion, estado, colorEstado
            );
        }
        
        if (vista.getPanelCostos() != null) {
            
            vista.getPanelCostos().actualizarCostos(costoTotal, longitudTotalMetros);
        }
        if (vista.getPanelMapa() != null) {
            vista.getPanelMapa().repaint();
        }
        
    
        });
    }

    private void ejecutarAlgoritmo(IMSTStrategy estrategia) {
        if (modelo.getNodos().size() < 2) {
            Util.UI.GestorAlertas.mostrarAdvertencia(vista, "Datos Insuficientes", "Añada al menos 2 nodos al mapa para poder enrutar la red.");
            return;
        }

        // 1. Bloqueamos el botón y avisamos que inició la animación
        vista.getBtnEjecutarPrim().setEnabled(false);
        vista.setMensajeEstado("Trazando red óptima paso a paso...", PaletaTema.NEON_AZUL);

        generarEnlacesValidos();

        // 2. ¡LA MAGIA! Mandamos el cálculo a un hilo secundario
      new Thread(() -> {
            try {
                estrategia.ejecutar(modelo); // Ejecuta Prim

                // 3. Al terminar con ÉXITO, volvemos a la interfaz para habilitar todo
                SwingUtilities.invokeLater(() -> {
                    if (vista.getPanelMapa() != null) vista.getPanelMapa().repaint();
                    modelo.notificarCambios(); 
                    vista.setMensajeEstado(estrategia.getNombre() + " finalizado.", PaletaTema.NEON_VERDE);
                    vista.getBtnEjecutarPrim().setEnabled(true); 
                });

            } catch (Exception ex) {
                // 4. SI OCURRE UN ERROR (Ej. Falta la OLT), lo atrapamos y avisamos a la interfaz
                SwingUtilities.invokeLater(() -> {
                    Util.UI.GestorAlertas.mostrarError(vista, "Error de Enrutamiento", ex.getMessage());
                    vista.setMensajeEstado("Enrutamiento abortado por falta de hardware.", PaletaTema.NEON_ROJO);
                    vista.getBtnEjecutarPrim().setEnabled(true); // Desbloqueamos el botón
                    
                    // Limpiamos los cables invisibles que se habían generado
                    modelo.getEnlaces().clear();
                    if (vista.getPanelMapa() != null) vista.getPanelMapa().repaint();
                });
            }
        }).start();
    }
    private void generarEnlacesValidos() {
        modelo.getEnlaces().clear(); 
        List<Nodo> nodos = modelo.getNodos();
        
        for (int i = 0; i < nodos.size(); i++) {
            for (int j = i + 1; j < nodos.size(); j++) {
                Nodo n1 = nodos.get(i);
                Nodo n2 = nodos.get(j);
                
               
          
                if (!esConexionValida(n1, n2)) {
                    continue; 
                }
                
                // Si la conexión es válida, calculamos la distancia y creamos el enlace virtual
                double distPixeles = Math.hypot(n1.getX() - n2.getX(), n1.getY() - n2.getY());
                double distanciaMetros = distPixeles * 2;
                
                modelo.agregarEnlace(new Enlace(n1, n2, distanciaMetros));
            }
        }
    }
    // Este método dicta las LEYES INMUTABLES de cómo se puede conectar una red FTTH
    private boolean esConexionValida(Nodo origen, Nodo destino) {
        TipoNodo t1 = origen.getTipo();
        TipoNodo t2 = destino.getTipo();

        // Regla 0: Nunca conectar un nodo consigo mismo
        if (origen.equals(destino)) return false;

        // Regla 1: OLT solo alimenta Empalmes o Splitters
        if (t1 == TipoNodo.CENTRAL_OLT && (t2 == TipoNodo.EMPALME || t2 == TipoNodo.SPLITTER_N1)) return true;
        if (t2 == TipoNodo.CENTRAL_OLT && (t1 == TipoNodo.EMPALME || t1 == TipoNodo.SPLITTER_N1)) return true;

        // Regla 2: Un Empalme puede conectar otro Empalme (para largas distancias) o un Splitter
        if (t1 == TipoNodo.EMPALME && (t2 == TipoNodo.EMPALME || t2 == TipoNodo.SPLITTER_N1 || t2 == TipoNodo.CAJA_NAP)) return true;
        
        // Regla 3: El Splitter N1 alimenta a las Cajas NAP
        if (t1 == TipoNodo.SPLITTER_N1 && t2 == TipoNodo.CAJA_NAP) return true;
        if (t2 == TipoNodo.SPLITTER_N1 && t1 == TipoNodo.CAJA_NAP) return true;

        // Regla 4: Las Cajas NAP alimentan a los Clientes (Cable Drop)
        if (t1 == TipoNodo.CAJA_NAP && t2 == TipoNodo.CLIENTE) return true;
        if (t2 == TipoNodo.CAJA_NAP && t1 == TipoNodo.CLIENTE) return true;

        // Cualquier otra combinación es ILEGAL (Ej. Cliente con Cliente, OLT con Cliente directo)
        return false;
    }
    private void limpiarConexiones() {
        modelo.getEnlaces().clear();
        modelo.limpiarSoluciones();
        modelo.notificarCambios();
        if (vista.getPanelMapa() != null) {
            vista.getPanelMapa().repaint();
        }
        
        // Resetear ambos paneles separados
        int nodosActuales = modelo.getNodos().size();
        
        if (vista.getPanelTelemetria() != null) {
            vista.getPanelTelemetria().actualizarTelemetria(
                nodosActuales,0, 0, 0.0, "DESCONECTADA", PaletaTema.TEXTO_GRIS
            );
        }
        
        if (vista.getPanelCostos() != null) {
            vista.getPanelCostos().actualizarCostos(0.0, 0.0);
        }
        
        vista.setMensajeEstado("Conexiones limpiadas. Nodos intactos.", PaletaTema.TEXTO_GRIS);
    }
    // Genera una malla completa (grafo completo) para que el algoritmo elija
}