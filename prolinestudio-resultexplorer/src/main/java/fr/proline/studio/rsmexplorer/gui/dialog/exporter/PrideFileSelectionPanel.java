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

import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author VD225637
 */
public class PrideFileSelectionPanel extends  PrideWizardPanel {
         
    private static PrideFileSelectionPanel m_panel = null; 
    private JTextField m_fileTextField;
    private JFileChooser m_fchooser;
    protected String errorMsg;
     
    public static PrideFileSelectionPanel getPrideFileSelectionPanel(){
        if(m_panel == null) {
            m_panel = new PrideFileSelectionPanel();
        }
        return m_panel;
    }
    
    private PrideFileSelectionPanel() {        
        super.initWizardPanels("<html><b>Step 4:</b> Export to file:</html>");
    }
    
    //VDS TODO : Mutualise code avec ExportDialog...
    @Override
    protected JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx=0;
        c.gridy=0;
        mainPanel.add(createFileSelectionPanel(),c);
        
        c.gridx=0;
        c.gridy++;
        c.weighty=1;
        mainPanel.add(Box.createVerticalGlue(), c);      
        return mainPanel;
    }
    
    private JPanel createFileSelectionPanel() {
        JPanel exportPanel = new JPanel(new GridBagLayout());
        exportPanel.setBorder(BorderFactory.createTitledBorder(" File Selection "));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        m_fileTextField = new JTextField(30);
        exportPanel.add(m_fileTextField, c);

        final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                m_fchooser = new JFileChooser();
                m_fchooser.setFileFilter(new FileNameExtensionFilter("XML file", "xml", "XML"));
                              
                int result = m_fchooser.showOpenDialog(addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = m_fchooser.getSelectedFile();
                    
                    String absolutePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    if (fileName.indexOf('.') == -1) {
                        absolutePath += "."+"xml";
                    }
                    m_fileTextField.setText(absolutePath);
                }
            }
        });

        c.gridx+=2;
        exportPanel.add(addFileButton, c);
     
        return exportPanel;
    }
    
    @Override
    public HashMap<String, Object> getExportPrideParams(){
        HashMap params = new HashMap();      
        return params;
    }

    @Override
    protected Component checkExportPrideParams() {
        errorMsg  = null;
        if(StringUtils.isEmpty(m_fileTextField.getText().trim())){
            errorMsg =  "An export filename should be specified";
            return m_fileTextField;
        }
        return null;        
    }

    @Override
    protected String getErrorMessage() {
       return errorMsg;
    }
    
    public String getFileName() {
        return m_fileTextField.getText().trim();
    }
        
}
