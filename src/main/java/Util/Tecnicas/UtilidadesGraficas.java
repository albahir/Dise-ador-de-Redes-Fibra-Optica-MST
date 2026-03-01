/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util.Tecnicas;


import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class UtilidadesGraficas {

    /**
     * Carga un icono de forma segura. Si falla, devuelve null pero no crashea la app.
     * @param ruta
     */
    public static ImageIcon cargarIcono(String ruta, int ancho, int alto) {
        try {
            URL url = UtilidadesGraficas.class.getResource(ruta);
            if (url == null) {
                System.err.println("No se encontró el recurso: " + ruta);
                return null;
            }
            BufferedImage img = ImageIO.read(url);
            // Escalado suave de alta calidad
            Image dimg = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
            return new ImageIcon(dimg);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Utilidad para formatear dinero (Ej: 1250000 -> "$ 1,250,000")
     */
    public static String formatearMoneda(double cantidad) {
        return String.format("$ %,.0f", cantidad);
    }
}