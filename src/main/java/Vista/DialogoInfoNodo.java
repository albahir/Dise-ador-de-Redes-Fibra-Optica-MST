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

/**
 * Cuadro de diálogo modal que muestra una auditoría completa de un equipo (Nodo).
 * Realiza un cálculo inverso (Trazabilidad Óptica) para determinar 
 * qué OLT alimenta a este equipo, cuánta luz recibe y qué clientes cuelgan de él.
 */
public class DialogoInfoNodo extends JDialog {

    // Dependencias principales
    private final Nodo nodo;
    private final TopologiaRed modelo;

    // Variables de estado y análisis en tiempo real
    private Nodo oltRaiz = null;
    private Nodo uplinkPadre = null;
    private final List<Nodo> downlinkHijos = new ArrayList<>();
    private double atenuacion = 0.0;

    // =======================================================
    // CONSTRUCTOR
    // =======================================================

    /**
     * Construye el inspector de nodo.
     * @param parent La ventana padre.
     * @param nodo El equipo a auditar.
     * @param modelo El grafo completo de la red necesario para rastrear la luz.
     */
    public DialogoInfoNodo(JFrame parent, Nodo nodo, TopologiaRed modelo) {
        super(parent, "Inspector de Red", false); 
        this.nodo = nodo;
        this.modelo = modelo;

        setUndecorated(true);
        
        // Ejecutamos el motor lógico ANTES de pintar la interfaz
        analizarTrazabilidadOptica();
        configurarDialogo();
        
        pack();
        setLocationRelativeTo(parent);
    }

    // =======================================================
    // LÓGICA DE NEGOCIO (ANÁLISIS DE RED)
    // =======================================================

