/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
