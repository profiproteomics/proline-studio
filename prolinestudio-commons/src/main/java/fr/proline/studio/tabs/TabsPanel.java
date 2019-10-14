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
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * panel to display multi tabs: either in a JTabbedPane or in a grid
 * @author MB243701
 */
public class TabsPanel extends JPanel {
    private final static int VIEW_TAB = 0;
    private final static int VIEW_GRID = 1;
    
    
    //indicates which display mode
    private int m_displayMode;
    
    // list of panels managed
    private List<IWrappedPanel> m_panels;
    
    private ILayoutPanel m_mainPanel;
    
    
    public TabsPanel() {
        super();
        this.m_panels = new ArrayList();
        this.m_displayMode= VIEW_GRID;
        initComponents();
    }
    
    private void initComponents(){
        setLayout(new BorderLayout());
        switch (m_displayMode){
            case VIEW_TAB : {
                m_mainPanel = new PanelAsTab();
                break;
            }
            case VIEW_GRID: {
                m_mainPanel = new PanelAsGrid();
                break;
            }
            default:{
                // should not happen
            }
        }
        this.add(m_mainPanel.getComponent(), BorderLayout.CENTER);
    }
    
    
    
    /**
     * set a list of panels to be displayed, a nbCols could be given to set the nbCols, otherwise (null) it would be the default value
     * @param panels 
     * @param nbCols 
     */
    public void setPanels(List<IWrappedPanel> panels, Integer nbCols){
        this.m_panels = panels;
        if (m_mainPanel != null){
            this.remove(this.m_mainPanel.getComponent());
        }
        switch (m_displayMode){
            case VIEW_TAB : {
                m_mainPanel = new PanelAsTab();
                break;
            }
            case VIEW_GRID: {
                m_mainPanel = new PanelAsGrid();
                break;
            }
            default:{
                // should not happen
            }
        }
        m_mainPanel.setPanels(panels, nbCols);
        this.add(m_mainPanel.getComponent());
        this.revalidate();
        this.repaint();
    }
    
    public void addTab(IWrappedPanel panel){
        this.m_panels.add(panel);
        m_mainPanel.addPanel(panel);
        revalidate();
        repaint();
    }
    
    public void removeTabAt(int i){
        m_mainPanel.removePanel(i);
        m_panels.remove(i);
    }
    
    
    
    public IWrappedPanel getSelectedPanel(){
        return m_mainPanel == null ? null : m_mainPanel.getSelectedPanel();
    }
    
    
    public  void changeLayout(){
        IWrappedPanel oldSelectedPanel = null;
        if (m_mainPanel != null){
            oldSelectedPanel= m_mainPanel.getSelectedPanel();
        }
        switch (m_displayMode){
            case VIEW_TAB : {
                m_displayMode = VIEW_GRID;
                break;
            }
            case VIEW_GRID: {
                m_displayMode = VIEW_TAB;
                break;
            }
            default:{
                // should not happen
                m_displayMode = VIEW_TAB;
            }
        }
        setPanels(m_panels, null);
        if (oldSelectedPanel != null){
            m_mainPanel.setSelectedPanel(oldSelectedPanel);
        }
    }
    
    public int getTabCount(){
        return this.m_panels.size();
    }
    
    public String getTitleAt(int index){
        if (index > -1 && index < this.getTabCount()){
            return this.m_panels.get(index).getTitle();
        }
        return null;
    }
    
    public JPanel getComponentAt(int index){
        if (index > -1 && index < this.getTabCount()){
            return this.m_panels.get(index).getComponent();
        }
        return null;
    }
    
    public void setSelectedIndex(int index){
        if (index > -1 && index < this.getTabCount()){
            m_mainPanel.setSelectedPanel(this.m_panels.get(index));
        }
    }
    
    public int getSelectedIndex(){
        IWrappedPanel p = m_mainPanel.getSelectedPanel();
        if (p != null){
            return m_panels.indexOf(p);
        }
        return -1;
    }
    
    public void setTabHeaderComponentAt(int index, Component c){
        m_mainPanel.setTabHeaderComponentAt(index, c);
    }
    
    public int indexOfTabHeaderComponent(Component c){
        return m_mainPanel.indexOfTabHeaderComponent(c);
    }
    
    public void  addChangeListener(ChangeListener l){
        m_mainPanel.addChangeListener(l);
    }
}
