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
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Controlador principal para el panel de diseño.
 * Se encarga de gestionar la interacción del usuario (clics, arrastre, teclado, zoom)
 * con la representación visual de la topología de red.
 */
public class ControladorDiseno {

    // Referencias a los componentes del patrón MVC
    private final TopologiaRed modelo;
    private final VentanaPrincipal vista;
    private final PanelDiseno panelDiseño;
    
    // Variables de estado para la interacción del usuario
    private Nodo nodoArrastrado = null;
    private Cursor cursorHerramientaActual = null;

    /**
     * Máquina de estados para identificar la herramienta activa en el panel.
     */
    private enum ModoHerramienta { NINGUNO, AGREGAR, ELIMINAR }
    private ModoHerramienta modoActual = ModoHerramienta.NINGUNO;
    private Modelo.dominio.TipoNodo tipoConstruccionActivo = null;

    /**
     * Constructor del controlador.
     * Vincula el modelo con la vista e inicializa los eventos.
     */
    public ControladorDiseno(TopologiaRed modelo, VentanaPrincipal vista, PanelDiseno panelDiseño) {
        this.modelo = modelo;
        this.vista = vista;
        this.panelDiseño = panelDiseño;
        this.panelDiseño.setModelo(modelo); // Vinculamos la vista con los datos

        initListeners();
        initAtajosTeclado();
    }

