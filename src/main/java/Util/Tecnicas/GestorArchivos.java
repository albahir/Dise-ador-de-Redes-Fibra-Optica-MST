package Util.Tecnicas;

import Modelo.dominio.TopologiaRed;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilitaria para gestionar la persistencia de datos de la topología.
 * Se encarga de guardar y cargar los objetos completos (TopologiaRed) 
 * utilizando los mecanismos de serialización binaria de Java.
 */
public class GestorArchivos {

    // Directorio relativo donde se almacenarán los archivos del proyecto
    private static final String DIRECTORIO_PROYECTOS = "proyectos";

    // =======================================================
    // MÉTODOS PÚBLICOS
    // =======================================================

    /**
     * Serializa la topología actual de la red y la guarda en un archivo binario en el disco.
     * @param red El objeto modelo completo a guardar.
     * @param nombreArchivo El nombre del archivo (se le añadirá automáticamente '.fiber' si no lo tiene).
     * @return true si la operación fue exitosa, false en caso de error.
     */
    public static boolean guardarProyecto(TopologiaRed red, String nombreArchivo) {
        asegurarDirectorio();
        
        // Le forzamos la extensión de dominio propio .fiber
        if (!nombreArchivo.endsWith(".fiber")) {
            nombreArchivo += ".fiber";
        }
        
        File archivo = new File(DIRECTORIO_PROYECTOS + File.separator + nombreArchivo);
        
        // Uso de try-with-resources para asegurar el cierre del flujo
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(red);
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lee un archivo binario del disco y reconstruye el objeto TopologiaRed en memoria.
     * @param nombreArchivo El nombre exacto del archivo a cargar (incluyendo extensión).
     * @return El objeto TopologiaRed restaurado, o null si falla la lectura.
     */
    public static TopologiaRed cargarProyecto(String nombreArchivo) {
        File archivo = new File(DIRECTORIO_PROYECTOS + File.separator + nombreArchivo);
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (TopologiaRed) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error al cargar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Escanea el directorio de proyectos buscando todos los archivos que posean
     * la extensión de nuestra aplicación (.fiber).
     * @return Una lista de cadenas con los nombres de los archivos encontrados, útil para poblar JTables.
     */
    public static List<String> listarProyectosGuardados() {
        asegurarDirectorio();
        List<String> lista = new ArrayList<>();
        File dir = new File(DIRECTORIO_PROYECTOS);
        
        // Filtramos el listado para aceptar estrictamente archivos .fiber
        File[] archivos = dir.listFiles((d, name) -> name.endsWith(".fiber"));
        
        if (archivos != null) {
            for (File f : archivos) {
                lista.add(f.getName());
            }
        }
        return lista;
    }

    // =======================================================
    // MÉTODOS PRIVADOS AUXILIARES
    // =======================================================

    /**
     * Verifica si la carpeta principal de proyectos existe. 
     * Si no, la crea de forma automática antes de realizar operaciones de I/O.
     */
    private static void asegurarDirectorio() {
        File dir = new File(DIRECTORIO_PROYECTOS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}