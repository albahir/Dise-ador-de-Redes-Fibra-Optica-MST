package Vista;

import Modelo.dominio.Enlace;
import Modelo.dominio.Nodo;
import Modelo.dominio.TipoNodo;
import Modelo.dominio.TopologiaRed;
import Util.UI.FabricaInterfaz;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DialogoInfoNodo extends JDialog {

    private final Nodo nodo;
    private final TopologiaRed modelo;

    // Datos analizados
    private Nodo oltRaiz = null;
    private Nodo uplinkPadre = null;
    private final List<Nodo> downlinkHijos = new ArrayList<>();
    private double atenuacion = 0.0;

    public DialogoInfoNodo(JFrame parent, Nodo nodo, TopologiaRed modelo) {
        super(parent, "Inspector de Red", false); 
        this.nodo = nodo;
        this.modelo = modelo;

        setUndecorated(true);
        analizarTrazabilidadOptica();
        configurarDialogo();
        
        pack();
        setLocationRelativeTo(parent);
    }

    private void analizarTrazabilidadOptica() {
        this.atenuacion = modelo.calcularAtenuacionHasta(nodo);

        this.oltRaiz = modelo.getNodos().stream()
                .filter(n -> n.getTipo() == TipoNodo.CENTRAL_OLT)
                .findFirst()
                .orElse(null);

        if (oltRaiz == null) return;

        Map<Nodo, Nodo> mapaPadres = new HashMap<>();
        Queue<Nodo> cola = new LinkedList<>();
        Set<Nodo> visitados = new HashSet<>();

        cola.add(oltRaiz);
        visitados.add(oltRaiz);

        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();
            for (Enlace e : modelo.getEnlaces()) {
                if (!e.esSolucion()) continue; 

                Nodo vecino = null;
                if (e.getOrigen().equals(actual)) vecino = e.getDestino();
                else if (e.getDestino().equals(actual)) vecino = e.getOrigen();

                if (vecino != null && !visitados.contains(vecino)) {
                    mapaPadres.put(vecino, actual); 
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }

        this.uplinkPadre = mapaPadres.get(nodo);
        for (Map.Entry<Nodo, Nodo> entry : mapaPadres.entrySet()) {
            if (entry.getValue().equals(nodo)) {
                this.downlinkHijos.add(entry.getKey());
            }
        }
    }

    private void configurarDialogo() {
        JPanel panelFondo = new JPanel(new BorderLayout(10, 10));
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        
        // Borde verde para indicar "Auditoría"
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaTema.NEON_VERDE, 2),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // --- ENCABEZADO ---
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion("Detalles de Dispositivo");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        // --- PANEL CENTRAL (Divide Datos Generales y Lista de Puertos) ---
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setOpaque(false);

        // 1. DATOS GENERALES FILTRADOS
        JPanel panelDatos = new JPanel(new GridLayout(0, 2, 15, 12));
        panelDatos.setOpaque(false);
        panelDatos.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Siempre mostrar ID y Tipo
        agregarFila(panelDatos, "ID del Equipo:", nodo.getId());
        agregarFila(panelDatos, "Tipo:", nodo.getTipo().getNombre());

        // Ocultar info de Alimentación (Uplink) si es OLT
        if (nodo.getTipo() != TipoNodo.CENTRAL_OLT) {
            double umbralMaximo = Modelo.dominio.ConfiguracionGlobal.getInstance().getUmbralSensibilidad();
            String strAtenuacion = String.format("%.2f dB", atenuacion);
            if (atenuacion > umbralMaximo) strAtenuacion += " (CRÍTICO)";
            agregarFila(panelDatos, "Pérdida Acumulada:", atenuacion > 0 ? strAtenuacion : "0.00 dBm (Sin enlace)");

            String textoUplink = (uplinkPadre != null) ? uplinkPadre.getId() + " (" + uplinkPadre.getTipo().name() + ")" : "Desconectado";
            agregarFila(panelDatos, "Alimentado por:", textoUplink);
            
            String textoRaiz = (oltRaiz != null) ? oltRaiz.getId() : "Ninguna";
            agregarFila(panelDatos, "OLT Principal:", textoRaiz);
        }

        // Ocultar info de Puertos y Salidas si es Cliente
        if (nodo.getTipo() != TipoNodo.CLIENTE) {
            agregarFila(panelDatos, "Puertos Totales:", String.valueOf(nodo.getMaxPuertosOut()));
            agregarFila(panelDatos, "Puertos Usados:", String.valueOf(downlinkHijos.size()));
        }

        panelCentral.add(panelDatos, BorderLayout.NORTH);

        // 2. LISTA DETALLADA DE CONEXIONES CON SCROLL (Solo si no es Cliente)
        if (nodo.getTipo() != TipoNodo.CLIENTE) {
            panelCentral.add(crearPanelPuertos(), BorderLayout.CENTER);
        }

        panelFondo.add(panelCentral, BorderLayout.CENTER);

        // --- BOTÓN CERRAR ---
        JButton btnCerrar = FabricaInterfaz.crearBotonAccion("Cerrar", PaletaTema.TEXTO_GRIS);
        btnCerrar.addActionListener(e -> dispose());
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        panelBoton.setBorder(new EmptyBorder(10, 0, 0, 0));
        panelBoton.add(btnCerrar);
        
        panelFondo.add(panelBoton, BorderLayout.SOUTH);

        setContentPane(panelFondo);
    }

    /**
     * Crea un panel con Scroll que lista exactamente qué equipo está en cada puerto.
     */
    private JPanel crearPanelPuertos() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel lblTituloConexiones = new JLabel("MAPA DE PUERTOS DE SALIDA");
        lblTituloConexiones.setForeground(PaletaTema.NEON_AZUL);
        lblTituloConexiones.setFont(new Font("SansSerif", Font.BOLD, 11));
        wrapper.add(lblTituloConexiones, BorderLayout.NORTH);

        if (downlinkHijos.isEmpty()) {
            JLabel lblVacio = new JLabel("No hay equipos conectados a las salidas.");
            lblVacio.setForeground(PaletaTema.TEXTO_GRIS);
            lblVacio.setFont(new Font("SansSerif", Font.ITALIC, 12));
            wrapper.add(lblVacio, BorderLayout.CENTER);
            return wrapper;
        }

        // Panel interno que contendrá la lista
        JPanel panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBackground(PaletaTema.FONDO_CONTENEDOR);

        int puerto = 1;
        for (Nodo hijo : downlinkHijos) {
            // Estilo técnico "Consola"
            JLabel lblHijo = new JLabel(String.format("  [PUERTO %02d]  %s (%s)", puerto, hijo.getId(), hijo.getTipo().name()));
            lblHijo.setForeground(PaletaTema.TEXTO_BLANCO);
            lblHijo.setFont(new Font("Consolas", Font.PLAIN, 12)); 
            lblHijo.setBorder(new EmptyBorder(6, 5, 6, 5));
            panelLista.add(lblHijo);
            puerto++;
        }

        // Envolvemos en un ScrollPane
        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setPreferredSize(new Dimension(350, 110)); // Muestra unos 4 o 5 elementos, luego hace scroll
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        scroll.getViewport().setBackground(PaletaTema.FONDO_CONTENEDOR);
        
        // Quitar comportamiento molesto de las barras en Swing
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16); // Scroll más fluido con la rueda del ratón

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void agregarFila(JPanel panel, String etiqueta, String valor) {
        JLabel lblEti = new JLabel(etiqueta);
        lblEti.setForeground(PaletaTema.TEXTO_GRIS);
        lblEti.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JLabel lblVal = new JLabel(valor);
        lblVal.setForeground(PaletaTema.TEXTO_BLANCO);
        lblVal.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        if (valor.contains("(CRÍTICO)")) {
            lblVal.setForeground(PaletaTema.NEON_ROJO);
            lblVal.setFont(new Font("SansSerif", Font.BOLD, 13));
        }

        panel.add(lblEti);
        panel.add(lblVal);
    }
}