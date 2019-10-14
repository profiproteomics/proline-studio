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
package fr.proline.studio.markerbar;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog to set the name of an annotation
 * @author jm235353
 */
public class AnnotationMarkerDialog extends DefaultDialog {
 
    private static AnnotationMarkerDialog m_singletonDialog = null;
    
    private JTextField m_descriptionTextfield = null;
    
    public static AnnotationMarkerDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AnnotationMarkerDialog(parent);
        }

        m_singletonDialog.reinit(); 
        
        return m_singletonDialog;
    }


    
    private AnnotationMarkerDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        
        setTitle("Add Annotation");

        setButtonVisible(BUTTON_HELP, false);

        initInternalPanel();


    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(getRenamePanel(), c);


        setInternalComponent(internalPanel);
    }
    

    
    private JPanel getRenamePanel() {
        JPanel renamePanel = new JPanel();
        renamePanel.setLayout(new GridBagLayout());
        renamePanel.setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel renameLabel = new JLabel("Description :");
        m_descriptionTextfield = new JTextField(30);

        c.gridx = 0;
        c.gridy = 0;
        renamePanel.add(renameLabel, c);


        c.gridx++;
        c.weightx = 1.0;
        renamePanel.add(m_descriptionTextfield, c);

        return renamePanel;
    }
    
    private void reinit() {
        m_descriptionTextfield.setText("");
    }
    
    @Override
    protected boolean okCalled() {
        
        String name = m_descriptionTextfield.getText();
       
        if (name.isEmpty()) {
            setStatus(true, "You must fill the description.");
            highlight(m_descriptionTextfield);
            return false;
        }

        return true;
    }
    
    
    public void setDescriptionField(String name) {
        m_descriptionTextfield.setText(name);
    }
    
    public String getDescriptionField() {
        return m_descriptionTextfield.getText();
    }

}
