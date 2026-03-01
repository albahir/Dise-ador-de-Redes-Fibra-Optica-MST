
package Controladores;

import Modelo.dominio.Enlace;
import Modelo.dominio.Nodo;
import Modelo.dominio.TopologiaRed;
import Vista.DialogoNodoDinamico;
import Vista.PanelDiseno;
import Vista.VentanaPrincipal;
import Util.UI.PaletaTema;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

public class ControladorDiseno {
    private final TopologiaRed modelo;
    private final VentanaPrincipal vista;
    private final PanelDiseno panelDiseño;
    private Nodo nodoArrastrado = null;

    // Máquina de estados para saber qué herramienta está activa
    private enum ModoHerramienta { NINGUNO, AGREGAR, ELIMINAR }
    private ModoHerramienta modoActual = ModoHerramienta.NINGUNO;
    private Modelo.dominio.TipoNodo tipoConstruccionActivo = null;

    public ControladorDiseno(TopologiaRed modelo, VentanaPrincipal vista, PanelDiseno panelDiseño) {
        this.modelo = modelo;
        this.vista = vista;
        this.panelDiseño = panelDiseño;
        this.panelDiseño.setModelo(modelo); // Vinculamos la vista con los datos

        initListeners();
    }

    private void initListeners() {
     
        vista.getPanelLateral().getBtnEliminarNodo().addActionListener(e -> setModo(ModoHerramienta.ELIMINAR));

        // 2. Escuchar TODOS los botones de la paleta de Hardware
        vista.getPanelLateral().getBotonesHerramientas().forEach((tipo, boton) -> {
            boton.addActionListener(e -> activarHerramientaConstruccion(tipo));
        });
        vista.getPanelLateral().getBtnEliminarNodo().addActionListener(e -> setModo(ModoHerramienta.ELIMINAR));

        // 2. Escuchar los clics del mouse sobre el lienzo negro
        panelDiseño.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                if (javax.swing.SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    Nodo nodoClicado = modelo.buscarNodoEn(x, y, 20);
                    if (nodoClicado != null) {
                        ejecutarEdicionDeNodo(nodoClicado); // Dispara la edición
                        return; // ¡CRÍTICO! Corta la ejecución para no afectar las herramientas activas
                    }
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    // 1. Primero intentamos tocar un Nodo (Prioridad alta)
                    Nodo nodoTocado = modelo.buscarNodoEn(x, y, 20); 
                    if (nodoTocado != null) {
                        Vista.DialogoInfoNodo info = new Vista.DialogoInfoNodo(vista, nodoTocado, modelo);
                        info.setVisible(true);
                        return; 
                    }
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    manejarClicEnMapa(x, y); 
                }
                    
                    // 2. Si no tocamos un nodo, intentamos tocar un Enlace (Cable verde)
                    // Usamos 7 píxeles de tolerancia (suficiente para no fallar el clic)
                    Enlace enlaceTocado = modelo.buscarEnlaceEn(x, y, 7);
                    if (enlaceTocado != null) {
                        Vista.DialogoInfoEnlace infoEnlace = new Vista.DialogoInfoEnlace(vista, enlaceTocado);
                        infoEnlace.setVisible(true);
                        return;
                    }
                    
                    return; // Si no tocó ni nodo ni enlace, no hace nada
                }

