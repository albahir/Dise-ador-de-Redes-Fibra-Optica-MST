package Controladores;

import Modelo.dominio.TopologiaRed;
import Vista.VentanaPrincipal;
import Util.UI.PaletaTema;
import Vista.PanelDiseno;
import java.awt.FileDialog;

import javax.swing.SwingUtilities;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Controlador principal de la aplicación.
 * Actúa como el orquestador central del patrón MVC, gestionando el ciclo de vida 
 * de la aplicación, los archivos (guardar/cargar proyectos), atajos de teclado 
 * y la instanciación de los sub-controladores.
 */
public class ControladorPrincipal {
    
    // Referencias principales del patrón MVC
    private final TopologiaRed modelo;
    private final VentanaPrincipal vista;
    
    // Variables de estado del proyecto actual
    private String archivoActual = null;
    private boolean hayCambiosSinGuardar = false;

    /**
     * Constructor del controlador principal.
     * Configura el aspecto visual, inicializa el modelo y la vista, y arranca 
     * los sub-controladores necesarios.
     */
    public ControladorPrincipal() {
        // 1. Configuración Visual (Delegada a Util)
        PaletaTema.configurarAspectoGlobal();

        // 2. Instanciación MVC
        this.modelo = new TopologiaRed();
        this.vista = new VentanaPrincipal();
        
        // Listener para detectar si el usuario ha realizado modificaciones en el mapa
        this.modelo.addTopologiaListener(() -> {
            hayCambiosSinGuardar = true;
        });

        // 3. Sub-controladores encargados de dominios específicos
        new ControladorOptimizacion(modelo, vista);
        new ControladorDiseno(modelo, vista, (PanelDiseno) vista.getPanelMapa());

        // 4. Inicialización de eventos y configuraciones
        initListenersArchivos();
        initController();
    }

    // =======================================================
    // PUNTO DE ENTRADA DE LA APLICACIÓN
    // =======================================================
    
    /**
     * Método principal (Main) que arranca la aplicación de forma segura 
     * dentro del hilo de eventos de Swing (Event Dispatch Thread).
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ControladorPrincipal());
    }

    // =======================================================
    // MÉTODOS PRIVADOS DE INICIALIZACIÓN
    // =======================================================

    /**
     * Termina de configurar el controlador, carga los datos iniciales en la vista
     * y hace visible la ventana principal.
     */
    private void initController() {
        actualizarTablaProyectos();
        vista.setMensajeEstado("Motor de enrutamiento MST inicializado con éxito.", PaletaTema.NEON_AZUL);
        vista.setVisible(true);
        initAtajosTeclado();
        
        System.out.println("Sistema FiberDesign Pro iniciado correctamente.");
        hayCambiosSinGuardar = false;
    }

