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
package fr.proline.studio.msfiles;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author AK249877
 */
public class AddWorkingSetDialog extends DefaultDialog {

    private static AddWorkingSetDialog m_singleton = null;
    private JTextField m_name, m_description;
    private JPanel m_paramPanel;
    private static WorkingSetRoot m_root;

    public static AddWorkingSetDialog getDialog(Window parent, WorkingSetRoot root) {
        m_root = root;
        if (m_singleton == null) {
            m_singleton = new AddWorkingSetDialog(parent);
        }
        m_singleton.m_name.setText(null);
        m_singleton.m_description.setText(null);
        
        return m_singleton;
    }

    private AddWorkingSetDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Add a working set");
        setSize(new Dimension(480, 320));
        setInternalComponent(initParamPanel());
    }

    private JPanel initParamPanel() {
        m_paramPanel = new JPanel();
        m_paramPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel nameLabel = new JLabel("Name : ");
        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        m_name = new JTextField();   
              
        JLabel descriptionLabel = new JLabel("Description : ");
        descriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        m_description = new JTextField();     

        c.gridy = 0;

        c.weightx = 0;
        c.gridx = 0;
        m_paramPanel.add(nameLabel, c);
        c.weightx = 1;
        c.gridx = 1;
        m_paramPanel.add(m_name, c);

        c.gridy++;

        c.weightx = 0;
        c.gridx = 0;
        m_paramPanel.add(descriptionLabel, c);
        c.weightx = 1;
        c.gridx = 1;
        m_paramPanel.add(m_description, c);

        m_paramPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder("Parameters")));

        return m_paramPanel;
    }
    
    @Override
    protected boolean okCalled() {

        if (!validateParameters()) {
            return false;
        }else{      
            return m_root.addWorkingset(m_name.getText(), m_description.getText());         
        }
    }
    
    private boolean validateParameters(){
        return true;
    }

}
