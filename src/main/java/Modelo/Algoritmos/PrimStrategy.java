package Modelo.Algoritmos;

import Modelo.dominio.Enlace;
import Modelo.dominio.Nodo;
import Modelo.dominio.TipoNodo;
import Modelo.dominio.TopologiaRed;

import java.util.*;

public class PrimStrategy implements IMSTStrategy {

    @Override
    public String getNombre() {
        return "Algoritmo de Prim (Direccional y Puertos)";
    }
@Override
    public void ejecutar(TopologiaRed red) {
        red.limpiarSoluciones();
        java.util.List<Nodo> nodos = red.getNodos();
        if (nodos.isEmpty()) return;

        // 1. Buscar la fuente de luz (La OLT)
        Nodo olt = nodos.stream()
                .filter(n -> n.getTipo() == TipoNodo.CENTRAL_OLT)
                .findFirst()
                .orElse(null);

        
           if (olt == null) {
           
            throw new IllegalStateException("No se puede iniciar el enrutamiento: ¡Falta la CENTRAL OLT en el mapa!");
           }
        boolean haySplitter = nodos.stream()
                .anyMatch(n -> n.getTipo() == TipoNodo.SPLITTER_N1);
                
        if (!haySplitter) {
            throw new IllegalStateException("No se puede iniciar el enrutamiento:\nDebe colocar al menos un Distribuidor (Splitter N1) en el mapa para ramificar la conexión de la OLT.");
        }

        Set<String> nodosConLuz = new HashSet<>();
        Map<String, Integer> puertosOutUsados = new HashMap<>();
        for (Nodo n : nodos) {
            puertosOutUsados.put(n.getId(), 0);
        }

        PriorityQueue<Enlace> frontera = new PriorityQueue<>(Comparator.comparingDouble(Enlace::getCosto));

        // Variables del Tracer
        double costoTotalSimulacion = 0.0;
        double distanciaTotalSimulacion = 0.0;
        int pasoTracer = 1;
        System.out.println("\n==================================================");
        System.out.println("🚀 INICIANDO SIMULACIÓN PRIM: " + olt.getId());
        System.out.println("==================================================");

        // 2. Encender la OLT
        nodosConLuz.add(olt.getId());
        agregarCablesVecinos(olt, red, nodosConLuz, frontera);

        // 3. Bucle principal
        while (!frontera.isEmpty()) {
            Enlace mejorCable = frontera.poll();

            Nodo origen = mejorCable.getOrigen();
            Nodo destino = mejorCable.getDestino();

            Nodo padre = null;
            Nodo hijo = null;

            if (nodosConLuz.contains(origen.getId()) && !nodosConLuz.contains(destino.getId())) {
                padre = origen;
                hijo = destino;
            } else if (nodosConLuz.contains(destino.getId()) && !nodosConLuz.contains(origen.getId())) {
                padre = destino;
                hijo = origen;
            }

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
                    
                    // CONEXIÓN EXITOSA
                    nodosConLuz.add(hijo.getId()); 
                    mejorCable.marcarComoSolucion(); 
                    puertosOutUsados.put(padre.getId(), puertosOutUsados.get(padre.getId()) + 1);
                    
                    costoTotalSimulacion += mejorCable.getCosto();
                    distanciaTotalSimulacion += mejorCable.getDistancia();
                    
                    System.out.println("--- [PASO " + pasoTracer + "] ---");
                    System.out.println("🔌 CONEXIÓN VÁLIDA: " + padre.getId() + " (" + padre.getTipo() + ") ---> " + hijo.getId() + " (" + hijo.getTipo() + ")");
                    System.out.println("📏 CABLE: " + String.format("%.2f", mejorCable.getDistancia()) + " m | Costo tramo: $" + String.format("%.2f", mejorCable.getCosto()));
                    System.out.println("🔋 PUERTOS PADRE: Usados " + puertosOutUsados.get(padre.getId()) + " de " + padre.getMaxPuertosOut());
                    System.out.println("💰 ACUMULADO RED: Metros: " + String.format("%.2f", distanciaTotalSimulacion) + " m | Inversión: $" + String.format("%.2f", costoTotalSimulacion));
                    
                    pasoTracer++;

                    // Expandir la luz al siguiente equipo válido
                    agregarCablesVecinos(hijo, red, nodosConLuz, frontera);

                    red.notificarCambios(); 
                    
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

    // Método auxiliar para buscar cables de un nodo hacia la oscuridad
    private void agregarCablesVecinos(Nodo nodo, TopologiaRed red, Set<String> nodosConLuz, PriorityQueue<Enlace> frontera) {
       if (nodo.getTipo() == TipoNodo.CLIENTE) {
            return; 
        }
        for (Enlace e : red.getEnlaces()) {
            if (e.getOrigen().equals(nodo) || e.getDestino().equals(nodo)) {
                Nodo otroExtremo = (e.getOrigen().equals(nodo)) ? e.getDestino() : e.getOrigen();
                // Solo nos interesan cables que van hacia equipos apagados
                if (!nodosConLuz.contains(otroExtremo.getId())) {
                    frontera.add(e);
                }
            }
        }
    }
    private boolean esJerarquiaOpticaValida(TipoNodo tipoPadre, TipoNodo tipoHijo) {
        if (tipoPadre == null || tipoHijo == null) return false;

        switch (tipoPadre) {
            case CENTRAL_OLT -> {
                // La OLT solo alimenta Splitters o pasa por Empalmes
                return tipoHijo == TipoNodo.SPLITTER_N1 || tipoHijo == TipoNodo.EMPALME;
            }
            case SPLITTER_N1 -> {
                // El Splitter alimenta NAPs, Empalmes, u otros Splitters (Cascada)
                return tipoHijo == TipoNodo.CAJA_NAP || tipoHijo == TipoNodo.EMPALME || tipoHijo == TipoNodo.SPLITTER_N1;
            }
            case CAJA_NAP -> {
                // La NAP ÚNICAMENTE alimenta Clientes finales (o empalmes de Drop)
                return tipoHijo == TipoNodo.CLIENTE || tipoHijo == TipoNodo.EMPALME;
            }
            case EMPALME -> {
                // Un empalme es transparente, la luz sigue su curso (pero nunca de regreso a la OLT)
                return tipoHijo != TipoNodo.CENTRAL_OLT; 
            }
            case CLIENTE -> {
                // Por doble seguridad: Un cliente jamás puede ser padre
                return false;
            }
            default -> {
                return false;
            }
        }
    }

}