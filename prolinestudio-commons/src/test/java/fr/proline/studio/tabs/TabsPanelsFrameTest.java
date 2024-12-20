/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.tabs;

import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author MB243701
 */
public class TabsPanelsFrameTest extends JFrame {
    
    private final static int nbPanelTest = 4;
    
    private JToolBar m_toolbarPanel;
    private JButton m_buttonLayout;
    private TabsPanel m_tabsPanel ;
    
    public TabsPanelsFrameTest() {
        super("Tabs Panel demo");
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        this.add(getToolBar(), BorderLayout.NORTH);
        m_tabsPanel = new TabsPanel();
        this.add(m_tabsPanel, BorderLayout.CENTER);
        List<IWrappedPanel> panels = new ArrayList<>();
        for (int i=0; i<nbPanelTest; i++){
            panels.add(new TestPanel(Long.valueOf(i+1)));
        }
        m_tabsPanel.setPanels(panels, null);
        
        
        pack();
        setSize(450, 350);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
    
    private JToolBar getToolBar(){
        if (this.m_toolbarPanel == null){
            m_toolbarPanel = new JToolBar();
            m_toolbarPanel.add(getButtonLayout());
        }
        return this.m_toolbarPanel;
    }
    
    
    private JButton getButtonLayout(){
        if(m_buttonLayout == null){
            m_buttonLayout = new JButton();
            m_buttonLayout.setIcon(IconManager.getIcon(IconManager.IconType.GRID));
            m_buttonLayout.setToolTipText("Change the presentation: tabs/grid");
            m_buttonLayout.addActionListener((ActionEvent e) -> m_tabsPanel.changeLayout());
        }
        return m_buttonLayout;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.getLogger(TabsPanelsFrameTest.class.getName()).log(Level.SEVERE, null, e);
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TabsPanelsFrameTest frameT = new TabsPanelsFrameTest();
                frameT.setVisible(true);
            }
        }); 
    }
}
