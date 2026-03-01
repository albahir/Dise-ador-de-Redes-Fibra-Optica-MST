
package Vista;

import Modelo.dominio.Enlace;
import Modelo.dominio.Nodo;
import Modelo.dominio.TopologiaRed;
import Util.UI.PaletaTema;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class PanelDiseno extends JPanel {

    private TopologiaRed modelo;
    private Nodo nodoSeleccionado; // Para resaltar el nodo si el usuario lo toca

    public PanelDiseno() {
        setBackground(PaletaTema.FONDO_PRINCIPAL); // Fondo oscuro
        // Cambiamos el cursor a una cruz para dar sensación de herramienta de diseño
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR)); 
    }

    public void setModelo(TopologiaRed modelo) {
        this.modelo = modelo;
        repaint();
    }

    public void setNodoSeleccionado(Nodo nodo) {
        this.nodoSeleccionado = nodo;
        repaint();
    }

    @Override

    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpia el panel
        if (modelo == null) return;
          Graphics2D g2 = (Graphics2D) g;
      
       // Activar Anti-Aliasing para que las curvas, líneas y círculos se vean suaves y HD
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ==========================================
        // 1. DIBUJAR EL FONDO REDONDEADO DEL LIENZO
        // ==========================================
        // Pintamos el relleno del color del panel
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25); // 25 es el radio de la curva
        
        // (Opcional) Le dibujamos un borde sutil para que se note la curva contra el fondo de la ventana
        g2.setColor(new Color(60, 60, 60)); // Gris oscuro
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
        // 1. DIBUJAR ENLACES (CABLES) PRIMERO PARA QUE QUEDEN DEBAJO DE LOS NODOS
        for (Enlace enlace : modelo.getEnlaces()) {
            Nodo origen = enlace.getOrigen();
            Nodo destino = enlace.getDestino();

            if (enlace.esSolucion()) {
              if (enlace.getColorAlerta() != null) {
                    g2.setColor(enlace.getColorAlerta());
                } else {
                    g2.setColor(PaletaTema.NEON_VERDE);
                }
              g2.setStroke(new BasicStroke(3.0f));
            } else {
                // No es solución: Línea delgada y gris (casi invisible para no estorbar)
                g2.setColor(new Color(60, 60, 60, 150)); 
                g2.setStroke(new BasicStroke(2.0f));
            }
            g2.drawLine(origen.getX(), origen.getY(), destino.getX(), destino.getY());
        }

        // 2. DIBUJAR NODOS (EQUIPOS)
        for (Nodo nodo : modelo.getNodos()) {
            int r = 16; // Radio del nodo
            int x = nodo.getX() - r;
            int y = nodo.getY() - r;

            // Intentar cargar la imagen si implementaste las rutas en TipoNodo
            boolean iconoDibujado = false;
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

            // RESPALDO: Si no hay ícono
            if (!iconoDibujado) {
                if (null == nodo.getTipo()) g2.setColor(PaletaTema.TEXTO_BLANCO);
                else switch (nodo.getTipo()) {
                    case CENTRAL_OLT -> g2.setColor(PaletaTema.NEON_VIOLETA);
                    case CAJA_NAP -> g2.setColor(PaletaTema.NEON_AZUL);
                    default -> g2.setColor(PaletaTema.TEXTO_BLANCO);
                }
                g2.fillOval(x, y, r * 2, r * 2);
                g2.setColor(Color.BLACK);
                g2.drawOval(x, y, r * 2, r * 2);
            }

            // ==========================================
            // EFECTO VISUAL: CÍRCULO DE ALERTA PUNTEADO
            // ==========================================
            if (nodo.getColorAlerta() != null) {
                g2.setColor(nodo.getColorAlerta());
                Stroke trazoOriginal = g2.getStroke(); // Guardamos el pincel original
                
                // Creamos un trazo punteado (líneas de 5px, espacios de 5px)
                float[] patronPunteado = {5f, 5f};
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, patronPunteado, 0f));
                
                // Anillo principal (Ligeramente más grande que el icono)
                int radioAlerta = r + 6; 
                g2.drawOval(nodo.getX() - radioAlerta, nodo.getY() - radioAlerta, radioAlerta * 2, radioAlerta * 2);
                
                // Segundo anillo más tenue (Efecto Radar)
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, patronPunteado, 0f));
                g2.drawOval(nodo.getX() - (radioAlerta + 5), nodo.getY() - (radioAlerta + 5), (radioAlerta + 5) * 2, (radioAlerta + 5) * 2);
                
                g2.setStroke(trazoOriginal); // Restauramos el pincel normal
            }

            // DIBUJAR SELECCIÓN (Aro alrededor del nodo si está seleccionado en Herramienta Editar)
            if (nodo.equals(nodoSeleccionado)) {
                g2.setColor(PaletaTema.NEON_ROJO);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f}, 0.0f));
                g2.drawOval(x - 5, y - 5, (r * 2) + 10, (r * 2) + 10);
            }

            // Dibujar el ID del nodo en pequeñito
            g2.setColor(PaletaTema.TEXTO_GRIS);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            int elevacionY = (nodo.getColorAlerta() != null) ? y - 18 : y - 5;
            
            g2.drawString(nodo.getId(), x - 5, elevacionY);
        }
    }
}
