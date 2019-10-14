/* 
 * Copyright (C) 2019 VD225637
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 *
 * @author MB243701
 */
public class GridPanel extends JPanel{
    private IWrappedPanel m_wrappedPanel;
    
    private JTabbedPane m_tabbedPane;
    private PanelAsGrid m_mainPanel;
    
    private boolean m_isSelected;
    public final static Color selectedColor = Color.white;
    public final static Color defaultColor = new Color(240, 240, 240);
    
    public GridPanel(PanelAsGrid mainPanel, IWrappedPanel wrappedPanel) {
        super();
        m_wrappedPanel = wrappedPanel;
        this.m_mainPanel = mainPanel;
        initComponents();
    }
    
    private void initComponents(){
        setLayout(new BorderLayout());
        m_tabbedPane = new JTabbedPane();
        m_tabbedPane.addTab(m_wrappedPanel.getTitle(), m_wrappedPanel.getComponent());
        m_tabbedPane.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
                m_mainPanel.changeSelectionPanel();
                setSelected(true);
            }

            @Override
            public void mousePressed(MouseEvent me) {
                
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                
            }

            @Override
            public void mouseEntered(MouseEvent me) {
                
            }

            @Override
            public void mouseExited(MouseEvent me) {
                
            }
        });
        this.add(m_tabbedPane, BorderLayout.CENTER);
        setSelected(false);
    }
    
    public boolean isSelected(){
        return m_isSelected;
    }
    
    public void setSelected(boolean isSelected){
        m_isSelected = isSelected;
        if (m_isSelected){
            setTabbedPaneBorderColor(selectedColor);
        }else{
            setTabbedPaneBorderColor(defaultColor);
        }
    }
    
    public void setTabbedPaneBorderColor(Color tabBorderColor) {

        UIManager.put("TabbedPane.selected", tabBorderColor);


        m_tabbedPane.setUI(new BasicTabbedPaneUI() {
               @Override
               protected void installDefaults() {
                   super.installDefaults();
                   highlight = UIManager.getColor("TabbedPane.light");
                   lightHighlight = UIManager.getColor("TabbedPane.highlight");
                   shadow = UIManager.getColor("TabbedPane.shadow");
                   darkShadow =UIManager.getColor("TabbedPane.darkShadow");
                   focus = UIManager.getColor("TabbedPane.focus");
               }
            });
    }
    
    public void setTabHeaderComponent(Component c){
        m_tabbedPane.setTabComponentAt(0, c);
    }
    
    public int indexOfTabHeaderComponent(Component c){
        return m_tabbedPane.indexOfTabComponent(c);
    }

}
