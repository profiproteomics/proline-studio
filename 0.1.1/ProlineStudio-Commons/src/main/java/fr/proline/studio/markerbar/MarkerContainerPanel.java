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

/**
 * Panel which contains MarkerBar at left, OverviewBar at right, and a user panel in the center
 * @author JM235353
 */
public class MarkerContainerPanel extends JPanel implements ViewChangeListener {

    private TreeMap<Integer, ArrayList<AbstractMarker>> markers = new TreeMap<>();
    private HashMap<Class, MarkerRendererInterface> renderers = new HashMap<>();
    private static final long serialVersionUID = 1L;
    private MarkerComponentInterface markerComponent = null;
    private static DefaultMarkerRenderer defaultRenderer = null;

    private OverviewBar overviewBar = null;
    private MarkerBar markerBar = null;
    

    
    public MarkerContainerPanel(JScrollPane sp, MarkerComponentInterface markerComponent) {

        this.markerComponent = markerComponent;
        markerBar = new MarkerBar(this);
        overviewBar = new OverviewBar(this);

        markerComponent.addViewChangeListerner(this);
        
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(markerBar, c);

        c.gridx++;
        c.weightx = 1;

        add(sp, c);

        c.gridx++;
        c.weightx = 0;
        add(overviewBar, c);


        sp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                markerBar.repaint();
            }
        });
        
        initDefaultRenderers();

    }

    private void initDefaultRenderers() {
        addRenderer(BookmarkMarker.class, new BookmarkMarkerRenderer());
        addRenderer(AnnotationMarker.class, new AnnotationMarkerRenderer());
    }
    
    public void addRenderer(Class c, MarkerRendererInterface renderer) {
        renderers.put(c, renderer);
    }

    public MarkerRendererInterface getRenderer(AbstractMarker marker) {
        MarkerRendererInterface renderer = renderers.get(marker.getClass());
        if (renderer == null) {
            if (defaultRenderer == null) {
                defaultRenderer = new DefaultMarkerRenderer(Color.red);
            }
            renderer = defaultRenderer;
        }
        return renderer;
    }

    public MarkerComponentInterface getMarkerComponentInterface() {
        return markerComponent;
    }

    public TreeMap<Integer, ArrayList<AbstractMarker>> getMarkerArray() {
        return markers;
    }

    public void addMarker(AbstractMarker marker) {
        Integer rowKey = Integer.valueOf(marker.getRow());
        ArrayList<AbstractMarker> markersInRow = markers.get(rowKey);
        if (markersInRow == null) {
            markersInRow = new ArrayList<AbstractMarker>(1);
            markers.put(rowKey, markersInRow);
        }
        markersInRow.add(marker);
        
        repaintBars();
    }
    
    public AbstractMarker getMarker(int row, int type) {
        ArrayList<AbstractMarker> markersInRow = markers.get(row);
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
        
        ArrayList<AbstractMarker> markersInRow = markers.get(row);
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
                    markers.remove(row);
                }
                
                return true;
            }
        }
        
        return false;
    }
    

    
    
    public boolean removeAllMarkers(int row) {

        ArrayList<AbstractMarker> markersInRow = markers.get(row);
        if (markersInRow == null) {
            return false;
        }

        markers.remove(row);
        repaintBars();

        return true;
    }
    
    public boolean removeAllMarkers() {
        if (markers.isEmpty()) {
            return false;
        }
        
        markers.clear();
        repaintBars();
        
        return true;
    }
    
    public boolean hasMarker(int row) {
        ArrayList<AbstractMarker> markersInRow = markers.get(row);
        if (markersInRow == null) {
            return false;
        }
        return true;
    }
    
    public boolean hasMarkers() {
        return !markers.isEmpty();
    }
    
    public int findNearestRowWithMarker(int rowInView) {
        
        int minDistance = Integer.MAX_VALUE;
        Integer nearestRowKey = null;
        
        Iterator<Integer> it = markers.keySet().iterator();
        while (it.hasNext()) {
            Integer rowKey = it.next();
            int row = markerComponent.convertRowIndexToView(rowKey.intValue());
            int distance = Math.abs(rowInView-row);
            if (distance<minDistance) {
                minDistance = distance;
                nearestRowKey = rowKey;
            }
        }
        if (nearestRowKey == null) {
            return rowInView;
        }
        
        return markerComponent.convertRowIndexToView(nearestRowKey.intValue());
        

        
        
    }
    
    
    private void repaintBars() {
        if (overviewBar != null) {
            overviewBar.repaint();
        }
        if (markerBar != null) {
            markerBar.repaint();
        }
    }

    @Override
    public void viewChanged() {
        repaintBars();
    }
    
     public void setMaxLineNumber(int maxLineNumber) {
         if ((markerBar != null) && (markerBar.setMaxLineNumber(maxLineNumber)) && (markerBar.isLineNumbersDisplayed()) ) {
            revalidate();
            repaint();
         }
    }
    
}
