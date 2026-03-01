package Modelo.Algoritmos;

import Modelo.dominio.Enlace;
import Modelo.dominio.Nodo;
import Modelo.dominio.TipoNodo;
import Modelo.dominio.TopologiaRed;

import java.util.*;

public class KruskalStrategy implements IMSTStrategy {

    @Override
    public String getNombre() {
        return "Algoritmo de Kruskal (Con Restricción de Puertos)";
    }

    @Override
    public void ejecutar(TopologiaRed red) {
        // 1. Limpiamos soluciones anteriores
        red.limpiarSoluciones();
        
        List<Enlace> todosLosEnlaces = new ArrayList<>(red.getEnlaces());
        List<Nodo> nodos = red.getNodos();

        // 2. Ordenamos los cables de MENOR a MAYOR costo (clave de Kruskal)
        todosLosEnlaces.sort(Comparator.comparingDouble(Enlace::getCosto));

        // 3. Inicializamos la estructura para detectar ciclos (Union-Find)
        UnionFind uf = new UnionFind(nodos);

        // --- ¡NUEVO! REGLA 3.1: CONTADOR DE PUERTOS ---
        // Mapa para registrar cuántas conexiones tiene cada nodo en el MST actual
        Map<String, Integer> conexionesPorNodo = new HashMap<>();
        for (Nodo n : nodos) {
            conexionesPorNodo.put(n.getId(), 0);
        }

        int aristasSeleccionadas = 0;
        int objetivo = nodos.size() - 1; // Un árbol tiene V-1 aristas

        // 4. Iteramos sobre los cables ordenados
        for (Enlace cable : todosLosEnlaces) {
            if (aristasSeleccionadas >= objetivo) break;

            Nodo n1 = cable.getOrigen();
            Nodo n2 = cable.getDestino();

            // Verificamos si los nodos ya están en el mismo grupo (para evitar ciclos)
            if (uf.find(n1) != uf.find(n2)) {
                
                // --- VALIDACIÓN DE PUERTOS (Degree Constraint) ---
                if (puedeConectarse(n1, conexionesPorNodo.get(n1.getId())) && 
                    puedeConectarse(n2, conexionesPorNodo.get(n2.getId()))) {
                    
                    // Si ambos tienen puertos libres, los unimos
                    uf.union(n1, n2);
                    cable.marcarComoSolucion();
                    aristasSeleccionadas++;
                    
                    // Actualizamos los contadores de puertos utilizados
                    conexionesPorNodo.put(n1.getId(), conexionesPorNodo.get(n1.getId()) + 1);
                    conexionesPorNodo.put(n2.getId(), conexionesPorNodo.get(n2.getId()) + 1);
                } else {
                    // Si la NAP o el Cliente están llenos, Kruskal ignorará este cable barato
                    // y buscará el siguiente cable más barato en la lista.
                }
            }
        }
        
        // Opcional: Podrías añadir lógica aquí para advertir si aristasSeleccionadas < objetivo
        // (es decir, si quedaron clientes desconectados porque no había suficientes puertos NAP).
    }

    /**
     * Verifica si un nodo aún tiene puertos disponibles físicos.
     * @param nodo El equipo a evaluar.
     * @param conexionesActuales Cuántas conexiones ya le hemos asignado en el árbol.
     * @return true si tiene puertos libres, false si está saturado.
     */
    private boolean puedeConectarse(Nodo nodo, int conexionesActuales) {
        if (null != nodo.getTipo()) switch (nodo.getTipo()) {
            case CLIENTE -> {
                // Un cliente solo puede tener 1 conexión (su cable Drop hacia la NAP)
                return conexionesActuales < 1;
            }
            case CAJA_NAP -> {
                // Una NAP tiene puertos limitados (ej. 16 puertos para clientes + 1 puerto troncal)
                // Asumimos que getCapacidadMaxima() devuelve el total de puertos del splitter
                return conexionesActuales <= nodo.getCapacidad();
            }
            case CENTRAL_OLT -> {
                // La OLT tiene alta capacidad (ej. 128 por puerto PON)
                return conexionesActuales <= nodo.getCapacidad();
            }
            default -> {
            }
        }
        // Para empalmes, podríamos usar su capacidad o dejarlos sin límite estricto
        return true; 
    }

    // --- Estructura Auxiliar: Union-Find (Disjoint Set) ---
    private static class UnionFind {
        private final Map<String, String> padre = new HashMap<>();

        public UnionFind(List<Nodo> nodos) {
            for (Nodo n : nodos) {
                padre.put(n.getId(), n.getId());
            }
        }

        public String find(Nodo n) {
            String raiz = n.getId();
            while (!padre.get(raiz).equals(raiz)) {
                raiz = padre.get(raiz);
            }
            String actual = n.getId();
            while (!actual.equals(raiz)) {
                String siguiente = padre.get(actual);
                padre.put(actual, raiz);
                actual = siguiente;
            }
            return raiz;
        }

        public void union(Nodo n1, Nodo n2) {
            String raiz1 = find(n1);
            String raiz2 = find(n2);
            if (!raiz1.equals(raiz2)) {
                padre.put(raiz1, raiz2);
            }
        }
    }
}