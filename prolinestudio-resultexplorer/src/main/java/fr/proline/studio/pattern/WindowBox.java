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
package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.utils.IconManager;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.ImageIcon;
import javax.swing.JPanel;


/**
 * A window box contains a set of Databox and can be display by a DataBoxViewerTopComponent
 * @author JM235353
 */
public class WindowBox {
    
    
    private final String m_name;
    private final SplittedPanelContainer m_windowPanel;
    private final AbstractDataBox m_entryBox;

    public WindowBox(String name, SplittedPanelContainer windowPanel, AbstractDataBox entryBox, Image icon) {
        m_name = name;
        m_windowPanel = windowPanel;
        m_entryBox = entryBox;
        if (icon == null) {
            switch (entryBox.getStyle()) {
                case STYLE_RSET:
                    icon = IconManager.getImage(IconManager.IconType.DATASET_RSET);
                    break;
                case STYLE_RSM:
                    icon = IconManager.getImage(IconManager.IconType.DATASET_RSM);
                    break;
                case STYLE_XIC:
                    icon = IconManager.getImage(IconManager.IconType.QUANT_XIC);
                    break;    
                case STYLE_SC:
                    icon = IconManager.getImage(IconManager.IconType.QUANT_SC);
                    break;    
            }
        }
        
        m_entryBox.setIcon(icon); 
        
    }
  
    public String getName(){
        return m_name;
    }
    
    public Image getIcon() {
        return m_entryBox.getIcon();
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
    
    public void selectDataWhenLoaded(HashSet data) {
        m_entryBox.selectDataWhenLoaded(data);
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
    
    public void retrieveTableModels(ArrayList<TableInfo> list) {

        m_entryBox.retrieveTableModels(list);
        if (!list.isEmpty()) {
            list.get(0).setIcon(new ImageIcon(getIcon()));
        }
    }

    public void addDatabox(AbstractDataBox nextDatabox, SplittedPanelContainer.PanelLayout layout) {

        AbstractDataBox curBox = m_entryBox;


        while (true) {
            ArrayList<AbstractDataBox> nextArray = curBox.getNextDataBoxArray();
            if ((nextArray != null) && (!nextArray.isEmpty())) {
                curBox = (AbstractDataBox) nextArray.get(0);
            } else {
                break;
            }
        }

        if (!curBox.equals(nextDatabox)) {
            curBox.addNextDataBox(nextDatabox);
        }
        nextDatabox.createPanel();

        // add the new panel to the window (below, as a tabb, or as splitted
        if (layout == SplittedPanelContainer.PanelLayout.VERTICAL) {
            m_windowPanel.registerAddedPanel((JPanel) nextDatabox.getPanel());
        } else if (layout == SplittedPanelContainer.PanelLayout.TABBED) {
            m_windowPanel.registerAddedPanelAsTab((JPanel) nextDatabox.getPanel());
        } else {
            // splitted
            m_windowPanel.registerAddedPanelAsSplitted((JPanel) nextDatabox.getPanel());
        }

        nextDatabox.dataChanged();
    }
}
