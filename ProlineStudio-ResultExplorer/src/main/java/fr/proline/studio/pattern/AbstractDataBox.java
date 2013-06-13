package fr.proline.studio.pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * A Box receive IN-parameters. According to them, it loads data from the database.
 * These data are displayed to the user in an associated Graphical Panel.
 * If the user select data in the panel, the box offers the selected data as OUT-parameters
 * for the next dataBox.
 * 
 * @author JM235353
 */
public abstract class AbstractDataBox implements ChangeListener {
   
    public enum DataBoxLayout {
      VERTICAL,
      HORIZONTAL,
      TABBED
    };
    
    // Panel corresponding to this box
    protected DataBoxPanelInterface m_panel;
    
    // In and out Parameters Registered
    private HashSet<DataParameter> m_inParameters = new HashSet<>();
    private ArrayList<DataParameter> m_outParameters = new ArrayList<>();
    
    
    private long projectId = -1;
    
    protected String name;
    protected String description = "";
    
    private DataBoxLayout layout = DataBoxLayout.VERTICAL;
    
    protected AbstractDataBox nextDataBox = null;
    protected AbstractDataBox previousDataBox = null;
    
    private int m_loadingId = 0;
    
    protected void registerInParameter(DataParameter parameter) {
        m_inParameters.add(parameter);
    }
    
    protected void registerOutParameter(DataParameter parameter)  {
        m_outParameters.add(parameter);
    }
    
    public boolean isDataDependant(Class dataType) {
        Iterator<DataParameter> it = m_inParameters.iterator();
        while (it.hasNext()) {
            DataParameter parameter = it.next();
            if (parameter.isDataDependant(dataType)) {
                return true;
            }
        }
        return false;
    }
  
    
    public boolean isCompatible(ArrayList<DataParameter> outParameters) {
        Iterator<DataParameter> it = m_inParameters.iterator();
        while (it.hasNext()) {
            DataParameter parameter = it.next();
            
            if (parameter.isCompatibleWithOutParameter(outParameters)) {
                return true;
            }
        }
        return false;
    }
    
    
    public boolean isCompatible(AbstractDataBox nextDataBox) {
        
        Iterator<DataParameter> it = nextDataBox.m_inParameters.iterator();
        
        while (it.hasNext()) {
            DataParameter parameter = it.next();
            
            if (parameter.isCompatibleWithOutParameter(m_outParameters)) {
                return true;
            }
        }
        if (previousDataBox != null) {
            return previousDataBox.isCompatible(nextDataBox);
        }
        return false;
    }
    
    public void setNextDataBox(AbstractDataBox nextDataBox) {
        this.nextDataBox = nextDataBox;
        nextDataBox.previousDataBox = this;
    }
    
    public abstract void createPanel();
    
    public abstract void dataChanged(Class dataType);
    
    public Object getData(boolean getArray, Class parameterType) {
        if (previousDataBox != null) {
            return previousDataBox.getData(getArray, parameterType);
        }
        return null;
    }
    
    public void setEntryData(Object data) {
        throw new UnsupportedOperationException();
    }
    
    public void propagateDataChanged(Class dataType) {
        if (nextDataBox != null) {
            if (nextDataBox.isDataDependant(dataType)) {
                nextDataBox.dataChanged(dataType);
            }
            nextDataBox.propagateDataChanged(dataType);
        }
        
    }
    
    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
    
    public long getProjectId() {
        if (projectId!=-1) {
            return projectId;
        }
        if (previousDataBox != null) {
            return previousDataBox.getProjectId();
        }
        return -1; // should not happen
        
    }
    
    public DataBoxPanelInterface getPanel() {
        return m_panel;
    }
    
    public void setLayout(DataBoxLayout layout) {
        this.layout = layout;
    }
    
    public DataBoxLayout getLayout() {
        return layout;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void windowClosed() {
    }
    
    public void windowOpened() {    
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
    }
    
    
    protected int setLoading() {
        final int loadingId = m_loadingId++;
        m_panel.setLoading(loadingId);
        return loadingId;
    }
    
    protected void setLoaded(int loadingId) {
        m_panel.setLoaded(loadingId);
    }

}
