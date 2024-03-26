/* 
 * Copyright (C) 2019
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
package fr.proline.studio.rsmexplorer.gui.dialog.exporter;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import fr.proline.studio.NbPreferences;

/**
 *
 * @author VD225637
 */
public class Export2MzIdentMLFilePanel extends JPanel {

    private DefaultDialog m_parent = null;

    private JTextField m_fileTextField;
    private JFileChooser m_fchooser;
    private boolean m_isFileMode;

    public Export2MzIdentMLFilePanel(DefaultDialog parent) {
        this(parent, true);
    }

    public Export2MzIdentMLFilePanel(DefaultDialog parent, boolean fileMode) {
        m_parent = parent;
        setLayout(new BorderLayout());
        m_isFileMode = fileMode;
        add(createMainPanel(), BorderLayout.CENTER);

    }
    private JPanel createMainPanel() {
        JPanel exportPanel = new JPanel();
        String defaultExportPath;
        Preferences preferences = NbPreferences.root();
        defaultExportPath = preferences.get("DefaultExcelExportPath", System.getProperty("user.home"));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        m_fileTextField = new JTextField(30);
        m_fileTextField.setText(defaultExportPath);
        m_fileTextField.setEditable(false);
        exportPanel.add(m_fileTextField, c);
         
        final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            m_fchooser = new JFileChooser(defaultExportPath);
            if(m_isFileMode){
                m_fchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            } else{
                m_fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }

            m_fchooser.setFileFilter(new FileNameExtensionFilter("MzIdent file", "mzid", "MZID"));
                             
            int result = m_fchooser.showOpenDialog(addFileButton);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = m_fchooser.getSelectedFile();
                File parentDir = file.getParentFile();
                boolean folderSpecified = false;
                if(!file.exists()){
                    folderSpecified = false;
                } else {
                    folderSpecified = file.isDirectory();
                }

                if(m_isFileMode && folderSpecified){
                    JOptionPane.showMessageDialog(m_parent, "You should specify a file name.", "Select File Error", JOptionPane.ERROR_MESSAGE);
                    m_fileTextField.setText("");
                    return;
                }

                if(!m_isFileMode && folderSpecified) {
                    m_fileTextField.setText(file.getAbsolutePath());
                    return;
                }

                if(!m_isFileMode && !folderSpecified) {
                    m_fileTextField.setText(parentDir.getAbsolutePath());
                    return;
                }

                //is a file and in filemode
                String absolutePath = file.getAbsolutePath();
                String fileName = file.getName();
                if(!FilenameUtils.getExtension(fileName).equalsIgnoreCase("mzid")){
                    absolutePath += ".mzid";
                }
                m_fileTextField.setText(absolutePath);
            }
        }

        });
        
        c.gridx+=2;
        exportPanel.add(addFileButton, c);
        return exportPanel;

    }
    
    public String getFileName(){
        return m_fileTextField.getText().trim();
    }
           
    protected boolean checkParameters() {
        if(StringUtils.isEmpty(m_fileTextField.getText().trim())){
            m_parent.setStatus(true, "An export filename should be specified");
            m_parent.highlight(m_fileTextField);
            return false;
        }   
        return true;        
    }

}
