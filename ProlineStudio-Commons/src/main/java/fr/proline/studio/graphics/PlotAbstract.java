package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.LockedDataModel;
import fr.proline.studio.graphics.marker.AbstractMarker;
import fr.proline.studio.parameter.ParameterList;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;

/**
 * Base class for all types of plot
 * @author JM235353
 */
public abstract class PlotAbstract {
    
    protected PlotType m_plotType;
    
    protected CompareDataInterface m_compareDataInterface = null;
    protected CrossSelectionInterface m_crossSelectionInterface = null;
    protected int m_colX;
    protected int m_colY;
    protected String m_parameterZ;
        
    protected PlotPanel m_plotPanel;
    
    private ArrayList<AbstractMarker> m_markersList = null;

    protected boolean m_locked = false;
    
    public abstract double getXMin();
    public abstract double getXMax();
    public abstract double getYMin();
    public abstract double getYMax();

    public abstract boolean needsXAxis();
    public abstract boolean needsYAxis();

    public abstract ArrayList<Integer> getSelection();
    public abstract void setSelection(ArrayList<Integer> selection);
    
    public abstract void parametersChanged();
    
    public abstract void paint(Graphics2D g);

    public PlotAbstract(PlotPanel plotPanel, PlotType plotType, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface) {
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
        update();
        updateAxisInteger();
    }
    public void updateAxisInteger() {
        if ((needsXAxis()) && (m_colX != -1)) {
            Class xClass = m_compareDataInterface.getDataColumnClass(m_colX);
            boolean isIntegerX = ((xClass.equals(Integer.class)) || (xClass.equals(Long.class)));
            m_plotPanel.getXAxis().setIsInteger(isIntegerX);
        }
        if ((needsYAxis()) && (m_colY != -1)) {
            Class yClass = m_compareDataInterface.getDataColumnClass(m_colY);
            boolean isIntegerY = ((yClass.equals(Integer.class)) || (yClass.equals(Long.class)));
            m_plotPanel.getYAxis().setIsInteger(isIntegerY);
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
    
    public void clearMarkers() {
        if (m_markersList == null) {
            return;
        }
        m_markersList.clear();
    }
    
    public void paintMarkers(Graphics2D g) {
        if (m_markersList == null) {
            return;
        }
        for (int i=0;i<m_markersList.size();i++) {
            m_markersList.get(i).paint(g);
        }
    }
    
    public boolean needsDoubleBuffering() {
        return false;
    }
    
    public boolean inside(int x, int y) {
        
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        int x1 = xAxis.valueToPixel(xAxis.getMinTick());
        int x2 = xAxis.valueToPixel(xAxis.getMaxTick());
        int y1 = yAxis.valueToPixel(yAxis.getMaxTick());
        int y2 = yAxis.valueToPixel(yAxis.getMinTick());
        return (x>=x1) && (x<=x2) && (y>=y1) && (y<=y2);
    }
    
    public void setIsPaintMarker(boolean isPaintMarker) {
        
    }
    
    public abstract boolean isMouseOnPlot(double x, double y);
    

}
