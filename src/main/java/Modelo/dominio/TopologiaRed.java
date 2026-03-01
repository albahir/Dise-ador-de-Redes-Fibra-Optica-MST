
package Modelo.dominio;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TopologiaRed implements Serializable  {
   private static final long serialVersionUID = 1L;
    private final List<Nodo> nodos;
    private final List<Enlace> enlaces; 
    private final transient List<TopologiaListener> listeners = new ArrayList<>();

    public TopologiaRed() {
        this.nodos = new ArrayList<>();
        this.enlaces = new ArrayList<>();
    }

    public void agregarNodo(Nodo nodo) {
        nodos.add(nodo);
    }

    public void agregarEnlace(Enlace enlace) {
        enlaces.add(enlace);
    }
    

  public void eliminarNodo(Nodo nodo) {
        enlaces.removeIf(e -> e.getOrigen().equals(nodo) || e.getDestino().equals(nodo));
        nodos.remove(nodo);
        notificarCambios();
    }
  public void actualizarNodo(String id, TipoNodo nuevoTipo, double nLat, double nLon) {
    for (Nodo n : nodos) {
        if (n.getId().equals(id)) {
            n.setTipo(nuevoTipo);
            n.setLatitud(nLat);
            n.setLongitud(nLon);
           
            limpiarSoluciones(); 
            notificarCambios();
            break;
        }
    }
}
  
  public Nodo buscarNodoEn(int x, int y, int radio) {
        for (Nodo n : nodos) {
            double dist = Math.hypot(n.getX() - x, n.getY() - y);
            if (dist < radio) return n;
        }
        return null;
    }
  public void agregarNodoSeguro(Nodo nuevo) {
    if (nodos.stream().anyMatch(n -> n.getId().equals(nuevo.getId()))) {
        throw new RuntimeException("Ya existe un nodo con el ID: " + nuevo.getId());
    }
    nodos.add(nuevo);
    notificarCambios();
}
    public void limpiarSoluciones() {
        for (Enlace e : enlaces) {
            e.desmarcar();
        }
        notificarCambios();
    }

    public List<Nodo> getNodos() { return nodos; }
    public List<Enlace> getEnlaces() { return enlaces; }
    
    // Devuelve SOLO los enlaces que forman la red óptima
    public List<Enlace> getEnlacesActivos() {
        List<Enlace> activos = new ArrayList<>();
        for (Enlace e : enlaces) {
            if (e.esSolucion()) {
                activos.add(e);
            }
        }
        return activos;
    }
    public boolean puedeConectar(Nodo n) {
        long conexionesActuales = enlaces.stream()
                .filter(e -> e.esSolucion()) 
                .filter(e -> e.getOrigen().equals(n) || e.getDestino().equals(n))
                .count();
        return conexionesActuales < n.getTipo().getCapacidadMaxima();
    }
public double calcularAtenuacionHasta(Nodo destino) {
        Nodo olt = null;
        for (Nodo n : nodos) {
            if (n.getTipo() == Modelo.dominio.TipoNodo.CENTRAL_OLT) {
                olt = n;
                break;
            }
        }
        
        // Si no hay OLT o el destino es la misma OLT, la pérdida es 0
        if (olt == null || olt.equals(destino)) return 0.0;

        java.util.Queue<Nodo> cola = new java.util.LinkedList<>();
        java.util.Map<Nodo, Double> atenuacionAcumulada = new java.util.HashMap<>();
        
        // ==========================================
        // VARIABLES DEL TESTER ÓPTICO
        // ==========================================
        java.util.Map<Nodo, String> historialRuta = new java.util.HashMap<>(); 
        
        java.util.Set<Nodo> visitados = new java.util.HashSet<>();

        cola.add(olt);
        atenuacionAcumulada.put(olt, 0.0); 
        historialRuta.put(olt, "💡 ORIGEN: " + olt.getId() + " (Potencia inicial = 0.0 dB)\n");
        visitados.add(olt);

        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();

            if (actual.equals(destino)) {
                // AUDITORÍA EN CONSOLA: Solo imprimimos el recibo final si el destino es un cliente
                // (Para no saturar la consola con lecturas a medias de las NAPs)
                if (destino.getTipo() == Modelo.dominio.TipoNodo.CLIENTE) {
                    System.out.println(historialRuta.get(actual));
                    System.out.println("🛑 TOTAL DE PÉRDIDA EN RECEPTOR " + destino.getId() + ": " + String.format("%.2f", atenuacionAcumulada.get(actual)) + " dB");
                    System.out.println("--------------------------------------------------\n");
                }
                return atenuacionAcumulada.get(actual);
            }

            for (Enlace e : enlaces) {
                if (!e.esSolucion()) continue; // Solo nos importan los cables verdes definitivos

                Nodo vecino = null;
                if (e.getOrigen().equals(actual)) vecino = e.getDestino();
                else if (e.getDestino().equals(actual)) vecino = e.getOrigen();

                if (vecino != null && !visitados.contains(vecino)) {
                    
                    // 1. Forzamos el recálculo físico para asegurar que usa la Configuración Global
                    e.calcularFisicaDelCable(); 
                    
                    // Parche de seguridad: Si cargas un proyecto viejo y el equipo nació con 0 dB, lo forzamos a leer la configuración
                    if (vecino.getAtenuacionInterna() == 0.0 && vecino.getTipo() != Modelo.dominio.TipoNodo.CLIENTE) {
                        vecino.configurarAtenuacionPorTipo(); 
                    }

                    // ==========================================
                    // 2. MATEMÁTICA ÓPTICA PURA
                    // ==========================================
                    double perdidaCable = e.getAtenuacionTotalCable(); // Pérdida por kilómetros + empalmes
                    double perdidaEquipo = vecino.getAtenuacionInterna(); // Pérdida por dividir la luz adentro de la caja

                    double atenuacionTotal = atenuacionAcumulada.get(actual) + perdidaCable + perdidaEquipo;
                    
                    // ==========================================
                    // 3. REGISTRO PARA EL TESTER
                    // ==========================================
                    String logAnterior = historialRuta.get(actual);
                    String logPaso = String.format("   ⬇ Viaje por Cable (%.1f m): Pierde %.2f dB\n   ⚙ Entra a %s (%s): Pierde %.2f dB\n   📉 Acumulado: %.2f dB\n", 
                                                    e.getDistancia(), perdidaCable, 
                                                    vecino.getTipo(), vecino.getId(), perdidaEquipo,
                                                    atenuacionTotal);
                                                    
                    historialRuta.put(vecino, logAnterior + logPaso);
                    
                    atenuacionAcumulada.put(vecino, atenuacionTotal);
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
        
        return -1.0; // Falla de conexión (Nodo inalcanzable)
    }
    public Enlace buscarEnlaceEn(int x, int y, int tolerancia) {
        for (Enlace e : enlaces) {
            if (!e.esSolucion()) continue; // Solo nos interesan los cables verdes (activos)

            int x1 = e.getOrigen().getX();
            int y1 = e.getOrigen().getY();
            int x2 = e.getDestino().getX();
            int y2 = e.getDestino().getY();

            double distanciaAlCable = distanciaPuntoSegmento(x, y, x1, y1, x2, y2);
            
            if (distanciaAlCable <= tolerancia) {
                return e;
            }
        }
        return null;
    }

    // Fórmula matemática para hallar la distancia de un punto a una línea
    private double distanciaPuntoSegmento(int px, int py, int x1, int y1, int x2, int y2) {
        double longitudCuadrada = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (longitudCuadrada == 0) return Math.hypot(px - x1, py - y1);
        
        // Calculamos la proyección del punto sobre la línea
        double t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / longitudCuadrada));
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);
        
        // Distancia entre el clic y la proyección
        return Math.hypot(px - projX, py - projY);
    }
    public interface TopologiaListener {
        void onTopologiaModificada();
    }
  

    public void addTopologiaListener(TopologiaListener listener) {
        listeners.add(listener);
    }

    public void notificarCambios() {
        for (TopologiaListener l : listeners) {
            l.onTopologiaModificada();
        }
    }
    public void actualizarFisicaDeCables(Nodo nodoMovido) {
        for (Enlace e : enlaces) {
            if (e.getOrigen().equals(nodoMovido) || e.getDestino().equals(nodoMovido)) {
                e.recalcularFisica();
            }
        }
       
        notificarCambios(); 
    }
    public void cargarEstado(TopologiaRed redCargada) {
        this.nodos.clear();
        this.enlaces.clear();
        
        // Inyectamos los datos del archivo
        this.nodos.addAll(redCargada.getNodos());
        this.enlaces.addAll(redCargada.getEnlaces());
        
        // ¡Al notificar, los Paneles de Telemetría y Costos se actualizarán solos!
        notificarCambios(); 
    }
}