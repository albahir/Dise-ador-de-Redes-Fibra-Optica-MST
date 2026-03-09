package Vista;

import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel gráfico flotante encargado de mostrar el desglose económico del proyecto.
 * Visualiza el costo total estimado, la longitud de fibra requerida
 * y una métrica de costo promedio por metro instalado.
 */
public class PanelCostos extends JPanel {

    // =======================================================
    // ATRIBUTOS DE INTERFAZ
    // =======================================================
    
    private JLabel lblCostoTotal;
    private JLabel lblLongitudFibra;
    private JLabel lblCostoPromedio;

    // =======================================================
    // CONSTRUCTOR
    // =======================================================

    /**
     * Constructor del panel de costos.
     * Configura el panel principal como contenedor transparente y alineado verticalmente.
     */
    public PanelCostos() {
        setOpaque(false); // Transparente para flotar nativamente sobre el mapa
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 15, 10, 15)); // Márgenes internos
        
        inicializarComponentes();
    }

    // =======================================================
    // MÉTODOS PÚBLICOS
    // =======================================================

    /**
     * Actualiza los cálculos económicos mostrados en las etiquetas del panel.
     * * @param costoTotal          El costo monetario total acumulado de la red.
     * @param longitudTotalMetros La longitud total física calculada para todo el cableado.
     */
    public void actualizarCostos(double costoTotal, double longitudTotalMetros) {
        // Formateo del costo total agregando separadores de miles
        lblCostoTotal.setText(String.format("$%,.0f", costoTotal)); 
        
        // Conversión a kilómetros para una lectura de distancia más amigable
        lblLongitudFibra.setText(String.format("%.1f km", longitudTotalMetros / 1000.0));
        
        // Prevención de división por cero y cálculo del promedio
        double promedio = (longitudTotalMetros > 0) ? (costoTotal / longitudTotalMetros) : 0.0;
        lblCostoPromedio.setText(String.format("$%.2f", promedio));
    }

    // =======================================================
    // MÉTODOS PROTEGIDOS (SOBREESCRITURAS DE SWING)
    // =======================================================

    /**
     * Sobrescribe el renderizador de componentes para pintar una "caja"
     * de fondo redondeada y semitransparente detrás del texto.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fondo semi-transparente oscuro (canal alfa en 50)
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
     * Configura y posiciona todos los componentes JLabel en el contenedor vertical.
     */
    private void inicializarComponentes() {
        lblCostoTotal = FabricaInterfaz.crearEtiquetaValorNeon("$0", PaletaTema.NEON_AZUL);
        lblLongitudFibra = FabricaInterfaz.crearEtiquetaValorNeon("0 km", PaletaTema.NEON_AZUL);
        lblCostoPromedio = FabricaInterfaz.crearEtiquetaValorNeon("$0", PaletaTema.NEON_AZUL);

        // Se reduce la fuente de los valores para ahorrar espacio de pantalla visual
        Font fuenteValores = new Font("SansSerif", Font.BOLD, 14);
        lblCostoTotal.setFont(fuenteValores);
        lblLongitudFibra.setFont(fuenteValores);
        lblCostoPromedio.setFont(fuenteValores);

        // Se agregan los contenedores usando un espaciador rígido de 2px
        add(FabricaInterfaz.crearBloqueDatoNeon("Costo Total:", lblCostoTotal));
        add(Box.createRigidArea(new Dimension(0, 2)));
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Distancia Total:", lblLongitudFibra));
        add(Box.createRigidArea(new Dimension(0, 2)));
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Costo/Metro:", lblCostoPromedio));
    }
}