                // --- CLIC IZQUIERDO NORMAL ---
                if (SwingUtilities.isLeftMouseButton(e)) {
                    manejarClicEnMapa(x, y); // Llama a tu máquina de estados
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    // Tolerancia de 20px para "agarrar" el nodo
                    nodoArrastrado = modelo.buscarNodoEn(e.getX(), e.getY(), 20);
                }
            }

            // --- NUEVO: SOLTAR EL NODO ---
            @Override
            public void mouseReleased(MouseEvent e) {
                if (nodoArrastrado != null) {
                    // Al soltarlo, actualizamos los metros de cable y el presupuesto óptico/financiero
                    modelo.actualizarFisicaDeCables(nodoArrastrado);
                    nodoArrastrado = null;
                }
            }
        });
        panelDiseño.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (nodoArrastrado != null) {
                    // Evitamos que el usuario arrastre el equipo fuera del área visible
                    int x = Math.max(20, Math.min(e.getX(), panelDiseño.getWidth() - 20));
                    int y = Math.max(20, Math.min(e.getY(), panelDiseño.getHeight() - 20));

                    nodoArrastrado.setX(x);
                    nodoArrastrado.setY(y);

                    // Animamos a 60FPS sin activar los cálculos matemáticos pesados
                    panelDiseño.repaint(); 
                }
            }
        });
    }

    private void setModo(ModoHerramienta nuevoModo) {
        this.modoActual = nuevoModo;
        // Feedback visual en la barra de estado
        vista.setMensajeEstado("Modo Activo: " + nuevoModo.name(), PaletaTema.NEON_AZUL);
        panelDiseño.setNodoSeleccionado(null); // Limpiar selecciones previas
    }

    private void manejarClicEnMapa(int x, int y) {
        switch (modoActual) {
            case AGREGAR -> {
                int margen = 20; // 20 píxeles de margen contra las paredes
                if (x < margen || y < margen || x > panelDiseño.getWidth() - margen || y > panelDiseño.getHeight() - margen) {
                    vista.setMensajeEstado("Fuera de límites: El equipo debe instalarse dentro del área visible del mapa.", PaletaTema.NEON_ROJO);
                    Toolkit.getDefaultToolkit().beep();
                    return; // Bloquea el clic
                }
                // =======================================================
                // REGLA DE NEGOCIO 1.1: RADIO DE EXCLUSIÓN
                // =======================================================
                int radioExclusion = 50; // 30 píxeles de distancia mínima permitida
                
                // Usamos el "radar" del modelo para ver si hay algo cerca del clic
                Nodo nodoCercano = modelo.buscarNodoEn(x, y, radioExclusion);
                
                if (nodoCercano != null) {
                    // Si hay un nodo chocando, bloqueamos la acción y avisamos
                    vista.setMensajeEstado("Violación de espacio: El equipo " + nodoCercano.getId() + " está demasiado cerca.", PaletaTema.NEON_ROJO);
                    
                    // Opcional: Hacer parpadear el nodo chocado o emitir un beep
                    Toolkit.getDefaultToolkit().beep(); 
                    return; // ¡CRÍTICO! Salimos del método para que NO se abra el diálogo
                }
               

                // Si el espacio está libre, procedemos normalmente:
                DialogoNodoDinamico dialogo = new DialogoNodoDinamico(vista, modelo);
                
                // ¡INYECCIÓN! Le decimos a la ventana qué tipo obligar
                dialogo.fijarTipoConstruccion(tipoConstruccionActivo); 
                
                dialogo.setVisible(true);

                if (dialogo.isConfirmado()) {
                    try {
                        Nodo nuevoNodo = new Nodo(dialogo.getIdNodo(), 0, 0, dialogo.getTipoSeleccionado());
                        nuevoNodo.setX(x);
                        nuevoNodo.setY(y);
                        nuevoNodo.setCapacidad(dialogo.getCapacidadConfigurada());
                        nuevoNodo.configurarAtenuacionPorTipo();
                        modelo.agregarNodoSeguro(nuevoNodo);
                        vista.setMensajeEstado("Equipo " + nuevoNodo.getId() + " instalado.", PaletaTema.NEON_VERDE);
                        panelDiseño.repaint(); 
                        
                        // REGLA DE LÍMITES: Si instaló una OLT, apagamos la herramienta automáticamente
                        if (tipoConstruccionActivo == Modelo.dominio.TipoNodo.CENTRAL_OLT) {
                            cancelarHerramientaActiva(); 
                        }
                        // Si es cliente, NAP o Splitter, la herramienta sigue activa y el cursor se mantiene!
                        
                    } catch (Exception ex) {
                        vista.setMensajeEstado("Error: " + ex.getMessage(), PaletaTema.NEON_ROJO);
                    }
                }
                break;
            }

            case ELIMINAR -> {
                Nodo nodoBorrar = modelo.buscarNodoEn(x, y, 20); 
                if (nodoBorrar != null) {
                    
                    // NUEVA VALIDACIÓN: ¿El equipo está alimentando a otros o tiene cables conectados?
                    boolean tieneCablesActivos = modelo.getEnlacesActivos().stream()
                            .anyMatch(e -> e.getOrigen().equals(nodoBorrar) || e.getDestino().equals(nodoBorrar));
                    
                    if (tieneCablesActivos) {
                        // Usamos tu Gestor de Alertas que acabamos de crear (necesitamos pedir confirmación, 
                        // pero como el Gestor no devuelve boolean, bloqueamos la eliminación directa y exigimos limpiar primero)
                        Util.UI.GestorAlertas.mostrarAdvertencia(vista, "Equipo en Uso", 
                                "El equipo " + nodoBorrar.getId() + " tiene conexiones ópticas activas.\n\nPara prevenir daños masivos en la red, limpie las conexiones (botón 'Limpiar Conexiones') antes de retirar el hardware.");
                        Toolkit.getDefaultToolkit().beep();
                        return; // Bloqueamos la eliminación
                    }

                    // Si está libre de cables (o los cables estaban apagados), procedemos
                    modelo.eliminarNodo(nodoBorrar);
                    vista.setMensajeEstado("Hardware " + nodoBorrar.getId() + " desmantelado.", PaletaTema.TEXTO_GRIS);
                    panelDiseño.repaint();
                }
            }

            
            default -> {
            }
        }
    }
   
    private void ejecutarEdicionDeNodo(Nodo nodoEditar) {
        // Feedback visual
        panelDiseño.setNodoSeleccionado(nodoEditar);
        vista.setMensajeEstado("Editando Equipo: " + nodoEditar.getId(), PaletaTema.NEON_VIOLETA);
        
        // 1. Abrimos el mismo diálogo de configuración
        DialogoNodoDinamico dialogoEdit = new DialogoNodoDinamico(vista, modelo);
        
        dialogoEdit.cargarDatosPrevios(nodoEditar);
        dialogoEdit.setVisible(true); 

        // 2. Si el técnico presionó "Confirmar Nodo"
        if (dialogoEdit.isConfirmado()) {
            try {
                // Actualizamos el hardware del equipo existente en caliente
                nodoEditar.setTipo(dialogoEdit.getTipoSeleccionado());
                nodoEditar.setCapacidad(dialogoEdit.getCapacidadConfigurada());
                
                modelo.notificarCambios();

                vista.setMensajeEstado("Equipo " + nodoEditar.getId() + " actualizado exitosamente.", PaletaTema.NEON_VERDE);
            } catch (Exception ex) {
                vista.setMensajeEstado("Error al editar: " + ex.getMessage(), PaletaTema.NEON_ROJO);
            }
        }
        
        // Soltamos la selección al terminar
        panelDiseño.setNodoSeleccionado(null); 
        panelDiseño.repaint();
    }
    private void activarHerramientaConstruccion(Modelo.dominio.TipoNodo tipo) {
        this.modoActual = ModoHerramienta.AGREGAR;
        this.tipoConstruccionActivo = tipo;
        vista.setMensajeEstado("Modo Instalación: " + tipo.getNombre() + " (Haz clic en el mapa)", PaletaTema.NEON_AZUL);
        panelDiseño.setNodoSeleccionado(null);

        // ¡MAGIA VISUAL! Cambiar el cursor del mouse por el icono del equipo
        try {
            java.net.URL url = getClass().getResource(tipo.getRutaIcono());
            if (url != null) {
                Image imgIcono = new ImageIcon(url).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                // Creamos un cursor personalizado, centrando el punto de clic (12, 12)
                Cursor cursorEquipo = Toolkit.getDefaultToolkit().createCustomCursor(imgIcono, new Point(12, 12), "cursorHardware");
                panelDiseño.setCursor(cursorEquipo);
                return;
            }
        } catch (Exception ignored) {}
        
        // Fallback: Si no hay icono, usamos la cruz de precisión
        panelDiseño.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    private void cancelarHerramientaActiva() {
        this.modoActual = ModoHerramienta.NINGUNO;
        this.tipoConstruccionActivo = null;
        panelDiseño.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); // Cursor normal de Windows
        vista.setMensajeEstado("Modo Selección activo.", PaletaTema.TEXTO_GRIS);
    }
}