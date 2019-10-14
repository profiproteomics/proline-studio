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
package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Panel with a list of checkbox to select/unselect multiple objet.
 * There is a button to select/unselect all.
 * 
 * @author JM235353
 */
public class JCheckBoxListPanel extends JPanel {
    
    private JCheckBoxList m_checkBoxList = null;
    private JCheckBox m_selectAllCheckBox = null;
    
    public JCheckBoxListPanel(JCheckBoxList checkBoxList, boolean showSelectAll) {
        setLayout(new GridBagLayout());
        setBackground(Color.white);
        
        m_checkBoxList = checkBoxList;
        
        if (showSelectAll) {
            m_selectAllCheckBox = new JCheckBox("Select / Unselect All", IconManager.getIcon(IconManager.IconType.SELECTED_CHECKBOXES));
            m_selectAllCheckBox.setBackground(Color.white);
            m_selectAllCheckBox.setFocusPainted(false);

            m_selectAllCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (m_selectAllCheckBox.isSelected()) {
                        m_checkBoxList.selectAll();
                    } else {
                        m_checkBoxList.unselectAll();
                    }
                    repaint();
                }

            });
        }
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 0, 5);
        
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        JScrollPane scrollPane = new JScrollPane(m_checkBoxList);
        add(scrollPane, c);
        
        if (showSelectAll) {
            c.gridy++;
            c.weighty = 0;
            add(m_selectAllCheckBox, c);
        }
  
    }
    
    public JCheckBoxList getCheckBoxList() {
        return m_checkBoxList;
    }
}
