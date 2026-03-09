package Vista;

import Modelo.dominio.TipoNodo;
import Modelo.dominio.TopologiaRed;
import Util.UI.FabricaInterfaz;
import static Util.UI.FabricaInterfaz.crearSpinnerOscuroDecimal;
import static Util.UI.FabricaInterfaz.crearSpinnerOscuroInt;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cuadro de diálogo modal dinámico para la creación y edición de Nodos (equipos).
 *  * Utiliza un CardLayout para mostrar diferentes campos de configuración
 * dependiendo del tipo de equipo seleccionado (OLT, NAP, Splitter, etc.).
 */
public class DialogoNodoDinamico extends JDialog {

    // =======================================================
    // ATRIBUTOS DE INTERFAZ Y MODELO
    // =======================================================
    
    // Componentes del formulario general
    private JTextField txtIdNodo;
    private JComboBox<TipoNodo> comboTipoNodo;
    private JComboBox<String> comboEstado;
    
    // Contenedores dinámicos
    private JPanel panelAtributosDinamicos;
    private CardLayout cardLayout;
    
    // Entradas específicas según el hardware
    private JSpinner spCapacidadOLT;
    private JSpinner spPotenciaTx;
    private JComboBox<String> comboSplitterNAP;
    private JComboBox<String> comboConfigSplitterN1;
    private JSpinner spDemandaCliente;
    
    // Variables de estado y referencia
    private boolean confirmado = false;
    private final TopologiaRed modelo;

    // =======================================================
    // CONSTRUCTOR
    // =======================================================

    /**
     * Construye el diálogo dinámico.
     * @param parent La ventana principal que actuará como padre.
     * @param modelo El modelo de datos de la red (necesario para validaciones de arquitectura).
     */
    public DialogoNodoDinamico(JFrame parent, TopologiaRed modelo) {
        super(parent, "Configurar Equipo de Red", true);
        this.modelo = modelo;
      
        setUndecorated(true); // Estilo personalizado sin bordes nativos del SO
        
        configurarDialogo();
        inicializarComponentes();
        
        setPreferredSize(new Dimension(420, 480));
        pack();
        setLocationRelativeTo(parent); // Centrar respecto al padre
    }

    // =======================================================
    // MÉTODOS PÚBLICOS
    // =======================================================

    /**
     * Retorna si el usuario hizo clic en "Confirmar" pasando las validaciones.
     * @return 
     */
    public boolean isConfirmado() { 
        return confirmado; 
    }

    /**
     * Obtiene el identificador escrito por el usuario.
     * @return 
     */
    public String getIdNodo() { 
        return txtIdNodo.getText(); 
    }

    /**
     * Obtiene el tipo de dispositivo seleccionado en el ComboBox principal.
     * @return 
     */
    public TipoNodo getTipoSeleccionado() { 
        return (TipoNodo) comboTipoNodo.getSelectedItem(); 
    }

