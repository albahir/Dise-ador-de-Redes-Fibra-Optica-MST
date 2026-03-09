package Vista;

import Modelo.dominio.ConfiguracionGlobal;
import Util.UI.FabricaInterfaz;
import static Util.UI.FabricaInterfaz.crearSpinnerOscuroDecimal;
import static Util.UI.FabricaInterfaz.crearSpinnerOscuroInt;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cuadro de diálogo modal que permite al usuario visualizar y editar 
 * los parámetros globales de la red (económicos y físicos).
 *  * Los datos se guardan en la instancia Singleton de ConfiguracionGlobal.
 */
public class DialogoConfiguracion extends JDialog {

    // =======================================================
    // ATRIBUTOS DE INTERFAZ Y ESTADO
    // =======================================================

    // Campos de entrada numérica (Spinners) para asegurar datos válidos
    private JSpinner spCostoMetro;
    private JSpinner spCostoBase;
    private JSpinner spAtenuacionKm;
    private JSpinner spPerdidaEmpalme;
    private JSpinner spPerdidaConector;
    private JSpinner spUmbral;
    private JSpinner spPerdidaSplitter;
    private JSpinner spPerdidaNAP;
    private JSpinner spVelocidadAnimacion;
    
    // Bandera para saber si el usuario aplicó los cambios o canceló
    private boolean guardado = false;

    // =======================================================
    // CONSTRUCTOR
    // =======================================================

    /**
     * Constructor del diálogo de configuración.
     * @param parent La ventana principal sobre la cual se centrará y bloqueará este diálogo.
     */
    public DialogoConfiguracion(JFrame parent) {
        super(parent, "Configuración Global del Proyecto", true);
        setUndecorated(true); // Estilo personalizado sin barra de título nativa
        
        configurarVentana();
        inicializarComponentes();
        cargarValoresActuales();
        
        pack(); // Ajusta el tamaño de la ventana al contenido
        setLocationRelativeTo(parent);
    }

    // =======================================================
    // MÉTODOS PÚBLICOS
    // =======================================================

    /**
     * Indica si el diálogo se cerró guardando los datos.
     * @return true si se guardaron los cambios, false si se canceló.
     */
    public boolean isGuardado() { 
        return guardado; 
    }

    // =======================================================
    // MÉTODOS PRIVADOS DE INICIALIZACIÓN (UI)
    // =======================================================

