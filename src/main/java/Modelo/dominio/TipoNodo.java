/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo.dominio;


public enum TipoNodo {
    // Usamos 'Building.png' o 'server.png' para la central principal
    CENTRAL_OLT(128, "Central OLT", "/icono/server.png"), 
    
    // Asumo que 'NAT.png' representa tu caja de distribución principal
    CAJA_NAP(16, "Caja NAP", "/icono/nat.png"),    
    
    // Un empalme o router intermedio
    EMPALME(6, "Cierre Empalme", "/icono/router.png"),
    SPLITTER_N1(8,"Distribuidor de OLT","/icono/split.png"),
    // El cliente final conectado por cable
    CLIENTE(1, "Cliente", "/icono/user.png");
    
   

    private final int capacidadMaxima;
    private final String nombre;
    private final String rutaIcono; // Nuevo atributo para la ruta de la imagen

    TipoNodo(int capacidadMaxima, String nombre, String rutaIcono) {
        this.capacidadMaxima = capacidadMaxima;
        this.nombre = nombre;
        this.rutaIcono = rutaIcono;
    }

    public int getCapacidadMaxima() { return capacidadMaxima; }
    public String getNombre() { return nombre; }
    public String getRutaIcono() { return rutaIcono; } // Nuevo getter para la vista
}