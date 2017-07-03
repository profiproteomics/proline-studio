package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.table.GlobalTableModelInterface;


/**
 * Parent for all objects in a graph (nodes, connectors, links)
 * @author JM235353
 */
public abstract class AbstractConnectedGraphObject extends AbstractGraphObject {

    protected GraphGroup m_group = null;
    
    public AbstractConnectedGraphObject(TypeGraphObject type) {
        super(type);
    }
    
    public void setGroup(GraphGroup group) {
        m_group = group;
    }
    
    public GraphGroup getGroup() {
        return m_group;
    }
    
    public abstract boolean isConnected(boolean recursive);
    public abstract boolean canSetSettings();
    public abstract boolean settingsDone();
    public abstract boolean calculationDone();

    public abstract String getPreviousDataName();
    public abstract String getDataName();
    public abstract String getTypeName();
    public abstract String getFullName();
  
    public abstract GlobalTableModelInterface getGlobalTableModelInterface(int index);
    
    
    public abstract void deleteAction();
}
