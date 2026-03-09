package Vista;

import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import java.awt.*;

/**
 * Contenedor principal (Frame) de la aplicación FiberDesign Pro.
 *  * Orquesta la disposición de los sub-paneles principales utilizando
 * LayoutManagers mixtos (BorderLayout para la estructura global, 
 * GridBagLayout para los overlays transparentes sobre el mapa).
 */
public class VentanaPrincipal extends JFrame {

    // =======================================================
    // COMPONENTES PRINCIPALES (UI)
    // =======================================================

    // Paneles estructurales
    private PanelLateral panelLateral; 
    private JLabel etiquetaEstado;
    private PanelTelemetria panelTelemetria;
    private PanelCostos panelCostos;
    private PanelDiseno panelCentralMapa;
    
    // Botones de la Barra Superior (Toolbar)
    private JButton btnGuardarProyecto;
    private JButton btnCargarProyecto;
    private JButton btnNuevoProyecto;
    private JButton btnConfiguracion;
    private JButton btnEjecutarPrim;
    private JButton btnLimpiarMapa;

    // =======================================================
    // CONSTRUCTOR
    // =======================================================

    /**
     * Construye la ventana principal, inyectando la configuración básica
     * y desplegando los componentes hijos.
     */
    public VentanaPrincipal() {
        configurarVentana();
        inicializarComponentes();
    }

    // =======================================================
    // MÉTODOS PÚBLICOS (GETTERS Y API VISUAL)
    // =======================================================

    /**
     * Actualiza el título en la barra del sistema operativo para mostrar el archivo en uso.
     * @param nombreProyecto El nombre del archivo `.fiber`, o nulo para el título por defecto.
     */
    public void actualizarTitulo(String nombreProyecto) {
        if (nombreProyecto == null || nombreProyecto.trim().isEmpty()) {
            setTitle("FiberDesign Pro - Sistema de Diseño de Redes MST");
        } else {
            setTitle("FiberDesign Pro - Proyecto Activo: " + nombreProyecto);
        }
    }

    /**
     * Actualiza el mensaje informativo en la barra inferior (Footer) de la aplicación.
     * @param mensaje Texto descriptivo de la acción reciente.
     * @param color El color (de la PaletaTema) que enfatice el estado del mensaje.
     */
    public void setMensajeEstado(String mensaje, Color color) {
        etiquetaEstado.setText("> " + mensaje);
        etiquetaEstado.setForeground(color);
    }

    // --- Getters de Paneles (Para los Controladores MVC) ---
    public JPanel getPanelMapa() { 
        return panelCentralMapa; 
    }
    
    public PanelLateral getPanelLateral() { 
        return panelLateral; 
    }
    
    public PanelTelemetria getPanelTelemetria() { 
        return panelTelemetria; 
    }
    
    public PanelCostos getPanelCostos() { 
        return panelCostos; 
    }

    // --- Getters de Botones (Para asignación de Listeners) ---
    public JButton getBtnNuevoProyecto() { 
        return btnNuevoProyecto; 
    }
    
    public JButton getBtnGuardarProyecto() { 
        return btnGuardarProyecto; 
    }
    
    public JButton getBtnCargarProyecto() { 
        return btnCargarProyecto; 
    }
    
    public JButton getBtnConfiguracion() { 
        return btnConfiguracion; 
    }
    
    public JButton getBtnEjecutarPrim() { 
        return btnEjecutarPrim; 
    }
    
    public JButton getBtnLimpiarMapa() { 
        return btnLimpiarMapa; 
    }

    // =======================================================
    // MÉTODOS PRIVADOS (CONFIGURACIÓN DE UI)
    // =======================================================

    /**
     * Aplica la configuración base al marco de la ventana (título, tamaño, cierres).
     */
    private void configurarVentana() {
        setTitle("FiberDesign Pro - Sistema de Diseño de Redes MST");
        setSize(1200, 700);
        setLocationRelativeTo(null); // Centrar la ventana en la pantalla del usuario
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Aplicar el fondo oscuro principal al contenedor base de la ventana
        getContentPane().setBackground(PaletaTema.FONDO_PANEL);
        setLayout(new BorderLayout());
    }

