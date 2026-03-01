package Vista;

import Modelo.dominio.Enlace;
import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogoInfoEnlace extends JDialog {

    private final Enlace enlace;

    public DialogoInfoEnlace(JFrame parent, Enlace enlace) {
        super(parent, "Inspector de Cable", false);
        this.enlace = enlace;

        setUndecorated(true);
        configurarDialogo();
        pack();
        setLocationRelativeTo(parent);
    }

    private void configurarDialogo() {
        JPanel panelFondo = new JPanel(new BorderLayout(10, 10));
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        
        // Borde Naranja Neón para diferenciar los cables de los equipos
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 0), 2),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // --- ENCABEZADO ---
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion("AUDITORÍA DE ENLACE ÓPTICO");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        // --- DATOS DEL CABLE ---
        JPanel panelDatos = new JPanel(new GridLayout(0, 2, 15, 12));
        panelDatos.setOpaque(false);
        panelDatos.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Origen y Destino
        agregarFila(panelDatos, "Origen (Tx):", enlace.getOrigen().getId() + " (" + enlace.getOrigen().getTipo().name() + ")");
        agregarFila(panelDatos, "Destino (Rx):", enlace.getDestino().getId() + " (" + enlace.getDestino().getTipo().name() + ")");

        // Física del Cable
        double longitudKm = enlace.getDistancia() / 1000.0;
        agregarFila(panelDatos, "Longitud Física:", String.format("%.2f Metros (%.3f Km)", enlace.getDistancia(), longitudKm));
        
        // Pérdida exclusiva de este tramo (0.3 dB por Km)
        double perdidaTramo = longitudKm * 0.3;
        agregarFila(panelDatos, "Atenuación del Tramo:", String.format("%.3f dB", perdidaTramo));

        // Ancho de banda (Simulado para GPON)
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

    private void agregarFila(JPanel panel, String etiqueta, String valor) {
        JLabel lblEti = new JLabel(etiqueta);
        lblEti.setForeground(PaletaTema.TEXTO_GRIS);
        lblEti.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JLabel lblVal = new JLabel(valor);
        lblVal.setForeground(PaletaTema.TEXTO_BLANCO);
        lblVal.setFont(new Font("Consolas", Font.PLAIN, 13)); 

        panel.add(lblEti);
        panel.add(lblVal);
    }
}