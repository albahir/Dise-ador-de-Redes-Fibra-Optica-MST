package Util.UI;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.UIManager;

/**
 * Diccionario centralizado de los recursos visuales del sistema (Colores y Tipografías).
 *  * También contiene la lógica para sobrescribir los estilos nativos de Java Swing (UIManager)
 * y forzar la aplicación completa a un aspecto de modo oscuro moderno.
 */
public class PaletaTema {
    
    // =======================================================
    // DEFINICIÓN DE CONSTANTES VISUALES
    // =======================================================

    // --- COLORES DE FONDO (Dark Mode) ---
    public static final Color FONDO_PRINCIPAL = new Color(24, 25, 28); // Gris casi negro (Lienzo)
    public static final Color FONDO_PANEL = new Color(35, 39, 46);     // Un poco más claro (Ventanas y laterales)
    public static final Color FONDO_CONTENEDOR = new Color(48, 52, 60); // Gris intermedio (Cajas de entrada)
    
    // --- COLORES NEÓN (Acentos y Estados) ---
    public static final Color NEON_AZUL = new Color(0, 243, 255);      // Cyan brillante (Nodos activos, focos)
    public static final Color NEON_VERDE = new Color(57, 255, 20);     // Verde Matrix (Éxito, Conectado)
    public static final Color NEON_ROJO = new Color(255, 42, 109);     // Rojo Cyberpunk (Error, Desconectado)
    public static final Color NEON_VIOLETA = new Color(112, 0, 255);   // Púrpura (Selección del usuario)

    // --- TEXTO ---
    public static final Color TEXTO_BLANCO = new Color(240, 240, 240);
    public static final Color TEXTO_GRIS = new Color(128, 128, 128);

    // --- FUENTES (Tipografía Técnica) ---
    // Se utiliza la familia base SansSerif en negrita para una mejor legibilidad en fondos oscuros
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FUENTE_REGULAR = new Font("SansSerif", Font.BOLD, 13); 
    public static final Font FUENTE_ESTADISTICAS = new Font("SansSerif", Font.BOLD, 14);

    // =======================================================
    // APLICACIÓN GLOBAL AL SISTEMA (SWING UIMANAGER)
    // =======================================================

    /**
     * Inyecta los colores de la paleta directamente en el gestor de UI de Java.
     * Esto hace que componentes estándar como JOptionPane, ScrollPanes o Tooltips
     * asuman el modo oscuro de forma automática sin necesidad de customizar cada objeto.
     */
    public static void configurarAspectoGlobal() {
        // 1. COLORES BASE OSCUROS
        Color fondoOscuro = PaletaTema.FONDO_PRINCIPAL; 
        Color fondoContenedor = PaletaTema.FONDO_CONTENEDOR;
        Color colorPaneles = PaletaTema.FONDO_PANEL; 
        Color textoClaro = PaletaTema.TEXTO_BLANCO;
        Color bordeSutil = new Color(60, 60, 60); 

        // Obligamos a los paneles nativos a usar nuestro color
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

        // 3. MATAR LOS BORDES BLANCOS CLÁSICOS DE SWING
        javax.swing.border.Border bordeOscuroLineal = BorderFactory.createLineBorder(bordeSutil);
        javax.swing.border.Border bordeVacio = BorderFactory.createEmptyBorder();

        UIManager.put("TextField.border", bordeOscuroLineal);
        UIManager.put("ComboBox.border", bordeOscuroLineal);
        UIManager.put("ScrollPane.border", bordeVacio); 
        UIManager.put("Button.border", bordeVacio);    

        // 4. ELIMINAR EL ANILLO DE FOCO BRILLANTE POR DEFECTO
        UIManager.put("Component.focusColor", PaletaTema.NEON_AZUL); 
        UIManager.put("Button.focus", new Color(0, 0, 0, 0)); // Hace el foco de los botones invisible
        UIManager.put("ComboBox.selectionBackground", PaletaTema.NEON_AZUL.darker());
        UIManager.put("ComboBox.selectionForeground", textoClaro);

        // 5. INTENTO DE FORZAR EL TEMA 'NIMBUS' (Si está instalado en el OS del usuario)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    
                    // Modificamos las variables base del tema Nimbus nativo para teñirlo de oscuro
                    UIManager.getLookAndFeelDefaults().put("nimbusBase", colorPaneles);
                    UIManager.getLookAndFeelDefaults().put("nimbusBlueGrey", fondoContenedor);
                    UIManager.getLookAndFeelDefaults().put("control", colorPaneles); 
                    break;
                }
            }
        } catch (Exception e) {
            // Si el motor Nimbus falla o no existe en el JRE, el sistema seguirá con el diseño plano por defecto.
        }
    }
}