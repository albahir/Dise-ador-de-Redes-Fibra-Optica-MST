
package Modelo.dominio;
import java.io.Serializable;

public final class Enlace implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Nodo origen;
    private final Nodo destino;
    private double distancia; 
    private double costo; 
    private boolean esParteDelMST; 
    private java.awt.Color colorAlerta = null;
    private static final double DISTANCIA_BOBINA_KM = 2.0; 
    private int empalmesAutomaticos;
    private double atenuacionTotalCable;

   public Enlace(Nodo origen, Nodo destino, double distancia) {
        if (origen.equals(destino)) throw new IllegalArgumentException("Un enlace no puede conectar un nodo consigo mismo.");
        if (distancia < 0) throw new IllegalArgumentException("La distancia debe ser positiva.");
        
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        
        this.esParteDelMST = false;
        
        // Calculamos el costo automáticamente al crear el enlace
        actualizarCostoCalculado();
        calcularFisicaDelCable();
    }
   
 public void calcularFisicaDelCable() {
        // OBTENEMOS LA CONFIGURACIÓN ACTUAL
        ConfiguracionGlobal config = ConfiguracionGlobal.getInstance();

        double distanciaKm = this.distancia / 1000.0;
        
        // Usamos los valores dinámicos del Singleton
        double perdidaDistancia = distanciaKm * config.getAtenuacionPorKm();
        
        this.empalmesAutomaticos = (int) Math.floor(distanciaKm / DISTANCIA_BOBINA_KM);
        double perdidaEmpalmes = this.empalmesAutomaticos * config.getPerdidaPorEmpalme();
        
        // 2 conectores * pérdida por conector
        double perdidaConectores = 2 * config.getPerdidaPorConector();
        
        this.atenuacionTotalCable = perdidaDistancia + perdidaEmpalmes + perdidaConectores;
    }

    // Métodos para marcar la solución
    public void marcarComoSolucion() {
        this.esParteDelMST = true;
    }

    public void desmarcar() {
        this.esParteDelMST = false;
    }

    public boolean esSolucion() {
        return esParteDelMST;
    }
  public void actualizarCostoCalculado() {
        ConfiguracionGlobal config = ConfiguracionGlobal.getInstance();
        // Costo = Distancia en metros * Precio por metro global
        this.costo = this.distancia * config.getCostoMetroFibra();
    }

    // Cálculo de pérdida de señal en este tramo
   public double getPerdidaDB() {
        ConfiguracionGlobal config = ConfiguracionGlobal.getInstance();
        double distanciaKm = this.distancia / 1000.0;
        return distanciaKm * config.getAtenuacionPorKm();
    }
   
    public void setColorAlerta(java.awt.Color colorAlerta) { this.colorAlerta = colorAlerta; }
    // Getters
    public Nodo getOponente(Nodo n) {
        if (n.equals(origen)) return destino;
        if (n.equals(destino)) return origen;
        return null;
    }
    public double getAtenuacionTotalCable() { return atenuacionTotalCable; }
    public Nodo getOrigen() { return origen; }
    public Nodo getDestino() { return destino; }
    public double getCosto() { return costo; }
    public double getDistancia() { return distancia; }
    public java.awt.Color getColorAlerta() { return colorAlerta; }
   
    public void recalcularFisica() {
        double distPixeles = Math.hypot(origen.getX() - destino.getX(), origen.getY() - destino.getY());
        this.distancia = distPixeles * 2; 
        actualizarCostoCalculado(); 
    }
    public void limpiarAlerta() { this.colorAlerta = null; }
    @Override
    public String toString() {
        return origen.getId() + " <--> " + destino.getId() + " ($" + costo + ")";
    }
}