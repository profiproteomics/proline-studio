package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.LockedDataModel;
import fr.proline.studio.graphics.cursor.AbstractCursor;
import fr.proline.studio.graphics.marker.AbstractMarker;
import fr.proline.studio.parameter.ParameterList;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import javax.swing.JPopupMenu;

/**
 * Base class for all types of plot
 * @author JM235353
 */
public abstract class PlotAbstract implements Axis.EnumXInterface, Axis.EnumYInterface {
    
    protected PlotType m_plotType;
    
    protected CompareDataInterface m_compareDataInterface = null;
    protected CrossSelectionInterface m_crossSelectionInterface = null;
    protected int m_colX;
    protected int m_colY;
    protected String m_parameterZ;
        
    protected BasePlotPanel m_plotPanel;
    
    private ArrayList<AbstractMarker> m_markersList = null;
    
    private ArrayList<AbstractCursor> m_cursorList = null;
    
    protected boolean m_isPaintMarker = true;

    protected boolean m_locked = false;
    
    public abstract double getXMin();
    public abstract double getXMax();
    public abstract double getYMin();
    public abstract double getYMax();

    public abstract boolean needsXAxis();
    public abstract boolean needsYAxis();

    public abstract ArrayList<Long> getSelectedIds();
    public abstract void setSelectedIds(ArrayList<Long> selection);
    
    public abstract void parametersChanged();
    
    public abstract void paint(Graphics2D g);

    public PlotAbstract(BasePlotPanel plotPanel, PlotType plotType, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface) {
        m_plotPanel = plotPanel;
        m_plotType = plotType;
        m_compareDataInterface = (m_locked) ? new LockedDataModel(compareDataInterface) : compareDataInterface;
        m_crossSelectionInterface = crossSelectionInterface;
    }
    
    
    
    public abstract String getToolTipText(double x, double y);
    
    
    public void update(int colX, int colY, String parameterZ) {
        m_colX = colX;
        m_colY = colY;
        m_parameterZ = parameterZ;
        updateAxisSpecificities();
        update();
    }
    public void updateAxisSpecificities() {
        if ((needsXAxis()) && (m_colX != -1)) {
            Class xClass = m_compareDataInterface.getDataColumnClass(m_colX);
            boolean isIntegerX = ((xClass.equals(Integer.class)) || (xClass.equals(Long.class)) || (xClass.equals(String.class)));
            boolean isEnumX = (xClass.equals(String.class));
            m_plotPanel.getXAxis().setSpecificities(isIntegerX, isEnumX);
        }
        if ((needsYAxis()) && (m_colY != -1)) {
            Class yClass = m_compareDataInterface.getDataColumnClass(m_colY);
            boolean isIntegerY = ((yClass.equals(Integer.class)) || (yClass.equals(Long.class))  || (yClass.equals(String.class)));
            boolean isEnumY = (yClass.equals(String.class));
            m_plotPanel.getYAxis().setSpecificities(isIntegerY, isEnumY);
        }
    }
    public abstract void update();
    public abstract boolean select(double x, double y, boolean append);
    public abstract boolean select(Path2D.Double path, double minX, double maxX, double minY, double maxY, boolean append);
    

    public abstract ArrayList<ParameterList> getParameters();
    
    public void addMarker(AbstractMarker m) {
        if (m_markersList == null) {
            m_markersList = new ArrayList<>();
        }
        m_markersList.add(m);
    }

    public void addCursor(AbstractCursor c) {
        if (m_cursorList == null) {
            m_cursorList = new ArrayList<>();
        }
        m_cursorList.add(c);
    }
    
    public MoveableInterface getOverMovable(int x, int y) {
        if (m_markersList != null) {
            int nb = m_markersList.size();
            for (int i = 0; i < nb; i++) {
                AbstractMarker m = m_markersList.get(i);
                if (m instanceof MoveableInterface) {
                    MoveableInterface movable = (MoveableInterface) m;
                    if (movable.isMoveable() && movable.inside(x, y)) {
                        return movable;
                    }
                }
            }
        }
        if (m_cursorList != null) {
            int nb = m_cursorList.size();
            for (int i = 0; i < nb; i++) {
                AbstractCursor c = m_cursorList.get(i);
                    if (c.isMoveable() && c.inside(x, y)) {
                        return c;
                    }
            }
        }
  
        return null;
    } 
    
    /**
     * remove the specified marker, returns true if the marker was in the list, false otherwise
     * @param m
     * @return 
     */
    public boolean removeMarker(AbstractMarker m) {
        if (m_markersList != null) {
            return m_markersList.remove(m);
        }
        return false;
    }
    
    public void clearMarkers() {
        if (m_markersList == null) {
            return;
        }
        m_markersList.clear();
    }
    
    public void paintMarkers(Graphics2D g) {
        if (!m_isPaintMarker) {
            return;
        }
        
        if (m_markersList == null) {
            return;
        }
        
        int nb = m_markersList.size();
        for (int i=0;i<nb;i++) {
            m_markersList.get(i).paint(g);
        }
    }
    
    public void paintCursors(Graphics2D g) {

        if (m_cursorList == null) {
            return;
        }

        int nb = m_cursorList.size();
        for (int i = 0; i < nb; i++) {
            m_cursorList.get(i).paint(g);
        }
    }
    
    public boolean needsDoubleBuffering() {
        return false;
    }
    
    public boolean inside(int x, int y) {
        
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        int x1 = xAxis.valueToPixel(xAxis.getMinValue());
        int x2 = xAxis.valueToPixel(xAxis.getMaxValue());
        int y1 = yAxis.valueToPixel(yAxis.getMaxValue());
        int y2 = yAxis.valueToPixel(yAxis.getMinValue());
        return (x>=x1) && (x<=x2) && (y>=y1) && (y<=y2);
    }
    
    /**
     * Return true when the Plot Area needs to be repainted
     * @param isPaintMarker
     * @return 
     */
    public boolean setIsPaintMarker(boolean isPaintMarker) {
        return false;
    }
    
    public abstract boolean isMouseOnPlot(double x, double y);
    public abstract boolean isMouseOnSelectedPlot(double x, double y);

    public JPopupMenu getPopupMenu(double x, double y) {
        return null;
    }
}