    /**
     * Utiliza un algoritmo BFS para recorrer el árbol MST construido.
     * Identifica el nodo padre (de donde viene la luz), el nodo raíz (OLT)
     * y extrae la lista de todos los equipos directamente conectados a los puertos de salida de este nodo.
     */
    private void analizarTrazabilidadOptica() {
        // Obtenemos la pérdida total desde la central hasta este equipo
        this.atenuacion = modelo.calcularAtenuacionHasta(nodo);

        // 1. Ubicamos la fuente de luz en el mapa
        this.oltRaiz = modelo.getNodos().stream()
                .filter(n -> n.getTipo() == TipoNodo.CENTRAL_OLT)
                .findFirst()
                .orElse(null);

        // Si no hay OLT instalada, la auditoría se detiene tempranamente
        if (oltRaiz == null) {
            return;
        }

        // 2. Mapeo de paternidad mediante Búsqueda en Anchura (BFS)
        Map<Nodo, Nodo> mapaPadres = new HashMap<>();
        Queue<Nodo> cola = new LinkedList<>();
        Set<Nodo> visitados = new HashSet<>();

        cola.add(oltRaiz);
        visitados.add(oltRaiz);

        // Trazamos toda la red activa (enlaces verdes) guardando quién conectó a quién
        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();
            for (Enlace e : modelo.getEnlaces()) {
                if (!e.esSolucion()) continue; 

                Nodo vecino = null;
                if (e.getOrigen().equals(actual)) vecino = e.getDestino();
                else if (e.getDestino().equals(actual)) vecino = e.getOrigen();

                if (vecino != null && !visitados.contains(vecino)) {
                    mapaPadres.put(vecino, actual); // El 'actual' es el padre del 'vecino'
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }

        // 3. Extracción de resultados para este nodo en específico
        this.uplinkPadre = mapaPadres.get(nodo);
        
        // Buscamos a todos los nodos cuyo padre registrado sea nuestro nodo inspeccionado
        for (Map.Entry<Nodo, Nodo> entry : mapaPadres.entrySet()) {
            if (entry.getValue().equals(nodo)) {
                this.downlinkHijos.add(entry.getKey());
            }
        }
    }

    // =======================================================
    // CONSTRUCCIÓN DE INTERFAZ GRÁFICA
    // =======================================================

    /**
     * Construye el contenedor visual y decide qué información ocultar
     * dependiendo de si el equipo es un Cliente, un Distribuidor o la OLT.
     */
    private void configurarDialogo() {
        JPanel panelFondo = new JPanel(new BorderLayout(10, 10));
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        
        // Borde verde neón para indicar "Inspección Exitosa / Auditoría de Hardware"
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaTema.NEON_VERDE, 2),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // --- ENCABEZADO ---
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion("Detalles de Dispositivo");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        // --- PANEL CENTRAL ---
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setOpaque(false);

        // BLOQUE 1: DATOS GENERALES (Matriz de 2 columnas)
        JPanel panelDatos = new JPanel(new GridLayout(0, 2, 15, 12));
        panelDatos.setOpaque(false);
        panelDatos.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Datos inmutables visibles en cualquier equipo
        agregarFila(panelDatos, "ID del Equipo:", nodo.getId());
        agregarFila(panelDatos, "Tipo:", nodo.getTipo().getNombre());

        // Lógica de visualización de alimentación: Ocultar si el equipo es la OLT matriz
        if (nodo.getTipo() != TipoNodo.CENTRAL_OLT) {
            double umbralMaximo = Modelo.dominio.ConfiguracionGlobal.getInstance().getUmbralSensibilidad();
            String strAtenuacion = String.format("%.2f dB", atenuacion);
            
            // Si la atenuación excede la sensibilidad, alertamos al usuario
            if (atenuacion > umbralMaximo) {
                strAtenuacion += " (CRÍTICO)";
            }
            agregarFila(panelDatos, "Pérdida Acumulada:", atenuacion > 0 ? strAtenuacion : "0.00 dBm (Sin enlace)");

            // Verificamos quién le da internet
            String textoUplink = (uplinkPadre != null) ? uplinkPadre.getId() + " (" + uplinkPadre.getTipo().name() + ")" : "Desconectado";
            agregarFila(panelDatos, "Alimentado por:", textoUplink);
            
            String textoRaiz = (oltRaiz != null) ? oltRaiz.getId() : "Ninguna";
            agregarFila(panelDatos, "OLT Principal:", textoRaiz);
        }

        // Lógica de visualización de puertos: Ocultar si es un Cliente final (no tiene salidas)
        if (nodo.getTipo() != TipoNodo.CLIENTE) {
            agregarFila(panelDatos, "Puertos Totales:", String.valueOf(nodo.getMaxPuertosOut()));
            agregarFila(panelDatos, "Puertos Usados:", String.valueOf(downlinkHijos.size()));
        }

        panelCentral.add(panelDatos, BorderLayout.NORTH);

        // BLOQUE 2: LISTA DE HIJOS (MAPA DE PUERTOS)
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
     * Construye una lista de desplazamiento vertical (ScrollPane) que enumera
     * secuencialmente los equipos que están conectados a los puertos de salida.
     */
    private JPanel crearPanelPuertos() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel lblTituloConexiones = new JLabel("MAPA DE PUERTOS DE SALIDA");
        lblTituloConexiones.setForeground(PaletaTema.NEON_AZUL);
        lblTituloConexiones.setFont(new Font("SansSerif", Font.BOLD, 11));
        wrapper.add(lblTituloConexiones, BorderLayout.NORTH);

        // Validación visual: Mostrar aviso si el distribuidor está vacío
        if (downlinkHijos.isEmpty()) {
            JLabel lblVacio = new JLabel("No hay equipos conectados a las salidas.");
            lblVacio.setForeground(PaletaTema.TEXTO_GRIS);
            lblVacio.setFont(new Font("SansSerif", Font.ITALIC, 12));
            wrapper.add(lblVacio, BorderLayout.CENTER);
            return wrapper;
        }

        // Caja de elementos apilados en Y (Lista)
        JPanel panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBackground(PaletaTema.FONDO_CONTENEDOR);

        // Generamos la impresión de cada puerto utilizando formato de consola técnica
        int puerto = 1;
        for (Nodo hijo : downlinkHijos) {
            JLabel lblHijo = new JLabel(String.format("  [PUERTO %02d]  %s (%s)", puerto, hijo.getId(), hijo.getTipo().name()));
            lblHijo.setForeground(PaletaTema.TEXTO_BLANCO);
            lblHijo.setFont(new Font("Consolas", Font.PLAIN, 12)); 
            lblHijo.setBorder(new EmptyBorder(6, 5, 6, 5));
            panelLista.add(lblHijo);
            puerto++;
        }

        // Envolvemos en un ScrollPane para prevenir que una lista gigante deforme el diálogo
        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setPreferredSize(new Dimension(350, 110)); // Muestra ~4 filas, luego activa scroll
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        scroll.getViewport().setBackground(PaletaTema.FONDO_CONTENEDOR);
        
        // Mejoras de UX sobre los controles nativos
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16); // Scroll más suave con la rueda del ratón

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Método auxiliar para insertar etiquetas y valores en el panel general.
     * Si detecta la palabra "(CRÍTICO)", cambia automáticamente el texto a Rojo Neón.
     */
    private void agregarFila(JPanel panel, String etiqueta, String valor) {
        JLabel lblEti = new JLabel(etiqueta);
        lblEti.setForeground(PaletaTema.TEXTO_GRIS);
        lblEti.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JLabel lblVal = new JLabel(valor);
        lblVal.setForeground(PaletaTema.TEXTO_BLANCO);
        lblVal.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        // Sistema de alerta visual dentro de la fila
        if (valor.contains("(CRÍTICO)")) {
            lblVal.setForeground(PaletaTema.NEON_ROJO);
            lblVal.setFont(new Font("SansSerif", Font.BOLD, 13));
        }

        panel.add(lblEti);
        panel.add(lblVal);
    }
}