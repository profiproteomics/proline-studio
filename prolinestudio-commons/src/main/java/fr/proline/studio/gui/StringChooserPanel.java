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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Panel to choose a string among a list presented with radio buttons
 * or let the user type a cursom one in a textfield.
 * 
 * @author JM235353
 */
public class StringChooserPanel extends JPanel {
    
    private final ArrayList<JRadioButton> m_radioButtonList = new ArrayList<>();
    private String[] m_possibilities = null;
    private JTextField m_userTextField = null;

    private ActionListener m_actionListener = null;
    
    public StringChooserPanel(String[] possibilitiesName, String[] possibilities, int width, String defaultValue, boolean useUserTextField) {
        super(new GridBagLayout());
        
        m_possibilities = possibilities;
        
        ButtonGroup buttonGroup = new ButtonGroup();
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridwidth = 2;
        
        ActionListener radioButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_userTextField != null) {
                    m_userTextField.setEnabled(false);
                }
                if (m_actionListener != null) {
                    m_actionListener.actionPerformed(null);
                }
            }
            
        };
        
        int nbChoices = possibilitiesName.length;
        for (int i=0;i<nbChoices;i++) {
            JRadioButton radioButton = new JRadioButton(possibilitiesName[i]);
            radioButton.addActionListener(radioButtonAction);
            c.gridx = (i % width)*2;
            c.gridy = i / width;
            m_radioButtonList.add(radioButton);
            add(radioButton, c);
            buttonGroup.add(radioButton);
        }
        
        if (useUserTextField) {

            JRadioButton radioButton = new JRadioButton("Other");
            radioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    m_userTextField.setEnabled(true);
                    m_userTextField.requestFocus();
                    if (m_actionListener != null) {
                        m_actionListener.actionPerformed(null);
                    }
                }

            });
            c.gridx = (nbChoices % width)*2;
            c.gridy = nbChoices / width;
            if (c.gridx>0) {
                c.gridx = 0;
                c.gridy++;
            }
            c.gridwidth = 1;
            add(radioButton, c);
            m_radioButtonList.add(radioButton);
            buttonGroup.add(radioButton);
            
            MouseListener actionOnTextFieldClick = new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    radioButton.setSelected(true);
                    m_userTextField.setEnabled(true);
                    m_userTextField.requestFocusInWindow();
                    if (m_actionListener != null) {
                        m_actionListener.actionPerformed(null);
                    }
                }
            };
            
            m_userTextField = new JTextField();
            m_userTextField.addMouseListener(actionOnTextFieldClick);
            m_userTextField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    textChanged();
                }

                public void removeUpdate(DocumentEvent e) {
                    textChanged();
                }

                public void insertUpdate(DocumentEvent e) {
                    textChanged();
                }

                public void textChanged() {
                    if (m_actionListener != null) {
                        m_actionListener.actionPerformed(null);
                    }
                }
            });
            m_userTextField.setEnabled(false);
            c.gridx++;
            c.weightx = 1;
            c.gridwidth = width*2-1;
            add(m_userTextField, c);
        }
        
        setText(defaultValue);
    }
    
    public String getText() {
        int nbChoices = m_radioButtonList.size();
        for (int i=0;i<nbChoices;i++) {
            if (m_radioButtonList.get(i).isSelected()) {
                if ((i == nbChoices-1) && (m_userTextField != null)) {
                    return m_userTextField.getText();
                }
                return m_possibilities[i];
            }
        }
        
        return ""; // should never happen
    }
    
    public void setText(String t) {
        if (t == null) {
            t = "";
        }
        
        int nbChoices = m_possibilities.length;
        for (int i = 0; i < nbChoices; i++) {
            String curText = m_possibilities[i];
            if (curText.compareTo(t) == 0) {
                m_radioButtonList.get(i).setSelected(true);
                return;
            }
        }
        if (m_userTextField != null) {
            m_radioButtonList.get(m_radioButtonList.size()-1).setSelected(true);
            m_userTextField.setText(t);
        }
    }
    
    public void setActionListener(ActionListener a) {
        m_actionListener = a;
    }
}
