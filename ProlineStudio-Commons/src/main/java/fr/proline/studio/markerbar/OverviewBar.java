package fr.proline.studio.markerbar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Bar which displays all markers and allow to jump the selected
 * marker in the Component included in MarkerContainerPanel
 * 
 * @author JM235353
 */
public class OverviewBar extends AbstractBar  implements MouseListener {

    private static final long serialVersionUID = 1L;

    public OverviewBar(MarkerContainerPanel containerPanel) {
        super(containerPanel);
        
        addMouseListener(this);
    }

    @Override
    public void paint(Graphics g) {

        g.setColor(m_almostWhiteColor);
        g.fillRect(1, 1, getWidth()-2, getHeight()-2);
        
        
        g.setColor(Color.lightGray);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        
        MarkerComponentInterface markerComponentInterface = m_containerPanel.getMarkerComponentInterface();
        
        double nbRows = (double) markerComponentInterface.getRowCount();
        double heightD = (double) getHeight();
        double h = heightD / nbRows;

        TreeMap<Integer, ArrayList<AbstractMarker>> markerMap = m_containerPanel.getMarkerArray();
        Iterator<Integer> itRow = markerMap.keySet().iterator();
        while (itRow.hasNext()) {
            Integer row = itRow.next();
            ArrayList<AbstractMarker> markersArrayList = markerMap.get(row);
            int size = markersArrayList.size();
            for (int i = 0; i < size; i++) {
                AbstractMarker marker = markersArrayList.get(i);

                int graphicalRow = markerComponentInterface.convertRowIndexToView(row.intValue());
                
                int y = (int) Math.round(((double) graphicalRow) * h);

                MarkerRendererInterface renderer = m_containerPanel.getRenderer(marker);

                renderer.paint(AbstractBar.BarType.OVERVIEW_BAR, g, 0, y, getWidth(), 4);

            }
        }


    }
    private static final Color m_almostWhiteColor = new Color(248,248,248);

    @Override
    public void mouseClicked(MouseEvent e) {
        MarkerComponentInterface componentInterface = m_containerPanel.getMarkerComponentInterface();
        int nbRows = componentInterface.getRowCount();
        
        int y = e.getY();
        int rowClicked = (int) Math.round(  ( ((double) y) / ((double) getHeight()) )*nbRows );
        
        // search for the nearest marker
        int rowWithMarker = m_containerPanel.findNearestRowWithMarker(rowClicked);
        
        // calculate number of pixels between the row with marker and the row clicked
        // if the difference is less that 5 pixels, act as if the user has clicked on the marker
        double nbPixels = (((double)Math.abs(rowWithMarker-rowClicked))/nbRows)*getHeight();
        if (nbPixels<=5) {
            componentInterface.scrollToVisible(rowWithMarker);
        } else {
            componentInterface.scrollToVisible(rowClicked);
        }
        
        
        
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
