package Vista;

import Modelo.dominio.Enlace;
import Modelo.dominio.Nodo;
import Modelo.dominio.TopologiaRed;
import Util.UI.PaletaTema;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Lienzo (Canvas) principal de la aplicación donde se renderiza gráficamente 
 * la topología de la red.  * Se encarga exclusivamente de la lógica de dibujo (Paint) de los nodos, enlaces,
 * estados de alerta y el nivel de zoom actual.
 */
public class PanelDiseno extends JPanel {

    // =======================================================
    // ATRIBUTOS DE ESTADO Y MODELO
    // =======================================================
    
    private TopologiaRed modelo;
    private Nodo nodoSeleccionado; // Referencia al nodo actualmente seleccionado por el usuario
    private double zoomFactor = 1.0;

    // =======================================================
    // CONSTRUCTOR
    // =======================================================

    /**
     * Constructor del lienzo de diseño.
     * Configura el fondo y el cursor base de la herramienta gráfica.
     */
    public PanelDiseno() {
        setBackground(PaletaTema.FONDO_PRINCIPAL); // Fondo oscuro base
        // Cursor de cruz para dar sensación de herramienta de diseño/ingeniería
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR)); 
    }

    // =======================================================
    // MÉTODOS PÚBLICOS (GETTERS Y SETTERS)
    // =======================================================

    public double getZoomFactor() { 
        return zoomFactor; 
    }
    
    public void setZoomFactor(double zoomFactor) { 
        this.zoomFactor = zoomFactor; 
        repaint(); // Forzar redibujado al cambiar el zoom
    }

    public void setModelo(TopologiaRed modelo) {
        this.modelo = modelo;
        repaint();
    }

    public Nodo getNodoSeleccionado() { 
        return nodoSeleccionado; 
    }

    public void setNodoSeleccionado(Nodo nodo) {
        this.nodoSeleccionado = nodo;
        repaint();
    }

    // =======================================================
    // MÉTODOS PROTEGIDOS (RENDERIZADO GRÁFICO)
    // =======================================================

    /**
     * Motor gráfico del panel. Dibuja secuencialmente en capas:
     * 1. Fondo contenedor redondeado.
     * 2. Cables (Enlaces) para que queden en el fondo.
     * 3. Equipos (Nodos) con sus iconos respectivos.
     * 4. Alertas y aros de selección superpuestos a los equipos.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpia el panel de renders previos
        if (modelo == null) {
            return;
        }

        // ==========================================
        // CAPA 1: FONDO REDONDEADO DEL LIENZO
        // ==========================================
        Graphics2D gFondo = (Graphics2D) g;
        gFondo.setColor(getBackground());
        gFondo.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25); // Curvatura de 25px
        
        // Borde sutil para separar el lienzo visualmente
        gFondo.setColor(new Color(60, 60, 60)); // Gris oscuro
        gFondo.setStroke(new BasicStroke(2.0f));
        gFondo.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);

        // ==========================================
        // CONFIGURACIÓN DEL PINCEL PRINCIPAL
        // ==========================================
        Graphics2D g2 = (Graphics2D) g.create();
        // Habilitar suavizado de formas y textos
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Aplicar el factor de zoom a todo el contexto gráfico
        g2.scale(zoomFactor, zoomFactor);

        // ==========================================
        // CAPA 2: DIBUJAR ENLACES (CABLES)
        // ==========================================
        for (Enlace enlace : modelo.getEnlaces()) {
            Nodo origen = enlace.getOrigen();
            Nodo destino = enlace.getDestino();

            if (enlace.esSolucion()) {
                // Si el enlace forma parte de la red resuelta
                if (enlace.getColorAlerta() != null) {
                    g2.setColor(enlace.getColorAlerta()); // Error/Alerta prioritaria
                } else {
                    g2.setColor(PaletaTema.NEON_VERDE); // Cable sano y activo
                }
                g2.setStroke(new BasicStroke(3.0f));
            } else {
                // Conexiones virtuales/descartadas: Línea delgada y semitransparente
                g2.setColor(new Color(60, 60, 60, 150)); 
                g2.setStroke(new BasicStroke(2.0f));
            }
            g2.drawLine(origen.getX(), origen.getY(), destino.getX(), destino.getY());
        }

        // ==========================================
        // CAPA 3: DIBUJAR NODOS (EQUIPOS)
        // ==========================================
        for (Nodo nodo : modelo.getNodos()) {
            int r = 16; // Radio del nodo
            int x = nodo.getX() - r;
            int y = nodo.getY() - r;

            boolean iconoDibujado = false;
            
            // Intento de cargar la imagen asignada en el TipoNodo
            try {
                String rutaIcono = nodo.getTipo().getRutaIcono(); 
                
                if (rutaIcono != null && !rutaIcono.isEmpty()) {
                    URL url = getClass().getResource(rutaIcono);
                    
                    if (url != null) {
                        Image img = new ImageIcon(url).getImage();
                        g2.drawImage(img, x, y, r * 2, r * 2, this);
                        iconoDibujado = true;
                    }
                }
            } catch (Exception e) { 
                System.err.println("Error procesando imagen: " + e.getMessage()); 
            }

            // RESPALDO GRÁFICO: Si el icono falla o no existe, dibujar figuras geométricas
            if (!iconoDibujado) {
                if (nodo.getTipo() == null) {
                    g2.setColor(PaletaTema.TEXTO_BLANCO);
                } else {
                    switch (nodo.getTipo()) {
                        case CENTRAL_OLT -> g2.setColor(PaletaTema.NEON_VIOLETA);
                        case CAJA_NAP -> g2.setColor(PaletaTema.NEON_AZUL);
                        default -> g2.setColor(PaletaTema.TEXTO_BLANCO);
                    }
                }
                g2.fillOval(x, y, r * 2, r * 2);
                g2.setColor(Color.BLACK);
                g2.drawOval(x, y, r * 2, r * 2);
            }

            // ==========================================
            // CAPA 4: EFECTOS VISUALES SUPERPUESTOS
            // ==========================================
            
            // A. CÍRCULO DE ALERTA PUNTEADO (Efecto Radar)
            if (nodo.getColorAlerta() != null) {
                g2.setColor(nodo.getColorAlerta());
                Stroke trazoOriginal = g2.getStroke(); 
                
                // Patrón punteado: líneas de 5px, espacios de 5px
                float[] patronPunteado = {5f, 5f};
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, patronPunteado, 0f));
                
                int radioAlerta = r + 6; 
                // Anillo principal
                g2.drawOval(nodo.getX() - radioAlerta, nodo.getY() - radioAlerta, radioAlerta * 2, radioAlerta * 2);
                
                // Segundo anillo exterior tenúe
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, patronPunteado, 0f));
                g2.drawOval(nodo.getX() - (radioAlerta + 5), nodo.getY() - (radioAlerta + 5), (radioAlerta + 5) * 2, (radioAlerta + 5) * 2);
                
                g2.setStroke(trazoOriginal); // Restaurar pincel normal
            }

            // B. SELECCIÓN BLANCA (Duplicado inofensivo en el código original, estandarizado)
            if (nodo.equals(nodoSeleccionado)) {
                g2.setColor(Color.WHITE); 
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f}, 0.0f));
                g2.drawOval(x - 5, y - 5, (r * 2) + 10, (r * 2) + 10);
            }

            // C. ETIQUETAS DE TEXTO (IDs)
            g2.setColor(PaletaTema.TEXTO_GRIS);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            // Si el nodo tiene una alerta, elevamos el texto para que no choque con los anillos
            int elevacionY = (nodo.getColorAlerta() != null) ? y - 18 : y - 5;
            
            g2.drawString(nodo.getId(), x - 5, elevacionY);
        }
        
        // Liberar recursos de gráficos del sistema
        g2.dispose();
    }
}