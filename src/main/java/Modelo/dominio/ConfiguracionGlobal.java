package Modelo.dominio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ConfiguracionGlobal implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String RUTA_ARCHIVO = "configuracion_fiber.dat";
    // Instancia única (Singleton)
    private static ConfiguracionGlobal instancia;

    // --- PARÁMETROS ECONÓMICOS ---
    private double costoMetroFibra = 1.50; // $1.50 USD por metro lineal
    private double costoInstalacionBase = 50.0; // Costo fijo por nodo (mano de obra)
    
    // --- PARÁMETROS FÍSICOS (ÓPTICOS) ---
    private double atenuacionPorKm = 0.35; // dB/km (Estándar G.652)
    private double perdidaPorEmpalme = 0.1; // dB por fusión
    private double perdidaPorConector = 0.5; // dB por conector mecánico
    private double umbralSensibilidad = 28.0; // Límite donde el cliente se queda sin servicio 
    private double perdidaSplitterN1 = 10.5; // Splitter 1:8 por defecto
    private double perdidaCajaNAP = 14.0;
    private int velocidadAnimacionMs = 300;
    // Constructor privado para evitar que nadie más cree instancias
    private ConfiguracionGlobal() {}

    public static synchronized ConfiguracionGlobal getInstance() {
       if (instancia == null) {
            instancia = cargarDesdeArchivo();
            
            // Si el archivo no existe o está corrupto, usamos los predefinidos y CREAMOS el archivo
            if (instancia == null) {
                instancia = new ConfiguracionGlobal();
                instancia.guardarEnDisco(); 
            }
        }
        return instancia;
    }
    
    private static ConfiguracionGlobal cargarDesdeArchivo() {
        File archivo = new File(RUTA_ARCHIVO);
        if (archivo.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                return (ConfiguracionGlobal) ois.readObject();
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudo leer la configuración, se usarán los valores por defecto.");
            }
        }
        return null; // Retorna null si no existe
    }

    // Método Público: Para sobrescribir el archivo cuando el usuario edite
    public void guardarEnDisco() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_ARCHIVO))) {
            oos.writeObject(this);
            System.out.println("Configuración guardada en disco exitosamente.");
        } catch (IOException e) {
            System.err.println("Error crítico al guardar configuración: " + e.getMessage());
        }
    }

    // --- GETTERS Y SETTERS ---
    public double getCostoMetroFibra() { return costoMetroFibra; }
    public void setCostoMetroFibra(double costo) { this.costoMetroFibra = costo; }

    public double getCostoInstalacionBase() { return costoInstalacionBase; }
    public void setCostoInstalacionBase(double costo) { this.costoInstalacionBase = costo; }

    public double getAtenuacionPorKm() { return atenuacionPorKm; }
    public void setAtenuacionPorKm(double atenuacion) { this.atenuacionPorKm = atenuacion; }

    public double getPerdidaPorEmpalme() { return perdidaPorEmpalme; }
    public void setPerdidaPorEmpalme(double perdida) { this.perdidaPorEmpalme = perdida; }

    public double getPerdidaPorConector() { return perdidaPorConector; }
    public void setPerdidaPorConector(double perdida) { this.perdidaPorConector = perdida; }

    public double getUmbralSensibilidad() { return umbralSensibilidad; }
    public void setUmbralSensibilidad(double umbral) { this.umbralSensibilidad = umbral; }
    public double getPerdidaSplitterN1() { return perdidaSplitterN1; }
    public void setPerdidaSplitterN1(double perdida) { this.perdidaSplitterN1 = perdida; }

    public double getPerdidaCajaNAP() { return perdidaCajaNAP; }
    public void setPerdidaCajaNAP(double perdida) { this.perdidaCajaNAP = perdida; }  
    // Método para inyectar una configuración cargada desde archivo (opcional para el futuro)
    public int getVelocidadAnimacionMs() { return velocidadAnimacionMs; }
    public void setVelocidadAnimacionMs(int velocidadAnimacionMs) { this.velocidadAnimacionMs = velocidadAnimacionMs; }
    public static void cargarConfiguracion(ConfiguracionGlobal config) {
        instancia = config;
    }
}
