package Util.Tecnicas;

import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Clase utilitaria encargada de generar cuadros de diálogo (pop-ups) personalizados.
 *  * Reemplaza los JOptionPane estándar de Java para mantener la coherencia visual
 * con el tema oscuro/cyberpunk de la aplicación.
 */
public class GestorAlertas {

    // =======================================================
    // MÉTODOS PÚBLICOS DE ALERTAS SIMPLES
    // =======================================================

    /**
     * Muestra un diálogo de error con borde rojo y sonido de alerta.
     * @param parent Componente padre sobre el cual se centrará el diálogo.
     * @param titulo Título de la ventana de alerta.
     * @param mensaje Detalle del error.
     */
    public static void mostrarError(Component parent, String titulo, String mensaje) {
        mostrarDialogo(parent, titulo, mensaje, PaletaTema.NEON_ROJO);
    }

    /**
     * Muestra un diálogo de advertencia con borde naranja y sonido de alerta.
     * @param parent
     * @param titulo
     * @param mensaje
     */
    public static void mostrarAdvertencia(Component parent, String titulo, String mensaje) {
        mostrarDialogo(parent, titulo, mensaje, new Color(255, 165, 0)); // Naranja Neón
    }

    /**
     * Muestra un diálogo de información general con borde azul.
     * @param parent
     * @param mensaje
     */
    public static void mostrarInfo(Component parent, String titulo, String mensaje) {
        mostrarDialogo(parent, titulo, mensaje, PaletaTema.NEON_AZUL);
    }

    /**
     * Muestra un diálogo de confirmación de éxito con borde verde.
     */
    public static void mostrarExito(Component parent, String titulo, String mensaje) {
        mostrarDialogo(parent, titulo, mensaje, PaletaTema.NEON_VERDE);
    }

    // =======================================================
    // MÉTODOS PÚBLICOS DE INTERACCIÓN (CONFIRMACIÓN E INPUT)
    // =======================================================

    /**
     * Muestra un cuadro de diálogo interactivo pidiendo al usuario confirmar una acción (Sí/No).
     * @return true si el usuario selecciona "Sí, continuar", false si cancela.
     */
    public static boolean pedirConfirmacion(Component parent, String titulo, String mensaje) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialogo = (window instanceof Frame) ? new JDialog((Frame) window, true) 
                                                    : new JDialog((Dialog) window, true);
        dialogo.setUndecorated(true);
        
        // Array de 1 elemento para poder modificar el resultado dentro de las expresiones lambda (callbacks)
        final boolean[] resultado = {false}; 

