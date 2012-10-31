/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class WindowBox {
    
    
    private String name;
    private SplittedPanelContainer windowPanel;
    private AbstractDataBox entryBox;
    
    public WindowBox(String name, SplittedPanelContainer windowPanel, AbstractDataBox entryBox) {
        this.name = name;
        this.windowPanel = windowPanel;
        this.entryBox = entryBox;
    }
    
    public String getName() {
        return name;
    }
    
    public JPanel getPanel() {
        return windowPanel;
    }
    
    public AbstractDataBox getEntryBox() {
        return entryBox;
    }
    
    public void setEntryData(Object data) {
        entryBox.setEntryData(data);
    }
    
    public void resetDefaultSize() {
        windowPanel.resetDefaultSize();
    }
    
}
