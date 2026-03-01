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

public class DialogoNodoDinamico extends JDialog {

    private JTextField txtIdNodo;
    private JComboBox<TipoNodo> comboTipoNodo;
    private JComboBox<String> comboEstado;
    
    private JPanel panelAtributosDinamicos;
    private CardLayout cardLayout;
    
    private JSpinner spCapacidadOLT;
    private JSpinner spPotenciaTx;
    private JComboBox<String> comboSplitterNAP;
    private JComboBox<String> comboConfigSplitterN1;
    private JSpinner spDemandaCliente;
    private boolean confirmado = false;
    private final TopologiaRed modelo;

    public DialogoNodoDinamico(JFrame parent,TopologiaRed modelo) {
        super(parent, "Configurar Equipo de Red", true);
        this.modelo = modelo;
      
        setUndecorated(true); 
        
        configurarDialogo();
        inicializarComponentes();
        
        setPreferredSize(new Dimension(420, 480));
        pack();
        setLocationRelativeTo(parent);
    }

   private void configurarDialogo() {
        // 1. Crear un panel maestro que será el fondo absoluto de la ventana
        JPanel panelFondoMaestro = new JPanel();
        panelFondoMaestro.setLayout(new BorderLayout());
        panelFondoMaestro.setBackground(PaletaTema.FONDO_PRINCIPAL);
        
        // 2. Aplicar el borde Neón y el padding directamente a este panel maestro
        panelFondoMaestro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaTema.NEON_AZUL, 2), // Borde azul de 2 píxeles
                new EmptyBorder(20, 25, 20, 25) // Espacio interior
        ));
        
        // 3. Reemplazar el panel de contenido por defecto de Java con el nuestro
        setContentPane(panelFondoMaestro);
    }
    private void inicializarComponentes() {
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);
        
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion("PROPIEDADES DEL NODO");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelGeneral = new JPanel(new GridLayout(3, 2, 10, 15));
        panelGeneral.setOpaque(false);
        panelGeneral.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        panelGeneral.add(crearLabel("ID del Equipo:"));
        // Lo inicializamos usando el primer valor del ComboBox por defecto
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

        // ATRIBUTOS DINÁMICOS
        cardLayout = new CardLayout();
        panelAtributosDinamicos = new JPanel(cardLayout);
        panelAtributosDinamicos.setBackground(PaletaTema.FONDO_PANEL);
        panelAtributosDinamicos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Tarjetas
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

        panelAtributosDinamicos.add(panelOLT, TipoNodo.CENTRAL_OLT.name());
        panelAtributosDinamicos.add(panelSplitterN1, TipoNodo.SPLITTER_N1.name());
        panelAtributosDinamicos.add(panelNAP, TipoNodo.CAJA_NAP.name());
        panelAtributosDinamicos.add(panelCliente, TipoNodo.CLIENTE.name());

      
        JPanel wrapperCentro = new JPanel(new BorderLayout());
        wrapperCentro.setOpaque(false);
        

        wrapperCentro.add(panelAtributosDinamicos, BorderLayout.NORTH);
        
        // Añadimos el envoltorio al centro del diálogo
        add(wrapperCentro, BorderLayout.CENTER);

        comboTipoNodo.addActionListener(e -> {
            TipoNodo seleccionado = (TipoNodo) comboTipoNodo.getSelectedItem();
            
            // 1. Cambia la tarjeta dinámica (Esto funciona perfecto)
            cardLayout.show(panelAtributosDinamicos, seleccionado.name());
            
            // 2. ¡EL ARREGLO! Solo genera un ID nuevo si NO estamos editando0
            if (txtIdNodo.isEnabled()) {
                txtIdNodo.setText(generarIdSugerido(seleccionado));
            }
        });

        // BOTONES
     JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(20, 0, 0, 13));
        
        JButton btnCancelar = FabricaInterfaz.crearBotonAccion("Cancelar", PaletaTema.NEON_AZUL);
        JButton btnGuardar = FabricaInterfaz.crearBotonAccion("Confirmar Nodo", PaletaTema.NEON_AZUL);
        
        btnGuardar.addActionListener((var e) -> {
            String idIngresado = txtIdNodo.getText().trim();

            // 1. Validar vacío
            if (idIngresado.isEmpty()) {
                Util.UI.GestorAlertas.mostrarError(this, "Validación Fallida", "Debe asignar un ID al nodo.");
                return;
            }

            // 2. Regla de ID Duplicado (Solo al crear)
            if (txtIdNodo.isEnabled()) {
                boolean idDuplicado = modelo.getNodos().stream()
                        .anyMatch(n -> n.getId().equalsIgnoreCase(idIngresado));

                if (idDuplicado) {
                    Util.UI.GestorAlertas.mostrarAdvertencia(this, "ID Duplicado", 
                            "Ya existe un equipo registrado con el ID '" + idIngresado + "'.\nPor favor, asigne un identificador único.");
                    return; 
                }
            }

            // 3. Regla de Unicidad de OLT
            TipoNodo tipoSeleccionado = (TipoNodo) comboTipoNodo.getSelectedItem();
            if (tipoSeleccionado == TipoNodo.CENTRAL_OLT) {
                long cantidadOLTs = modelo.getNodos().stream()
                        .filter(n -> n.getTipo() == TipoNodo.CENTRAL_OLT && !n.getId().equalsIgnoreCase(idIngresado))
                        .count();
                
                if (cantidadOLTs > 0) {
                    Util.UI.GestorAlertas.mostrarAdvertencia(this, "Violación de Arquitectura", 
                            "Ya existe una CENTRAL OLT en este diseño.\n\nUna red FTTH/GPON estándar debe converger hacia una única central de luz.");
                    return; 
                }
            }

            

            // Si pasa TODAS las validaciones, confirmamos y cerramos
            confirmado = true;
            dispose(); 
        });

        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);
        
        comboTipoNodo.setSelectedIndex(0);
    }
    public int getCapacidadConfigurada() {
        TipoNodo tipo = getTipoSeleccionado();
        try {
            if (null != tipo) switch (tipo) {
                case CENTRAL_OLT -> {
                    spCapacidadOLT.commitEdit();
                    return ((Number) spCapacidadOLT.getValue()).intValue();
                }
                case SPLITTER_N1 -> {
                    String configSplit = (String) comboConfigSplitterN1.getSelectedItem();
                    if (configSplit != null && configSplit.contains(":")) {
                        return Integer.parseInt(configSplit.split(":")[1]); 
                    }
                }
                case CAJA_NAP -> {
                    // Extraemos el número después de los dos puntos (ej. "1:4" -> 4)
                    String config = (String) comboSplitterNAP.getSelectedItem();
                    if (config != null && config.contains(":")) {
                        return Integer.parseInt(config.split(":")[1]);
                    }
                }
               
                default -> {
                }
            }
        } catch (Exception e) {
            System.err.println("Error leyendo capacidad, usando valor por defecto.");
        }
        // Si es un Cliente (o hay error), retorna la capacidad por defecto de la clase enum
        return tipo.getCapacidadMaxima(); 
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(PaletaTema.TEXTO_GRIS);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        return lbl;
    }