    /**
     * Extrae el valor de capacidad (puertos o demanda) dependiendo del tipo
     * de hardware que esté actualmente seleccionado y visible.
     * @return El número entero representativo de la capacidad.
     */
    public int getCapacidadConfigurada() {
        TipoNodo tipo = getTipoSeleccionado();
        try {
            if (null != tipo) {
                switch (tipo) {
                    case CENTRAL_OLT -> {
                        spCapacidadOLT.commitEdit();
                        return ((Number) spCapacidadOLT.getValue()).intValue();
                    }
                    case SPLITTER_N1 -> {
                        String configSplit = (String) comboConfigSplitterN1.getSelectedItem();
                        if (configSplit != null && configSplit.contains(":")) {
                            return Integer.parseInt(configSplit.split(":")[1]); // Extrae el '8' de "1:8"
                        }
                    }
                    case CAJA_NAP -> {
                        String config = (String) comboSplitterNAP.getSelectedItem();
                        if (config != null && config.contains(":")) {
                            return Integer.parseInt(config.split(":")[1]); // Extrae el '16' de "1:16"
                        }
                    }
                    default -> {
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error leyendo capacidad, usando valor por defecto.");
        }
        // Si es un Cliente (o hay un error de parseo), retorna la capacidad máxima definida por defecto
        return tipo.getCapacidadMaxima(); 
    }

    /**
     * Pre-carga el formulario con los datos de un nodo ya existente.
     * Se usa cuando el usuario edita (doble clic) en lugar de crear uno nuevo.
     * @param nodo El nodo con los datos a editar.
     */
    public void cargarDatosPrevios(Modelo.dominio.Nodo nodo) {
        // 1. Cargar y bloquear el ID (En telecomunicaciones no se le cambia el nombre a un equipo ya cableado)
        if (txtIdNodo != null) {
            txtIdNodo.setText(nodo.getId());
            txtIdNodo.setEnabled(false); // Bloqueamos la edición del ID
        }

        // 2. Seleccionar el Tipo en el ComboBox principal y bloquearlo
        if (comboTipoNodo != null) {
            comboTipoNodo.setSelectedItem(nodo.getTipo());
            comboTipoNodo.setEnabled(false); 
        }

        // 3. Pre-cargar la Capacidad en el panel correspondiente según el hardware
        Modelo.dominio.TipoNodo tipo = nodo.getTipo();
        
        if (null != tipo) {
            switch (tipo) {
                case CAJA_NAP -> {
                    if (comboSplitterNAP != null) {
                        comboSplitterNAP.setSelectedItem("1:" + nodo.getCapacidad());
                    }
                }
                case CENTRAL_OLT -> {
                    if (spCapacidadOLT != null) {
                        spCapacidadOLT.setValue(nodo.getCapacidad());
                    }
                }
                case SPLITTER_N1 -> {
                    if (comboConfigSplitterN1 != null) {
                        comboConfigSplitterN1.setSelectedItem("1:" + nodo.getCapacidad());
                    }
                }
                default -> {
                }
            }
        }
    }

    /**
     * Fuerza al formulario a comportarse para un tipo específico de hardware
     * cuando el usuario lo selecciona desde la paleta de herramientas.
     */
    public void fijarTipoConstruccion(Modelo.dominio.TipoNodo tipoConstruccion) {
        if (comboTipoNodo != null) {
            comboTipoNodo.setSelectedItem(tipoConstruccion);
            comboTipoNodo.setEnabled(false); // Bloquea el selector
            
            // Fuerza a que se muestre la tarjeta dinámica correcta
            cardLayout.show(panelAtributosDinamicos, tipoConstruccion.name());
            
            // Genera el ID sugerido para este tipo
            txtIdNodo.setText(generarIdSugerido(tipoConstruccion));
        }
    }

    // =======================================================
    // MÉTODOS PRIVADOS (CONSTRUCCIÓN Y LÓGICA INTERNA)
    // =======================================================

    /**
     * Aplica los estilos del fondo maestro, reemplazando el Panel nativo.
     */
    private void configurarDialogo() {
        JPanel panelFondoMaestro = new JPanel();
        panelFondoMaestro.setLayout(new BorderLayout());
        panelFondoMaestro.setBackground(PaletaTema.FONDO_PRINCIPAL);
        
        panelFondoMaestro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaTema.NEON_AZUL, 2), 
                new EmptyBorder(20, 25, 20, 25)
        ));
        
        setContentPane(panelFondoMaestro);
    }

    /**
     * Inicializa los campos fijos, las tarjetas dinámicas (CardLayout) y los botones.
     */
    private void inicializarComponentes() {
        // --- PANEL NORTE (Datos Generales) ---
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);
        
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion("PROPIEDADES DEL NODO");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelGeneral = new JPanel(new GridLayout(3, 2, 10, 15));
        panelGeneral.setOpaque(false);
        panelGeneral.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        panelGeneral.add(crearLabel("ID del Equipo:"));
        txtIdNodo = FabricaInterfaz.crearTextFieldOscuro(generarIdSugerido(TipoNodo.values()[0]));
        panelGeneral.add(txtIdNodo);
        
        panelGeneral.add(crearLabel("Tipo de Dispositivo:"));
        comboTipoNodo = FabricaInterfaz.crearComboBoxOscuro(TipoNodo.values());
        panelGeneral.add(comboTipoNodo);

        panelGeneral.add(crearLabel("Estado de Despliegue:"));
        comboEstado = FabricaInterfaz.crearComboBoxOscuro(new String[]{"Planeado", "En Construcción", "Operativo"});
        panelGeneral.add(comboEstado);

