package fr.proline.studio.tabs;

import java.awt.Component;
import javax.swing.JPanel;


/**
 * Contains the methods needed for the panel display
 * @author MB243701
 */
public interface IWrappedPanel {
    
    /**
     * return the title to be displayed
     * @return 
     */
    public String getTitle();
    
    /**
     * returns the component to be displayed
     * @return 
     */
    public JPanel getComponent();
    
    public Long getId();
    
    public void setTabHeaderComponent(Component c);
    
    public Component getTabHeaderComponent();
}
