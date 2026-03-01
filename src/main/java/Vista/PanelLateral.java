package Vista;

import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

public class PanelLateral extends JPanel {

    // Reducimos a solo 3 botones principales de gestión
    private JButton btnAgregarNodo;
    
    private JButton btnEliminarNodo;
    // Algoritmos
    private JTable tablaProyectos;
    private javax.swing.table.DefaultTableModel modeloTablaProyectos;
    private final Map<Modelo.dominio.TipoNodo, JButton> botonesHerramientas = new HashMap<>();
   
    public PanelLateral() {
        setBackground(PaletaTema.FONDO_PANEL);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10)); 
        setPreferredSize(new Dimension(270, 0)); 

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // ==========================================
        // TARJETA 1: GESTIÓN DE RED (CRUD)
        // ==========================================
        JPanel panelGestion = FabricaInterfaz.crearContenedorRedondeado();
        panelGestion.add(FabricaInterfaz.crearTituloSeccion("GESTIÓN DE RED"));
        panelGestion.add(Box.createRigidArea(new Dimension(0, 5)));

        
        // 2. Generar un botón por cada tipo de hardware
        JPanel panelGridPaleta = new JPanel(new GridLayout(0, 2, 5, 5)); // Grid de 2 columnas
        panelGridPaleta.setOpaque(false);

        for (Modelo.dominio.TipoNodo tipo : Modelo.dominio.TipoNodo.values()) {
            if (tipo == Modelo.dominio.TipoNodo.EMPALME) {
                continue; 
            }
            JButton btnHardware = new JButton(tipo.getNombre());
            btnHardware.setFont(new Font("SansSerif", Font.BOLD, 10));
            btnHardware.setBackground(PaletaTema.FONDO_PANEL);
            btnHardware.setForeground(PaletaTema.TEXTO_BLANCO);
            btnHardware.setFocusPainted(false);
            btnHardware.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
            btnHardware.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Intentar cargar el icono si existe
            try {
                java.net.URL url = getClass().getResource(tipo.getRutaIcono());
                if (url != null) {
                    Image img = new ImageIcon(url).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                    btnHardware.setIcon(new ImageIcon(img));
                    btnHardware.setVerticalTextPosition(SwingConstants.BOTTOM);
                    btnHardware.setHorizontalTextPosition(SwingConstants.CENTER);
                }
            } catch (Exception ignored) {}
         btnHardware.setBorder(BorderFactory.createLineBorder(Util.UI.PaletaTema.NEON_AZUL, 0));
            botonesHerramientas.put(tipo, btnHardware);
            panelGridPaleta.add(btnHardware);
        }

        panelGestion.add(panelGridPaleta);
        panelGestion.add(Box.createRigidArea(new Dimension(0, 10)));
       
        btnEliminarNodo = FabricaInterfaz.crearBotonAccion("🗑 Eliminar Nodo", PaletaTema.NEON_AZUL);

       

        agregarBotonAContenedor(btnEliminarNodo, panelGestion);
        
        add(panelGestion);
        add(Box.createRigidArea(new Dimension(0, 10))); 
        
        JPanel panelProyectos = FabricaInterfaz.crearContenedorRedondeado();
        panelProyectos.setLayout(new BorderLayout(0, 5)); // Menos separación interna
        
        JLabel lblProyectos = FabricaInterfaz.crearTituloSeccion("PROYECTOS RECIENTES");
        lblProyectos.setHorizontalAlignment(SwingConstants.CENTER);
        panelProyectos.add(lblProyectos, BorderLayout.NORTH);

        modeloTablaProyectos = new javax.swing.table.DefaultTableModel(new Object[]{"Nombre del Archivo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaProyectos = new JTable(modeloTablaProyectos);
        tablaProyectos.setBackground(PaletaTema.FONDO_PRINCIPAL);
        tablaProyectos.setForeground(PaletaTema.TEXTO_BLANCO);
        
    
        tablaProyectos.setSelectionBackground(new Color(0, 80, 140)); // Azul oscuro tipo "Selección de Windows"
        tablaProyectos.setSelectionForeground(Color.WHITE);
        
        tablaProyectos.setGridColor(new Color(60, 60, 60));
        tablaProyectos.setRowHeight(25);
        tablaProyectos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
      
        tablaProyectos.getTableHeader().setBackground(PaletaTema.FONDO_PANEL);
        tablaProyectos.getTableHeader().setForeground(PaletaTema.NEON_AZUL);
        tablaProyectos.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        tablaProyectos.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PaletaTema.NEON_AZUL));

        JScrollPane scrollTabla = new JScrollPane(tablaProyectos);
        // ¡ARREGLO DE ESPACIO! Reducimos el alto de 150 a 110 para que no desborde el panel general
        scrollTabla.setPreferredSize(new Dimension(250, 110)); 
        scrollTabla.getViewport().setBackground(PaletaTema.FONDO_PRINCIPAL);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        scrollTabla.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panelProyectos.add(scrollTabla, BorderLayout.CENTER);

        add(panelProyectos);
        add(Box.createVerticalGlue()); 
        
    }

    private void agregarBotonAContenedor(JButton boton, JPanel contenedor) {
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedor.add(boton);
        contenedor.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    // Getters actualizados
    public JButton getBtnAgregarNodo() { return btnAgregarNodo; }
    public JButton getBtnEliminarNodo() { return btnEliminarNodo; }
    public JTable getTablaProyectos() { return tablaProyectos; }
    public Map<Modelo.dominio.TipoNodo, JButton> getBotonesHerramientas() { return botonesHerramientas; }
    public DefaultTableModel getModeloTablaProyectos() { return modeloTablaProyectos; }
}