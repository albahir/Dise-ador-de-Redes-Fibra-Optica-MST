/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util.UI;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FabricaInterfaz {
    public static JPanel crearContenedorRedondeado() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PaletaTema.FONDO_CONTENEDOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); // Radio de 15px
                g2.dispose();
            }
        };
        panel.setOpaque(false); // Necesario para que el fondo redondeado se vea bien
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding interno del contenedor
        return panel;
    }

    /**
     * Crea un botón estilo "Cyberpunk": Fondo transparente, borde neón,
     * se ilumina al pasar el mouse.
     */
    public static JButton crearBotonAccion(String texto, Color colorNeonBorde) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 12; // Curvatura del botón

                // Fondo dinámico según el mouse
                if (getModel().isPressed()) {
                    g2.setColor(colorNeonBorde.darker());
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(colorNeonBorde.getRed(), colorNeonBorde.getGreen(), colorNeonBorde.getBlue(), 70));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                } else {
                    g2.setColor(PaletaTema.FONDO_PANEL); 
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                }
                
                // Borde neón redondeado
                g2.setColor(colorNeonBorde);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        btn.setForeground(PaletaTema.TEXTO_BLANCO); // TEXTO SIEMPRE BLANCO
        btn.setFont(PaletaTema.FUENTE_REGULAR);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); 
        btn.setBorderPainted(false); // Quitamos el borde cuadrado de Swing
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15)); 
        return btn;
    }

    /**
     * Crea un panel oscuro estándar para contenedores laterales.
     */
    public static JPanel crearPanelLateral() {
        JPanel panel = new JPanel();
        panel.setBackground(PaletaTema.FONDO_PANEL);
        panel.setLayout(new BorderLayout());
        // Borde derecho sutil para separar del mapa
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(60, 60, 60)));
        return panel;
    }

    /**
     * Crea etiquetas para títulos de secciones (ej: "ALGORITMOS")
     * @param texto
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
     * Crea etiquetas para datos (ej: "Costo Total: $500")
     * Usa el color neón para el valor numérico.
     */
  public static JLabel crearEtiquetaDato(String etiqueta, String valor) {
        // Texto completamente blanco para mantener la uniformidad
        JLabel label = new JLabel(etiqueta + ": " + valor);
        label.setFont(PaletaTema.FUENTE_ESTADISTICAS);
        label.setForeground(PaletaTema.TEXTO_BLANCO);
        return label;
    }
  public static JLabel crearEtiquetaValorNeon(String valorInicial, Color colorNeon) {
        JLabel lbl = new JLabel(valorInicial);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 20)); // Fuente gigante
        lbl.setForeground(colorNeon);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    /**
     * Crea un panel transparente que agrupa un título pequeño gris y la etiqueta de valor gigante.
     * @param titulo El texto superior (ej. "Costo Total:")
     * @param lblValor El JLabel devuelto por crearEtiquetaValorNeon()
     */
    public static JPanel crearBloqueDatoNeon(String titulo, JLabel lblValor) {
        JPanel panel = new JPanel();
        panel.setOpaque(false); // Transparente
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Título pequeño y gris
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblTitulo.setForeground(new Color(200, 200, 200));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agrupamos
        panel.add(lblTitulo);
        panel.add(Box.createRigidArea(new Dimension(0, 2))); // Margen mínimo
        panel.add(lblValor);

        return panel;
    }
 public static JTextField crearTextFieldOscuro(String textoInicial) {
        JTextField txt = new JTextField(textoInicial);
        txt.setBackground(PaletaTema.FONDO_CONTENEDOR);
        txt.setForeground(PaletaTema.TEXTO_BLANCO);
        txt.setCaretColor(PaletaTema.NEON_AZUL);
        txt.setFont(PaletaTema.FUENTE_REGULAR);
        
        // Borde estricto: Gris oscuro por fuera, padding por dentro
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return txt;
    }

    public static <T> JComboBox<T> crearComboBoxOscuro(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setBackground(PaletaTema.FONDO_CONTENEDOR);
        combo.setForeground(PaletaTema.TEXTO_BLANCO);
        combo.setFont(PaletaTema.FUENTE_REGULAR);
        combo.setFocusable(false);
        combo.setOpaque(true); // Obliga a pintar el fondo

        // Borde delgado y oscuro
        combo.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

        // Magia para obligar a pintar el cuadro de texto principal del ComboBox de color oscuro
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(PaletaTema.FONDO_CONTENEDOR); // Fuerza el fondo oscuro
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            protected JButton createArrowButton() {
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
                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // Más altura para ser legible
                
                if (isSelected) {
                    label.setBackground(PaletaTema.NEON_AZUL.darker().darker()); // Fondo azul oscuro al pasar el mouse
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
    public static JSpinner crearSpinnerOscuroInt(int valorInicial, int min, int max, int step) {
        SpinnerNumberModel modelo = new SpinnerNumberModel(valorInicial, min, max, step);
        JSpinner spinner = new JSpinner(modelo);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
        spinner.setEditor(editor);
        aplicarEstiloSpinner(editor.getTextField(), spinner);
        return spinner;
    }

    public static JSpinner crearSpinnerOscuroDecimal(double valorInicial, double min, double max, double step) {
        SpinnerNumberModel modelo = new SpinnerNumberModel(valorInicial, min, max, step);
        JSpinner spinner = new JSpinner(modelo);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.00");
        spinner.setEditor(editor);
        aplicarEstiloSpinner(editor.getTextField(), spinner);
        return spinner;
    }

    public static void aplicarEstiloSpinner(JFormattedTextField txt, JSpinner spinner) {
        txt.setBackground(new Color(30, 30, 30));
        txt.setForeground(PaletaTema.FONDO_PRINCIPAL);
        txt.setCaretColor(PaletaTema.FONDO_PANEL);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                new EmptyBorder(2, 5, 2, 5)
        ));
        txt.setFont(PaletaTema.FUENTE_REGULAR);
        
        // Estilo exterior del spinner
        spinner.setBorder(BorderFactory.createEmptyBorder());
        spinner.setBackground(new Color(30, 30, 30));
        
    }
}