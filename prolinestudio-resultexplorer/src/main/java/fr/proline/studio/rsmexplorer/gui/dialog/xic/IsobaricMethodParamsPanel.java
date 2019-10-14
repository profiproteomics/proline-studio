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

import fr.proline.core.orm.uds.QuantitationMethod;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author CB205360
 */
public class IsobaricMethodParamsPanel extends AbstractParamsPanel {

    private QuantitationMethod m_quantMethod;
    private JTextField m_extractionMoZTolTF;   
    private JComboBox<String> m_reporterSourceCbx;
    
    public IsobaricMethodParamsPanel(QuantitationMethod method) {
        m_quantMethod = method;
        
        setLayout(new BorderLayout());
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Isobaric tagging parameters"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        mainPanel.add(createExtractionParamPanel(), c);

        c.gridy++;
        mainPanel.add(createReporterSourcePanel(), c);
        

        c.gridy++;
        c.weighty = 1;
        mainPanel.add(Box.createVerticalGlue(), c);
        
        return mainPanel;
    }

    
    private JPanel createReporterSourcePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel extractionMoZTolLabel = new JLabel("reporter ion source:");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        panel.add(extractionMoZTolLabel, c);

        m_reporterSourceCbx = new JComboBox<>();
        m_reporterSourceCbx.addItem("Proline spectrum");
        m_reporterSourceCbx.addItem("mzdb MS2 spectrum");
        m_reporterSourceCbx.addItem("mzdb MS3 spectrum");
        c.gridx++;
        c.weightx = 0;
        panel.add(m_reporterSourceCbx, c);
        
        c.gridx++;
        c.weightx = 1;
        panel.add(Box.createHorizontalGlue(), c);
        
        return panel;
    }

        
    private JPanel createExtractionParamPanel() {
        JPanel panel = new JPanel(new GridBagLayout());        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel extractionMoZTolLabel = new JLabel("Moz tolerance (ppm):");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        panel.add(extractionMoZTolLabel, c);

        m_extractionMoZTolTF = new JTextField(10);
        c.gridx++;
        c.weightx = 0;
        panel.add(m_extractionMoZTolTF, c);
        
        c.gridx++;
        c.weightx = 1;
        panel.add(Box.createHorizontalGlue(), c);
        
        return panel;
    }
    
    
    
    @Override
    public Map<String, Object> getQuantParams() {
        Map<String,Object> params = new HashMap<>();
        Map<String,Object> extractionParams = new HashMap<>();
        extractionParams.put("moz_tol", m_extractionMoZTolTF.getText());
        extractionParams.put("moz_tol_unit", "PPM");
        params.put("extraction_params", extractionParams);
        String sourceValue = (String)m_reporterSourceCbx.getSelectedItem();
        params.put("reporter_ion_data_source", sourceValue.replaceAll(" ", "_").toUpperCase());
        return params;
    }

    @Override
    public void setQuantParams(Map<String, Object> quantParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
