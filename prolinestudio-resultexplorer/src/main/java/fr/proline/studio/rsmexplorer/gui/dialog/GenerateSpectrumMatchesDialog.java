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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.uds.FragmentationRuleSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 *
 * @author VD225637
 */
public class GenerateSpectrumMatchesDialog extends DefaultDialog {
    
//    private static GenerateSpectrumMatchesDialog m_singletonDialog = null;
    private JLabel m_fragmentationRuleSetsLabel = null;
    private JComboBox m_fragmentationRuleSetsComboBox = null;
    private JCheckBox m_forceGenerateChB = null;
    private JCheckBox m_useDefinedFRSChB = null;

    private List<MsiSearch> m_msiSearches = null;
    private boolean oneMerged = false;

    public GenerateSpectrumMatchesDialog(Window parent, MsiSearch msiSearch){
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        m_msiSearches = Collections.singletonList(msiSearch);

        initDialog();
    }
    
    public GenerateSpectrumMatchesDialog(Window parent, List<DDataset> allDSs){
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        if(allDSs != null && !allDSs.isEmpty()) {
            m_msiSearches = allDSs.stream().map(ds -> ds.getResultSet().getMsiSearch()).collect(Collectors.toList());
            oneMerged = allDSs.stream().filter(ds -> !DDatasetType.AggregationInformation.NONE.equals(ds.getAggregationInformation())).count() > 0;
        }
        initDialog();
    }

    private void initDialog(){
        setTitle("Generate Spectrum Matches");
        setDocumentationSuffix("id.1idq7dh");
        setHelpHeaderText("Choose Fragmentation Rule Set that will be used to generate spectrum matches.<br>" +
                " With force parameter set, new generated spectrum matches will overwrite existing ones.");
        initInternalPanel();
        pack();
    }

    private void initInternalPanel() {
                
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        m_forceGenerateChB = new JCheckBox("Force new spectrum matches generation", false);
        internalPanel.add(m_forceGenerateChB, c);
        
        c.gridy++;
        c.gridwidth = 3;        
        internalPanel.add(createFragmentatoinRuleSetPanel(), c);  
        setInternalComponent(internalPanel);
    }
    
     private JPanel createFragmentatoinRuleSetPanel() {
                
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Fragmentation Rules"));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        String dsFrs = getFragmentationRuleSetsLabel();
        m_useDefinedFRSChB = new JCheckBox("Use fragmentation rule set defined at import ("+dsFrs+")", false);               
        m_useDefinedFRSChB.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                m_fragmentationRuleSetsLabel.setEnabled(! m_useDefinedFRSChB.isSelected());
                m_fragmentationRuleSetsComboBox.setEnabled(! m_useDefinedFRSChB.isSelected());
            }            
        });
        panel.add(m_useDefinedFRSChB, c);
        
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 1.0;
        m_fragmentationRuleSetsLabel = new JLabel("Select Fragmentation Rule Set :");
        m_fragmentationRuleSetsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(m_fragmentationRuleSetsLabel, c);
        
        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 1;
        m_fragmentationRuleSetsComboBox = new JComboBox(DatabaseDataManager.getDatabaseDataManager().getFragmentationRuleSetsWithNullArray());       
        panel.add(m_fragmentationRuleSetsComboBox, c);  
        
        c.gridx++;
        c.weightx = 0;
        JButton viewFragmentationRuleSet = new JButton(IconManager.getIcon(IconManager.IconType.DOCUMENT_LIST));    
        viewFragmentationRuleSet.setMargin(new java.awt.Insets(2, 2, 2, 2));
        viewFragmentationRuleSet.setToolTipText("View Fragmentation Rule Sets");
        final GenerateSpectrumMatchesDialog dialog = this;
        viewFragmentationRuleSet.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FragmentationRuleSetViewer viewer  = new FragmentationRuleSetViewer(dialog);
                viewer.setVisible(true);
                        
            }
        });
        panel.add(viewFragmentationRuleSet, c);  
        return panel;
    }
    
     private String getFragmentationRuleSetsLabel(){
        StringBuilder sb = new StringBuilder();
        boolean oneNull = false;
        boolean oneNotNull = false;
        if (m_msiSearches == null || m_msiSearches.isEmpty())
            return "unknown - see dataset properties";
        for (MsiSearch msiSearch : m_msiSearches){
            if(msiSearch == null)
                oneNull = true;
            else {
                FragmentationRuleSet fragSet = DatabaseDataManager.getDatabaseDataManager().getFragmentationRuleSet(msiSearch.getSearchSetting().getFragmentationRuleSetId());
                if (fragSet == null)
                    oneNull = true;
                else {
                    if (oneNotNull)
                        sb.append(", ");
                    sb.append(fragSet.getName());
                    oneNotNull = true;
                }
            }
        }
        if(oneMerged){
            if(oneNotNull)
                sb.append("; ");
            sb.append("merged dataset...");
        }
        if(oneNull){
            if(oneNotNull)
                sb.append("; ");
            sb.append("unknown");
        }
        
        return sb.toString();
     }
     
    @Override
    protected boolean okCalled() {
        if(!m_useDefinedFRSChB.isSelected() && m_fragmentationRuleSetsComboBox.getSelectedItem() ==null){
            setStatus(true, "A Fragmentation Rule Set should be selected");
            highlight(m_fragmentationRuleSetsComboBox);
            return false;
        }
        
        return true;
    }
     
    public Boolean getDoForceGenerate() {
        return m_forceGenerateChB.isSelected();
    }
    
    public long getFragmentationRuleSetId() {
        if(m_useDefinedFRSChB.isSelected())
            return -1L;
        FragmentationRuleSet fragmentationRuleSet = (FragmentationRuleSet) m_fragmentationRuleSetsComboBox.getSelectedItem();
        if(fragmentationRuleSet == null)
            return -1L;
        return fragmentationRuleSet.getId();
    }
}
