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
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.gui.dialog.xic.AssociationWrapper.AssociationType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author AK249877
 */
public class XICDropZoneInfo extends JPanel {

    private final JList m_droppedFilesList;
    private final DefaultListModel m_model;
    private final JLabel m_statusLabel;
    private int m_associatedCounter;

    public XICDropZoneInfo() {

        this.setLayout(new BorderLayout());

        m_model = new DefaultListModel();

        m_droppedFilesList = new JList(m_model);

        m_droppedFilesList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AssociationWrapper) {
                    AssociationWrapper wrapper = (AssociationWrapper) value;
                    setText(wrapper.getPath());
                    if (wrapper.getAssociationType() == AssociationType.ASSOCIATED) {
                        setForeground(Color.GREEN);
                    } else {
                        setForeground(Color.RED);
                    }
                    if (isSelected) {
                        setBackground(getBackground().darker());
                    }
                }
                return c;
            }

        });

        JScrollPane listPane = new JScrollPane(m_droppedFilesList);

        this.add(listPane, BorderLayout.CENTER);

        m_statusLabel = new JLabel();
        m_statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));
        this.add(m_statusLabel, BorderLayout.SOUTH);

    }

    public void updateInfo(HashMap<String, AssociationWrapper> associations) {
        clearInfo();

        for (AssociationWrapper assocWrapper : associations.values()) {
            m_model.addElement(assocWrapper);

            if (assocWrapper.getAssociationType() == AssociationWrapper.AssociationType.ASSOCIATED) {
                m_associatedCounter++;
            } 
        }

        m_statusLabel.setText(String.valueOf(m_model.size()) + " files dropped. " + m_associatedCounter + " files were associated.");
    }

    public void clearInfo() {
        m_model.removeAllElements();
        m_associatedCounter = 0;       
        m_statusLabel.setText("0 files dropped.");
    }

}
