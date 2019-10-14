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
package fr.proline.studio.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * @kx, tabbedPanel in CustomOptions Panel, one title with it's checkBox 
 * @author JM235353
 */
public class CheckboxTabPanel extends JPanel {
   
    private String m_sheetId = null;

    private JCheckBox m_checkBox;
    private JLabel m_label;
    private JTabbedPane ctrlPane;
    /**
     * 
     * @param tabbedPane, the control super panel
     * @param title
     * @param sheetId 
     */
    public CheckboxTabPanel(JTabbedPane tabbedPane, String title, String sheetId) {
        setLayout(new FlowLayout());
          
        setOpaque(false);

        ctrlPane = tabbedPane;

        m_sheetId = sheetId;
        
        m_checkBox = new JCheckBox();
        m_checkBox.setOpaque(false);
        m_label = new JLabel(title);
        
        add(m_checkBox);
        add(m_label);
        
        final CheckboxTabPanel _this = this;
        m_checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSelected = m_checkBox.isSelected();

                //JPM.WART : big wart due to the fact that dnd changes the index of tabs
                int indexCur = -1;
                int nbTabs = ctrlPane.getTabCount();
                for (int i = 0; i < nbTabs; i++) {
                    Component tabComponent = ctrlPane.getTabComponentAt(i);
                    if (tabComponent == _this) {
                        indexCur = i;
                        break;
                    }
                }

                if (indexCur != -1) {
                    ctrlPane.setEnabledAt(indexCur, isSelected);
                }

                if (isSelected) {
                    m_label.setForeground(Color.black);
                } else {
                    m_label.setForeground(Color.lightGray);
                }

            }
        });

    }
    /**
     * set the CheckBox
     * @param isSelected 
     */
    public void setSelected(boolean isSelected) {
        m_checkBox.setSelected(isSelected);
        if (isSelected) {
            m_label.setForeground(Color.black);
        } else {
            m_label.setForeground(Color.lightGray);
        }
    }

    public boolean isSelected() {
        return m_checkBox.isSelected();
    }
    
    /**
     * get Tab title name
     * @return 
     */
    public String getText() {
        return m_label.getText();
    }

    /**
     * set Tab title name
     * @param text 
     */
    public void setText(String text) {
        m_label.setText(text);
    }

    public String getSheetId() {
        return m_sheetId;
    }
    
    public void setSheetId(String sheetId) {
        m_sheetId = sheetId;
    }

}
