package Controladores;

import Modelo.dominio.TopologiaRed;
import Vista.VentanaPrincipal;
import Util.UI.PaletaTema;
import Vista.PanelDiseno;
import java.awt.FileDialog;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;

public class ControladorPrincipal {
    private final TopologiaRed modelo;
    private final VentanaPrincipal vista;
    // Sub-controladores
    
     private String archivoActual = null;
     private boolean hayCambiosSinGuardar = false;
    public ControladorPrincipal() {
        // 1. Configuración Visual (Delegada a Util)
        PaletaTema.configurarAspectoGlobal();

        // 2. Instanciación MVC
        this.modelo = new TopologiaRed();
        this.vista = new VentanaPrincipal();
        this.modelo.addTopologiaListener(() -> {
            hayCambiosSinGuardar = true;
        });

        // 3. Sub-controladores
        new ControladorOptimizacion(modelo, vista);
        new ControladorDiseno(modelo, vista, (PanelDiseno) vista.getPanelMapa());

        // 4. Inicialización
        initListenersArchivos();
        initController();
    }

    private void initController() {
        actualizarTablaProyectos();
        vista.setMensajeEstado("Motor de enrutamiento MST inicializado con éxito.", PaletaTema.NEON_AZUL);
        vista.setVisible(true);
        System.out.println("Sistema FiberDesign Pro iniciado correctamente.");
        hayCambiosSinGuardar = false;
    }

