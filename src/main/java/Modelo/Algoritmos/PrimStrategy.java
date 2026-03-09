package Modelo.Algoritmos;

import Modelo.dominio.Enlace;
import Modelo.dominio.Nodo;
import Modelo.dominio.TipoNodo;
import Modelo.dominio.TopologiaRed;

import java.util.*;

/**
 * Implementación concreta del algoritmo de Prim adaptado para redes ópticas.
 *  * A diferencia del algoritmo de Prim clásico, esta versión incorpora reglas de negocio
 * específicas: direccionalidad de la luz (jerarquía óptica) y límite físico de puertos.
 */
public class PrimStrategy implements IMSTStrategy {

    @Override
    public String getNombre() {
        return "Algoritmo de Prim (Direccional y Puertos)";
    }

    /**
     * Ejecuta el algoritmo iterativo de Prim para conectar la red de forma óptima
     * desde la central (OLT) hacia los clientes, respetando las restricciones físicas.
     * * @param red La topología de red que contiene los nodos y los enlaces posibles.
     */
    @Override
    public void ejecutar(TopologiaRed red) {
        // Limpiamos cualquier enrutamiento previo antes de iniciar
        red.limpiarSoluciones();
        java.util.List<Nodo> nodos = red.getNodos();
        if (nodos.isEmpty()) return;

        // 1. Buscar la fuente de luz principal (La OLT)
        Nodo olt = nodos.stream()
                .filter(n -> n.getTipo() == TipoNodo.CENTRAL_OLT)
                .findFirst()
                .orElse(null);

        // Validaciones iniciales críticas para que el algoritmo pueda operar
        if (olt == null) {
            throw new IllegalStateException("No se puede iniciar el enrutamiento: ¡Falta la CENTRAL OLT en el mapa!");
        }
        
        boolean haySplitter = nodos.stream()
                .anyMatch(n -> n.getTipo() == TipoNodo.SPLITTER_N1);
                
        if (!haySplitter) {
            throw new IllegalStateException("No se puede iniciar el enrutamiento:\nDebe colocar al menos un Distribuidor (Splitter N1) en el mapa para ramificar la conexión de la OLT.");
        }

        // Estructuras de datos para el control del estado del algoritmo
        Set<String> nodosConLuz = new HashSet<>();
        Map<String, Integer> puertosOutUsados = new HashMap<>();
        
        // Inicializamos el contador de puertos usados en 0 para todos los nodos
        for (Nodo n : nodos) {
            puertosOutUsados.put(n.getId(), 0);
        }

        // Cola de prioridad (Frontera) que siempre nos dará el cable más barato/corto disponible
        PriorityQueue<Enlace> frontera = new PriorityQueue<>(Comparator.comparingDouble(Enlace::getCosto));

        // Variables de seguimiento (Tracer) para la consola
        double costoTotalSimulacion = 0.0;
        double distanciaTotalSimulacion = 0.0;
        int pasoTracer = 1;
        
        System.out.println("\n==================================================");
        System.out.println("🚀 INICIANDO SIMULACIÓN PRIM: " + olt.getId());
        System.out.println("==================================================");

        // 2. Encender la OLT y buscar sus posibles conexiones iniciales
        nodosConLuz.add(olt.getId());
        agregarCablesVecinos(olt, red, nodosConLuz, frontera);

        // 3. Bucle principal de expansión del árbol
        while (!frontera.isEmpty()) {
            // Extraemos el enlace más económico de los disponibles en la frontera
            Enlace mejorCable = frontera.poll();

            Nodo origen = mejorCable.getOrigen();
            Nodo destino = mejorCable.getDestino();

            Nodo padre = null;
            Nodo hijo = null;

            // Determinamos la dirección del flujo de luz: de un nodo "iluminado" a uno "apagado"
            if (nodosConLuz.contains(origen.getId()) && !nodosConLuz.contains(destino.getId())) {
                padre = origen;
                hijo = destino;
            } else if (nodosConLuz.contains(destino.getId()) && !nodosConLuz.contains(origen.getId())) {
                padre = destino;
                hijo = origen;
            }

            // Si encontramos una dirección válida de propagación
            if (padre != null && hijo != null) {
                
                // =======================================================
                // AUDITORÍA SEMÁNTICA 1: LEY DE JERARQUÍA DIRECCIONAL
                // =======================================================
                if (!esJerarquiaOpticaValida(padre.getTipo(), hijo.getTipo())) {
                    // Silenciamos el log para que no ensucie la consola, simplemente ignoramos este cable ilegal
                    continue; 
                }

                // =======================================================
                // AUDITORÍA SEMÁNTICA 2: LÍMITE DE PUERTOS FÍSICOS
                // =======================================================
                if (puertosOutUsados.get(padre.getId()) < padre.getMaxPuertosOut()) {
                    
                    // CONEXIÓN EXITOSA: El cable pasa todas las validaciones
                    nodosConLuz.add(hijo.getId()); 
                    mejorCable.marcarComoSolucion(); 
                    puertosOutUsados.put(padre.getId(), puertosOutUsados.get(padre.getId()) + 1);
                    
                    costoTotalSimulacion += mejorCable.getCosto();
                    distanciaTotalSimulacion += mejorCable.getDistancia();
                    
                    // Imprimir progreso en consola
                    System.out.println("--- [PASO " + pasoTracer + "] ---");
                    System.out.println("🔌 CONEXIÓN VÁLIDA: " + padre.getId() + " (" + padre.getTipo() + ") ---> " + hijo.getId() + " (" + hijo.getTipo() + ")");
                    System.out.println("📏 CABLE: " + String.format("%.2f", mejorCable.getDistancia()) + " m | Costo tramo: $" + String.format("%.2f", mejorCable.getCosto()));
                    System.out.println("🔋 PUERTOS PADRE: Usados " + puertosOutUsados.get(padre.getId()) + " de " + padre.getMaxPuertosOut());
                    System.out.println("💰 ACUMULADO RED: Metros: " + String.format("%.2f", distanciaTotalSimulacion) + " m | Inversión: $" + String.format("%.2f", costoTotalSimulacion));
                    
                    pasoTracer++;

                    // Expandir la luz: buscar a dónde podemos ir desde este nuevo nodo iluminado
                    agregarCablesVecinos(hijo, red, nodosConLuz, frontera);

                    red.notificarCambios(); 
                    
                    // Pausa para animar la visualización en la interfaz gráfica
                    try {
                        int pausaMs = Modelo.dominio.ConfiguracionGlobal.getInstance().getVelocidadAnimacionMs();
                        Thread.sleep(pausaMs);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.out.println("⚠️ SATURACIÓN: " + padre.getId() + " llenó sus puertos, ignorando conexión a " + hijo.getId());
                }
            }
        }
        
        System.out.println("==================================================");
        System.out.println("🏁 SIMULACIÓN FINALIZADA SIN ERRORES JERÁRQUICOS");
        System.out.println("==================================================\n");
    }

    /**
     * Método auxiliar para buscar los cables conectados a un nodo y agregarlos a la cola 
     * de prioridad (frontera), siempre y cuando el otro extremo aún no tenga luz (no haya sido visitado).
     * * @param nodo El nodo recién iluminado que va a propagar la señal.
     * @param red La topología que contiene todos los enlaces posibles.
     * @param nodosConLuz Conjunto de nodos que ya forman parte del árbol.
     * @param frontera La cola de prioridad que ordena los enlaces disponibles por costo.
     */
    private void agregarCablesVecinos(Nodo nodo, TopologiaRed red, Set<String> nodosConLuz, PriorityQueue<Enlace> frontera) {
        // Los clientes son hojas del árbol, no pueden repartir luz hacia adelante
        if (nodo.getTipo() == TipoNodo.CLIENTE) {
            return; 
        }
        
        for (Enlace e : red.getEnlaces()) {
            // Verificamos si este cable está conectado al nodo actual
            if (e.getOrigen().equals(nodo) || e.getDestino().equals(nodo)) {
                Nodo otroExtremo = (e.getOrigen().equals(nodo)) ? e.getDestino() : e.getOrigen();
                
                // Solo nos interesan cables que van hacia equipos apagados (evita ciclos)
                if (!nodosConLuz.contains(otroExtremo.getId())) {
                    frontera.add(e);
                }
            }
        }
    }

    /**
     * Evalúa si la conexión entre dos tipos de equipos respeta la jerarquía lógica 
     * de una red de fibra óptica.
     *      * * @param tipoPadre El equipo que emitirá la luz (origen).
     * @param tipoHijo El equipo que recibirá la luz (destino).
     * @return true si la conexión es válida según las leyes de la red, false en caso contrario.
     */
    private boolean esJerarquiaOpticaValida(TipoNodo tipoPadre, TipoNodo tipoHijo) {
        if (tipoPadre == null || tipoHijo == null) return false;

        switch (tipoPadre) {
            case CENTRAL_OLT -> {
                // La OLT solo alimenta Splitters o pasa por Empalmes
                return tipoHijo == TipoNodo.SPLITTER_N1 || tipoHijo == TipoNodo.EMPALME;
            }
            case SPLITTER_N1 -> {
                // El Splitter alimenta NAPs, Empalmes, u otros Splitters (Arquitectura en cascada)
                return tipoHijo == TipoNodo.CAJA_NAP || tipoHijo == TipoNodo.EMPALME || tipoHijo == TipoNodo.SPLITTER_N1;
            }
            case CAJA_NAP -> {
                // La NAP ÚNICAMENTE alimenta Clientes finales (o pasa por empalmes de Cable Drop)
                return tipoHijo == TipoNodo.CLIENTE || tipoHijo == TipoNodo.EMPALME;
            }
            case EMPALME -> {
                // Un empalme es transparente, la luz sigue su curso (pero nunca regresa hacia una OLT)
                return tipoHijo != TipoNodo.CENTRAL_OLT; 
            }
            case CLIENTE -> {
                // Por doble seguridad: Un cliente es un sumidero final y jamás puede actuar como padre
                return false;
            }
            default -> {
                return false;
            }
        }
    }
}