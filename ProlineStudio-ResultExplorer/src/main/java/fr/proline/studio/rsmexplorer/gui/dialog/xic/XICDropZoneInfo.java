/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.gui.dialog.xic.AssociationWrapper.AssociationType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
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

    private final JList m_list;
    private final DefaultListModel m_model;
    private final JLabel m_label;
    private int m_associatedCounter, m_notAssociatedCounter;

    public XICDropZoneInfo() {

        this.setLayout(new BorderLayout());

        m_model = new DefaultListModel();

        m_list = new JList(m_model);

        m_list.setCellRenderer(new DefaultListCellRenderer() {

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

        JScrollPane listPane = new JScrollPane(m_list);

        this.add(listPane, BorderLayout.CENTER);

        m_label = new JLabel();
        m_label.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));
        this.add(m_label, BorderLayout.SOUTH);
        
        clearInfo();

    }

    public void updateInfo(ArrayList<AssociationWrapper> associations) {
        clearInfo();

        for (int i = 0; i < associations.size(); i++) {
            m_model.addElement(associations.get(i));
            if (associations.get(i).getAssociationType() == AssociationWrapper.AssociationType.ASSOCIATED) {
                m_associatedCounter++;
            } else {
                m_notAssociatedCounter++;
            }
        }
        
        m_label.setText(String.valueOf(m_model.size())+" files dropped. "+m_associatedCounter+" files were associated.");
    }

    public void clearInfo() {
        m_model.removeAllElements();
        m_associatedCounter = 0;
        m_notAssociatedCounter = 0;
        m_label.setText("0 files dropped.");
    }

}
