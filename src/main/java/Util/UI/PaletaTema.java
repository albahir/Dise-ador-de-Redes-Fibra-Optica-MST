/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util.UI;


import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.UIManager;

public class PaletaTema {
    // --- COLORES DE FONDO (Dark Mode) ---
    public static final Color FONDO_PRINCIPAL = new Color(24, 25, 28); // Gris casi negro
    public static final Color FONDO_PANEL = new Color(35, 39, 46);     // Un poco más claro para distinguir áreas
    public static final Color FONDO_CONTENEDOR = new Color(48, 52, 60);
    // --- COLORES NEÓN (Acentos) ---
    public static final Color NEON_AZUL = new Color(0, 243, 255);      // Cyan brillante (Nodos activos)
    public static final Color NEON_VERDE = new Color(57, 255, 20);     // Verde Matrix (Éxito/Conectado)
    public static final Color NEON_ROJO = new Color(255, 42, 109);     // Rojo Cyberpunk (Error/Desconectado)
    public static final Color NEON_VIOLETA = new Color(112, 0, 255);   // Púrpura (Selección)

    // --- TEXTO ---
    public static final Color TEXTO_BLANCO = new Color(240, 240, 240);
    public static final Color TEXTO_GRIS = new Color(128, 128, 128);

    // --- FUENTES (Tipografía Técnica) ---
    // Intentaremos usar una fuente monoespaciada o sans-serif limpia
    // Usamos SansSerif BOLD para que no se vea tan delgada
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FUENTE_REGULAR = new Font("SansSerif", Font.BOLD, 13); 
    public static final Font FUENTE_ESTADISTICAS = new Font("SansSerif", Font.BOLD, 14);

    public static void configurarAspectoGlobal() {
        // 1. COLORES BASE OSCUROS
        Color fondoOscuro = PaletaTema.FONDO_PRINCIPAL; // Negro
        Color fondoContenedor = PaletaTema.FONDO_CONTENEDOR;
        Color colorPaneles = PaletaTema.FONDO_PANEL; // Azul oscuro/Gris
        Color textoClaro = PaletaTema.TEXTO_BLANCO;
        Color bordeSutil = new Color(60, 60, 60); 

        // ¡CORRECCIÓN! Le decimos a los paneles generales que usen el color gris/azul
        UIManager.put("Panel.background", colorPaneles);
        UIManager.put("OptionPane.background", colorPaneles);
        UIManager.put("Dialog.background", colorPaneles);
        UIManager.put("Viewport.background", fondoOscuro);

        // 2. TEXTOS CLAROS
        UIManager.put("Label.foreground", textoClaro);
        UIManager.put("OptionPane.messageForeground", textoClaro);
        UIManager.put("Button.foreground", textoClaro);
        UIManager.put("ComboBox.foreground", textoClaro);
        UIManager.put("TextField.foreground", textoClaro);

        // 3. --- MATAR LOS BORDES BLANCOS ---
        javax.swing.border.Border bordeOscuroLineal = BorderFactory.createLineBorder(bordeSutil);
        javax.swing.border.Border bordeVacio = BorderFactory.createEmptyBorder();

        UIManager.put("TextField.border", bordeOscuroLineal);
        UIManager.put("ComboBox.border", bordeOscuroLineal);
        UIManager.put("ScrollPane.border", bordeVacio); 
        UIManager.put("Button.border", bordeVacio);    

        // 4. ELIMINAR EL ANILLO DE FOCO BRILLANTE
        UIManager.put("Component.focusColor", PaletaTema.NEON_AZUL); 
        UIManager.put("Button.focus", new Color(0, 0, 0, 0)); 
        UIManager.put("ComboBox.selectionBackground", PaletaTema.NEON_AZUL.darker());
        UIManager.put("ComboBox.selectionForeground", textoClaro);

        // Opcional: Intentar forzar un estilo más plano si está disponible
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    
                    // ¡AQUÍ ESTÁ EL CAMBIO MAESTRO!
                    UIManager.getLookAndFeelDefaults().put("nimbusBase", colorPaneles);
                    UIManager.getLookAndFeelDefaults().put("nimbusBlueGrey", fondoContenedor);
                    UIManager.getLookAndFeelDefaults().put("control", colorPaneles); // <-- Ahora manda el colorPaneles
                    break;
                }
            }
        } catch (Exception e) {
            // Si falla Nimbus, seguimos con el default
        }
    }
}