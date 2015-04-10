/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.pride;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author VD225637
 */
public class PrideProtocolDescPanel extends PrideWizardPanel {
    
    private static PrideProtocolDescPanel m_panel = null;
    private JTextArea m_protocolDescriptionTextArea;    
        
    private String errorMsg;
    
    private PrideProtocolDescPanel() {
        super.initWizardPanels("<html><b>Step 2:</b> Protocol Description</html>");
    }
        
    public static PrideProtocolDescPanel getPrideProtocolDescPanel(){
        if(m_panel == null) {
            m_panel = new PrideProtocolDescPanel();
        }
        return m_panel;
    }
    
    @Override
    public HashMap<String, Object> getExportPrideParams(){
        HashMap params = new HashMap();
        if(!StringUtils.isEmpty(m_protocolDescriptionTextArea.getText().trim()))
            params.put("protocol_description",m_protocolDescriptionTextArea.getText().trim());
        return params;
     }
    
    
    @Override
    protected Component checkExportPrideParams(){
        return null;   
    }
    
    @Override
    protected String getErrorMessage(){
        return null;
    }
    
    @Override
    protected JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder("Full Protocol description"));
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();      
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
//        c.gridx = 0;
//        c.gridy = 0;
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        JLabel protocolLabel = new JLabel("Full Protocol description :");
//        mainPanel.add(protocolLabel, c);  
        
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx=1;
        c.weighty=1;
        m_protocolDescriptionTextArea  = new JTextArea();
        m_protocolDescriptionTextArea.setText("<Protocol>\n<ProtocolName>TO BE REPLACED !!!! </ProtocolName>\n<ProtocolSteps></ProtocolSteps>\n</Protocol>");
        JScrollPane protocolSP = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        protocolSP.setViewportView(m_protocolDescriptionTextArea);
        mainPanel.add(protocolSP, c);
        
     
        
        
        return mainPanel;
    }

    
}
