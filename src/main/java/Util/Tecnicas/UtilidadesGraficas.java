package Util.Tecnicas;

import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Clase utilitaria que agrupa funciones misceláneas independientes del dominio.
 * Provee herramientas técnicas como el renderizado de imágenes y formateo de divisas.
 */
public class UtilidadesGraficas {

    /**
     * Carga y redimensiona un recurso gráfico (icono) de forma segura.
     * Si el archivo no se encuentra, gestiona la excepción y devuelve null 
     * en lugar de detener la ejecución de toda la aplicación.
     * * @param ruta La ruta relativa del recurso dentro del Classpath.
     * @param ancho El ancho en píxeles al que se escalará la imagen.
     * @param alto El alto en píxeles al que se escalará la imagen.
     * @return Un objeto ImageIcon listo para usarse en componentes Swing, o null si falla.
     */
    public static ImageIcon cargarIcono(String ruta, int ancho, int alto) {
        try {
            URL url = UtilidadesGraficas.class.getResource(ruta);
            if (url == null) {
                System.err.println("No se encontró el recurso: " + ruta);
                return null;
            }
            BufferedImage img = ImageIO.read(url);
            
            // Escalado suave de alta calidad para evitar pixelado en UI
            Image dimg = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
            return new ImageIcon(dimg);
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Utilidad matemática para dar formato legible a cifras económicas.
     * Convierte un número bruto (Ej: 1250000.5) en formato de moneda (Ej: "$ 1,250,000").
     * * @param cantidad El valor double a formatear.
     * @return Una cadena de texto presentable para la interfaz gráfica.
     */
    public static String formatearMoneda(double cantidad) {
        return String.format("$ %,.0f", cantidad);
    }
}