    /**
     * Vincula los eventos de los botones de la interfaz relacionados con 
     * la gestión de archivos y configuración general.
     */
    private void initListenersArchivos() {
        vista.getBtnGuardarProyecto().addActionListener(e -> manejarGuardado());
        vista.getBtnCargarProyecto().addActionListener(e -> manejarCarga());
        vista.getBtnConfiguracion().addActionListener(e -> manejarConfiguracion());
        vista.getBtnNuevoProyecto().addActionListener(e -> manejarNuevoProyecto());
       
        // Evento al seleccionar un proyecto de la tabla lateral
        vista.getPanelLateral().getTablaProyectos().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                manejarSeleccionTabla();
            }
        });
        
        // Evento para limpiar el mapa activo y desvincular el archivo
        vista.getBtnLimpiarMapa().addActionListener(e -> {
            archivoActual = null;
            vista.actualizarTitulo(null); // Devuelve el título a la normalidad
        });
    }

    /**
     * Configura los atajos de teclado globales de la ventana principal.
     */
    private void initAtajosTeclado() {
        // Obtenemos el mapa de entradas de la ventana principal
        JPanel panelRaiz = (JPanel) vista.getContentPane();
        InputMap inputMap = panelRaiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panelRaiz.getActionMap();

        // 1. Atajo: Ctrl + S (Guardar)
        inputMap.put(KeyStroke.getKeyStroke("control S"), "guardar");
        actionMap.put("guardar", new AbstractAction() {
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                manejarGuardado(); 
            }
        });
        
        // Atajo: Ctrl + N (Nuevo Proyecto)
        inputMap.put(KeyStroke.getKeyStroke("control N"), "nuevo");
        actionMap.put("nuevo", new AbstractAction() {
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                manejarNuevoProyecto(); 
            }
        });
        
        // 2. Atajo: F5 (Ejecutar Prim)
        inputMap.put(KeyStroke.getKeyStroke("F5"), "ejecutarPrim");
        actionMap.put("ejecutarPrim", new AbstractAction() {
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                if (vista.getBtnEjecutarPrim().isEnabled()) {
                    vista.getBtnEjecutarPrim().doClick(); // Simula el clic en el botón
                }
            }
        });
    }

    // =======================================================
    // LÓGICA DE NEGOCIO Y MANEJO DE EVENTOS
    // =======================================================

    /**
     * Maneja la creación de un nuevo proyecto en blanco.
     * Incluye validaciones para no perder trabajo no guardado.
     */
    private void manejarNuevoProyecto() {
        // 1. Protección Anti-Desastres: Preguntamos si hay equipos en el mapa con cambios sin guardar
        if (!modelo.getNodos().isEmpty() && hayCambiosSinGuardar) {
            boolean confirmado = Util.Tecnicas.GestorAlertas.pedirConfirmacion(vista, 
                "Nuevo Proyecto", 
                "¿Estás seguro de crear un nuevo proyecto?\nHay cambios sin guardar que se perderán.");
                
            if (!confirmado) {
                return; 
            }
        }

        // 2. Destrucción del mapa actual
        modelo.getNodos().clear();
        modelo.getEnlaces().clear();
        
        // 3. Amnesia: Olvidamos el archivo con el que estábamos trabajando
        archivoActual = null;
        vista.actualizarTitulo(null);
        
        // 4. Notificamos a la interfaz para que se repinte en blanco
        modelo.notificarCambios();
        hayCambiosSinGuardar = false;
        
        if (vista.getPanelMapa() != null) {
            vista.getPanelMapa().repaint();
        }
        
        vista.setMensajeEstado("Nuevo proyecto en blanco iniciado.", Util.UI.PaletaTema.NEON_AZUL);
    }
   
    /**
     * Maneja la acción de guardar el proyecto actual.
     * Si ya existe un archivo activo, lo sobrescribe silenciosamente.
     * Si no existe, pide al usuario un nombre para crear uno nuevo.
     */
    private void manejarGuardado() {
        if (archivoActual != null) {
            // 1. ACTUALIZACIÓN SILENCIOSA (El proyecto ya tiene un archivo asociado)
            if (Util.Tecnicas.GestorArchivos.guardarProyecto(modelo, archivoActual)) {
                hayCambiosSinGuardar = false;
                vista.setMensajeEstado("Proyecto actualizado: " + archivoActual, PaletaTema.NEON_VERDE);
                vista.actualizarTitulo(archivoActual); // Actualiza el título
                
                // Mostramos un popup de éxito para que el usuario esté 100% seguro
                Util.Tecnicas.GestorAlertas.mostrarExito(vista, "Actualización Exitosa", "Se han guardado los cambios en '" + archivoActual + "'.");
                actualizarTablaProyectos(); 
            } else {
                Util.Tecnicas.GestorAlertas.mostrarError(vista, "Error", "No se pudo actualizar el archivo.");
            }
        } else {
            // 2. GUARDAR COMO NUEVO PROYECTO (No hay archivo asociado aún)
            String nombre = Util.Tecnicas.GestorAlertas.pedirEntradaTexto(vista, 
                    "Guardar Nuevo Proyecto", 
                    "Ingrese el nombre del proyecto (sin extensión):");
            
            if (nombre != null && !nombre.trim().isEmpty()) {
                if (Util.Tecnicas.GestorArchivos.guardarProyecto(modelo, nombre)) {
                    // Asegura que tenga la extensión correcta
                    archivoActual = nombre.endsWith(".fiber") ? nombre : nombre + ".fiber";
                    hayCambiosSinGuardar = false;
                    vista.setMensajeEstado("Proyecto activo: " + archivoActual, PaletaTema.NEON_AZUL);
                    vista.actualizarTitulo(archivoActual); // Actualiza el título
                    
                    Util.Tecnicas.GestorAlertas.mostrarExito(vista, "Guardado Exitoso", "Proyecto '" + archivoActual + "' guardado correctamente.");
                    actualizarTablaProyectos(); 
                } else {
                    Util.Tecnicas.GestorAlertas.mostrarError(vista, "Error", "No se pudo guardar el archivo.");
                }
            }
        }
    }

    /**
     * Abre un cuadro de diálogo del sistema para cargar un archivo `.fiber` externo.
     */
    private void manejarCarga() {
        // Protección Anti-Desastres al cargar externamente
        if (hayCambiosSinGuardar && !modelo.getNodos().isEmpty()) {
            boolean confirmado = Util.Tecnicas.GestorAlertas.pedirConfirmacion(vista, 
                "Cargar Proyecto", 
                "Hay cambios sin guardar en el mapa actual.\n¿Desea descartarlos y cargar el proyecto seleccionado?");
                
            if (!confirmado) {
                // Limpia la selección en la tabla si el usuario cancela
                if (vista.getPanelLateral().getTablaProyectos().getSelectedRow() != -1) {
                     vista.getPanelLateral().getTablaProyectos().clearSelection();
                }
                return;
            }
        }
        
        // Uso de FileDialog nativo del sistema operativo
        FileDialog fd = new java.awt.FileDialog(vista, "Cargar Proyecto FiberDesign", java.awt.FileDialog.LOAD);
        fd.setDirectory(new File("proyectos").getAbsolutePath());
        fd.setFile("*.fiber");
        fd.setVisible(true); // Esto pausa el código y abre la ventana nativa
        
        String nombreArchivo = fd.getFile(); // Retorna solo el nombre, ej: "nueva.fiber"
        
        if (nombreArchivo != null) {
            TopologiaRed redCargada = Util.Tecnicas.GestorArchivos.cargarProyecto(nombreArchivo);
            
            if (redCargada != null) {
                modelo.cargarEstado(redCargada);
                archivoActual = nombreArchivo; // ¡Memorizamos el archivo cargado!
                vista.actualizarTitulo(archivoActual);
                hayCambiosSinGuardar = false;
                
                if (vista.getPanelMapa() != null) {
                    vista.getPanelMapa().repaint();
                }
                vista.setMensajeEstado("Proyecto '" + archivoActual + "' cargado y activo.", PaletaTema.NEON_VERDE);
              
            } else {
                Util.Tecnicas.GestorAlertas.mostrarError(vista, "Error", "Archivo corrupto o ilegible.");
            }
        }
    }

    /**
     * Carga un proyecto directamente cuando el usuario hace clic sobre un elemento 
     * en la tabla lateral de proyectos recientes/guardados.
     */
    private void manejarSeleccionTabla() {
        if (hayCambiosSinGuardar && !modelo.getNodos().isEmpty()) {
            boolean confirmado = Util.Tecnicas.GestorAlertas.pedirConfirmacion(vista, 
                "Cargar Proyecto", 
                "Hay cambios sin guardar.\n¿Descartar y cargar el proyecto seleccionado?");
            
            if (!confirmado) {
                // Si el usuario dice "Cancelar", quitamos la selección de la tabla para no confundirlo
                if (vista.getPanelLateral().getTablaProyectos().getSelectedRow() != -1) {
                    vista.getPanelLateral().getTablaProyectos().clearSelection();
                }
                return; // Cortamos la ejecución, nos quedamos en el proyecto actual
            }
        }
        
        int fila = vista.getPanelLateral().getTablaProyectos().getSelectedRow();
        if (fila != -1) {
            String nombreArchivo = (String) vista.getPanelLateral().getModeloTablaProyectos().getValueAt(fila, 0);
            TopologiaRed redCargada = Util.Tecnicas.GestorArchivos.cargarProyecto(nombreArchivo);
            
            if (redCargada != null) {
                modelo.cargarEstado(redCargada);
                archivoActual = nombreArchivo; 
                vista.actualizarTitulo(archivoActual);
                hayCambiosSinGuardar = false;
                
                if (vista.getPanelMapa() != null) {
                    vista.getPanelMapa().repaint();
                }
                vista.setMensajeEstado("Proyecto '" + archivoActual + "' cargado y activo.", PaletaTema.NEON_VERDE);
            }
        }
    }

    /**
     * Abre el cuadro de diálogo de configuraciones globales.
     * Si se realizan cambios, se encarga de recalcular físicamente toda la red.
     */
    private void manejarConfiguracion() {
        Vista.DialogoConfiguracion dialogo = new Vista.DialogoConfiguracion(vista);
        dialogo.setVisible(true);

        if (dialogo.isGuardado()) {
            // 1. Recalcular costos y física de TODOS los enlaces existentes
            for (Modelo.dominio.Enlace enlace : modelo.getEnlaces()) {
                enlace.actualizarCostoCalculado();
                enlace.calcularFisicaDelCable();
            }
            
            // 2. Notificar a la vista para que actualice la Telemetría y Costos
            modelo.notificarCambios();
            vista.getPanelMapa().repaint();
            
            Util.Tecnicas.GestorAlertas.mostrarExito(vista, "Configuración Actualizada", 
                    "Se han recalculado todos los costos y pérdidas de la red.");
        }
    }

    /**
     * Actualiza la tabla lateral leyendo el directorio de proyectos guardados.
     */
    private void actualizarTablaProyectos() {
        javax.swing.table.DefaultTableModel modeloTabla = vista.getPanelLateral().getModeloTablaProyectos();
        modeloTabla.setRowCount(0); 
        
        for (String proy : Util.Tecnicas.GestorArchivos.listarProyectosGuardados()) {
            modeloTabla.addRow(new Object[]{proy});
        }
    }
}