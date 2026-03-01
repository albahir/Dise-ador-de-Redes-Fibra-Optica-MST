package Util.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GestorAlertas {

    public static void mostrarError(Component parent, String titulo, String mensaje) {
        mostrarDialogo(parent, titulo, mensaje, PaletaTema.NEON_ROJO);
    }

    public static void mostrarAdvertencia(Component parent, String titulo, String mensaje) {
        mostrarDialogo(parent, titulo, mensaje, new Color(255, 165, 0)); // Naranja Neón
    }

    public static void mostrarInfo(Component parent, String titulo, String mensaje) {
        mostrarDialogo(parent, titulo, mensaje, PaletaTema.NEON_AZUL);
    }

    public static void mostrarExito(Component parent, String titulo, String mensaje) {
        mostrarDialogo(parent, titulo, mensaje, PaletaTema.NEON_VERDE);
    }

    private static void mostrarDialogo(Component parent, String titulo, String mensaje, Color colorTema) {
        // Encontrar la ventana padre para hacer el diálogo modal
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialogo = (window instanceof Frame) ? new JDialog((Frame) window, true) 
                                                    : new JDialog((Dialog) window, true);

        dialogo.setUndecorated(true);

        // Panel de fondo con el borde dinámico según el tipo de alerta
        JPanel panelFondo = new JPanel(new BorderLayout(15, 15));
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorTema, 2),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // Título
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion(titulo.toUpperCase());
        lblTitulo.setForeground(colorTema);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        // Mensaje (Usamos HTML para que respete los saltos de línea \n)
        String htmlMensaje = "<html><div style='text-align: center; width: 250px;'>" 
                             + mensaje.replace("\n", "<br>") + "</div></html>";
        JLabel lblMensaje = new JLabel(htmlMensaje);
        lblMensaje.setForeground(PaletaTema.TEXTO_BLANCO);
        lblMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblMensaje, BorderLayout.CENTER);

        // Botón Aceptar
        JButton btnOk = FabricaInterfaz.crearBotonAccion("Aceptar", colorTema);
        btnOk.addActionListener(e -> dialogo.dispose());
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        panelBoton.add(btnOk);
        panelFondo.add(panelBoton, BorderLayout.SOUTH);

        dialogo.setContentPane(panelFondo);
        dialogo.pack();
        dialogo.setLocationRelativeTo(parent);
        
        // Efecto de sonido del sistema según el error
        if (colorTema == PaletaTema.NEON_ROJO || colorTema == new Color(255, 165, 0)) {
            Toolkit.getDefaultToolkit().beep();
        }
        
        dialogo.setVisible(true);
    }
    public static boolean pedirConfirmacion(Component parent, String titulo, String mensaje) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialogo = (window instanceof Frame) ? new JDialog((Frame) window, true) 
                                                    : new JDialog((Dialog) window, true);
        dialogo.setUndecorated(true);
        final boolean[] resultado = {false}; // Array de 1 elemento para poder modificarlo dentro del lambda

        JPanel panelFondo = new JPanel(new BorderLayout(15, 15));
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        Color colorTema = new Color(255, 165, 0); // Naranja para advertencias de confirmación
        
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorTema, 2),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion(titulo.toUpperCase());
        lblTitulo.setForeground(colorTema);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        String htmlMensaje = "<html><div style='text-align: center; width: 250px;'>" 
                             + mensaje.replace("\n", "<br>") + "</div></html>";
        JLabel lblMensaje = new JLabel(htmlMensaje);
        lblMensaje.setForeground(PaletaTema.TEXTO_BLANCO);
        lblMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblMensaje, BorderLayout.CENTER);

        // Botones Sí y No
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setOpaque(false);
        JButton btnSi = FabricaInterfaz.crearBotonAccion("Sí, continuar", PaletaTema.NEON_VERDE);
        JButton btnNo = FabricaInterfaz.crearBotonAccion("Cancelar", PaletaTema.NEON_ROJO);

        btnSi.addActionListener(e -> { resultado[0] = true; dialogo.dispose(); });
        btnNo.addActionListener(e -> { resultado[0] = false; dialogo.dispose(); });

        panelBotones.add(btnSi);
        panelBotones.add(btnNo);
        panelFondo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setContentPane(panelFondo);
        dialogo.pack();
        dialogo.setLocationRelativeTo(parent);
        Toolkit.getDefaultToolkit().beep();
        dialogo.setVisible(true);

        return resultado[0];
    }

    // ==========================================
    // NUEVO: Método para pedir entrada de texto (Input)
    // ==========================================
    public static String pedirEntradaTexto(Component parent, String titulo, String mensaje) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialogo = (window instanceof Frame) ? new JDialog((Frame) window, true) 
                                                    : new JDialog((Dialog) window, true);
        dialogo.setUndecorated(true);
        final String[] resultado = {null};

        JPanel panelFondo = new JPanel(new BorderLayout(15, 15));
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaTema.NEON_AZUL, 2),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion(titulo.toUpperCase());
        lblTitulo.setForeground(PaletaTema.NEON_AZUL);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(0, 15));
        panelCentro.setOpaque(false);
        
        String htmlMensaje = "<html><div style='text-align: center; width: 250px;'>" 
                             + mensaje.replace("\n", "<br>") + "</div></html>";
        JLabel lblMensaje = new JLabel(htmlMensaje);
        lblMensaje.setForeground(PaletaTema.TEXTO_BLANCO);
        lblMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        panelCentro.add(lblMensaje, BorderLayout.NORTH);

        JTextField txtInput = FabricaInterfaz.crearTextFieldOscuro("");
        txtInput.setHorizontalAlignment(SwingConstants.CENTER);
        panelCentro.add(txtInput, BorderLayout.SOUTH);
        
        panelFondo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setOpaque(false);
        JButton btnOk = FabricaInterfaz.crearBotonAccion("Aceptar", PaletaTema.NEON_VERDE);
        JButton btnCancelar = FabricaInterfaz.crearBotonAccion("Cancelar", PaletaTema.TEXTO_GRIS);

        btnOk.addActionListener(e -> { resultado[0] = txtInput.getText(); dialogo.dispose(); });
        btnCancelar.addActionListener(e -> { resultado[0] = null; dialogo.dispose(); });

        panelBotones.add(btnOk);
        panelBotones.add(btnCancelar);
        panelFondo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setContentPane(panelFondo);
        dialogo.pack();
        dialogo.setLocationRelativeTo(parent);
        dialogo.setVisible(true);

        return resultado[0];
    }
}