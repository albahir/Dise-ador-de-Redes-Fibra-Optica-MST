package Vista;

import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

/**
 * Representa el panel lateral (menú de herramientas) de la interfaz gráfica.
 * Contiene los botones para la gestión y construcción de la red (nodos/hardware)
 * y una tabla visual que lista los proyectos guardados recientemente.
 */
public class PanelLateral extends JPanel {

    // =======================================================
    // ATRIBUTOS DE INTERFAZ
    // =======================================================
    
    // Botones de acciones principales
    private JButton btnAgregarNodo;
    private JButton btnEliminarNodo;
    
    // Componentes de la tabla de proyectos recientes
    private JTable tablaProyectos;
    private DefaultTableModel modeloTablaProyectos;
    
    // Mapa que asocia cada tipo de hardware con su respectivo botón en la paleta
    private final Map<Modelo.dominio.TipoNodo, JButton> botonesHerramientas = new HashMap<>();
   
    // =======================================================
    // CONSTRUCTOR
    // =======================================================

    /**
     * Constructor del panel lateral.
     * Configura el diseño general, el fondo y delega la creación de los controles.
     */
    public PanelLateral() {
        setBackground(PaletaTema.FONDO_PANEL);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10)); 
        setPreferredSize(new Dimension(270, 0)); 

        inicializarComponentes();
    }

    // =======================================================
    // MÉTODOS PÚBLICOS (GETTERS)
    // =======================================================

    public JButton getBtnAgregarNodo() { 
        return btnAgregarNodo; 
    }
    
    public JButton getBtnEliminarNodo() { 
        return btnEliminarNodo; 
    }
    
    public JTable getTablaProyectos() { 
        return tablaProyectos; 
    }
    
    public Map<Modelo.dominio.TipoNodo, JButton> getBotonesHerramientas() { 
        return botonesHerramientas; 
    }
    
    public DefaultTableModel getModeloTablaProyectos() { 
        return modeloTablaProyectos; 
    }

    // =======================================================
    // MÉTODOS PRIVADOS DE CONSTRUCCIÓN DE INTERFAZ
    // =======================================================

    /**
     * Construye y ensambla todos los componentes visuales del panel lateral.
     * Está dividido en dos tarjetas o bloques principales: Gestión de Red y Proyectos Recientes.
     */
    private void inicializarComponentes() {
        // ==========================================
        // TARJETA 1: GESTIÓN DE RED (CRUD Y PALETA)
        // ==========================================
        JPanel panelGestion = FabricaInterfaz.crearContenedorRedondeado();
        panelGestion.add(FabricaInterfaz.crearTituloSeccion("GESTIÓN DE RED"));
        panelGestion.add(Box.createRigidArea(new Dimension(0, 5)));

        // Generar un botón por cada tipo de hardware disponible en el Enum
        JPanel panelGridPaleta = new JPanel(new GridLayout(0, 2, 5, 5)); // Grid de 2 columnas
        panelGridPaleta.setOpaque(false);

        for (Modelo.dominio.TipoNodo tipo : Modelo.dominio.TipoNodo.values()) {
            // Se excluyen los empalmes lógicos de la paleta de usuario
            if (tipo == Modelo.dominio.TipoNodo.EMPALME) {
                continue; 
            }
            
            // Configuración del botón de herramienta
            JButton btnHardware = new JButton(tipo.getNombre());
            btnHardware.setFont(new Font("SansSerif", Font.BOLD, 10));
            btnHardware.setBackground(PaletaTema.FONDO_PANEL);
            btnHardware.setForeground(PaletaTema.TEXTO_BLANCO);
            btnHardware.setFocusPainted(false);
            btnHardware.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
            btnHardware.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Intentar cargar el icono correspondiente al hardware
            try {
                java.net.URL url = getClass().getResource(tipo.getRutaIcono());
                if (url != null) {
                    Image img = new ImageIcon(url).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                    btnHardware.setIcon(new ImageIcon(img));
                    btnHardware.setVerticalTextPosition(SwingConstants.BOTTOM);
                    btnHardware.setHorizontalTextPosition(SwingConstants.CENTER);
                }
            } catch (Exception ignored) {
                // Falla silenciosa: si no hay icono, se muestra solo el texto
            }
            
            btnHardware.setBorder(BorderFactory.createLineBorder(Util.UI.PaletaTema.NEON_AZUL, 0));
            botonesHerramientas.put(tipo, btnHardware);
            panelGridPaleta.add(btnHardware);
        }

        panelGestion.add(panelGridPaleta);
        panelGestion.add(Box.createRigidArea(new Dimension(0, 10)));
       
        // Botón general de eliminación
        btnEliminarNodo = FabricaInterfaz.crearBotonAccion("🗑 Eliminar Nodo", PaletaTema.NEON_AZUL);
        agregarBotonAContenedor(btnEliminarNodo, panelGestion);
        
        add(panelGestion);
        add(Box.createRigidArea(new Dimension(0, 10))); 
        
        // ==========================================
        // TARJETA 2: PROYECTOS RECIENTES
        // ==========================================
        JPanel panelProyectos = FabricaInterfaz.crearContenedorRedondeado();
        panelProyectos.setLayout(new BorderLayout(0, 5)); 
        
        JLabel lblProyectos = FabricaInterfaz.crearTituloSeccion("PROYECTOS RECIENTES");
        lblProyectos.setHorizontalAlignment(SwingConstants.CENTER);
        panelProyectos.add(lblProyectos, BorderLayout.NORTH);

        // Modelo de tabla no editable
        modeloTablaProyectos = new DefaultTableModel(new Object[]{"Nombre del Archivo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
        };

        // Configuración visual de la tabla
        tablaProyectos = new JTable(modeloTablaProyectos);
        tablaProyectos.setBackground(PaletaTema.FONDO_PRINCIPAL);
        tablaProyectos.setForeground(PaletaTema.TEXTO_BLANCO);
        tablaProyectos.setSelectionBackground(new Color(0, 80, 140)); // Azul oscuro tipo "Selección de Windows"
        tablaProyectos.setSelectionForeground(Color.WHITE);
        tablaProyectos.setGridColor(new Color(60, 60, 60));
        tablaProyectos.setRowHeight(25);
        tablaProyectos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        // Configuración visual de la cabecera de la tabla
        tablaProyectos.getTableHeader().setBackground(PaletaTema.FONDO_PANEL);
        tablaProyectos.getTableHeader().setForeground(PaletaTema.NEON_AZUL);
        tablaProyectos.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        tablaProyectos.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PaletaTema.NEON_AZUL));

        // Envoltura con Scroll
        JScrollPane scrollTabla = new JScrollPane(tablaProyectos);
        scrollTabla.setPreferredSize(new Dimension(250, 110)); // Restringe el alto para que no desborde
        scrollTabla.getViewport().setBackground(PaletaTema.FONDO_PRINCIPAL);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        scrollTabla.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panelProyectos.add(scrollTabla, BorderLayout.CENTER);

        add(panelProyectos);
        add(Box.createVerticalGlue()); // Empuja todo hacia arriba
    }

    /**
     * Método auxiliar para dar un tamaño estandarizado y agregar márgenes 
     * a los botones sueltos dentro de un contenedor.
     */
    private void agregarBotonAContenedor(JButton boton, JPanel contenedor) {
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedor.add(boton);
        contenedor.add(Box.createRigidArea(new Dimension(0, 10)));
    }
}