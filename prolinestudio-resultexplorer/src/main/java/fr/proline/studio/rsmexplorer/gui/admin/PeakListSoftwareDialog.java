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

import fr.proline.core.orm.uds.SpectrumTitleParsingRule;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author JM235353
 */
public class PeakListSoftwareDialog extends DefaultDialog  {

    private static PeakListSoftwareDialog m_singletonDialog = null;

    public enum DialogMode {
        CREATE_USER,
        MODIFY_USER,
        VIEW_USER
    }

    private static final String COMBOBOX_SELECTION_STRING = "< Select Predefined >";
    
    private JTextField m_nameTextField;
    private JTextField m_versionTextField;
    
    private JTextField m_rawFileIdentifierTextField;
    private JTextField m_firstCycleTextField;
    private JTextField m_lastCycleTextField;
    private JTextField m_firstScanTextField;
    private JTextField m_lastScanTextField;
    private JTextField m_firstTimeTextField;
    private JTextField m_lastTimeTextField;
    
    private JComboBox m_rawFileIdentifierComboBox;
    private JComboBox m_firstCycleComboBox;
    private JComboBox m_lastCycleComboBox;
    private JComboBox m_firstScanComboBox;
    private JComboBox m_lastScanComboBox;
    private JComboBox m_firstTimeComboBox;
    private JComboBox m_lastTimeComboBox;
    
    private HashSet<String> rawFileIdentifierSet = new HashSet<>();
    private HashSet<String> firstCycleSet = new HashSet<>();
    private HashSet<String> lastCycleSet = new HashSet<>();
    private HashSet<String> firstScanSet = new HashSet<>();
    private HashSet<String> lastScanSet = new HashSet<>();
    private HashSet<String> firstTimeSet = new HashSet<>();
    private HashSet<String> lastTimeSet = new HashSet<>();
    
    private boolean m_comboboxAreBeingUpdated = false;
    
