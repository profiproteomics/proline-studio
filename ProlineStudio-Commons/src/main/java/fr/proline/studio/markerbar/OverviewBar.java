package fr.proline.studio.markerbar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class OverviewBar extends AbstractBar  implements MouseListener {

    private static final long serialVersionUID = 1L;

    public OverviewBar(MarkerContainerPanel containerPanel) {
        super(containerPanel);
        
        addMouseListener(this);
    }

    @Override
    public void paint(Graphics g) {

        g.setColor(Color.lightGray);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        
        double nbRows = (double) containerPanel.getMarkerComponentInterface().getRowCount();
        double heightD = (double) getHeight();
        double h = heightD / nbRows;

        TreeMap<Integer, ArrayList<AbstractMarker>> markerMap = containerPanel.getMarkerArray();
        Iterator<Integer> itRow = markerMap.keySet().iterator();
        while (itRow.hasNext()) {
            Integer row = itRow.next();
            ArrayList<AbstractMarker> markersArrayList = markerMap.get(row);
            int size = markersArrayList.size();
            for (int i = 0; i < size; i++) {
                AbstractMarker marker = markersArrayList.get(i);

                int y = (int) Math.round(((double) row.intValue()) * h);

                MarkerRendererInterface renderer = containerPanel.getRenderer(marker);

                renderer.paint(AbstractBar.BarType.OVERVIEW_BAR, g, 0, y, getWidth(), 4);

            }
        }


    }

    @Override
    public void mouseClicked(MouseEvent e) {
        MarkerComponentInterface componentInterface = containerPanel.getMarkerComponentInterface();
        int nbRows = componentInterface.getRowCount();
        
        int y = e.getY();
        int rowClicked = (int) Math.round(  ( ((double) y) / ((double) getHeight()) )*nbRows );
        
        //System.out.println(rowClicked);
        
        componentInterface.scrollToVisible(rowClicked);
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