private String generarIdSugerido(TipoNodo tipo) {
        String prefijo;
        prefijo = switch (tipo) {
            case CENTRAL_OLT -> "OLT-";
            case CAJA_NAP -> "NAP-";
            case SPLITTER_N1 -> "SPL-";
            case CLIENTE -> "CLI-";
            default -> "NOD-";
        };
        
        // Generamos un número aleatorio de 4 dígitos (ej. 4092)
        int numeroAleatorio = (int) (Math.random() * 9000) + 1000;
        return prefijo + numeroAleatorio;
    }
    public boolean isConfirmado() { return confirmado; }
    public String getIdNodo() { return txtIdNodo.getText(); }
    public TipoNodo getTipoSeleccionado() { return (TipoNodo) comboTipoNodo.getSelectedItem(); }

public void cargarDatosPrevios(Modelo.dominio.Nodo nodo) {
        // 1. Cargar y bloquear el ID (En telecomunicaciones no se le cambia el nombre a un equipo ya cableado)
        // NOTA: Reemplaza 'txtIdNodo' por el nombre real de tu campo de texto del ID si se llama distinto.
        if (txtIdNodo != null) {
            txtIdNodo.setText(nodo.getId());
            txtIdNodo.setEnabled(false); // Bloqueamos la edición del ID
        }

        // 2. Seleccionar el Tipo en el ComboBox principal
       if (comboTipoNodo != null) {
            comboTipoNodo.setSelectedItem(nodo.getTipo());
            comboTipoNodo.setEnabled(false); 
        }

        // 3. Pre-cargar la Capacidad en el panel correspondiente
        Modelo.dominio.TipoNodo tipo = nodo.getTipo();
        
        if (null != tipo) switch (tipo) {
            case CAJA_NAP -> {
                // Convertimos el número 4 en el texto "1:4" para el ComboBox
                if (comboSplitterNAP != null) {
                    comboSplitterNAP.setSelectedItem("1:" + nodo.getCapacidad());
                }
            }
            
            case CENTRAL_OLT -> {
                if (spCapacidadOLT != null) spCapacidadOLT.setValue(nodo.getCapacidad());
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
}