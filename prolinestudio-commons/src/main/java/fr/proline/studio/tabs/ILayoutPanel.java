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
