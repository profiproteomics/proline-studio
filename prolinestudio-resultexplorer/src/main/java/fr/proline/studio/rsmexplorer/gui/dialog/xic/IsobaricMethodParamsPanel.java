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
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.studio.NbPreferences;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 *
 * @author CB205360
 */
public class IsobaricMethodParamsPanel extends AbstractParamsPanel {

    private JTextField m_extractionMoZTolTF;
    private JComboBox<String> m_reporterSourceCbx;
    private JCheckBox m_rescaleAbundancestoMS1CB;
    public final static String ISOBARIC_PARAMS_PREFIX = "IsobaricParameters";
    private final boolean m_readOnly;

    protected final static Double DEFAULT_ISOBARIC_EXTRACTION_MOZTOL_VALUE = 25.0;
    protected final static String DEFAULT_MOZTOL_UNIT = "PPM";
    enum ReporterSources {
        PROLINE_SPECTRUM("Proline spectrum"),
        MZDB_MS2_SPECTRUM("mzdb MS2 spectrum"),
        MZDB_MS3_SPECTRUM("mzdb MS3 spectrum");

        private  String m_displayValue;
         ReporterSources(String displayValue){
            this.m_displayValue = displayValue;
        }

        public String getDisplayValue(){
             return  m_displayValue;
        }

        public static ReporterSources getResourceSourcesForDisplay(String displayValue){
             for(ReporterSources rs : ReporterSources.values()){
                 if(rs.getDisplayValue().equals(displayValue))
                     return rs;
             }
             return null;
        }
    }

    public IsobaricMethodParamsPanel(QuantitationMethod method) {
        this(method, false);
    }

    public IsobaricMethodParamsPanel(QuantitationMethod method, boolean readOnly) {
        m_readOnly = readOnly;
        m_parameterList = new ParameterList(ISOBARIC_PARAMS_PREFIX);
        createParameters();
        m_parameterList.updateValues(NbPreferences.root());

        setLayout(new BorderLayout());
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
    }

    private void createParameters(){
        m_extractionMoZTolTF = new JTextField();
        DoubleParameter extractionMoZTolParameter = new DoubleParameter("extractionMoZTol", "Extraction moz tolerance", m_extractionMoZTolTF, DEFAULT_ISOBARIC_EXTRACTION_MOZTOL_VALUE, Double.valueOf(0), null);
        m_extractionMoZTolTF.setEnabled(!m_readOnly);
        m_parameterList.add(extractionMoZTolParameter);

        m_rescaleAbundancestoMS1CB = new JCheckBox("Rescale reporter abundances to MS1 signal", false);
        BooleanParameter rescaleAbundanceParameter = new BooleanParameter("RescaleAb2MS1", "Rescale Abundance to MS1", m_rescaleAbundancestoMS1CB, false);
        m_rescaleAbundancestoMS1CB.setEnabled(!m_readOnly);
        m_parameterList.add(rescaleAbundanceParameter);



        String[] rscValue = new String[ReporterSources.values().length];
        String[] rscKeys = new String[ReporterSources.values().length];
        int index= 0;
        for(ReporterSources rs : ReporterSources.values()) {
            rscValue[index]= rs.getDisplayValue();
            rscKeys[index++] = rs.name();
        }
        m_reporterSourceCbx = new JComboBox<>(rscValue);
        m_reporterSourceCbx.setEnabled(!m_readOnly);
        ObjectParameter reporterSourceParameter = new ObjectParameter<>("reporterSource", "Reporter extract source", m_reporterSourceCbx, rscValue, rscKeys, 0, null);
        m_parameterList.add(reporterSourceParameter);

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
        mainPanel.add(createRescalePanel(), c);
        
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
        
        JLabel extractionMoZTolLabel = new JLabel("Reporter ions will be extracted from:");
        extractionMoZTolLabel.setEnabled(!m_readOnly);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        panel.add(extractionMoZTolLabel, c);

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
        
        JLabel extractionMoZTolLabel = new JLabel("Reporter ions m/z tolerance (ppm):");
        extractionMoZTolLabel.setEnabled(!m_readOnly);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        panel.add(extractionMoZTolLabel, c);

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
        extractionParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
        params.put("extraction_params", extractionParams);

        String sourceValue = (String)m_reporterSourceCbx.getSelectedItem();
        ReporterSources rs = ReporterSources.getResourceSourcesForDisplay(sourceValue);
        params.put("reporter_ion_data_source", rs.name());

        //temporary put this value into "label_free_quant_config". If true this value will be replaced
        // by the label_free configuration, if false it will be removed from the params
        params.put("label_free_quant_config", m_rescaleAbundancestoMS1CB.isSelected());
        return params;
    }

    @Override
    public void setQuantParams(Map<String, Object> quantParams) {
        if(quantParams == null || quantParams.isEmpty()){
            resetValues();
        } else {
            try {
                String extractParamVal = ((Map<String, Object>) quantParams.get("extraction_params")).getOrDefault("moz_tol","").toString();
                m_extractionMoZTolTF.setText(extractParamVal);

                String reporterDS = (String) quantParams.get("reporter_ion_data_source");
                m_reporterSourceCbx.setSelectedItem(ReporterSources.valueOf(reporterDS).getDisplayValue());

                m_rescaleAbundancestoMS1CB.setSelected(quantParams.containsKey("label_free_quant_config"));

            } catch (NullPointerException | ClassCastException exep){
                resetValues();
            }

        }
    }

    private void resetValues(){
        m_extractionMoZTolTF.setText("");
        m_reporterSourceCbx.setSelectedIndex(0);
        m_rescaleAbundancestoMS1CB.setSelected(false);
    }

  private Component createRescalePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.insets = new java.awt.Insets(5, 5, 5, 5);
    c.gridx = 0;
    c.gridy = 0;
    
   panel.add(m_rescaleAbundancestoMS1CB, c);
    
    c.gridx++;
    c.weightx = 1;
    panel.add(Box.createHorizontalGlue(), c);
    
    return panel;
  }

    
}