    // =======================================================
    // VINCULACIÓN DE EVENTOS (LIMPIO Y DECLARATIVO)
    // =======================================================
    private void initListenersArchivos() {
        vista.getBtnGuardarProyecto().addActionListener(e -> manejarGuardado());
        vista.getBtnCargarProyecto().addActionListener(e -> manejarCarga());
        vista.getBtnConfiguracion().addActionListener(e -> manejarConfiguracion());
        vista.getBtnNuevoProyecto().addActionListener(e -> manejarNuevoProyecto());
       
        
        vista.getPanelLateral().getTablaProyectos().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) manejarSeleccionTabla();
        });
        vista.getBtnLimpiarMapa().addActionListener(e -> {
    archivoActual = null;
    vista.actualizarTitulo(null); // Devuelve el título a la normalidad
});
    }

    // =======================================================
    // LÓGICA DE NEGOCIO (MÉTODOS PRIVADOS SEPARADOS)
    // =======================================================
    private void manejarNuevoProyecto() {
        // 1. Protección Anti-Desastres: Preguntamos si hay equipos en el mapa
        if (!modelo.getNodos().isEmpty() && hayCambiosSinGuardar) {
            // ¡ADIÓS JOPTIONPANE!
            boolean confirmado = Util.UI.GestorAlertas.pedirConfirmacion(vista, 
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
        if (vista.getPanelMapa() != null) vista.getPanelMapa().repaint();
        
        vista.setMensajeEstado("Nuevo proyecto en blanco iniciado.", Util.UI.PaletaTema.NEON_AZUL);
    }
   private void manejarGuardado() {
        if (archivoActual != null) {
            // 1. ACTUALIZACIÓN SILENCIOSA
            if (Util.GestorArchivos.guardarProyecto(modelo, archivoActual)) {
                hayCambiosSinGuardar = false;
                vista.setMensajeEstado("Proyecto actualizado: " + archivoActual, PaletaTema.NEON_VERDE);
                vista.actualizarTitulo(archivoActual); // Actualiza el título
                
                // Mostramos un popup de éxito para que el usuario esté 100% seguro
                Util.UI.GestorAlertas.mostrarExito(vista, "Actualización Exitosa", "Se han guardado los cambios en '" + archivoActual + "'.");
                actualizarTablaProyectos(); 
            } else {
                Util.UI.GestorAlertas.mostrarError(vista, "Error", "No se pudo actualizar el archivo.");
            }
        } else {
            // 2. GUARDAR COMO NUEVO PROYECTO
           String nombre = Util.UI.GestorAlertas.pedirEntradaTexto(vista, 
                    "Guardar Nuevo Proyecto", 
                    "Ingrese el nombre del proyecto (sin extensión):");
            
            if (nombre != null && !nombre.trim().isEmpty()) {
                if (Util.GestorArchivos.guardarProyecto(modelo, nombre)) {
                    archivoActual = nombre.endsWith(".fiber") ? nombre : nombre + ".fiber";
                    hayCambiosSinGuardar = false;
                    vista.setMensajeEstado("Proyecto activo: " + archivoActual, PaletaTema.NEON_AZUL);
                    vista.actualizarTitulo(archivoActual); // Actualiza el título
                    
                    Util.UI.GestorAlertas.mostrarExito(vista, "Guardado Exitoso", "Proyecto '" + archivoActual + "' guardado correctamente.");
                    actualizarTablaProyectos(); 
                } else {
                    Util.UI.GestorAlertas.mostrarError(vista, "Error", "No se pudo guardar el archivo.");
                }
            }
        }
    }

    private void manejarCarga() {
      if (hayCambiosSinGuardar && !modelo.getNodos().isEmpty()) {
            // ¡ADIÓS JOPTIONPANE!
            boolean confirmado = Util.UI.GestorAlertas.pedirConfirmacion(vista, 
                "Cargar Proyecto", 
                "Hay cambios sin guardar en el mapa actual.\n¿Desea descartarlos y cargar el proyecto seleccionado?");
                
            if (!confirmado) {
                if(vista.getPanelLateral().getTablaProyectos().getSelectedRow() != -1){
                     vista.getPanelLateral().getTablaProyectos().clearSelection();
                }
                return;
            }
        }
       FileDialog fd = new java.awt.FileDialog(vista, "Cargar Proyecto FiberDesign", java.awt.FileDialog.LOAD);
        fd.setDirectory(new File("proyectos").getAbsolutePath());
        fd.setFile("*.fiber");
        fd.setVisible(true); // Esto pausa el código y abre la ventana nativa
        
        String nombreArchivo = fd.getFile(); // Retorna solo el nombre, ej: "nueva.fiber"
        
        if (nombreArchivo != null) {
            TopologiaRed redCargada = Util.GestorArchivos.cargarProyecto(nombreArchivo);
            
            if (redCargada != null) {
                modelo.cargarEstado(redCargada);
                archivoActual = nombreArchivo; // ¡Memorizamos el archivo cargado!
                  vista.actualizarTitulo(archivoActual);
                  hayCambiosSinGuardar = false;
                if (vista.getPanelMapa() != null) vista.getPanelMapa().repaint();
                vista.setMensajeEstado("Proyecto '" + archivoActual + "' cargado y activo.", PaletaTema.NEON_VERDE);
              
            } else {
                Util.UI.GestorAlertas.mostrarError(vista, "Error", "Archivo corrupto o ilegible.");
            }
        }
    }

    private void manejarSeleccionTabla() {
        if (hayCambiosSinGuardar && !modelo.getNodos().isEmpty()) {
            // ¡USAMOS TU NUEVO GESTOR DE ALERTAS CYBERPUNK!
            boolean confirmado = Util.UI.GestorAlertas.pedirConfirmacion(vista, 
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
            TopologiaRed redCargada = Util.GestorArchivos.cargarProyecto(nombreArchivo);
            
            if (redCargada != null) {
                modelo.cargarEstado(redCargada);
                archivoActual = nombreArchivo; 
                vista.actualizarTitulo(archivoActual);
                hayCambiosSinGuardar = false;
                if (vista.getPanelMapa() != null) vista.getPanelMapa().repaint();
                vista.setMensajeEstado("Proyecto '" + archivoActual + "' cargado y activo.", PaletaTema.NEON_VERDE);
            }
        }
    }

  

    private void actualizarTablaProyectos() {
        javax.swing.table.DefaultTableModel modeloTabla = vista.getPanelLateral().getModeloTablaProyectos();
        modeloTabla.setRowCount(0); 
        
        for (String proy : Util.GestorArchivos.listarProyectosGuardados()) {
            modeloTabla.addRow(new Object[]{proy});
        }
    }
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
            
            Util.UI.GestorAlertas.mostrarExito(vista, "Configuración Actualizada", 
                    "Se han recalculado todos los costos y pérdidas de la red.");
        }
    }

    // =======================================================
    // PUNTO DE ENTRADA (Opcional: Mover a una clase App.java)
    // =======================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ControladorPrincipal());
    }
}