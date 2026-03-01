/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo.Algoritmos;


import Modelo.dominio.TopologiaRed;

public interface IMSTStrategy {
    /**
     * Ejecuta el algoritmo de Árbol de Expansión Mínima.
     * Modifica la red marcando los enlaces seleccionados como "activos".
     * * @param red La topología sobre la cual operar.
     * @param red
     */
    void ejecutar(TopologiaRed red);
    
    // Para mostrar en el combo box de la interfaz gráfica
    String getNombre();
}