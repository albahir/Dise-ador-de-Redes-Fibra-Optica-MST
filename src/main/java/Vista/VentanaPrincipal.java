/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;


import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    // Componentes principales

    private PanelLateral panelLateral; // Clase dedicada para organizar la barra derecha
    private JLabel etiquetaEstado;
    private PanelTelemetria panelTelemetria;
    private PanelCostos panelCostos;
    private PanelDiseno panelCentralMapa;
    // --- Botones de la Barra Superior ---
    private JButton btnGuardarProyecto;
    private JButton btnCargarProyecto;
    private JButton btnNuevoProyecto;
 
    private JButton btnConfiguracion;
    private JButton btnEjecutarPrim;
    private JButton btnLimpiarMapa;

    public VentanaPrincipal() {
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("FiberDesign Pro - Sistema de Diseño de Redes MST");
        setSize(1200, 700);
        setLocationRelativeTo(null); // Centrar en pantalla
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Aplicar el fondo oscuro principal al contenedor de la ventana
        getContentPane().setBackground(PaletaTema.FONDO_PANEL);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        JPanel panelToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        Color colorFondoToolbar = PaletaTema.FONDO_PANEL; // El color azul oscuro del fondo
        panelToolbar.setBackground(colorFondoToolbar); 
        panelToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));
        panelToolbar.setPreferredSize(new Dimension(0, 50));
        
        btnNuevoProyecto = FabricaInterfaz.crearBotonAccion("📄 Nuevo", colorFondoToolbar);
        btnGuardarProyecto = FabricaInterfaz.crearBotonAccion("💾 Guardar", colorFondoToolbar);
        btnCargarProyecto = FabricaInterfaz.crearBotonAccion("📂 Cargar", colorFondoToolbar);
        btnEjecutarPrim = FabricaInterfaz.crearBotonAccion("▶", colorFondoToolbar);
        btnEjecutarPrim.setForeground(Util.UI.PaletaTema.NEON_AZUL); 
        
        btnLimpiarMapa = FabricaInterfaz.crearBotonAccion("Limpiar", colorFondoToolbar);
        btnConfiguracion = FabricaInterfaz.crearBotonAccion("⚙ Configuración", colorFondoToolbar);
        
        panelToolbar.add(btnNuevoProyecto);
        panelToolbar.add(btnGuardarProyecto);
        panelToolbar.add(btnCargarProyecto);
        JLabel separador = new JLabel(" | ");
        separador.setForeground(new Color(100, 100, 100));
        panelToolbar.add(separador);
         panelToolbar.add(btnEjecutarPrim);
        panelToolbar.add(btnLimpiarMapa);
        JLabel separador2 = new JLabel(" | ");
        separador2.setForeground(new Color(100, 100, 100));
        panelToolbar.add(separador2);
         panelToolbar.add(btnConfiguracion);

        add(panelToolbar, BorderLayout.NORTH);
        // 1. ÁREA CENTRAL: El Mapa de Red (Placeholder por ahora)
        panelCentralMapa = new PanelDiseno();
        panelCentralMapa.setBackground(PaletaTema.FONDO_PRINCIPAL);
        
        // Usamos GridBagLayout para empujar el contenedor a una esquina
        panelCentralMapa.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0.5; // Dividimos la fuerza de empuje 50/50 horizontalmente
        gbc.weighty = 1.0; // Empujan todo hacia el fondo de la pantalla

        // --- A. ANCLAR TELEMETRÍA (ABAJO A LA IZQUIERDA) ---
        panelTelemetria = new PanelTelemetria();
        gbc.gridx = 0; // Columna izquierda
        gbc.anchor = GridBagConstraints.SOUTHWEST; // Empuja al suroeste
        gbc.insets = new Insets(0, 30, 30, 0); // Margen inferior e izquierdo
        panelCentralMapa.add(panelTelemetria, gbc);
      
       
        panelCostos = new PanelCostos();
        gbc.gridx = 1; // Columna derecha
        gbc.anchor = GridBagConstraints.SOUTHEAST; // Empuja al sureste
        gbc.insets = new Insets(0, 0, 30, 30); // Margen inferior y derecho
        panelCentralMapa.add(panelCostos, gbc);
        // Aquí luego instanciaremos tu clase que hace los dibujos (Graphics2D)
        add(panelCentralMapa, BorderLayout.CENTER);

        // 2. PANEL LATERAL: Herramientas y Métricas
        panelLateral = new PanelLateral(); 
        add(panelLateral, BorderLayout.WEST);

        // 3. BARRA DE ESTADO: Abajo
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstado.setBackground(PaletaTema.FONDO_PANEL);
        panelEstado.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)));
        
        etiquetaEstado = new JLabel("> Sistema inicializado y en espera de instrucciones...");
        etiquetaEstado.setFont(PaletaTema.FUENTE_REGULAR);
        etiquetaEstado.setForeground(PaletaTema.NEON_VERDE);
        panelEstado.add(etiquetaEstado);
        
        add(panelEstado, BorderLayout.SOUTH);
    }
    public void actualizarTitulo(String nombreProyecto) {
        if (nombreProyecto == null || nombreProyecto.trim().isEmpty()) {
            setTitle("FiberDesign Pro - Sistema de Diseño de Redes MST");
        } else {
            setTitle("FiberDesign Pro - Proyecto Activo: " + nombreProyecto);
        }
    }

    // Getters para que el Controlador pueda acceder a la UI
    public JPanel getPanelMapa() { return panelCentralMapa; }
    public PanelLateral getPanelLateral() { return panelLateral; }
    public PanelTelemetria getPanelTelemetria() { return panelTelemetria; }
    public PanelCostos getPanelCostos() { return panelCostos; }
    
    // Método para actualizar la barra de estado inferior
    public void setMensajeEstado(String mensaje, Color color) {
        etiquetaEstado.setText("> " + mensaje);
        etiquetaEstado.setForeground(color);
    }
    public JButton getBtnGuardarProyecto() { return btnGuardarProyecto; }
    public JButton getBtnCargarProyecto() { return btnCargarProyecto; }
    public JButton getBtnConfiguracion() { return btnConfiguracion; }
    public JButton getBtnEjecutarPrim() { return btnEjecutarPrim; }
    public JButton getBtnLimpiarMapa() { return btnLimpiarMapa; }
    public javax.swing.JButton getBtnNuevoProyecto() {
    return btnNuevoProyecto;
}
    
}