package Vista;

import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel gráfico que actúa como un cuadro de mandos (dashboard) flotante.
 * Muestra métricas de telemetría en tiempo real sobre el estado de la red:
 * cantidad de nodos, clientes conectados, pérdida óptica máxima y el estado global.
 */
public class PanelTelemetria extends JPanel {

    // =======================================================
    // ATRIBUTOS DE INTERFAZ
    // =======================================================
    
    private JLabel lblNodosTotales;
    private JLabel lblClientes;
    private JLabel lblAtenuacion;
    private JLabel lblEstadoRed;

    // =======================================================
    // CONSTRUCTOR
    // =======================================================

    /**
     * Constructor del panel de telemetría.
     * Configura el fondo transparente y la disposición vertical de los elementos.
     */
    public PanelTelemetria() {
        setOpaque(false); // Crítico: Permite que el panel central sea visible por debajo (transparencia)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 15, 10, 15)); // Padding interno
        
        inicializarComponentes();
    }

    // =======================================================
    // MÉTODOS PÚBLICOS
    // =======================================================

    /**
     * Actualiza los valores numéricos y visuales de las etiquetas mostradas en el panel.
     * Cambia el color del texto dinámicamente si hay problemas en la red (ej. clientes desconectados).
     * * @param totalNodos         Cantidad total de nodos de infraestructura (OLT/NAP).
     * @param clientesConectados Número de clientes que están recibiendo señal activa.
     * @param totalClientes      Número total de clientes colocados en el mapa.
     * @param atenuacionMax      La pérdida óptica máxima registrada en toda la red.
     * @param estado             Texto descriptivo del estado actual (ej. "ÓPTIMA", "SATURADA").
     * @param colorEstado        Color representativo para resaltar el estado de la red.
     */
    public void actualizarTelemetria(int totalNodos, int clientesConectados, int totalClientes, double atenuacionMax, String estado, Color colorEstado) {
        
        lblNodosTotales.setText("Nodos(OLT/NAP): " + totalNodos);
        
        // Asignamos siempre texto plano para no romper el centrado nativo de Swing
        lblClientes.setText(clientesConectados + " de " + totalClientes);
        
        // Evaluamos la conexión para aplicar colores dinámicos de alerta
        if (clientesConectados < totalClientes) {
            lblClientes.setForeground(PaletaTema.NEON_ROJO); 
        } else {
            // Si todos están conectados, vuelve a su color azul neón original
            lblClientes.setForeground(PaletaTema.NEON_AZUL); 
        }
        
        lblAtenuacion.setText(String.format("Pérdida Max: %.2f dB", atenuacionMax));
        
        // Cambiamos el color del estado dinámicamente inyectando HTML básico compatible con JLabel
        lblEstadoRed.setText("<html>Estado: <font color='" + 
                String.format("#%02x%02x%02x", colorEstado.getRed(), colorEstado.getGreen(), colorEstado.getBlue()) + 
                "'>" + estado + "</font></html>");
    }

    // =======================================================
    // MÉTODOS PROTEGIDOS (SOBREESCRITURAS DE SWING)
    // =======================================================

    /**
     * Dibuja un fondo redondeado y semitransparente detrás del panel,
     * dándole un estilo moderno y flotante sobre el mapa de diseño.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Suavizado de bordes para que las curvas se rendericen limpias (Anti-aliasing)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Usamos el color del contenedor pero le añadimos un canal Alpha (50 de 255) para la transparencia
        g2.setColor(new Color(PaletaTema.FONDO_CONTENEDOR.getRed(), 
                              PaletaTema.FONDO_CONTENEDOR.getGreen(), 
                              PaletaTema.FONDO_CONTENEDOR.getBlue(), 50)); 
                              
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        g2.dispose();
        
        super.paintComponent(g);
    }

    // =======================================================
    // MÉTODOS PRIVADOS DE INICIALIZACIÓN
    // =======================================================

    /**
     * Inicializa y añade al panel las etiquetas visuales con el diseño configurado.
     */
    private void inicializarComponentes() {
        lblEstadoRed = FabricaInterfaz.crearEtiquetaValorNeon("En espera", PaletaTema.TEXTO_BLANCO);
        lblNodosTotales = FabricaInterfaz.crearEtiquetaValorNeon("0", PaletaTema.NEON_AZUL);
        lblClientes = FabricaInterfaz.crearEtiquetaValorNeon("0", PaletaTema.NEON_AZUL);
        lblAtenuacion = FabricaInterfaz.crearEtiquetaValorNeon("0 dB", PaletaTema.NEON_AZUL);

        // Reducimos la fuente de los valores numéricos a un tamaño intermedio (14)
        Font fuenteValores = new Font("SansSerif", Font.BOLD, 14);
        lblEstadoRed.setFont(fuenteValores);
        lblNodosTotales.setFont(fuenteValores);
        lblClientes.setFont(fuenteValores);
        lblAtenuacion.setFont(fuenteValores);

        // Agregamos los bloques de datos intercalando un pequeño espaciador rígido (2px)
        add(FabricaInterfaz.crearBloqueDatoNeon("Estado de Red:", lblEstadoRed));
        add(Box.createRigidArea(new Dimension(0, 2))); 
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Infraestructura:", lblNodosTotales));
        add(Box.createRigidArea(new Dimension(0, 2)));
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Clientes Activos:", lblClientes));
        add(Box.createRigidArea(new Dimension(0, 2)));
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Pérdida Máxima:", lblAtenuacion));
    }
}