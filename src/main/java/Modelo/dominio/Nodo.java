package Modelo.dominio;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa un equipo físico individual dentro de la red óptica (OLT, NAP, Cliente, etc.).
 * Almacena su identificación, ubicación en el mapa (x, y), capacidad operativa,
 * y se encarga de calcular su atenuación óptica interna.  */
public class Nodo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Identificador único del nodo en el sistema (ej. "OLT-1", "NAP-A")
    private final String id;
    
    // Coordenadas geográficas
    private double latitud;
    private double longitud;
    
    // Clasificación y características del hardware
    private TipoNodo tipo;
    private int capacidad;
    
    // Coordenadas en píxeles para el renderizado en el Panel de Diseño
    private int x;
    private int y;
    
    // Variables de cálculo físico y estado de la interfaz gráfica
    private double atenuacionInterna;
    private java.awt.Color colorAlerta = null;

    /**
     * Constructor principal del Nodo.
     * Inicializa los datos básicos y aplica validaciones estrictas para evitar nodos corruptos.
     * @param id Identificador único del equipo.
     * @param latitud Coordenada de latitud.
     * @param longitud Coordenada de longitud.
     * @param tipo El rol de hardware que representa este nodo dentro de la red.
     */
    public Nodo(String id, double latitud, double longitud, TipoNodo tipo) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del nodo no puede estar vacío.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de nodo es obligatorio.");
        }
        
        this.id = id;
        this.latitud = latitud;
        this.longitud = longitud;
        this.tipo = tipo;
    }

    // =======================================================
    // LÓGICA DE NEGOCIO Y CÁLCULOS
    // =======================================================

    /**
     * Configura la pérdida de señal (atenuación) intrínseca del equipo
     * basándose en su tipo de hardware y leyendo los valores oficiales de la Configuración Global.
     */
    public void configurarAtenuacionPorTipo() {
        if (this.tipo == null) {
            return;
        }
        
        // ¡ÚNICA FUENTE DE VERDAD! Leemos del Singleton para mantener consistencia
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

    /**
     * Determina la cantidad máxima de conexiones de ENTRADA (Uplink) que puede recibir este equipo.
     * @return El número de puertos de entrada permitidos.
     */
    public int getMaxPuertosIn() {
        if (this.tipo == TipoNodo.CENTRAL_OLT) {
            return 0; // La OLT genera la luz principal, no recibe cables de entrada en este modelo
        }
        return 1; // NAP, Empalmes y Clientes reciben exactamente 1 cable Uplink/Drop
    }

    /**
     * Determina la cantidad máxima de conexiones de SALIDA (Downlink) que puede emitir este equipo.
     * @return El número de puertos de salida o derivación permitidos.
     */
    public int getMaxPuertosOut() {
        if (this.tipo == TipoNodo.CLIENTE) {
            return 0; // El cliente es el fin de la línea, no distribuye luz
        }
        return this.capacidad; // OLT, NAP y Empalmes usan la capacidad operativa que se les configuró
    }

    // =======================================================
    // MÉTODOS DE ESTADO (UI)
    // =======================================================

    public void limpiarAlerta() {
        this.colorAlerta = null;
    }

    // =======================================================
    // GETTERS Y SETTERS
    // =======================================================

    public int getX() { 
        return x; 
    }
    
    public void setX(int x) { 
        this.x = x; 
    }

    public int getY() { 
        return y; 
    }
    
    public void setY(int y) { 
        this.y = y; 
    }

    public String getId() { 
        return id; 
    }

    public double getLatitud() { 
        return latitud; 
    }
    
    public void setLatitud(double latitud) { 
        this.latitud = latitud; 
    }

    public double getLongitud() { 
        return longitud; 
    }
    
    public void setLongitud(double longitud) { 
        this.longitud = longitud; 
    }

    public TipoNodo getTipo() { 
        return tipo; 
    }
    
    public void setTipo(TipoNodo tipo) { 
        this.tipo = tipo; 
    }

    public int getCapacidad() {
        return capacidad;
    }
    
    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public double getAtenuacionInterna() { 
        return atenuacionInterna; 
    }

    public java.awt.Color getColorAlerta() {
        return colorAlerta;
    }
    
    public void setColorAlerta(java.awt.Color colorAlerta) {
        this.colorAlerta = colorAlerta;
    }

    // =======================================================
    // MÉTODOS SOBRESCRITOS (Object)
    // =======================================================

    @Override
    public String toString() {
        return id + " (" + tipo + ")";
    }

    /**
     * Compara si dos nodos son la misma entidad lógica.
     * Fundamental para que los algoritmos de enrutamiento y estructuras de datos 
     * (como Set y Map) identifiquen un nodo basándose exclusivamente en su ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Nodo nodo = (Nodo) o;
        return Objects.equals(id, nodo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}