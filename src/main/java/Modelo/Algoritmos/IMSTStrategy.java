package Modelo.Algoritmos;

import Modelo.dominio.TopologiaRed;

/**
 * Interfaz que define la estrategia para los algoritmos de enrutamiento.
 * Utiliza el patrón de diseño Strategy para permitir la implementación de 
 * múltiples algoritmos de Árbol de Expansión Mínima (MST).
 */
public interface IMSTStrategy {
    
    /**
     * Ejecuta el algoritmo de Árbol de Expansión Mínima.
     * Modifica la red marcando los enlaces seleccionados como "activos".
     * * @param red La topología de red sobre la cual operar y trazar la ruta.
     * @param red
     */
    void ejecutar(TopologiaRed red);
    
    /**
     * Obtiene el nombre legible del algoritmo.
     * Útil para mostrar las opciones disponibles en el ComboBox de la interfaz gráfica.
     * * @return El nombre descriptivo del algoritmo.
     * @return 
     */
    String getNombre();
}