    public static PeakListSoftwareDialog getDialog(Window parent, DialogMode mode) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new PeakListSoftwareDialog(parent);
        }
        
        m_singletonDialog.initMode(mode);
        
        
        
        return m_singletonDialog;
    }

    public PeakListSoftwareDialog(Window parent) {
        super(parent, Dialog.ModalityType.DOCUMENT_MODAL);

        //setDocumentationSuffix("h.eb8nfjv41vkz"); //JPM.TODO

        initInternalPanel();

    }

    private void initMode(DialogMode mode) {

        boolean enableParsingFields = true;
        boolean enableModifications = true;
        switch(mode) {
            case CREATE_USER:
                setTitle("Add Peaklist Software");
                break;
            case MODIFY_USER:
                setTitle("Modify Peaklist Software");
                enableParsingFields = false;
                break;
            case VIEW_USER:
                setTitle("View Peaklist Software");
                enableParsingFields = false;
                enableModifications = false;
                break;
        }
        m_nameTextField.setText("");
        m_versionTextField.setText("");
        m_rawFileIdentifierTextField.setText("");
        m_firstCycleTextField.setText("");
        m_lastCycleTextField.setText("");
        m_firstScanTextField.setText("");
        m_lastScanTextField.setText("");
        m_firstTimeTextField.setText("");
        m_lastTimeTextField.setText("");
        m_nameTextField.setEnabled(enableModifications);
        m_versionTextField.setEnabled(enableModifications);
        m_rawFileIdentifierTextField.setEnabled(enableParsingFields);
        m_rawFileIdentifierComboBox.setEnabled(enableParsingFields);
        m_firstCycleTextField.setEnabled(enableParsingFields);
        m_firstCycleComboBox.setEnabled(enableParsingFields);
        m_lastCycleTextField.setEnabled(enableParsingFields);
        m_lastCycleComboBox.setEnabled(enableParsingFields);
        m_firstScanTextField.setEnabled(enableParsingFields);
        m_firstScanComboBox.setEnabled(enableParsingFields);
        m_lastScanTextField.setEnabled(enableParsingFields);
        m_lastScanComboBox.setEnabled(enableParsingFields);
        m_firstTimeTextField.setEnabled(enableParsingFields);
        m_firstTimeComboBox.setEnabled(enableParsingFields);
        m_lastTimeTextField.setEnabled(enableParsingFields);
        m_lastTimeComboBox.setEnabled(enableParsingFields);
        
        
        DatabaseDataManager.getDatabaseDataManager().getParsingRules(rawFileIdentifierSet, firstCycleSet, lastCycleSet, firstScanSet, lastScanSet, firstTimeSet, lastTimeSet);

        fillComboboxWithHashSet(m_rawFileIdentifierComboBox, rawFileIdentifierSet);
        fillComboboxWithHashSet(m_firstCycleComboBox, firstCycleSet);
        fillComboboxWithHashSet(m_lastCycleComboBox, lastCycleSet);
        fillComboboxWithHashSet(m_firstScanComboBox, firstScanSet);
        fillComboboxWithHashSet(m_lastScanComboBox, lastScanSet);
        fillComboboxWithHashSet(m_firstTimeComboBox, firstTimeSet);
        fillComboboxWithHashSet(m_lastTimeComboBox, lastTimeSet);

        
    }
    
    private void fillComboboxWithHashSet(JComboBox combobox, HashSet<String> hashSet) {

        m_comboboxAreBeingUpdated = true;
        try {

            combobox.removeAllItems();
            combobox.addItem(COMBOBOX_SELECTION_STRING);
            for (String rule : hashSet) {
                combobox.addItem(rule);
            }
        } finally {
            m_comboboxAreBeingUpdated = false;
        }
    }
    
    private void addActionListenerToCombobox(final JComboBox combobox, JTextField textField) {
        combobox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_comboboxAreBeingUpdated) {
                    return;
                }
                
                
                
                String parsingRule = combobox.getSelectedItem().toString();
                if (parsingRule.compareTo(COMBOBOX_SELECTION_STRING) == 0) {
                    return;
                }
                textField.setText(parsingRule);
                
                combobox.setSelectedIndex(0);
            }
            
        });
    }
    
    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        JPanel peaklistPanel = createPeaklistPanel();
        JPanel testPanel = createTestPanel();
        JPanel parsingRulesPanel = createParsingRulesPanel();
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;

        
        internalPanel.add(peaklistPanel, c);

        c.gridy++;
        internalPanel.add(parsingRulesPanel, c);

        c.gridy++;
        internalPanel.add(testPanel, c);
        
        setInternalComponent(internalPanel);

    }
    
    private JPanel createPeaklistPanel() {
        JPanel peaklistPanel = new JPanel(new java.awt.GridBagLayout());
        peaklistPanel.setLayout(new java.awt.GridBagLayout());
        peaklistPanel.setBorder(BorderFactory.createTitledBorder("Peaklist Software"));
        
        
        JLabel nameLabel = new JLabel("Name :");
        JLabel versionLabel = new JLabel("Version :");

        
        m_nameTextField = new JTextField(30);
        m_versionTextField = new JTextField(30);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        
        peaklistPanel.add(nameLabel, c);
        
        c.gridx++;
        peaklistPanel.add(m_nameTextField, c);
        
        c.gridx = 0;
        c.gridy++;
        peaklistPanel.add(versionLabel, c);
        
        c.gridx++;
        peaklistPanel.add(m_versionTextField, c);
        
        return peaklistPanel;
    }
    
    private JPanel createParsingRulesPanel() {
        
        JPanel parsingRulesPanel = new JPanel(new java.awt.GridBagLayout());
        parsingRulesPanel.setLayout(new java.awt.GridBagLayout());
        parsingRulesPanel.setBorder(BorderFactory.createTitledBorder("Spectrum Title Parsing Rules"));
        
        JLabel rawFileIdentifierLabel = new JLabel("Raw File Identifier :");
        JLabel firstCycleLabel = new JLabel("First Cycle :");
        JLabel lastCycleLabel = new JLabel("Last Cycle");
        JLabel firstScanLabel = new JLabel("First Scan :");
        JLabel lastScanLabel = new JLabel("Last Scan :");
        JLabel firstTimeLabel = new JLabel("First Time :");
        JLabel lastTimeLabel = new JLabel("Last Time :");
        
        m_rawFileIdentifierTextField = new JTextField(30);
        m_rawFileIdentifierComboBox = new JComboBox();
        m_firstCycleTextField = new JTextField(30);
        m_firstCycleComboBox = new JComboBox();
        m_lastCycleTextField = new JTextField(30);
        m_lastCycleComboBox = new JComboBox();
        m_firstScanTextField = new JTextField(30);
        m_firstScanComboBox = new JComboBox();
        m_lastScanTextField = new JTextField(30);
        m_lastScanComboBox = new JComboBox();
        m_firstTimeTextField = new JTextField(30);
        m_firstTimeComboBox = new JComboBox();
        m_lastTimeTextField = new JTextField(30);
        m_lastTimeComboBox = new JComboBox();

        addActionListenerToCombobox(m_rawFileIdentifierComboBox, m_rawFileIdentifierTextField);
        addActionListenerToCombobox(m_firstCycleComboBox, m_firstCycleTextField);
        addActionListenerToCombobox(m_lastCycleComboBox, m_lastCycleTextField);
        addActionListenerToCombobox(m_firstScanComboBox, m_firstScanTextField);
        addActionListenerToCombobox(m_lastScanComboBox, m_lastScanTextField);
        addActionListenerToCombobox(m_firstTimeComboBox, m_firstTimeTextField);
        addActionListenerToCombobox(m_lastTimeComboBox, m_lastTimeTextField);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        

        c.gridy++;
        parsingRulesPanel.add(rawFileIdentifierLabel, c);
        
        c.gridx++;
        parsingRulesPanel.add(m_rawFileIdentifierTextField, c);

        c.gridx++;
        parsingRulesPanel.add(m_rawFileIdentifierComboBox, c);
        
        c.gridx = 0;
        c.gridy++;
        parsingRulesPanel.add(firstCycleLabel, c);
        
        c.gridx++;
        parsingRulesPanel.add(m_firstCycleTextField, c);

        c.gridx++;
        parsingRulesPanel.add(m_firstCycleComboBox, c);
        
        c.gridx = 0;
        c.gridy++;
        parsingRulesPanel.add(lastCycleLabel, c);
        
        c.gridx++;
        parsingRulesPanel.add(m_lastCycleTextField, c);

        c.gridx++;
        parsingRulesPanel.add(m_lastCycleComboBox, c);
        
        c.gridx = 0;
        c.gridy++;
        parsingRulesPanel.add(firstScanLabel, c);
        
        c.gridx++;
        parsingRulesPanel.add(m_firstScanTextField, c);

        c.gridx++;
        parsingRulesPanel.add(m_firstScanComboBox, c);
        
        c.gridx = 0;
        c.gridy++;
        parsingRulesPanel.add(lastScanLabel, c);
        
        c.gridx++;
        parsingRulesPanel.add(m_lastScanTextField, c);

        c.gridx++;
        parsingRulesPanel.add(m_lastScanComboBox, c);
        
        c.gridx = 0;
        c.gridy++;
        parsingRulesPanel.add(firstTimeLabel, c);
        
        c.gridx++;
        parsingRulesPanel.add(m_firstTimeTextField, c);

        c.gridx++;
        parsingRulesPanel.add(m_firstTimeComboBox, c);
        
        c.gridx = 0;
        c.gridy++;
        parsingRulesPanel.add(lastTimeLabel, c);
        
        c.gridx++;
        parsingRulesPanel.add(m_lastTimeTextField, c);

        c.gridx++;
        parsingRulesPanel.add(m_lastTimeComboBox, c);
        
        return parsingRulesPanel;
    }
    
    private JPanel createTestPanel() {
        JPanel testPanel = new JPanel(new java.awt.GridBagLayout());
        testPanel.setLayout(new java.awt.GridBagLayout());
        testPanel.setBorder(BorderFactory.createTitledBorder("Spectrum Title Parsing Test"));
        
        JLabel spectrumTitleLabel = new JLabel("Spectrum Title:");
        JTextField spectrumTitleTextField = new JTextField(30);
        JButton testButton = new JButton("Test", IconManager.getIcon(IconManager.IconType.TEST));
        
        PeakListSoftwareDialog dialog = this;
        
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                StringBuilder sb = new StringBuilder();
                
                String spectrumTitle = spectrumTitleTextField.getText().trim();
                
                parse(sb, m_rawFileIdentifierTextField.getText().trim(), "Raw File Identifier :",spectrumTitle);
                parse(sb, m_firstCycleTextField.getText().trim(), "First Cycle :",spectrumTitle);
                parse(sb, m_lastCycleTextField.getText().trim(), "Last Cycle :",spectrumTitle);
                parse(sb, m_firstScanTextField.getText().trim(), "First Scan :",spectrumTitle);
                parse(sb, m_lastScanTextField.getText().trim(), "Last Scan :",spectrumTitle);
                parse(sb, m_firstTimeTextField.getText().trim(), "First Time :",spectrumTitle);
                parse(sb, m_lastTimeTextField.getText().trim(), "Last Time :",spectrumTitle);

                InfoDialog parseResultDialog = new InfoDialog(dialog, InfoDialog.InfoType.NO_ICON, "Spectrum Title Parsing Result", sb.toString());
                parseResultDialog.setButtonVisible(InfoDialog.BUTTON_OK, false);
                parseResultDialog.setButtonName(BUTTON_CANCEL, "Close");
                parseResultDialog.centerToWindow(dialog);
                parseResultDialog.setVisible(true);
            }
            
        });
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        testPanel.add(spectrumTitleLabel, c);
        
        c.gridx++;
        c.weightx = 1;
        testPanel.add(spectrumTitleTextField, c);
        
        c.gridx++;
        c.weightx = 0;
        testPanel.add(testButton, c);
        
        
        return testPanel;
    }
    
    private void parse(StringBuilder sb, String rule, String fieldName, String stringToParse) {
        sb.append(fieldName);
        if (!rule.isEmpty()) {
            Pattern pattern = Pattern.compile(rule);
            Matcher match = pattern.matcher(stringToParse);
            boolean findAMatch = match.find();
            if (findAMatch) {
                String firstMatch = match.group(1);
                sb.append(firstMatch);
            }
            
        }
        sb.append('\n');
    }
    
    public void setPeaklistSoftwareInfo(String name, String version, SpectrumTitleParsingRule parsingRule) {
        m_nameTextField.setText(name);
        m_versionTextField.setText(version);
        
        String rule = parsingRule.getRawFileIdentifier();
        m_rawFileIdentifierTextField.setText(rule);
        
        
        rule = parsingRule.getFirstCycle();
        m_firstCycleTextField.setText(rule);
        
        
        rule = parsingRule.getLastCycle();
        m_lastCycleTextField.setText(rule);
        
        rule = parsingRule.getFirstScan();
        m_firstScanTextField.setText(rule);

        rule = parsingRule.getLastScan();
        m_lastScanTextField.setText(rule);
        
        rule = parsingRule.getFirstTime();
        m_firstTimeTextField.setText(rule);

        rule = parsingRule.getLastTime();
        m_lastTimeTextField.setText(rule);
  
    }

    public String getPeaklistSoftwareName() {
        return m_nameTextField.getText().trim();
    }
    
    public String getVersion() {
        return m_versionTextField.getText().trim();
    }
    
    public String getRawFileIdentifier() {
        return m_rawFileIdentifierTextField.getText().trim();
    }
    public String getFirstCycle() {
        return m_firstCycleTextField.getText().trim();
    }    
    public String getLastCycle() {
        return m_lastCycleTextField.getText().trim();
    }    
    public String getFirstScan() {
        return m_firstScanTextField.getText().trim();
    }    
    public String getLastScan() {
        return m_lastScanTextField.getText().trim();
    }    
    public String getFirstTime() {
        return m_firstTimeTextField.getText().trim();
    }
    public String getLastTime() {
        return m_lastTimeTextField.getText().trim();
    }    

    public SpectrumTitleParsingRule getSpectrumTitleParsingRule() {
        SpectrumTitleParsingRule parsingRule = new SpectrumTitleParsingRule();
        parsingRule.setRawFileIdentifier(getRawFileIdentifier());
        parsingRule.setFirstCycle(getFirstCycle());
        parsingRule.setLastCycle(getLastCycle());
        parsingRule.setFirstScan(getFirstScan());
        parsingRule.setLastScan(getLastScan());
        parsingRule.setFirstTime(getFirstTime());
        parsingRule.setLastTime(getLastTime());
     
        return parsingRule;
    }
    


    private boolean checkParameters() {
        String name = getPeaklistSoftwareName();
        if (name.isEmpty() || (name.length()<3)) {
            setStatus(true, "Name must contain at least 3 characters.");
            highlight(m_nameTextField);
            return false;
        }


        return true;
    }
    
    @Override
    protected boolean okCalled() {
        
        // check parameters
        if (!checkParameters()) {
            return false;
        }
        
        return true;
    }




}