        panelNorte.add(panelGeneral, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // --- PANEL CENTRAL (Atributos Dinámicos usando CardLayout) ---
        cardLayout = new CardLayout();
        panelAtributosDinamicos = new JPanel(cardLayout);
        panelAtributosDinamicos.setBackground(PaletaTema.FONDO_PANEL);
        panelAtributosDinamicos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Creación de las "Tarjetas" (Cards)
        JPanel panelOLT = new JPanel(new GridLayout(2, 2, 10, 15));
        panelOLT.setOpaque(false);
        panelOLT.add(crearLabel("Hilos Troncales:"));
        spCapacidadOLT = crearSpinnerOscuroInt(128, 1, 1024, 1);
        panelOLT.add(spCapacidadOLT);
        panelOLT.add(crearLabel("Potencia Tx (dBm):"));
        spPotenciaTx = crearSpinnerOscuroDecimal(2.5, -10.0, 10.0, 0.1);
        panelOLT.add(spPotenciaTx);

        JPanel panelSplitterN1 = new JPanel(new GridLayout(2, 2, 10, 15));
        panelSplitterN1.setOpaque(false);
        panelSplitterN1.add(crearLabel("Config Splitter N1:"));
        comboConfigSplitterN1 = FabricaInterfaz.crearComboBoxOscuro(new String[]{"1:2", "1:4", "1:8", "1:16"});
        panelSplitterN1.add(comboConfigSplitterN1);

        JPanel panelNAP = new JPanel(new GridLayout(2, 2, 10, 15));
        panelNAP.setOpaque(false);
        panelNAP.add(crearLabel("Config Splitter:"));
        comboSplitterNAP = FabricaInterfaz.crearComboBoxOscuro(new String[]{"1:4", "1:8", "1:16"});
        panelNAP.add(comboSplitterNAP);

        JPanel panelCliente = new JPanel(new GridLayout(2, 2, 10, 15));
        panelCliente.setOpaque(false);
        panelCliente.add(crearLabel("Ancho Banda (Mbps):"));
        spDemandaCliente = crearSpinnerOscuroInt(500, 10, 10000, 10);
        panelCliente.add(spDemandaCliente);

        // Agregamos las tarjetas al CardLayout vinculándolas con el nombre del Enum
        panelAtributosDinamicos.add(panelOLT, TipoNodo.CENTRAL_OLT.name());
        panelAtributosDinamicos.add(panelSplitterN1, TipoNodo.SPLITTER_N1.name());
        panelAtributosDinamicos.add(panelNAP, TipoNodo.CAJA_NAP.name());
        panelAtributosDinamicos.add(panelCliente, TipoNodo.CLIENTE.name());

        JPanel wrapperCentro = new JPanel(new BorderLayout());
        wrapperCentro.setOpaque(false);
        wrapperCentro.add(panelAtributosDinamicos, BorderLayout.NORTH);
        add(wrapperCentro, BorderLayout.CENTER);

        // Evento que intercambia las tarjetas visuales al cambiar el ComboBox
        comboTipoNodo.addActionListener(e -> {
            TipoNodo seleccionado = (TipoNodo) comboTipoNodo.getSelectedItem();
            
            // Cambia la tarjeta dinámica
            cardLayout.show(panelAtributosDinamicos, seleccionado.name());
            
            // Solo genera un ID nuevo si estamos creando un equipo nuevo (no editando)
            if (txtIdNodo.isEnabled()) {
                txtIdNodo.setText(generarIdSugerido(seleccionado));
            }
        });

        // --- PANEL SUR (Botones y Validaciones) ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(20, 0, 0, 13));
        
        JButton btnCancelar = FabricaInterfaz.crearBotonAccion("Cancelar", PaletaTema.NEON_AZUL);
        JButton btnGuardar = FabricaInterfaz.crearBotonAccion("Confirmar Nodo", PaletaTema.NEON_AZUL);
        
        btnGuardar.addActionListener((var e) -> {
            String idIngresado = txtIdNodo.getText().trim();

            // Validación 1: Campo vacío
            if (idIngresado.isEmpty()) {
                Util.Tecnicas.GestorAlertas.mostrarError(this, "Validación Fallida", "Debe asignar un ID al nodo.");
                return;
            }

            // Validación 2: Regla de ID Duplicado (Ignorada si estamos editando)
            if (txtIdNodo.isEnabled()) {
                boolean idDuplicado = modelo.getNodos().stream()
                        .anyMatch(n -> n.getId().equalsIgnoreCase(idIngresado));

                if (idDuplicado) {
                    Util.Tecnicas.GestorAlertas.mostrarAdvertencia(this, "ID Duplicado", 
                            "Ya existe un equipo registrado con el ID '" + idIngresado + "'.\nPor favor, asigne un identificador único.");
                    return; 
                }
            }

            // Validación 3: Regla Arquitectónica - Unicidad de la OLT
            TipoNodo tipoSeleccionado = (TipoNodo) comboTipoNodo.getSelectedItem();
            if (tipoSeleccionado == TipoNodo.CENTRAL_OLT) {
                long cantidadOLTs = modelo.getNodos().stream()
                        .filter(n -> n.getTipo() == TipoNodo.CENTRAL_OLT && !n.getId().equalsIgnoreCase(idIngresado))
                        .count();
                
                if (cantidadOLTs > 0) {
                    Util.Tecnicas.GestorAlertas.mostrarAdvertencia(this, "Violación de Arquitectura", 
                            "Ya existe una CENTRAL OLT en este diseño.\n\nUna red FTTH/GPON estándar debe converger hacia una única central de luz.");
                    return; 
                }
            }

            // Si pasa TODAS las validaciones, cambiamos la bandera y cerramos
            confirmado = true;
            dispose(); 
        });

        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);
        
        // Disparamos la configuración inicial
        comboTipoNodo.setSelectedIndex(0);
    }

    /**
     * Construye un JLabel estandarizado en formato "Etiqueta" para usar en los formularios.
     */
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(PaletaTema.TEXTO_GRIS);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        return lbl;
    }

    /**
     * Genera automáticamente una cadena de texto sugerida (Ej. "OLT-4892")
     * basándose en el tipo de hardware que el usuario va a insertar.
     */
    private String generarIdSugerido(TipoNodo tipo) {
        String prefijo;
        prefijo = switch (tipo) {
            case CENTRAL_OLT -> "OLT-";
            case CAJA_NAP -> "NAP-";
            case SPLITTER_N1 -> "SPL-";
            case CLIENTE -> "CLI-";
            default -> "NOD-";
        };
        
        // Generamos un número aleatorio de 4 dígitos
        int numeroAleatorio = (int) (Math.random() * 9000) + 1000;
        return prefijo + numeroAleatorio;
    }
}