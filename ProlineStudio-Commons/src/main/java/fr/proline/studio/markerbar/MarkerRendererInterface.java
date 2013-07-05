package fr.proline.studio.markerbar;

import java.awt.Graphics;

/**
 * Renderer of a Marker must implements this interface
 * @author JM235353
 */
public interface MarkerRendererInterface {

    public void paint(AbstractBar.BarType barType, Graphics g, int x, int y, int width, int height);
}
