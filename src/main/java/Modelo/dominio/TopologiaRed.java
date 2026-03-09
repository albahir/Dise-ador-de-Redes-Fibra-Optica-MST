package Modelo.dominio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase principal del modelo (Dominio) que representa la topología completa de la red de fibra óptica.
 *  * Actúa como el contenedor central de todos los nodos (equipos) y enlaces (cables).
 * Implementa el patrón Observer (TopologiaListener) para notificar a la interfaz
 * gráfica de cualquier cambio físico o lógico en la red.
 */
public class TopologiaRed implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Colecciones principales de la red
    private final List<Nodo> nodos;
    private final List<Enlace> enlaces; 
    
    // Lista de observadores (transient para que no se guarde en el archivo binario)
    private final transient List<TopologiaListener> listeners = new ArrayList<>();

    /**
     * Constructor por defecto.
     * Inicializa las listas vacías para comenzar un proyecto en blanco.
     */
    public TopologiaRed() {
        this.nodos = new ArrayList<>();
        this.enlaces = new ArrayList<>();
    }

    // =======================================================
    // GESTIÓN BÁSICA DE LA RED (AÑADIR / ELIMINAR)
    // =======================================================

    /**
     * Agrega un nuevo nodo a la red sin realizar validaciones previas.
     * @param nodo El nodo a insertar.
     */
    public void agregarNodo(Nodo nodo) {
        nodos.add(nodo);
    }

    /**
     * Agrega un nuevo nodo a la red verificando que su ID sea único.
     * @param nuevo El nodo a insertar.
     * @throws RuntimeException Si ya existe un equipo con ese mismo ID.
     */
    public void agregarNodoSeguro(Nodo nuevo) {
        if (nodos.stream().anyMatch(n -> n.getId().equals(nuevo.getId()))) {
            throw new RuntimeException("Ya existe un nodo con el ID: " + nuevo.getId());
        }
        nodos.add(nuevo);
        notificarCambios();
    }

    /**
     * Agrega un nuevo enlace (cable virtual o real) a la red.
     * @param enlace El cable a conectar.
     */
    public void agregarEnlace(Enlace enlace) {
        enlaces.add(enlace);
    }

    /**
     * Elimina un nodo de la topología y destruye todos los cables conectados a él.
     * @param nodo El equipo a desmantelar.
     */
    public void eliminarNodo(Nodo nodo) {
        enlaces.removeIf(e -> e.getOrigen().equals(nodo) || e.getDestino().equals(nodo));
        nodos.remove(nodo);
        notificarCambios();
    }

    /**
     * Modifica las propiedades de un nodo existente y reinicia las conexiones
     * de la red para evitar inconsistencias lógicas.
     */
    public void actualizarNodo(String id, TipoNodo nuevoTipo, double nLat, double nLon) {
        for (Nodo n : nodos) {
            if (n.getId().equals(id)) {
                n.setTipo(nuevoTipo);
                n.setLatitud(nLat);
                n.setLongitud(nLon);
                
                // Si cambiamos un equipo, la ruta actual queda obsoleta
                limpiarSoluciones(); 
                notificarCambios();
                break;
            }
        }
    }

    /**
     * Restaura todo el estado de la topología leyendo los datos de un proyecto guardado.
     * Útil al cargar un archivo .fiber desde el disco.
     * @param redCargada El objeto deserializado con la red a restaurar.
     */
    public void cargarEstado(TopologiaRed redCargada) {
        this.nodos.clear();
        this.enlaces.clear();
        
        // Inyectamos los datos del archivo
        this.nodos.addAll(redCargada.getNodos());
        this.enlaces.addAll(redCargada.getEnlaces());
        
        // ¡Al notificar, los Paneles de Telemetría y Costos se actualizarán solos!
        notificarCambios(); 
    }

    // =======================================================
    // MÉTODOS DE BÚSQUEDA Y ESPACIALES (CLICK DEL RATÓN)
    // =======================================================

    /**
     * Busca el primer nodo que se encuentre dentro de un radio en píxeles
     * respecto a una coordenada dada. (Útil para saber si hicimos clic en un equipo).
     * @param x Coordenada X del ratón.
     * @param y Coordenada Y del ratón.
     * @param radio Margen de error o "hitbox" en píxeles.
     * @return El Nodo encontrado, o null si el área está vacía.
     */
    public Nodo buscarNodoEn(int x, int y, int radio) {
        for (Nodo n : nodos) {
            double dist = Math.hypot(n.getX() - x, n.getY() - y);
            if (dist < radio) {
                return n;
            }
        }
        return null;
    }

    /**
     * Busca si el usuario hizo clic sobre la línea gráfica de un cable.
     * Utiliza matemáticas vectoriales para calcular la distancia del clic al segmento.
     * @param x Coordenada X del ratón.
     * @param y Coordenada Y del ratón.
     * @param tolerancia Margen de error en píxeles para "tocar" el cable.
     * @return El Enlace seleccionado, o null si no se tocó ninguno.
     */
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

    /**
     * Fórmula matemática auxiliar para hallar la distancia más corta
     * desde un punto libre (el clic del ratón) hasta un segmento de línea (el cable).
     */
    private double distanciaPuntoSegmento(int px, int py, int x1, int y1, int x2, int y2) {
        double longitudCuadrada = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (longitudCuadrada == 0) {
            return Math.hypot(px - x1, py - y1);
        }
        
        // Calculamos la proyección ortogonal del punto sobre la línea finita
        double t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / longitudCuadrada));
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);
        
        // Retornamos la distancia euclidiana entre el clic y el punto proyectado
        return Math.hypot(px - projX, py - projY);
    }

    // =======================================================
    // LÓGICA DE ALGORITMOS Y FÍSICA DE RED
    // =======================================================

    /**
     * Desmarca todos los enlaces de la red, convirtiéndolos en "cables virtuales" invisibles.
     * Esto resetea el resultado del algoritmo MST.
     */
    public void limpiarSoluciones() {
        for (Enlace e : enlaces) {
            e.desmarcar();
        }
        notificarCambios();
    }

    /**
     * Verifica si un equipo tiene puertos disponibles para conectar un nuevo cable,
     * basándose en la capacidad máxima de su hardware.
     * @param n
     * @return 
     */
    public boolean puedeConectar(Nodo n) {
        long conexionesActuales = enlaces.stream()
                .filter(Enlace::esSolucion) 
                .filter(e -> e.getOrigen().equals(n) || e.getDestino().equals(n))
                .count();
        return conexionesActuales < n.getTipo().getCapacidadMaxima();
    }

    /**
     * Cuando el usuario arrastra un equipo con el ratón, este método recalcula
     * la longitud física (en metros) de todos los cables que están pegados a él.
     * @param nodoMovido El equipo cuyas coordenadas acaban de cambiar.
     */
    public void actualizarFisicaDeCables(Nodo nodoMovido) {
        for (Enlace e : enlaces) {
            if (e.getOrigen().equals(nodoMovido) || e.getDestino().equals(nodoMovido)) {
                e.recalcularFisica();
            }
        }
        notificarCambios(); 
    }

    /**
     * Realiza un test óptico (OTDR virtual) desde la central OLT hasta un nodo destino.
     * Simula el viaje de la luz a través de los cables activos, sumando la pérdida (atenuación)
     * generada por las distancias, los empalmes y los divisores ópticos internos (Splitters/NAPs).
     * @param destino El equipo final donde queremos medir la señal recibida.
     * @return La pérdida total de señal en decibelios (dB), o -1.0 si el nodo está desconectado.
     */
    public double calcularAtenuacionHasta(Nodo destino) {
        Nodo olt = null;
        for (Nodo n : nodos) {
            if (n.getTipo() == Modelo.dominio.TipoNodo.CENTRAL_OLT) {
                olt = n;
                break;
            }
        }
        
        // Si no hay OLT instalada o el destino es la misma OLT, la pérdida inicial es 0
        if (olt == null || olt.equals(destino)) return 0.0;

        // Implementación de un algoritmo BFS (Búsqueda en Anchura) para seguir la ruta de la luz
        java.util.Queue<Nodo> cola = new java.util.LinkedList<>();
        java.util.Map<Nodo, Double> atenuacionAcumulada = new java.util.HashMap<>();
        
        // Variables del Tester Óptico (Para imprimir la auditoría en consola)
        java.util.Map<Nodo, String> historialRuta = new java.util.HashMap<>(); 
        java.util.Set<Nodo> visitados = new java.util.HashSet<>();

        cola.add(olt);
        atenuacionAcumulada.put(olt, 0.0); 
        historialRuta.put(olt, "💡 ORIGEN: " + olt.getId() + " (Potencia inicial = 0.0 dB)\n");
        visitados.add(olt);

        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();

            // Si la luz llegó al equipo que queríamos medir
            if (actual.equals(destino)) {
                // AUDITORÍA EN CONSOLA: Solo imprimimos el recibo final si el destino es un cliente final
                if (destino.getTipo() == Modelo.dominio.TipoNodo.CLIENTE) {
                    System.out.println(historialRuta.get(actual));
                    System.out.println("🛑 TOTAL DE PÉRDIDA EN RECEPTOR " + destino.getId() + ": " + String.format("%.2f", atenuacionAcumulada.get(actual)) + " dB");
                    System.out.println("--------------------------------------------------\n");
                }
                return atenuacionAcumulada.get(actual);
            }

            // Buscar el siguiente tramo del circuito
            for (Enlace e : enlaces) {
                if (!e.esSolucion()) continue; // Solo la luz viaja por los cables verdes definitivos

                Nodo vecino = null;
                if (e.getOrigen().equals(actual)) vecino = e.getDestino();
                else if (e.getDestino().equals(actual)) vecino = e.getOrigen();

                if (vecino != null && !visitados.contains(vecino)) {
                    
                    // 1. Forzamos el recálculo físico para asegurar que usa los precios/pérdidas globales más recientes
                    e.calcularFisicaDelCable(); 
                    
                    // Parche de seguridad para retrocompatibilidad con proyectos viejos (Si nació con 0 dB, forzamos recálculo)
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
        
        // Retorna -1 si el BFS no encontró un camino (El nodo no tiene cables que lo unan a la OLT)
        return -1.0; 
    }

    // =======================================================
    // GETTERS SIMPLES
    // =======================================================

    public List<Nodo> getNodos() { 
        return nodos; 
    }
    
    public List<Enlace> getEnlaces() { 
        return enlaces; 
    }
    
    /**
     * Devuelve EXCLUSIVAMENTE los cables que fueron marcados como definitivos (verdes)
     * por el algoritmo de enrutamiento y descarta las conexiones virtuales temporales.
     * @return 
     */
    public List<Enlace> getEnlacesActivos() {
        List<Enlace> activos = new ArrayList<>();
        for (Enlace e : enlaces) {
            if (e.esSolucion()) {
                activos.add(e);
            }
        }
        return activos;
    }

    // =======================================================
    // PATRÓN OBSERVER (INTERFAZ Y MÉTODOS DE EVENTOS)
    // =======================================================

    /**
     * Interfaz interna (Observer) para que otros componentes, como los paneles gráficos,
     * puedan suscribirse y reaccionar cuando el mapa cambie.
     */
    public interface TopologiaListener {
        void onTopologiaModificada();
    }

    /**
     * Inscribe a un componente visual o controlador para que reciba avisos del modelo.
     * @param listener
     */
    public void addTopologiaListener(TopologiaListener listener) {
        listeners.add(listener);
    }

    /**
     * Dispara la alerta a todos los suscriptores. Obligatorio llamarlo cada vez
     * que se mueve, borra, agrega o enruta algo en la red.
     */
    public void notificarCambios() {
        for (TopologiaListener l : listeners) {
            l.onTopologiaModificada();
        }
    }
}