    /**
     * Inicializa, posiciona y decora la barra de herramientas, los paneles laterales,
     * el lienzo central interactivo y la barra inferior de estado.
     */
    private void inicializarComponentes() {
        // --- 1. BARRA SUPERIOR (TOOLBAR) ---
        JPanel panelToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        Color colorFondoToolbar = PaletaTema.FONDO_PANEL;
        panelToolbar.setBackground(colorFondoToolbar); 
        panelToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));
        panelToolbar.setPreferredSize(new Dimension(0, 50));
        
        btnNuevoProyecto = FabricaInterfaz.crearBotonAccion("📄 Nuevo", colorFondoToolbar);
        btnGuardarProyecto = FabricaInterfaz.crearBotonAccion("💾 Guardar", colorFondoToolbar);
        btnCargarProyecto = FabricaInterfaz.crearBotonAccion("📂 Cargar", colorFondoToolbar);
        
        btnEjecutarPrim = FabricaInterfaz.crearBotonAccion("▶ PRIM", colorFondoToolbar);
        btnEjecutarPrim.setForeground(Util.UI.PaletaTema.NEON_AZUL); 
        
        btnLimpiarMapa = FabricaInterfaz.crearBotonAccion("Limpiar", colorFondoToolbar);
        btnConfiguracion = FabricaInterfaz.crearBotonAccion("⚙ Configuración", colorFondoToolbar);
        
        // Añadir elementos a la Toolbar con separadores visuales
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

        // --- 2. ÁREA CENTRAL (MAPA INTERACTIVO Y OVERLAYS) ---
        panelCentralMapa = new PanelDiseno();
        panelCentralMapa.setBackground(PaletaTema.FONDO_PRINCIPAL);
        
        // Utilizamos GridBagLayout como un "truco" para superponer paneles (Overlays) 
        // semi-transparentes sobre el lienzo en el que se dibuja el mapa de la red.
        panelCentralMapa.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0.5; // Divide la fuerza de anclaje 50/50 horizontalmente
        gbc.weighty = 1.0; // Empuja los contenedores fuertemente hacia la parte inferior de la pantalla

        // A. Panel de Telemetría (Flotante abajo a la izquierda)
        panelTelemetria = new PanelTelemetria();
        gbc.gridx = 0; 
        gbc.anchor = GridBagConstraints.SOUTHWEST; 
        gbc.insets = new Insets(0, 30, 30, 0); // Margen de separación (Inferior e Izquierdo)
        panelCentralMapa.add(panelTelemetria, gbc);
      
        // B. Panel de Costos (Flotante abajo a la derecha)
        panelCostos = new PanelCostos();
        gbc.gridx = 1; 
        gbc.anchor = GridBagConstraints.SOUTHEAST; 
        gbc.insets = new Insets(0, 0, 30, 30); // Margen de separación (Inferior y Derecho)
        panelCentralMapa.add(panelCostos, gbc);
        
        add(panelCentralMapa, BorderLayout.CENTER);

        // --- 3. PANEL LATERAL (HERRAMIENTAS) ---
        panelLateral = new PanelLateral(); 
        add(panelLateral, BorderLayout.WEST);

        // --- 4. BARRA DE ESTADO (FOOTER INFERIOR) ---
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstado.setBackground(PaletaTema.FONDO_PANEL);
        panelEstado.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)));
        
        etiquetaEstado = new JLabel("> Sistema inicializado y en espera de instrucciones...");
        etiquetaEstado.setFont(PaletaTema.FUENTE_REGULAR);
        etiquetaEstado.setForeground(PaletaTema.NEON_VERDE);
        panelEstado.add(etiquetaEstado);
        
        add(panelEstado, BorderLayout.SOUTH);
    }
}