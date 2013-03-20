package fr.proline.studio.markerbar;

import java.awt.Graphics;

public interface MarkerRendererInterface {

    public void paint(AbstractBar.BarType barType, Graphics g, int x, int y, int width, int height);
}