    /**
     * Configura los atajos de teclado para el panel de diseño.
     */
    private void initAtajosTeclado() {
        panelDiseño.setFocusable(true); // Crítico para que escuche el teclado
        
        // Mapea la tecla "Suprimir" (DELETE) a la acción de borrar un nodo
        panelDiseño.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "borrarNodo");
        panelDiseño.getActionMap().put("borrarNodo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                eliminarNodoSeleccionado();
            }
        });
    }

    /**
     * Convierte las coordenadas del evento del ratón a las coordenadas reales del modelo,
     * tomando en cuenta el nivel de zoom actual aplicado en el panel.
     */
    private Point obtenerCoordenadasReales(MouseEvent e) {
        double zoom = panelDiseño.getZoomFactor();
        return new Point((int)(e.getX() / zoom), (int)(e.getY() / zoom));
    }

    /**
     * Inicializa y registra todos los oyentes de eventos (listeners) del ratón y la interfaz.
     * Maneja herramientas, zoom, clics y arrastre de elementos.
     */
    private void initListeners() {
        // 1. Configuración de botones de la paleta lateral
        vista.getPanelLateral().getBtnEliminarNodo().addActionListener(e -> setModo(ModoHerramienta.ELIMINAR));
        vista.getPanelLateral().getBotonesHerramientas().forEach((tipo, boton) -> {
            boton.addActionListener(e -> activarHerramientaConstruccion(tipo));
        });

        // 2. Lógica de Zoom con la rueda del ratón
        panelDiseño.addMouseWheelListener(e -> {
            double zoomActual = panelDiseño.getZoomFactor();
            if (e.getWheelRotation() < 0) {
                zoomActual *= 1.1; // Zoom In
            } else {
                zoomActual /= 1.1; // Zoom Out
            }
            // Mantiene el zoom dentro de límites razonables (50% a 250%)
            zoomActual = Math.max(0.5, Math.min(zoomActual, 2.5)); 
            panelDiseño.setZoomFactor(zoomActual);
        });

        // 3. Lógica de clics principales en el panel
        panelDiseño.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point real = obtenerCoordenadasReales(e);
                int x = real.x;
                int y = real.y;

                // A) DOBLE CLIC IZQUIERDO: Editar propiedades del nodo
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    Nodo nodoClicado = modelo.buscarNodoEn(x, y, 20);
                    if (nodoClicado != null) {
                        ejecutarEdicionDeNodo(nodoClicado); 
                    }
                    return; // Corta la ejecución para no activar el clic simple
                }
                
                // B) CLIC DERECHO: Abrir inspector de información (Nodo o Enlace)
                if (SwingUtilities.isRightMouseButton(e)) {
                    Nodo nodoTocado = modelo.buscarNodoEn(x, y, 20); 
                    if (nodoTocado != null) {
                        new Vista.DialogoInfoNodo(vista, nodoTocado, modelo).setVisible(true);
                        return; 
                    }
                    Enlace enlaceTocado = modelo.buscarEnlaceEn(x, y, 7);
                    if (enlaceTocado != null) {
                        new Vista.DialogoInfoEnlace(vista, enlaceTocado).setVisible(true);
                    }
                    return; 
                }

                // C) CLIC IZQUIERDO SIMPLE: Seleccionar, Agregar o Eliminar nodos
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    Nodo nodoClicado = modelo.buscarNodoEn(x, y, 20);
                    
                    if (nodoClicado != null) {
                        // Si tocamos un equipo, lo seleccionamos
                        panelDiseño.setNodoSeleccionado(nodoClicado);
                        // Si la herramienta "Eliminar" está activa, procedemos a borrarlo
                        if (modoActual == ModoHerramienta.ELIMINAR) {
                            eliminarNodoSeleccionado();
                        }
                    } else {
                        // Si tocamos un espacio vacío, limpiamos la selección
                        panelDiseño.setNodoSeleccionado(null);
                        // Si estamos en modo Agregar, intentamos crear un equipo en esa coordenada
                        if (modoActual == ModoHerramienta.AGREGAR) {
                            manejarClicEnMapa(x, y); 
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Al presionar, verificamos si estamos "agarrando" un nodo para arrastrarlo
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Point real = obtenerCoordenadasReales(e);
                    nodoArrastrado = modelo.buscarNodoEn(real.x, real.y, 20);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Al soltar el clic, terminamos el arrastre y actualizamos las conexiones (cables)
                if (nodoArrastrado != null) {
                    if (!modelo.getEnlacesActivos().isEmpty()) {
                        try { 
                            modelo.actualizarFisicaDeCables(nodoArrastrado); 
                        } catch (Exception ignored) {}
                    }
                    nodoArrastrado = null;
                }
            }
        });
        
        // 4. Lógica de movimiento y arrastre del ratón
        panelDiseño.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Cambia el cursor visualmente si pasamos sobre un nodo en modo agregar
                if (modoActual == ModoHerramienta.AGREGAR && cursorHerramientaActual != null) {
                    Point real = obtenerCoordenadasReales(e);
                    Nodo nodoHover = modelo.buscarNodoEn(real.x, real.y, 20);
                    
                    if (nodoHover != null) {
                        panelDiseño.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de selección
                    } else {
                        panelDiseño.setCursor(cursorHerramientaActual); // Cursor de instalación de hardware
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Si hay un nodo siendo arrastrado, actualizamos sus coordenadas
                if (nodoArrastrado != null) {
                    // Evitamos que el usuario arrastre el equipo fuera del área visible del panel
                    int x = Math.max(20, Math.min(e.getX(), panelDiseño.getWidth() - 20));
                    int y = Math.max(20, Math.min(e.getY(), panelDiseño.getHeight() - 20));

                    nodoArrastrado.setX(x);
                    nodoArrastrado.setY(y);

                    // Repintamos para animar el arrastre sin cálculos pesados
                    panelDiseño.repaint(); 
                }
            }
        });
    }

    /**
     * Cambia el modo actual de la herramienta (Agregar, Eliminar, Ninguno)
     * y actualiza el estado visual de la interfaz.
     */
    private void setModo(ModoHerramienta nuevoModo) {
        this.modoActual = nuevoModo;
        // Feedback visual en la barra de estado inferior
        vista.setMensajeEstado("Modo Activo: " + nuevoModo.name(), PaletaTema.NEON_AZUL);
        panelDiseño.setNodoSeleccionado(null); // Limpiar selecciones previas al cambiar de modo
    }

    /**
     * Gestiona la lógica de creación o eliminación cuando se hace clic en un área vacía
     * o sobre un nodo, dependiendo del modo activo.
     */
    private void manejarClicEnMapa(int x, int y) {
        switch (modoActual) {
            case AGREGAR -> {
                int margen = 20; // Margen en píxeles contra los bordes del panel
                if (x < margen || y < margen || x > panelDiseño.getWidth() - margen || y > panelDiseño.getHeight() - margen) {
                    vista.setMensajeEstado("Fuera de límites: El equipo debe instalarse dentro del área visible del mapa.", PaletaTema.NEON_ROJO);
                    Toolkit.getDefaultToolkit().beep();
                    return; // Bloquea la creación si está fuera de los límites
                }
                
                // REGLA DE NEGOCIO 1.1: RADIO DE EXCLUSIÓN
                int radioExclusion = 50; // Distancia mínima permitida entre equipos
                
                // Verifica si hay un nodo demasiado cerca de la coordenada del clic
                Nodo nodoCercano = modelo.buscarNodoEn(x, y, radioExclusion);
                
                if (nodoCercano != null) {
                    // Bloquea la acción para evitar superposiciones
                    vista.setMensajeEstado("Violación de espacio: El equipo " + nodoCercano.getId() + " está demasiado cerca.", PaletaTema.NEON_ROJO);
                    Toolkit.getDefaultToolkit().beep(); 
                    return; 
                }

                // Si el espacio está libre, se abre el diálogo para configurar el nuevo equipo
                DialogoNodoDinamico dialogo = new DialogoNodoDinamico(vista, modelo);
                dialogo.fijarTipoConstruccion(tipoConstruccionActivo); 
                dialogo.setVisible(true);

                // Si el usuario confirma en el diálogo, se crea y agrega el nodo al modelo
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
                        
                        // Si instaló una OLT, se desactiva la herramienta de instalación automáticamente
                        if (tipoConstruccionActivo == Modelo.dominio.TipoNodo.CENTRAL_OLT) {
                            cancelarHerramientaActiva(); 
                        }
                    } catch (Exception ex) {
                        vista.setMensajeEstado("Error: " + ex.getMessage(), PaletaTema.NEON_ROJO);
                    }
                }
                break;
            }

            case ELIMINAR -> {
                // Lógica de eliminación al hacer clic (respaldo redundante para el clic simple)
                Nodo nodoBorrar = modelo.buscarNodoEn(x, y, 20); 
                if (nodoBorrar != null) {
                    panelDiseño.setNodoSeleccionado(nodoBorrar);
                    eliminarNodoSeleccionado();
                }
            }
            
            default -> {
                // No se realiza ninguna acción si no hay modo activo
            }
        }
    }

    /**
     * Elimina del modelo el nodo que se encuentre actualmente seleccionado en la vista.
     * Incluye validaciones de seguridad para evitar borrar nodos en uso.
     */
    private void eliminarNodoSeleccionado() {
        Nodo nodoBorrar = panelDiseño.getNodoSeleccionado();
        if (nodoBorrar == null) return; // Validación de seguridad nula

        // Verifica si el nodo tiene conexiones (enlaces) existentes
        boolean tieneCablesActivos = modelo.getEnlacesActivos().stream()
                .anyMatch(e -> e.getOrigen().equals(nodoBorrar) || e.getDestino().equals(nodoBorrar));
        
        if (tieneCablesActivos) {
            // Advierte al usuario y bloquea la eliminación si hay cables conectados
            Util.Tecnicas.GestorAlertas.mostrarAdvertencia(vista, "Equipo en Uso", 
                    "El equipo " + nodoBorrar.getId() + " tiene conexiones ópticas activas.\n\nPara prevenir daños masivos en la red, limpie las conexiones antes de retirar el hardware.");
            Toolkit.getDefaultToolkit().beep();
            return; 
        }

        // Si está libre de conexiones, se elimina definitivamente
        modelo.eliminarNodo(nodoBorrar);
        panelDiseño.setNodoSeleccionado(null); 
        vista.setMensajeEstado("Hardware " + nodoBorrar.getId() + " desmantelado con tecla Suprimir.", PaletaTema.TEXTO_GRIS);
        panelDiseño.repaint();
    }
   
    /**
     * Abre el cuadro de diálogo para modificar las propiedades de un nodo existente.
     */
    private void ejecutarEdicionDeNodo(Nodo nodoEditar) {
        panelDiseño.setNodoSeleccionado(nodoEditar);
        vista.setMensajeEstado("Editando Equipo: " + nodoEditar.getId(), PaletaTema.NEON_VIOLETA);
        
        DialogoNodoDinamico dialogoEdit = new DialogoNodoDinamico(vista, modelo);
        dialogoEdit.cargarDatosPrevios(nodoEditar);
        dialogoEdit.setVisible(true); 

        // Procesamiento tras la confirmación del usuario en el diálogo
        if (dialogoEdit.isConfirmado()) {
            try {
                int capacidadVieja = nodoEditar.getCapacidad();
                int capacidadNueva = dialogoEdit.getCapacidadConfigurada();
                
                // Se aplican los nuevos valores al hardware
                nodoEditar.setTipo(dialogoEdit.getTipoSeleccionado());
                nodoEditar.setCapacidad(capacidadNueva);
                
                // Si la capacidad del equipo se reduce, se limpian las conexiones para evitar inconsistencias
                if (capacidadNueva < capacidadVieja) {
                    modelo.getEnlaces().clear();
                    modelo.limpiarSoluciones();
                    vista.setMensajeEstado("Capacidad reducida en " + nodoEditar.getId() + ". Se han limpiado las conexiones, presione F5 para re-enrutar.", Util.UI.PaletaTema.NEON_ROJO);
                } else {
                    vista.setMensajeEstado("Equipo " + nodoEditar.getId() + " actualizado exitosamente.", Util.UI.PaletaTema.NEON_VERDE);
                }
                
                modelo.notificarCambios();
            } catch (Exception ex) {
                vista.setMensajeEstado("Error al editar: " + ex.getMessage(), Util.UI.PaletaTema.NEON_ROJO);
            }
        }
        
        panelDiseño.setNodoSeleccionado(null); 
        panelDiseño.repaint();
    }

    /**
     * Configura el controlador en modo "AGREGAR" y cambia el cursor del ratón 
     * por el icono del tipo de nodo que se va a construir.
     */
    private void activarHerramientaConstruccion(Modelo.dominio.TipoNodo tipo) {
        this.modoActual = ModoHerramienta.AGREGAR;
        this.tipoConstruccionActivo = tipo;
        vista.setMensajeEstado("Modo Instalación: " + tipo.getNombre() + " (Haz clic en el mapa)", PaletaTema.NEON_AZUL);
        panelDiseño.setNodoSeleccionado(null);

        // Intenta cargar el icono del equipo para usarlo como cursor
        try {
            java.net.URL url = getClass().getResource(tipo.getRutaIcono());
            if (url != null) {
                Image imgIcono = new ImageIcon(url).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                cursorHerramientaActual = Toolkit.getDefaultToolkit().createCustomCursor(imgIcono, new Point(12, 12), "cursorHardware");
                panelDiseño.setCursor(cursorHerramientaActual);
                return;
            }
        } catch (Exception ignored) {}
        
        // Fallback: Si el icono falla, usa un cursor de cruz por defecto
        cursorHerramientaActual = new Cursor(Cursor.CROSSHAIR_CURSOR);
        panelDiseño.setCursor(cursorHerramientaActual);
    }

    /**
     * Desactiva la herramienta de construcción actual y devuelve la aplicación
     * a su estado de selección estándar.
     */
    private void cancelarHerramientaActiva() {
        this.modoActual = ModoHerramienta.NINGUNO;
        this.tipoConstruccionActivo = null;
        panelDiseño.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); // Cursor normal del sistema
        vista.setMensajeEstado("Modo Selección activo.", PaletaTema.TEXTO_GRIS);
    }
}