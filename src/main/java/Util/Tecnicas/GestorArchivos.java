package Util;

import Modelo.dominio.TopologiaRed;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivos {

    private static final String DIRECTORIO_PROYECTOS = "proyectos";

    // 1. Crea la carpeta si no existe
    private static void asegurarDirectorio() {
        File dir = new File(DIRECTORIO_PROYECTOS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // 2. Guardar a disco
    public static boolean guardarProyecto(TopologiaRed red, String nombreArchivo) {
        asegurarDirectorio();
        // Le forzamos la extensión .fiber
        if (!nombreArchivo.endsWith(".fiber")) {
            nombreArchivo += ".fiber";
        }
        
        File archivo = new File(DIRECTORIO_PROYECTOS + File.separator + nombreArchivo);
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(red);
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 3. Cargar desde disco
    public static TopologiaRed cargarProyecto(String nombreArchivo) {
        File archivo = new File(DIRECTORIO_PROYECTOS + File.separator + nombreArchivo);
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (TopologiaRed) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error al cargar: " + e.getMessage());
            return null;
        }
    }

    // 4. Leer la carpeta para llenar la JTable
    public static List<String> listarProyectosGuardados() {
        asegurarDirectorio();
        List<String> lista = new ArrayList<>();
        File dir = new File(DIRECTORIO_PROYECTOS);
        
        File[] archivos = dir.listFiles((d, name) -> name.endsWith(".fiber"));
        if (archivos != null) {
            for (File f : archivos) {
                lista.add(f.getName());
            }
        }
        return lista;
    }
}