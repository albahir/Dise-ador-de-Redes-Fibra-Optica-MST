package Vista;

import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelTelemetria extends JPanel {

    private JLabel lblNodosTotales;
    private JLabel lblClientes;
    private JLabel lblAtenuacion;
    private JLabel lblEstadoRed;
  

    public PanelTelemetria() {
        setOpaque(false); // Crítico: Permite que el panel central sea visible por debajo
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 15, 10, 15)); // Padding interno
        
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        lblEstadoRed = FabricaInterfaz.crearEtiquetaValorNeon("En espera", PaletaTema.TEXTO_BLANCO);
        lblNodosTotales = FabricaInterfaz.crearEtiquetaValorNeon("0", PaletaTema.NEON_AZUL);
        lblClientes = FabricaInterfaz.crearEtiquetaValorNeon("0", PaletaTema.NEON_AZUL);
        lblAtenuacion= FabricaInterfaz.crearEtiquetaValorNeon("0 dB", PaletaTema.NEON_AZUL);

        // 2. REDUCIMOS LA FUENTE DE LOS VALORES (De 20 a 14)
        Font fuenteValores = new Font("SansSerif", Font.BOLD, 14);
        lblEstadoRed.setFont(fuenteValores);
        lblNodosTotales.setFont(fuenteValores);
        lblClientes.setFont(fuenteValores);
        lblAtenuacion.setFont(fuenteValores);

        // 3. REDUCIMOS EL ESPACIO ENTRE LÍNEAS (De 5 a 2)
        add(FabricaInterfaz.crearBloqueDatoNeon("Estado de Red:", lblEstadoRed));
        add(Box.createRigidArea(new Dimension(0, 2))); 
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Infraestructura:", lblNodosTotales));
        add(Box.createRigidArea(new Dimension(0, 2)));
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Clientes Activos:", lblClientes));
        add(Box.createRigidArea(new Dimension(0, 2)));
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Pérdida Máxima:", lblAtenuacion));
    }

    // Dibujamos el fondo redondeado y semitransparente
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Usamos el color del contenedor pero le añadimos Alpha (200 de 255) para la transparencia
        g2.setColor(new Color(PaletaTema.FONDO_CONTENEDOR.getRed(), 
                              PaletaTema.FONDO_CONTENEDOR.getGreen(), 
                              PaletaTema.FONDO_CONTENEDOR.getBlue(), 50)); 
                              
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        g2.dispose();
        
        super.paintComponent(g);
    }

    public void actualizarTelemetria(int totalNodos,int clientesConectados, int totalClientes, 
                                   double atenuacionMax, 
                                   String estado, Color colorEstado) {
        
        lblNodosTotales.setText("Nodos(OLT/NAP): " + totalNodos);
      // 1. Asignamos siempre texto plano para no romper el centrado de Swing
        lblClientes.setText(clientesConectados + " de " + totalClientes);
        
        // 2. Cambiamos el color de forma nativa si hay clientes desconectados
        if (clientesConectados < totalClientes) {
            lblClientes.setForeground(PaletaTema.NEON_ROJO); 
        } else {
            // Si todos están conectados, vuelve a su color azul neón original
            lblClientes.setForeground(PaletaTema.NEON_AZUL); 
        }
        lblAtenuacion.setText(String.format("Pérdida Max: %.2f dB", atenuacionMax));
        
        // Cambiamos el color del estado dinámicamente usando HTML básico compatible con JLabel
        lblEstadoRed.setText("<html>Estado: <font color='" + 
                String.format("#%02x%02x%02x", colorEstado.getRed(), colorEstado.getGreen(), colorEstado.getBlue()) + 
                "'>" + estado + "</font></html>");
    }
}