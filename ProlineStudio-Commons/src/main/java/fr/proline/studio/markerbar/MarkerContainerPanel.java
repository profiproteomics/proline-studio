package fr.proline.studio.markerbar;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MarkerContainerPanel extends JPanel {

    private TreeMap<Integer, ArrayList<AbstractMarker>> markers = new TreeMap<Integer, ArrayList<AbstractMarker>>();
    private HashMap<Class, MarkerRendererInterface> renderers = new HashMap<Class, MarkerRendererInterface>();
    private static final long serialVersionUID = 1L;
    private MarkerComponentInterface markerComponent = null;
    private static DefaultMarkerRenderer defaultRenderer = null;

    private OverviewBar overviewBar = null;
    private MarkerBar markerBar = null;
    

    
    public MarkerContainerPanel(JScrollPane sp, MarkerComponentInterface markerComponent) {

        this.markerComponent = markerComponent;
        markerBar = new MarkerBar(this);
        overviewBar = new OverviewBar(this);

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
        Integer rowKey = Integer.valueOf(marker.getRowStart());
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
    
    public int findNearestRowWithMarker(int row) {
        Integer rowKey = Integer.valueOf(row);
        Integer nearestLowerRowKey = markers.floorKey(rowKey);
        Integer nearestHigherRowKey = markers.higherKey(rowKey);
        
        int rowLower = -1;
        if (nearestLowerRowKey != null) {
            rowLower = nearestLowerRowKey.intValue();
        }
        
        int rowHigher= -1;
        if (nearestHigherRowKey != null) {
            rowHigher = nearestHigherRowKey.intValue();
        }

        if ((rowLower == -1) && (rowHigher == -1)) {
            return row;
        } else if ((rowLower != -1) && (rowHigher == -1)) {
            return rowLower;
        } else if ((rowLower == -1) && (rowHigher != -1)) {
            return rowHigher;
        } else {
            if ((row-rowLower)<=(rowHigher-row)) {
                return rowLower;
            } else {
                return rowHigher;
            }
        }
        
        
        
    }
    
    
    private void repaintBars() {
        if (overviewBar != null) {
            overviewBar.repaint();
        }
        if (markerBar != null) {
            markerBar.repaint();
        }
    }
}
