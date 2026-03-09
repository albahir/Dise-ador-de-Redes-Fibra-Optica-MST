
package Modelo.dominio;

import java.io.Serializable;

/**
 * Representa una conexión física (un tramo de fibra óptica) entre dos nodos de la red.
 * Contiene la lógica para calcular la atenuación óptica (pérdida de señal) y los
 * costos asociados a ese tramo basándose en la configuración global.
 */
public final class Enlace implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Constante que define el tamaño estándar de una bobina de fibra en kilómetros
    private static final double DISTANCIA_BOBINA_KM = 2.0; 
    
    // Referencias inmutables a los extremos del enlace
    private final Nodo origen;
    private final Nodo destino;
    
    // Atributos físicos y económicos
    private double distancia; 
    private double costo; 
    private double atenuacionTotalCable;
    private int empalmesAutomaticos;
    
    // Variables de estado algorítmico y visual
    private boolean esParteDelMST; 
    private java.awt.Color colorAlerta = null;

    /**
     * Constructor del enlace.
     * Inicializa las referencias, valida los datos y dispara los cálculos
     * automáticos de costos y atenuación óptica.
     * * @param origen Nodo donde inicia el cable.
     * @param origen
     * @param destino Nodo donde termina el cable.
     * @param distancia Longitud física del cable (usualmente en metros).
     */
    public Enlace(Nodo origen, Nodo destino, double distancia) {
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("Un enlace no puede conectar un nodo consigo mismo.");
        }
        if (distancia < 0) {
            throw new IllegalArgumentException("La distancia debe ser positiva.");
        }
        
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.esParteDelMST = false;
        
        // Calculamos el costo automáticamente al crear el enlace
        actualizarCostoCalculado();
        calcularFisicaDelCable();
    }

    /**
     * Calcula la pérdida total de señal (atenuación) de este tramo específico de cable.
     * Toma en cuenta la distancia pura, la cantidad de empalmes necesarios según
     * el tamaño de las bobinas comerciales, y la pérdida por conectores mecánicos en los extremos.
     */
    public void calcularFisicaDelCable() {
        // Obtenemos los valores vigentes del Singleton
        ConfiguracionGlobal config = ConfiguracionGlobal.getInstance();

        double distanciaKm = this.distancia / 1000.0;
        
        // 1. Pérdida por la longitud de la fibra misma (Atenuación pura)
        double perdidaDistancia = distanciaKm * config.getAtenuacionPorKm();
        
        // 2. Pérdida por empalmes de fusión (Se requiere un empalme por cada bobina de fibra agotada)
        this.empalmesAutomaticos = (int) Math.floor(distanciaKm / DISTANCIA_BOBINA_KM);
        double perdidaEmpalmes = this.empalmesAutomaticos * config.getPerdidaPorEmpalme();
        
        // 3. Pérdida por conectores (Asumimos 2 conectores mecánicos obligatorios por tramo, uno en cada punta)
        double perdidaConectores = 2 * config.getPerdidaPorConector();
        
        this.atenuacionTotalCable = perdidaDistancia + perdidaEmpalmes + perdidaConectores;
    }

    /**
     * Actualiza el costo económico del enlace tomando el precio actual por metro
     * registrado en la configuración global.
     */
    public void actualizarCostoCalculado() {
        ConfiguracionGlobal config = ConfiguracionGlobal.getInstance();
        this.costo = this.distancia * config.getCostoMetroFibra();
    }

    /**
     * Recalcula la distancia física y el costo basado en la posición en pantalla
     * de los nodos de origen y destino utilizando geometría euclidiana (Hipotenusa).
     * El resultado de píxeles se multiplica por 2 para dar un estimado en metros.
     */
    public void recalcularFisica() {
        double distPixeles = Math.hypot(origen.getX() - destino.getX(), origen.getY() - destino.getY());
        this.distancia = distPixeles * 2; 
        actualizarCostoCalculado(); 
    }

    /**
     * Devuelve la pérdida de señal generada EXCLUSIVAMENTE por la distancia,
     * ignorando conectores o empalmes.
     * * @return La pérdida en decibelios (dB) producto solo del trayecto.
     * @return 
     */
    public double getPerdidaDB() {
        ConfiguracionGlobal config = ConfiguracionGlobal.getInstance();
        double distanciaKm = this.distancia / 1000.0;
        return distanciaKm * config.getAtenuacionPorKm();
    }

    /**
     * Si le proporcionamos uno de los nodos extremos, este método nos 
     * devuelve el nodo que se encuentra en la otra punta del cable.
     * * @param n El nodo conocido.
     * @param n
     * @return El nodo opuesto, o null si el nodo provisto no pertenece a este enlace.
     */
    public Nodo getOponente(Nodo n) {
        if (n.equals(origen)) return destino;
        if (n.equals(destino)) return origen;
        return null;
    }

    // =======================================================
    // MÉTODOS DE ESTADO (SOLUCIÓN Y ALERTAS)
    // =======================================================

    /**
     * Marca el enlace como seleccionado/activo dentro de la topología resuelta por el algoritmo.
     */
    public void marcarComoSolucion() {
        this.esParteDelMST = true;
    }

    /**
     * Desmarca el enlace, indicando que fue descartado por el algoritmo o que la red fue limpiada.
     */
    public void desmarcar() {
        this.esParteDelMST = false;
    }

    public boolean esSolucion() {
        return esParteDelMST;
    }
    
    public void setColorAlerta(java.awt.Color colorAlerta) { 
        this.colorAlerta = colorAlerta; 
    }
    
    public void limpiarAlerta() { 
        this.colorAlerta = null; 
    }

    // =======================================================
    // GETTERS SIMPLES
    // =======================================================

    public double getAtenuacionTotalCable() { 
        return atenuacionTotalCable; 
    }
    
    public Nodo getOrigen() { 
        return origen; 
    }
    
    public Nodo getDestino() { 
        return destino; 
    }
    
    public double getCosto() { 
        return costo; 
    }
    
    public double getDistancia() { 
        return distancia; 
    }
    
    public java.awt.Color getColorAlerta() { 
        return colorAlerta; 
    }

    @Override
    public String toString() {
        return origen.getId() + " <--> " + destino.getId() + " ($" + costo + ")";
    }
}