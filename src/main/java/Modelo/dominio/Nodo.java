
package Modelo.dominio;



import java.io.Serializable;
import java.util.Objects;

public class Nodo implements Serializable  {
    private static final long serialVersionUID = 1L;
    private final String id;
    private double latitud;
    private double longitud;
    private TipoNodo tipo;
    private int capacidad;
    private int x;
    private int y;
    private double atenuacionInterna;
    private java.awt.Color colorAlerta = null;
    public Nodo(String id, double latitud, double longitud, TipoNodo tipo) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("El ID del nodo no puede estar vacío.");
        if (tipo == null) throw new IllegalArgumentException("El tipo de nodo es obligatorio.");
        this.id = id;
        this.latitud = latitud;
        this.longitud = longitud;
        this.tipo = tipo;
    }
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public String getId() { return id; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public TipoNodo getTipo() { return tipo; }
    public int getCapacidad() {
        return capacidad;
    }
    public int getMaxPuertosIn() {
        if (this.tipo == TipoNodo.CENTRAL_OLT) return 0; // La OLT genera la luz, no la recibe
        return 1; // NAP, Empalmes y Clientes reciben exactamente 1 cable Uplink/Drop
    }

    public int getMaxPuertosOut() {
        if (this.tipo == TipoNodo.CLIENTE) return 0; // El cliente es el fin de la línea
        return this.capacidad; // OLT, NAP y Empalmes usan la capacidad que configuraste en la interfaz
    }
    public java.awt.Color getColorAlerta() {
        return colorAlerta;
    }

 

    public void limpiarAlerta() {
        this.colorAlerta = null;
    }
    //Setters
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public void setTipo(TipoNodo tipo) { this.tipo = tipo; }
    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
   public void configurarAtenuacionPorTipo() {
        if (this.tipo == null) return;
        
        // ¡ÚNICA FUENTE DE VERDAD! Leemos del Singleton
        ConfiguracionGlobal config = ConfiguracionGlobal.getInstance();
        
        switch (this.tipo) {
            case CENTRAL_OLT:
                this.atenuacionInterna = 0.0; 
                break;
            case SPLITTER_N1:
                this.atenuacionInterna = config.getPerdidaSplitterN1(); 
                break;
            case CAJA_NAP:
                this.atenuacionInterna = config.getPerdidaCajaNAP(); 
                break;
            case CLIENTE:
                this.atenuacionInterna = 0.0; 
                break;
            default:
                this.atenuacionInterna = 0.0;
        }
    }
    public double getAtenuacionInterna() { return atenuacionInterna; }
    public void setColorAlerta(java.awt.Color colorAlerta) {
        this.colorAlerta = colorAlerta;
    }
    @Override
    public String toString() {
        return id + " (" + tipo + ")";
    }

    // Necesario para que los algoritmos sepan si dos nodos son el mismo
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nodo nodo = (Nodo) o;
        return Objects.equals(id, nodo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