        JPanel panelFondo = new JPanel(new BorderLayout(15, 15));
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        Color colorTema = new Color(255, 165, 0); // Naranja para advertencias críticas
        
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorTema, 2),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // Título del diálogo
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion(titulo.toUpperCase());
        lblTitulo.setForeground(colorTema);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        // Formateo del mensaje usando HTML para respetar saltos de línea e inyectar un ancho máximo
        String htmlMensaje = "<html><div style='text-align: center; width: 250px;'>" 
                             + mensaje.replace("\n", "<br>") + "</div></html>";
        JLabel lblMensaje = new JLabel(htmlMensaje);
        lblMensaje.setForeground(PaletaTema.TEXTO_BLANCO);
        lblMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblMensaje, BorderLayout.CENTER);

        // Creación del panel inferior con los botones Sí y No
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setOpaque(false);
        JButton btnSi = FabricaInterfaz.crearBotonAccion("Sí, continuar", PaletaTema.NEON_VERDE);
        JButton btnNo = FabricaInterfaz.crearBotonAccion("Cancelar", PaletaTema.NEON_ROJO);

        // Asignación de acciones a los botones modificando la variable 'resultado'
        btnSi.addActionListener(e -> { 
            resultado[0] = true; 
            dialogo.dispose(); 
        });
        btnNo.addActionListener(e -> { 
            resultado[0] = false; 
            dialogo.dispose(); 
        });

        panelBotones.add(btnSi);
        panelBotones.add(btnNo);
        panelFondo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setContentPane(panelFondo);
        dialogo.pack();
        dialogo.setLocationRelativeTo(parent);
        Toolkit.getDefaultToolkit().beep();
        dialogo.setVisible(true); // Bloquea el hilo de ejecución hasta que se cierre el diálogo

        return resultado[0];
    }

    /**
     * Muestra un cuadro de diálogo solicitando al usuario que introduzca texto.
     * @return El texto introducido por el usuario, o null si cancela la operación.
     */
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

        // Campo de entrada de texto personalizado
        JTextField txtInput = FabricaInterfaz.crearTextFieldOscuro("");
        txtInput.setHorizontalAlignment(SwingConstants.CENTER);
        panelCentro.add(txtInput, BorderLayout.SOUTH);
        
        panelFondo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setOpaque(false);
        JButton btnOk = FabricaInterfaz.crearBotonAccion("Aceptar", PaletaTema.NEON_VERDE);
        JButton btnCancelar = FabricaInterfaz.crearBotonAccion("Cancelar", PaletaTema.TEXTO_GRIS);

        btnOk.addActionListener(e -> { 
            resultado[0] = txtInput.getText(); 
            dialogo.dispose(); 
        });
        btnCancelar.addActionListener(e -> { 
            resultado[0] = null; 
            dialogo.dispose(); 
        });

        panelBotones.add(btnOk);
        panelBotones.add(btnCancelar);
        panelFondo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setContentPane(panelFondo);
        dialogo.pack();
        dialogo.setLocationRelativeTo(parent);
        dialogo.setVisible(true);

        return resultado[0];
    }

    // =======================================================
    // MÉTODOS PRIVADOS AUXILIARES
    // =======================================================

    /**
     * Método base unificado para construir los diálogos informativos simples (Error, Info, etc.).
     * Construye un JDialog modal sin decoración nativa del sistema operativo.
     */
    private static void mostrarDialogo(Component parent, String titulo, String mensaje, Color colorTema) {
        // Encontrar la ventana padre principal para hacer el diálogo modal (bloquear la ventana principal)
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialogo = (window instanceof Frame) ? new JDialog((Frame) window, true) 
                                                    : new JDialog((Dialog) window, true);

        // Quita la barra de título estándar de Windows/Mac
        dialogo.setUndecorated(true);

        // Panel de fondo con el borde dinámico según el tipo de alerta (colorTema)
        JPanel panelFondo = new JPanel(new BorderLayout(15, 15));
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorTema, 2),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // Configuración del título
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion(titulo.toUpperCase());
        lblTitulo.setForeground(colorTema);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        // Formateo del mensaje usando HTML para que respete los saltos de línea (\n)
        String htmlMensaje = "<html><div style='text-align: center; width: 250px;'>" 
                             + mensaje.replace("\n", "<br>") + "</div></html>";
        JLabel lblMensaje = new JLabel(htmlMensaje);
        lblMensaje.setForeground(PaletaTema.TEXTO_BLANCO);
        lblMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblMensaje, BorderLayout.CENTER);

        // Configuración del botón de aceptación unificado
        JButton btnOk = FabricaInterfaz.crearBotonAccion("Aceptar", colorTema);
        btnOk.addActionListener(e -> dialogo.dispose());
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        panelBoton.add(btnOk);
        panelFondo.add(panelBoton, BorderLayout.SOUTH);

        dialogo.setContentPane(panelFondo);
        dialogo.pack();
        dialogo.setLocationRelativeTo(parent); // Centrar en pantalla o respecto al padre
        
        // Efecto de sonido del sistema solo para errores y advertencias
        if (colorTema == PaletaTema.NEON_ROJO || colorTema == new Color(255, 165, 0)) {
            Toolkit.getDefaultToolkit().beep();
        }
        
        dialogo.setVisible(true);
    }
}