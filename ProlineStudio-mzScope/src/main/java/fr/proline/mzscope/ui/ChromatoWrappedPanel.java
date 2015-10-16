/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.utils.ButtonTabComponent;
import fr.proline.studio.tabs.IWrappedPanel;
import java.awt.Component;
import javax.swing.JPanel;

/**
 *
 * @author MB243701
 */
public class ChromatoWrappedPanel implements IWrappedPanel{
    
    private ChromatogramPanel chromatogramPanel;
    private String title;
    private Long id;
    private ButtonTabComponent tabHeaderComponent;

    public ChromatoWrappedPanel(Long id, String title, ChromatogramPanel chromatogramPanel) {
        this.id = id;
        this.chromatogramPanel = chromatogramPanel;
        this.title= title;
    }
    
    

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public JPanel getComponent() {
        return this.chromatogramPanel;
    }

    @Override
    public Long getId() {
        return this.id;
    }
    
    @Override
    public void setTabHeaderComponent(Component c){
        if (c instanceof ButtonTabComponent)
            this.tabHeaderComponent = (ButtonTabComponent)c;
    }
    
    @Override
    public Component getTabHeaderComponent(){
        return tabHeaderComponent;
    }
    
}
