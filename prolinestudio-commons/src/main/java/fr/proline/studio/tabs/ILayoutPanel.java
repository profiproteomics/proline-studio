package fr.proline.studio.tabs;

import java.awt.Component;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 *
 * @author MB243701
 */
public interface ILayoutPanel {
    
    /***
     * return the panel component to be displayed
     * @return 
     */
    public JPanel getComponent();
    
    /**
     * return the selected IWrappedPanel
     * @return 
     */
    public IWrappedPanel getSelectedPanel();
    
    /**
     * set a list of panels and display them
     * @param panels
     * @param nbCols 
     */
    public void setPanels(List<IWrappedPanel> panels, Integer nbCols);
    
    public void setSelectedPanel(IWrappedPanel panel);
    
    public void setTabHeaderComponentAt(int index, Component c);
    
    public int indexOfTabHeaderComponent(Component c);
    
    public void addPanel(IWrappedPanel panel);
    
    public void removePanel(int id);
    
    public void  addChangeListener(ChangeListener l);
    
}
