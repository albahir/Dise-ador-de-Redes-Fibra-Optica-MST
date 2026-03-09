package Vista;

import Modelo.dominio.Enlace;
import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cuadro de diálogo modal que muestra información técnica detallada 
 * (Auditoría Óptica) sobre un cable específico de la red cuando el usuario 
 * hace clic derecho sobre él en el mapa.
 */
public class DialogoInfoEnlace extends JDialog {

    private final Enlace enlace;

    // =======================================================
    // CONSTRUCTOR
    // =======================================================

    /**
     * Constructor del inspector de cable.
     * @param parent La ventana principal.
     * @param enlace El cable del cual se extraerán las propiedades.
     */
    public DialogoInfoEnlace(JFrame parent, Enlace enlace) {
        super(parent, "Inspector de Cable", false);
        this.enlace = enlace;

        setUndecorated(true);
        configurarDialogo();
        
        pack();
        setLocationRelativeTo(parent);
    }

    // =======================================================
    // MÉTODOS PRIVADOS DE UI
    // =======================================================

    /**
     * Construye la interfaz gráfica que muestra las propiedades del enlace:
     * Nodos conectados, distancia real y decibelios perdidos en el trayecto.
     */
    private void configurarDialogo() {
        JPanel panelFondo = new JPanel(new BorderLayout(10, 10));
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        
        // Borde Naranja Neón para diferenciar visualmente la inspección de cables de la de equipos
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 0), 2),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // --- ENCABEZADO ---
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion("AUDITORÍA DE ENLACE ÓPTICO");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        // --- DATOS DEL CABLE (Matriz 2 columnas) ---
        JPanel panelDatos = new JPanel(new GridLayout(0, 2, 15, 12));
        panelDatos.setOpaque(false);
        panelDatos.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Conexiones: Origen y Destino
        agregarFila(panelDatos, "Origen (Tx):", enlace.getOrigen().getId() + " (" + enlace.getOrigen().getTipo().name() + ")");
        agregarFila(panelDatos, "Destino (Rx):", enlace.getDestino().getId() + " (" + enlace.getDestino().getTipo().name() + ")");

        // Propiedades Físicas: Longitud
        double longitudKm = enlace.getDistancia() / 1000.0;
        agregarFila(panelDatos, "Longitud Física:", String.format("%.2f Metros (%.3f Km)", enlace.getDistancia(), longitudKm));
        
        // Pérdida exclusiva de este tramo (utilizando el valor estándar de atenuación)
        double perdidaTramo = longitudKm * 0.3;
        agregarFila(panelDatos, "Atenuación del Tramo:", String.format("%.3f dB", perdidaTramo));

        // Información comercial (Ancho de banda simulado para GPON)
        String anchoBanda = enlace.getDestino().getTipo().name().equals("CLIENTE") ? 
                            "Drop Asimétrico (1.25G/2.5G)" : "Troncal GPON (2.5 Gbps)";
        agregarFila(panelDatos, "Capacidad Enlace:", anchoBanda);

        panelFondo.add(panelDatos, BorderLayout.CENTER);

        // --- BOTÓN CERRAR ---
        JButton btnCerrar = FabricaInterfaz.crearBotonAccion("Cerrar", PaletaTema.TEXTO_GRIS);
        btnCerrar.addActionListener(e -> dispose());
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        panelBoton.add(btnCerrar);
        
        panelFondo.add(panelBoton, BorderLayout.SOUTH);
        setContentPane(panelFondo);
    }

    /**
     * Método auxiliar que inserta una etiqueta descriptiva y su respectivo valor 
     * en el GridLayout del panel de datos. Utiliza una fuente monoespaciada para el valor.
     */
    private void agregarFila(JPanel panel, String etiqueta, String valor) {
        JLabel lblEti = new JLabel(etiqueta);
        lblEti.setForeground(PaletaTema.TEXTO_GRIS);
        lblEti.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JLabel lblVal = new JLabel(valor);
        lblVal.setForeground(PaletaTema.TEXTO_BLANCO);
        lblVal.setFont(new Font("Consolas", Font.PLAIN, 13)); // Fuente tipo código para el dato

        panel.add(lblEti);
        panel.add(lblVal);
    }
}