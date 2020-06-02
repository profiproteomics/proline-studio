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
package fr.proline.studio.rsmexplorer;

import fr.proline.studio.pattern.DataParameter;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.table.TableInfo;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author VD225637
 */
public class MultiDataBoxViewerTopComponent extends DataBoxViewerTopComponent {

    private WindowBox[] m_windowBoxes = null;

    public MultiDataBoxViewerTopComponent(WindowBox[] windowBoxes, String name) {
        super(windowBoxes[0]);
        m_windowBoxes = windowBoxes;
        removeAll();
        setLayout(new GridLayout());
        add(createPanel());
        setName(name);
    }

    private JPanel createPanel() {
        JPanel internalPanel = new JPanel(new GridLayout());

          JTabbedPane tabbedPane = new JTabbedPane();
        for (WindowBox wBox : m_windowBoxes) {
            ImageIcon boxIcon = null;
            if(wBox.getIcon() != null)
                boxIcon = new ImageIcon(wBox.getIcon());
            tabbedPane.addTab(wBox.getName(), boxIcon, wBox.getPanel(), null);
        }
        
        internalPanel.add(tabbedPane);

        return internalPanel;
    }

    @Override
    public void retrieveTableModels(ArrayList<TableInfo> list) {
        for (WindowBox wBox : m_windowBoxes) {
            wBox.retrieveTableModels(list);
        }
    }

    @Override
    protected void componentOpened() {
        for (WindowBox wBox : m_windowBoxes) {
            wBox.windowOpened();
        }
    }

    @Override
    protected void componentClosed() {
        for (WindowBox wBox : m_windowBoxes) {
            wBox.windowClosed();
        }
    }

    @Override
    public ParameterList getInParameters(){
        ParameterList inParameters = new ParameterList();
         for (WindowBox wBox : m_windowBoxes) {
             ParameterList windowInParameters = wBox.getEntryBox().getInParameters();
             inParameters.addParameter(windowInParameters);
        }
        return inParameters;
    }
    
    @Override
    public ParameterList getOutParameters(){
        ParameterList outParameters = new ParameterList();
         for (WindowBox wBox : m_windowBoxes) {
             ParameterList windowOutParameters = wBox.getEntryBox().getOutParameters();
             outParameters.addParameter(windowOutParameters);
        }
        return outParameters;    
    }

}
