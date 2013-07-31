package fr.proline.studio.pattern;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.SplittedPanelContainer;
import java.awt.event.ActionListener;
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
public abstract class AbstractDataBox implements ChangeListener, SplittedPanelContainer.UserActions {
   
    public enum DataBoxLayout {
      VERTICAL,
      HORIZONTAL,
      TABBED
    };
    
    // Panel corresponding to this box
    protected DataBoxPanelInterface m_panel;
    
    // In and out Parameters Registered
    private HashSet<GroupParameter> m_inParameters = new HashSet<>();
    private ArrayList<GroupParameter> m_outParameters = new ArrayList<>();
    
    
    private long m_projectId = -1;
    
    protected String m_name;
    protected String m_description = "";
    
    private DataBoxLayout m_layout = DataBoxLayout.VERTICAL;
    
    protected AbstractDataBox m_nextDataBox = null;
    protected AbstractDataBox m_previousDataBox = null;
    
    private int m_loadingId = 0;
    
    protected void registerInParameter(GroupParameter parameter) {
        m_inParameters.add(parameter);
    }
    
    protected void registerOutParameter(GroupParameter parameter)  {
        m_outParameters.add(parameter);
    }
    
    public ArrayList<GroupParameter> getOutParameters() {
        return m_outParameters;
    }
    
    public HashSet<GroupParameter> getInParameters() {
        return m_inParameters;
    }
    
    public boolean isDataDependant(Class dataType) {
        Iterator<GroupParameter> it = m_inParameters.iterator();
        while (it.hasNext()) {
            GroupParameter parameter = it.next();
            if (parameter.isDataDependant(dataType)) {
                return true;
            }
        }
        return false;
    }
  
    
    public double calculateParameterCompatibilityDistance(ArrayList<GroupParameter> outParameters) {
        Iterator<GroupParameter> it = m_inParameters.iterator();
        while (it.hasNext()) {
            GroupParameter parameter = it.next();
            
            if (parameter.isCompatibleWithOutParameter(outParameters)) {
                return 0;
            }
        }
        return -1;
    }
    
    
    public double calculateParameterCompatibilityDistance(AvailableParameters avalaibleParameters, AbstractDataBox nextDataBox) {
        
        
        return avalaibleParameters.calculateParameterCompatibilityDistance(nextDataBox);

    }
    
    public void setNextDataBox(AbstractDataBox nextDataBox) {
        m_nextDataBox = nextDataBox;
        if (nextDataBox != null) {
            nextDataBox.m_previousDataBox = this;
        }
    }
    
    public abstract void createPanel();
    
    public abstract void dataChanged();
    
    public Object getData(boolean getArray, Class parameterType) {
        if (m_previousDataBox != null) {
            return m_previousDataBox.getData(getArray, parameterType);
        }
        return null;
    }
    
    public void setEntryData(Object data) {
        throw new UnsupportedOperationException();
    }
    
    public void propagateDataChanged(Class dataType) {
        if (m_nextDataBox != null) {
            if (m_nextDataBox.isDataDependant(dataType)) {
                m_nextDataBox.dataChanged();
            }
            m_nextDataBox.propagateDataChanged(dataType);
        }
        
    }
    
    public void setProjectId(long projectId) {
        m_projectId = projectId;
    }
    
    public long getProjectId() {
        if (m_projectId!=-1) {
            return m_projectId;
        }
        if (m_previousDataBox != null) {
            return m_previousDataBox.getProjectId();
        }
        return -1; // should not happen
        
    }
    
    public DataBoxPanelInterface getPanel() {
        return m_panel;
    }
    
    public void setLayout(DataBoxLayout layout) {
        m_layout = layout;
    }
    
    public DataBoxLayout getLayout() {
        return m_layout;
    }
    
    public String getName() {
        return m_name;
    }
    
    public String getDescription() {
        return m_description;
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
    
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return new RemoveDataBoxActionListener(splittedPanel, this);
    }
        
    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return new AddDataBoxActionListener(splittedPanel, this);
    }
    


}