    /**
     * Configura el panel principal (fondo) del diálogo con los colores y bordes del tema.
     */
    private void configurarVentana() {
        JPanel panelFondo = new JPanel(new BorderLayout());
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaTema.NEON_AZUL, 2),
                new EmptyBorder(20, 25, 20, 25)
        ));
        setContentPane(panelFondo);
    }

    /**
     * Instancia, configura y posiciona todos los campos de texto, etiquetas y botones.
     */
    private void inicializarComponentes() {
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion("PARÁMETROS DEL SISTEMA");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitulo, BorderLayout.NORTH);

        // FORMULARIO CENTRAL: Matriz de 9 filas por 2 columnas
        JPanel panelForm = new JPanel(new GridLayout(9, 2, 10, 15));
        panelForm.setOpaque(false);
        panelForm.setBorder(new EmptyBorder(20, 0, 20, 0));

        // --- PARÁMETROS ECONÓMICOS ---
        panelForm.add(crearLabel("Costo Fibra ($/m):"));
        // Parámetros: (Valor inicial, Min, Max, Salto por clic)
        spCostoMetro = crearSpinnerOscuroDecimal(1.50, 0.01, 500.0, 0.10); 
        panelForm.add(spCostoMetro);

        panelForm.add(crearLabel("Costo Base Nodo ($):"));
        spCostoBase = crearSpinnerOscuroDecimal(50.0, 0.0, 10000.0, 10.0);
        panelForm.add(spCostoBase);

        // --- PARÁMETROS FÍSICOS ÓPTICOS (Límites GPON/FTTH) ---
        panelForm.add(crearLabel("Atenuación (dB/km):"));
        // Rango: 0.1 a 2.0 (La fibra G.652 estándar pierde ~0.35 dB/km)
        spAtenuacionKm = crearSpinnerOscuroDecimal(0.35, 0.1, 2.0, 0.05); 
        panelForm.add(spAtenuacionKm);

        panelForm.add(crearLabel("Pérdida Empalme (dB):"));
        // Rango: 0.0 a 1.0 (Típico 0.1 dB por fusión. Más de 1.0 es una mala fusión)
        spPerdidaEmpalme = crearSpinnerOscuroDecimal(0.1, 0.0, 1.0, 0.01);
        panelForm.add(spPerdidaEmpalme);
        
        panelForm.add(crearLabel("Pérdida Splitter N1 (dB):"));
        // Rango: 1.0 a 25.0 (Típico 10.5 dB para un divisor 1:8)
        spPerdidaSplitter = crearSpinnerOscuroDecimal(10.5, 1.0, 25.0, 0.5);
        panelForm.add(spPerdidaSplitter);

        panelForm.add(crearLabel("Pérdida Splitter NAP (dB):"));
        // Rango: 1.0 a 25.0 (Típico 14.0 dB para divisor 1:16 en la caja terminal)
        spPerdidaNAP = crearSpinnerOscuroDecimal(14.0, 1.0, 25.0, 0.5);
        panelForm.add(spPerdidaNAP);
        
        panelForm.add(crearLabel("Pérdida Conector (dB):"));
        // Rango: 0.0 a 2.0 (Típico 0.5 dB por conector mecánico)
        spPerdidaConector = crearSpinnerOscuroDecimal(0.5, 0.0, 2.0, 0.1);
        panelForm.add(spPerdidaConector);

        panelForm.add(crearLabel("Pérdida Máxima Red (dB):"));
        // Rango: 15.0 a 40.0 (GPON Class B+ soporta hasta 28dB, Class C+ hasta 32dB)
        spUmbral = crearSpinnerOscuroDecimal(28.0, 15.0, 40.0, 0.5);
        panelForm.add(spUmbral);
        
        // --- PARÁMETROS DE SOFTWARE ---
        panelForm.add(crearLabel("Velocidad Prim (ms):"));
        // Mínimo 10ms (muy rápido), Máximo 5000ms (5 segundos por salto visual)
        spVelocidadAnimacion = crearSpinnerOscuroInt(300, 10, 5000, 50); 
        panelForm.add(spVelocidadAnimacion);

        add(panelForm, BorderLayout.CENTER);

        // --- BOTONES INFERIORES ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);

        JButton btnCancelar = FabricaInterfaz.crearBotonAccion("Cancelar", PaletaTema.NEON_AZUL);
        JButton btnGuardar = FabricaInterfaz.crearBotonAccion("Aplicar Cambios", PaletaTema.NEON_AZUL);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarCambios());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        
        add(panelBotones, BorderLayout.SOUTH);
    }

    /**
     * Método auxiliar para crear etiquetas con el estilo de la paleta.
     */
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(PaletaTema.NEON_AZUL);
        lbl.setFont(PaletaTema.FUENTE_REGULAR);
        return lbl;
    }

    // =======================================================
    // LÓGICA DE NEGOCIO (CARGA Y GUARDADO)
    // =======================================================

    /**
     * Lee la instancia actual de la configuración global y puebla
     * los spinners de la interfaz con los valores vigentes.
     */
    private void cargarValoresActuales() {
        ConfiguracionGlobal config = ConfiguracionGlobal.getInstance();
        
        spCostoMetro.setValue(config.getCostoMetroFibra());
        spCostoBase.setValue(config.getCostoInstalacionBase());
        spAtenuacionKm.setValue(config.getAtenuacionPorKm());
        spPerdidaEmpalme.setValue(config.getPerdidaPorEmpalme());
        spPerdidaConector.setValue(config.getPerdidaPorConector());
        spUmbral.setValue(config.getUmbralSensibilidad());
        spPerdidaSplitter.setValue(config.getPerdidaSplitterN1());
        spPerdidaNAP.setValue(config.getPerdidaCajaNAP());
        spVelocidadAnimacion.setValue(config.getVelocidadAnimacionMs());
    }

    /**
     * Extrae los valores modificados por el usuario, los inyecta en el Singleton
     * y ejecuta el guardado en disco. Contiene validación de errores de parsing.
     */
    private void guardarCambios() {
        try {
            // commitEdit() asegura que si el usuario tipeó un número directamente en la caja
            // sin presionar 'Enter' o cambiar de foco, el componente registre el valor final.
            spCostoMetro.commitEdit();
            spCostoBase.commitEdit();
            spAtenuacionKm.commitEdit();
            spPerdidaEmpalme.commitEdit();
            spPerdidaConector.commitEdit();
            spUmbral.commitEdit();
            spPerdidaSplitter.commitEdit();
            spPerdidaNAP.commitEdit();
            spVelocidadAnimacion.commitEdit();

            // Obtenemos el objeto base e inyectamos la nueva data
            ConfiguracionGlobal config = ConfiguracionGlobal.getInstance();
            
            config.setCostoMetroFibra(((Number) spCostoMetro.getValue()).doubleValue());
            config.setCostoInstalacionBase(((Number) spCostoBase.getValue()).doubleValue());
            config.setAtenuacionPorKm(((Number) spAtenuacionKm.getValue()).doubleValue());
            config.setPerdidaPorEmpalme(((Number) spPerdidaEmpalme.getValue()).doubleValue());
            config.setPerdidaPorConector(((Number) spPerdidaConector.getValue()).doubleValue());
            config.setUmbralSensibilidad(((Number) spUmbral.getValue()).doubleValue());
            config.setPerdidaSplitterN1(((Number) spPerdidaSplitter.getValue()).doubleValue());
            config.setPerdidaCajaNAP(((Number) spPerdidaNAP.getValue()).doubleValue());
            config.setVelocidadAnimacionMs(((Number) spVelocidadAnimacion.getValue()).intValue());
            
            // Persistencia binaria
            config.guardarEnDisco();
            guardado = true;
            dispose();
            
        } catch (Exception e) {
            Util.Tecnicas.GestorAlertas.mostrarError(this, "Error de Validación", "Revise que los valores introducidos sean correctos.");
        }
    }
}