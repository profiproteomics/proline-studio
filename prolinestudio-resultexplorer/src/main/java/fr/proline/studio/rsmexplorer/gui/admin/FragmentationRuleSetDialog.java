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
package fr.proline.studio.rsmexplorer.gui.admin;

import fr.proline.core.orm.uds.FragmentationRule;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.JCheckBoxList;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class  FragmentationRuleSetDialog extends DefaultDialog   {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static FragmentationRuleSetDialog m_singletonDialog = null;
      
    private JTextField m_nameTextField;
    private JCheckBoxList<FragmentationRule> m_fragmentationRulesCBList; 
         
    public static final FragmentationRuleSetDialog getDialog(Window parent){
        if(m_singletonDialog == null)
            m_singletonDialog =  new FragmentationRuleSetDialog(parent);
                
        return m_singletonDialog;
    }
    
    private FragmentationRuleSetDialog(Window parent){
        super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
        initInternalPanel();
    }
    
    private void initInternalPanel() {
        JPanel internalPanel = new JPanel();   
        internalPanel.setLayout(new java.awt.GridBagLayout());

        JLabel nameLabel = new JLabel("Name :");
        m_nameTextField = new JTextField(30);
        m_nameTextField.setText("");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
               
        internalPanel.add(nameLabel,c);
        
        c.gridx++; // next col
        internalPanel.add(m_nameTextField, c);
        
        c.gridx = 0;
        c.gridy++; //next line
        c.fill = GridBagConstraints.NONE;
        JLabel rulesLabel = new JLabel("Fragmentation Rules:");
        internalPanel.add(rulesLabel,c);
        
        List<FragmentationRule> rules = Arrays.asList(DatabaseDataManager.getDatabaseDataManager().getFragmentationRulesArray());        
        List<Boolean> selected = new ArrayList<>(Collections.nCopies(rules.size(), Boolean.FALSE));        
        m_fragmentationRulesCBList = new JCheckBoxList<>(rules, selected);
        JScrollPane rulesPane = new JScrollPane();
        rulesPane.setViewportView(m_fragmentationRulesCBList);
        c.gridx++; // next col
        internalPanel.add(rulesPane, c);
        setInternalComponent(internalPanel);
    }
    
    
    @Override
    protected boolean okCalled() {
        if(StringUtils.isEmpty(getName())){
            setStatus(true, "A name should be specified");
            highlight(m_nameTextField);
            return false;
        }
        if(getSelectedFragmentationRule().isEmpty()) {
            setStatus(true, "At least one rule should be specified");
            highlight(m_fragmentationRulesCBList);
            return false;
        }
        return true;
    }
    
    public List<FragmentationRule> getSelectedFragmentationRule(){
        return m_fragmentationRulesCBList.getSelectedItems();
    }
    
    public String getName(){
        return m_nameTextField.getText();
    }
    
}
