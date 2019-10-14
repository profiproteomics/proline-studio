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
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

/**
 *
 * @author MB243701
 */
public class PanelAsTab extends JPanel implements ILayoutPanel{

    // list of panels managed
    private List<IWrappedPanel> m_panels;
    
    private JTabbedPane m_tabbedPane;
    
    public PanelAsTab() {
        initComponents();
    }
    
    private void initComponents(){
        this.setLayout(new BorderLayout());
        m_panels = new ArrayList();
        m_tabbedPane = new JTabbedPane();
        this.add(m_tabbedPane);
    }
    
    @Override
    public JPanel getComponent(){
        return this;
    }
    
    @Override
    public IWrappedPanel getSelectedPanel() {
        int id =m_tabbedPane.getSelectedIndex();
        if (id != -1 && id< m_panels.size()){
            return m_panels.get(id);
        }else{
            return null;
        }
    }

    @Override
    public void setPanels(List<IWrappedPanel> panels, Integer nbCols) {
        this.m_panels = panels;
        displayPanels();
    }
    
    @Override
    public void setSelectedPanel(IWrappedPanel panel){
        int id = m_panels.indexOf(panel);
        if (id != -1){
            m_tabbedPane.setSelectedIndex(id);
        }
    }

    private void displayPanels() {
        this.removeAll();
        m_tabbedPane = new JTabbedPane();
        int id=0;
        for(IWrappedPanel panel: m_panels){
            m_tabbedPane.add(panel.getTitle(), panel.getComponent());
            if (panel.getTabHeaderComponent() != null){
                setTabHeaderComponentAt(id++, panel.getTabHeaderComponent());
            }
        }
        this.add(m_tabbedPane);
    }
    
    @Override
    public void setTabHeaderComponentAt(int index, Component c){
        m_tabbedPane.setTabComponentAt(index, c);
    }
    
    @Override
    public int indexOfTabHeaderComponent(Component c){
        return m_tabbedPane.indexOfTabComponent(c);
    }
    
    
    @Override
    public void addPanel(IWrappedPanel panel){
        m_panels.add(panel);
        m_tabbedPane.addTab(panel.getTitle(), panel.getComponent());
        if (panel.getTabHeaderComponent() != null){
            setTabHeaderComponentAt(m_panels.size()-1, panel.getTabHeaderComponent());
        }
    }
    
    @Override
    public void removePanel(int id){
        m_tabbedPane.remove(id);
    }
    
    @Override
    public void  addChangeListener(ChangeListener l){
        m_tabbedPane.addChangeListener(l);
    }
    

}
