/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.markerbar;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * Panel which contains MarkerBar at left, OverviewBar at right, and a user panel in the center
 * @author JM235353
 */
public class MarkerContainerPanel extends JPanel implements ViewChangeListener, TableModelListener {

    private TreeMap<Integer, ArrayList<AbstractMarker>> m_markers = new TreeMap<>();
    private HashMap<Class, MarkerRendererInterface> m_renderers = new HashMap<>();
    private static final long serialVersionUID = 1L;
    private MarkerComponentInterface m_markerComponent = null;
    private static DefaultMarkerRenderer m_defaultRenderer = null;

    private OverviewBar m_overviewBar = null;
    private MarkerBar m_markerBar = null;
    

    
    public MarkerContainerPanel(JScrollPane sp, MarkerComponentInterface markerComponent) {

        m_markerComponent = markerComponent;
        
        m_markerBar = new MarkerBar(this);
        m_overviewBar = new OverviewBar(this);

        markerComponent.addViewChangeListerner(this);
        markerComponent.addTableModelListener(this);
        
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(m_markerBar, c);

        c.gridx++;
        c.weightx = 1;

        add(sp, c);

        c.gridx++;
        c.weightx = 0;
        add(m_overviewBar, c);


        sp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                m_markerBar.repaint();
            }
        });
        
        sp.getViewport().setBackground(Color.white);
        sp.setBackground(Color.white);
        
        initDefaultRenderers();

    }

    private void initDefaultRenderers() {
        addRenderer(BookmarkMarker.class, new BookmarkMarkerRenderer());
        addRenderer(AnnotationMarker.class, new AnnotationMarkerRenderer());
    }
    
    public void addRenderer(Class c, MarkerRendererInterface renderer) {
        m_renderers.put(c, renderer);
    }

    public MarkerRendererInterface getRenderer(AbstractMarker marker) {
        MarkerRendererInterface renderer = m_renderers.get(marker.getClass());
        if (renderer == null) {
            if (m_defaultRenderer == null) {
                m_defaultRenderer = new DefaultMarkerRenderer(Color.red);
            }
            renderer = m_defaultRenderer;
        }
        return renderer;
    }

    public MarkerComponentInterface getMarkerComponentInterface() {
        return m_markerComponent;
    }

    public TreeMap<Integer, ArrayList<AbstractMarker>> getMarkerArray() {
        return m_markers;
    }

    public void addMarker(AbstractMarker marker) {
        Integer rowKey = Integer.valueOf(marker.getRow());
        ArrayList<AbstractMarker> markersInRow = m_markers.get(rowKey);
        if (markersInRow == null) {
            markersInRow = new ArrayList<>(1);
            m_markers.put(rowKey, markersInRow);
        }
        markersInRow.add(marker);
        
        repaintBars();
    }
    
    public AbstractMarker getMarker(int row, int type) {
        ArrayList<AbstractMarker> markersInRow = m_markers.get(row);
        if (markersInRow == null) {
            return null;
        }
        
        int size = markersInRow.size();
        for (int i=0;i<size;i++) {
            AbstractMarker marker = markersInRow.get(i);
            if (marker.getType() == type) {
                return marker;
            }
        }
        
        return null;
    }
    
    public boolean removeMarker(int row, int type) {
        
        ArrayList<AbstractMarker> markersInRow = m_markers.get(row);
        if (markersInRow == null) {
            return false;
        }
        
        int size = markersInRow.size();
        for (int i=0;i<size;i++) {
            AbstractMarker marker = markersInRow.get(i);
            if (marker.getType() == type) {
                markersInRow.remove(i);
                repaintBars();
                
                if (markersInRow.isEmpty()) {
                    m_markers.remove(row);
                }
                
                return true;
            }
        }
        
        return false;
    }
    

    
    
    public boolean removeAllMarkers(int row) {

        ArrayList<AbstractMarker> markersInRow = m_markers.get(row);
        if (markersInRow == null) {
            return false;
        }

        m_markers.remove(row);
        repaintBars();

        return true;
    }
    
    public boolean removeAllMarkers() {
        if (m_markers.isEmpty()) {
            return false;
        }
        
        m_markers.clear();
        repaintBars();
        
        return true;
    }
    
    public boolean hasMarker(int row) {
        ArrayList<AbstractMarker> markersInRow = m_markers.get(row);
        if (markersInRow == null) {
            return false;
        }
        return true;
    }
    
    public boolean hasMarkers() {
        return !m_markers.isEmpty();
    }
    
    public int findNearestRowWithMarker(int rowInView) {
        
        int minDistance = Integer.MAX_VALUE;
        Integer nearestRowKey = null;
        
        Iterator<Integer> it = m_markers.keySet().iterator();
        while (it.hasNext()) {
            Integer rowKey = it.next();
            int row = m_markerComponent.convertRowIndexNonFilteredModelToView(rowKey.intValue());
            int distance = Math.abs(rowInView-row);
            if (distance<minDistance) {
                minDistance = distance;
                nearestRowKey = rowKey;
            }
        }
        if (nearestRowKey == null) {
            return rowInView;
        }
        
        return m_markerComponent.convertRowIndexNonFilteredModelToView(nearestRowKey.intValue());
        

        
        
    }
    
    
    private void repaintBars() {
        if (m_overviewBar != null) {
            m_overviewBar.repaint();
        }
        if (m_markerBar != null) {
            m_markerBar.repaint();
        }
    }

    @Override
    public void viewChanged() {
        repaintBars();
    }
    
     public void setMaxLineNumber(int maxLineNumber) {
         if ((m_markerBar != null) && (m_markerBar.setMaxLineNumber(maxLineNumber)) && (m_markerBar.isLineNumbersDisplayed()) ) {
            m_markerComponent.calculateVisibleRange();
            revalidate();
            repaint();
         }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        viewChanged();
    }
    
}
