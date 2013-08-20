package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;
import java.awt.Image;
import javax.swing.JPanel;


/**
 * A window box contains a set of Databox and can be display by a DataBoxViewerTopComponent
 * @author JM235353
 */
public class WindowBox {
    
    
    private String m_name;
    private SplittedPanelContainer m_windowPanel;
    private AbstractDataBox m_entryBox;
    private Image m_icon;
    
    public WindowBox(String name, SplittedPanelContainer windowPanel, AbstractDataBox entryBox, Image icon) {
        m_name = name;
        m_windowPanel = windowPanel;
        m_entryBox = entryBox;
        m_icon = icon;
    }
  
    public String getName() {
        return m_name;
    }
    
    public Image getIcon() {
        return m_icon;
    }
    
    public JPanel getPanel() {
        return m_windowPanel;
    }
    
    public AbstractDataBox getEntryBox() {
        return m_entryBox;
    }
    
    public void setEntryData(long projectId, Object data) {
        m_entryBox.setProjectId(projectId);
        m_entryBox.setEntryData(data);
    }
    
    public void resetDefaultSize() {
        m_windowPanel.resetDefaultSize();
    }

    public void windowClosed() {
        m_entryBox.windowClosed();
    }
    
    public void windowOpened() {
        m_entryBox.windowOpened();
    }
    


    
}
