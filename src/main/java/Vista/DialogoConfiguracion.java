package Vista;

import Modelo.dominio.ConfiguracionGlobal;
import Util.UI.FabricaInterfaz;
import static Util.UI.FabricaInterfaz.crearSpinnerOscuroDecimal;
import static Util.UI.FabricaInterfaz.crearSpinnerOscuroInt;
import Util.UI.PaletaTema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogoConfiguracion extends JDialog {

    // Cambiamos los JTextField por JSpinner
    private JSpinner spCostoMetro;
    private JSpinner spCostoBase;
    private JSpinner spAtenuacionKm;
    private JSpinner spPerdidaEmpalme;
    private JSpinner spPerdidaConector;
    private JSpinner spUmbral;
    private JSpinner spPerdidaSplitter;
    private JSpinner spPerdidaNAP;
    private JSpinner spVelocidadAnimacion;
    
    private boolean guardado = false;

    public DialogoConfiguracion(JFrame parent) {
        super(parent, "Configuración Global del Proyecto", true);
        setUndecorated(true); 
        configurarVentana();
        inicializarComponentes();
        cargarValoresActuales();
        pack();
        setLocationRelativeTo(parent);
    }

    private void configurarVentana() {
        JPanel panelFondo = new JPanel(new BorderLayout());
        panelFondo.setBackground(PaletaTema.FONDO_PRINCIPAL);
        panelFondo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaTema.NEON_AZUL, 2),
                new EmptyBorder(20, 25, 20, 25)
        ));
        setContentPane(panelFondo);
    }

    private void inicializarComponentes() {
        JLabel lblTitulo = FabricaInterfaz.crearTituloSeccion("PARÁMETROS DEL SISTEMA");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitulo, BorderLayout.NORTH);

        // FORMULARIO CENTRAL: Corregido a 8 filas x 2 columnas para que quepan todos
        JPanel panelForm = new JPanel(new GridLayout(9, 2, 10, 15));
        panelForm.setOpaque(false);
        panelForm.setBorder(new EmptyBorder(20, 0, 20, 0));

        // ==========================================
        // PARÁMETROS ECONÓMICOS
        // ==========================================
        panelForm.add(crearLabel("Costo Fibra ($/m):"));
        // (Valor inicial, Min, Max, Salto)
        spCostoMetro = crearSpinnerOscuroDecimal(1.50, 0.01, 500.0, 0.10); 
        panelForm.add(spCostoMetro);

        panelForm.add(crearLabel("Costo Base Nodo ($):"));
        spCostoBase = crearSpinnerOscuroDecimal(50.0, 0.0, 10000.0, 10.0);
        panelForm.add(spCostoBase);

        // ==========================================
        // PARÁMETROS FÍSICOS (Límites GPON/FTTH reales)
        // ==========================================
        panelForm.add(crearLabel("Atenuación (dB/km):"));
        // Rango: 0.1 a 2.0 (G.652 es ~0.35)
        spAtenuacionKm = crearSpinnerOscuroDecimal(0.35, 0.1, 2.0, 0.05); 
        panelForm.add(spAtenuacionKm);

        panelForm.add(crearLabel("Pérdida Empalme (dB):"));
        // Rango: 0.0 a 1.0 (Típico 0.1. Más de 1.0 es fusión defectuosa)
        spPerdidaEmpalme = crearSpinnerOscuroDecimal(0.1, 0.0, 1.0, 0.01);
        panelForm.add(spPerdidaEmpalme);
        
        panelForm.add(crearLabel("Pérdida Splitter N1 (dB):"));
        // Rango: 1.0 a 25.0 (Típico 10.5 para 1:8)
        spPerdidaSplitter = crearSpinnerOscuroDecimal(10.5, 1.0, 25.0, 0.5);
        panelForm.add(spPerdidaSplitter);

        panelForm.add(crearLabel("Pérdida Splitter NAP (dB):"));
        // Rango: 1.0 a 25.0 (Típico 14.0 para 1:16)
        spPerdidaNAP = crearSpinnerOscuroDecimal(14.0, 1.0, 25.0, 0.5);
        panelForm.add(spPerdidaNAP);
        
        panelForm.add(crearLabel("Pérdida Conector (dB):"));
        // Rango: 0.0 a 2.0 (Típico 0.5)
        spPerdidaConector = crearSpinnerOscuroDecimal(0.5, 0.0, 2.0, 0.1);
        panelForm.add(spPerdidaConector);

        panelForm.add(crearLabel("Pérdida Máxima Red (dB):"));
        // Rango: 15.0 a 40.0 (GPON Class B+ es 28dB, Class C+ es 32dB)
        spUmbral = crearSpinnerOscuroDecimal(28.0, 15.0, 40.0, 0.5);
        panelForm.add(spUmbral);
        panelForm.add(crearLabel("Velocidad Prim (milisegundos):"));
        // Mínimo 10ms (super rápido), Máximo 5000ms (5 seg por cable)
        spVelocidadAnimacion = crearSpinnerOscuroInt(300, 10, 5000, 50); 
        panelForm.add(spVelocidadAnimacion);

        add(panelForm, BorderLayout.CENTER);

        // BOTONES
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

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(PaletaTema.NEON_AZUL);
        lbl.setFont(PaletaTema.FUENTE_REGULAR);
        return lbl;
    }

 

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

    private void guardarCambios() {
        try {
            // Commit asegura que si el usuario tipeó en la caja y no dio Enter, tome el valor
            spCostoMetro.commitEdit();
            spCostoBase.commitEdit();
            spAtenuacionKm.commitEdit();
            spPerdidaEmpalme.commitEdit();
            spPerdidaConector.commitEdit();
            spUmbral.commitEdit();
            spPerdidaSplitter.commitEdit();
            spPerdidaNAP.commitEdit();
            spVelocidadAnimacion.commitEdit();

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
            
            config.guardarEnDisco();
            guardado = true;
            dispose();
        } catch (Exception e) {
            Util.UI.GestorAlertas.mostrarError(this, "Error de Validación", "Revise que los valores introducidos sean correctos.");
        }
    }
    
    public boolean isGuardado() { return guardado; }
}