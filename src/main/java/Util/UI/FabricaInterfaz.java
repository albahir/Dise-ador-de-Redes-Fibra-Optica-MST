package Util.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Clase fábrica (Factory Pattern) encargada de la creación de los componentes visuales
 * personalizados de la aplicación.
 *  * Centraliza la instanciación de botones, paneles, campos de texto y otros elementos
 * de Swing, aplicando el diseño "Cyberpunk" (modo oscuro con acentos de neón) de manera uniforme.
 */
public class FabricaInterfaz {

    // =======================================================
    // CONTENEDORES Y PANELES
    // =======================================================

    /**
     * Crea un panel contenedor con bordes redondeados y fondo semi-transparente.
     * Útil para agrupar elementos visuales (como formularios o bloques de estadísticas).
     * @return Un JPanel con gráficos personalizados (bordes curvos).
     */
    public static JPanel crearContenedorRedondeado() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Se utiliza Graphics2D para habilitar el Anti-Aliasing (suavizado de bordes)
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PaletaTema.FONDO_CONTENEDOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); // Radio de curvatura de 15px
                g2.dispose();
            }
        };
        
        panel.setOpaque(false); // Necesario para que el fondo redondeado se dibuje sin esquinas blancas
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding interno del contenedor
        
        return panel;
    }

    /**
     * Crea un panel oscuro estándar utilizado como barra lateral.
     * @return Un JPanel configurado con el fondo de la paleta y un borde derecho separador.
     */
    public static JPanel crearPanelLateral() {
        JPanel panel = new JPanel();
        panel.setBackground(PaletaTema.FONDO_PANEL);
        panel.setLayout(new BorderLayout());
        
        // Borde derecho sutil (1px) para separar visualmente el panel del lienzo del mapa
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(60, 60, 60)));
        
        return panel;
    }

    // =======================================================
    // BOTONES
    // =======================================================

    /**
     * Crea un botón estilo "Cyberpunk": Fondo transparente por defecto, 
     * borde de color neón, y se ilumina internamente al pasar el mouse (Rollover) o al presionarlo.
     * @param texto El texto a mostrar en el botón.
     * @param colorNeonBorde El color del borde y de la iluminación de interacción.
     * @return Un JButton con comportamiento gráfico modificado.
     */
    public static JButton crearBotonAccion(String texto, Color colorNeonBorde) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 12; // Curvatura del botón

                // Lógica de renderizado dinámico según la interacción del mouse
                if (getModel().isPressed()) {
                    // Al hacer clic: Fondo lleno y oscuro del color base
                    g2.setColor(colorNeonBorde.darker());
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                } else if (getModel().isRollover()) {
                    // Al pasar el mouse: Fondo semi-transparente del color base (Canal Alpha en 70)
                    g2.setColor(new Color(colorNeonBorde.getRed(), colorNeonBorde.getGreen(), colorNeonBorde.getBlue(), 70));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                } else {
                    // Estado inactivo: Fondo oscuro normal
                    g2.setColor(PaletaTema.FONDO_PANEL); 
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                }
                
                // Dibujo del borde neón externo
                g2.setColor(colorNeonBorde);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        // Configuración de propiedades base del botón Swing para evitar que sobrescriban el diseño
        btn.setForeground(PaletaTema.TEXTO_BLANCO); 
        btn.setFont(PaletaTema.FUENTE_REGULAR);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); 
        btn.setBorderPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15)); 
        
        return btn;
    }

    // =======================================================
    // ETIQUETAS (LABELS) Y TEXTOS
    // =======================================================

    /**
     * Crea una etiqueta formateada para actuar como título de sección (ej: "ALGORITMOS").
     * @param texto El texto del título.
     * @return Un JLabel centrado y formateado en mayúsculas.
     */
    public static JLabel crearTituloSeccion(String texto) {
        JLabel label = new JLabel(texto.toUpperCase());
        label.setFont(PaletaTema.FUENTE_TITULO);
        label.setForeground(PaletaTema.TEXTO_BLANCO);
        label.setBorder(new EmptyBorder(15, 10, 15, 10));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Crea una etiqueta genérica para mostrar un par clave-valor (ej: "Costo Total: $500").
     * @param etiqueta El nombre del dato.
     * @param valor El valor correspondiente.
     * @return Un JLabel con la fuente estándar de estadísticas.
     */
    public static JLabel crearEtiquetaDato(String etiqueta, String valor) {
        JLabel label = new JLabel(etiqueta + ": " + valor);
        label.setFont(PaletaTema.FUENTE_ESTADISTICAS);
        label.setForeground(PaletaTema.TEXTO_BLANCO);
        return label;
    }

    /**
     * Crea una etiqueta gigante con un color llamativo (Neón) para resaltar datos críticos.
     * @param valorInicial El número o texto a resaltar.
     * @param colorNeon El color de la fuente.
     * @return Un JLabel destacado.
     */
    public static JLabel crearEtiquetaValorNeon(String valorInicial, Color colorNeon) {
        JLabel lbl = new JLabel(valorInicial);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 20)); // Fuente gigante
        lbl.setForeground(colorNeon);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    /**
     * Crea un contenedor visual que agrupa un título descriptivo pequeño y un valor resaltado.
     * @param titulo El texto superior descriptivo (ej. "Costo Total:").
     * @param lblValor El componente JLabel con el valor numérico gigante.
     * @return Un JPanel transparente con alineación vertical.
     */
    public static JPanel crearBloqueDatoNeon(String titulo, JLabel lblValor) {
        JPanel panel = new JPanel();
        panel.setOpaque(false); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblTitulo.setForeground(new Color(200, 200, 200));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblTitulo);
        panel.add(Box.createRigidArea(new Dimension(0, 2))); // Margen de separación interno
        panel.add(lblValor);

        return panel;
    }

    // =======================================================
    // CAMPOS DE ENTRADA (INPUTS)
    // =======================================================

    /**
     * Crea un campo de texto simple con fondo oscuro y cursor visible.
     * @param textoInicial El texto predeterminado.
     * @return Un JTextField modificado.
     */
    public static JTextField crearTextFieldOscuro(String textoInicial) {
        JTextField txt = new JTextField(textoInicial);
        txt.setBackground(PaletaTema.FONDO_CONTENEDOR);
        txt.setForeground(PaletaTema.TEXTO_BLANCO);
        txt.setCaretColor(PaletaTema.NEON_AZUL); // Cursor (palito parpadeante) color neón
        txt.setFont(PaletaTema.FUENTE_REGULAR);
        
        // Borde compuesto: Línea oscura por fuera y un espacio (padding) por dentro
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        return txt;
    }

    /**
     * Crea un menú desplegable (ComboBox) altamente personalizado para el tema oscuro.
     * Sobrescribe los renderizadores nativos de Swing para pintar el fondo de la lista y los botones.
     * @param <T> El tipo de objeto que almacena el ComboBox.
     * @param items Un arreglo con los elementos a mostrar.
     * @return Un JComboBox con diseño aplicado.
     */
    public static <T> JComboBox<T> crearComboBoxOscuro(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setBackground(PaletaTema.FONDO_CONTENEDOR);
        combo.setForeground(PaletaTema.TEXTO_BLANCO);
        combo.setFont(PaletaTema.FUENTE_REGULAR);
        combo.setFocusable(false);
        combo.setOpaque(true); 

        combo.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

        // Magia de UI: Alteramos la forma en la que se pinta la caja principal y la flecha del ComboBox
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(PaletaTema.FONDO_CONTENEDOR); // Obliga a pintar el fondo oscuro ignorando Windows
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            protected JButton createArrowButton() {
                // Personalizamos el botón de la flecha de despliegue
                JButton btn = new JButton("▼");
                btn.setBackground(PaletaTema.FONDO_CONTENEDOR);
                btn.setForeground(PaletaTema.TEXTO_GRIS);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return btn;
            }
        });

        // Estilizar la lista que se despliega hacia abajo
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // Margen interno para hacer cada fila más alta
                
                // Color de hover/selección en las opciones del menú desplegable
                if (isSelected) {
                    label.setBackground(PaletaTema.NEON_AZUL.darker().darker());
                    label.setForeground(PaletaTema.TEXTO_BLANCO);
                } else {
                    label.setBackground(PaletaTema.FONDO_CONTENEDOR);
                    label.setForeground(PaletaTema.TEXTO_BLANCO);
                }
                return label;
            }
        });

        return combo;
    }

    /**
     * Crea un control numérico de incremento/decremento (Spinner) para números enteros.
     */
    public static JSpinner crearSpinnerOscuroInt(int valorInicial, int min, int max, int step) {
        SpinnerNumberModel modelo = new SpinnerNumberModel(valorInicial, min, max, step);
        JSpinner spinner = new JSpinner(modelo);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#"); // Formato entero
        spinner.setEditor(editor);
        aplicarEstiloSpinner(editor.getTextField(), spinner);
        return spinner;
    }

    /**
     * Crea un control numérico de incremento/decremento (Spinner) para números decimales.
     */
    public static JSpinner crearSpinnerOscuroDecimal(double valorInicial, double min, double max, double step) {
        SpinnerNumberModel modelo = new SpinnerNumberModel(valorInicial, min, max, step);
        JSpinner spinner = new JSpinner(modelo);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.00"); // Formato con 2 decimales
        spinner.setEditor(editor);
        aplicarEstiloSpinner(editor.getTextField(), spinner);
        return spinner;
    }

    /**
     * Helper privado para aplicar los colores y márgenes oscuros a un JSpinner.
     * @param txt El campo de texto subyacente del Spinner.
     * @param spinner El componente envolvente.
     */
    public static void aplicarEstiloSpinner(JFormattedTextField txt, JSpinner spinner) {
        txt.setBackground(new Color(30, 30, 30));
        txt.setForeground(PaletaTema.FONDO_PRINCIPAL);
        txt.setCaretColor(PaletaTema.FONDO_PANEL);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                new EmptyBorder(2, 5, 2, 5)
        ));
        txt.setFont(PaletaTema.FUENTE_REGULAR);
        
        // Estilo exterior del componente spinner principal
        spinner.setBorder(BorderFactory.createEmptyBorder());
        spinner.setBackground(new Color(30, 30, 30));
    }
}