/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;


import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelCostos extends JPanel {

    private JLabel lblCostoTotal;
    private JLabel lblLongitudFibra;
    private JLabel lblCostoPromedio;

    public PanelCostos() {
        setOpaque(false); // Transparente para flotar sobre el mapa
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 15, 10, 15)); 
        
        inicializarComponentes();
    }

   private void inicializarComponentes() {
       lblCostoTotal = FabricaInterfaz.crearEtiquetaValorNeon("$0", PaletaTema.NEON_AZUL);
        lblLongitudFibra = FabricaInterfaz.crearEtiquetaValorNeon("0 km", PaletaTema.NEON_AZUL);
        lblCostoPromedio = FabricaInterfaz.crearEtiquetaValorNeon("$0", PaletaTema.NEON_AZUL);

        // 2. REDUCIMOS LA FUENTE DE LOS VALORES (De 20 a 14)
        Font fuenteValores = new Font("SansSerif", Font.BOLD, 14);
        lblCostoTotal.setFont(fuenteValores);
        lblLongitudFibra.setFont(fuenteValores);
        lblCostoPromedio.setFont(fuenteValores);

        // 3. REDUCIMOS EL ESPACIO ENTRE LÍNEAS (De 5 a 2)
        add(FabricaInterfaz.crearBloqueDatoNeon("Costo Total:", lblCostoTotal));
        add(Box.createRigidArea(new Dimension(0, 2)));
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Distancia Total:", lblLongitudFibra));
        add(Box.createRigidArea(new Dimension(0, 2)));
        
        add(FabricaInterfaz.crearBloqueDatoNeon("Costo/Metro:", lblCostoPromedio));
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Fondo semi-transparente oscuro
        g2.setColor(new Color(PaletaTema.FONDO_CONTENEDOR.getRed(), 
                              PaletaTema.FONDO_CONTENEDOR.getGreen(), 
                              PaletaTema.FONDO_CONTENEDOR.getBlue(), 50)); 
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        g2.dispose();
        super.paintComponent(g);
    }

    public void actualizarCostos(double costoTotal, double longitudTotalMetros) {
        lblCostoTotal.setText(String.format("$%,.0f", costoTotal)); 
        lblLongitudFibra.setText(String.format("%.1f km", longitudTotalMetros / 1000.0));
        
        double promedio = (longitudTotalMetros > 0) ? (costoTotal / longitudTotalMetros) : 0.0;
        lblCostoPromedio.setText(String.format("$%.2f", promedio));
    }
}