package Modelo.dominio;

/**
 * Enumeración que define los diferentes tipos de dispositivos o hardware
 * que pueden existir en la topología de la red óptica.  * Cada tipo tiene una capacidad por defecto, un nombre legible y un icono asociado para la interfaz.
 */
public enum TipoNodo {
    
    // Central principal que emite la señal óptica
    CENTRAL_OLT(128, "Central OLT", "/icono/server.png"), 
    
    // Caja de distribución principal para clientes finales
    CAJA_NAP(16, "Caja NAP", "/icono/nat.png"),    
    
    // Un empalme o cierre intermedio para extender la red de distribución
    EMPALME(6, "Cierre Empalme", "/icono/router.png"),
    
    // Distribuidor o divisor óptico de primer nivel conectado a la OLT
    SPLITTER_N1(8, "Distribuidor de OLT", "/icono/split.png"),
    
    // El cliente final conectado a la caja NAP mediante cable drop
    CLIENTE(1, "Cliente", "/icono/user.png");
    
    // Atributos inmutables de cada tipo de nodo
    private final int capacidadMaxima;
    private final String nombre;
    private final String rutaIcono; // Ruta relativa de la imagen para la vista gráfica

    /**
     * Constructor de la enumeración.
     * @param capacidadMaxima Capacidad de puertos o conexiones máximas por defecto.
     * @param nombre Nombre legible del dispositivo para mostrar en la UI.
     * @param rutaIcono Ruta del icono para su representación visual.
     */
    TipoNodo(int capacidadMaxima, String nombre, String rutaIcono) {
        this.capacidadMaxima = capacidadMaxima;
        this.nombre = nombre;
        this.rutaIcono = rutaIcono;
    }

    // =======================================================
    // GETTERS
    // =======================================================

    public int getCapacidadMaxima() { 
        return capacidadMaxima; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    
    public String getRutaIcono() { 
        return rutaIcono; 
    }
}