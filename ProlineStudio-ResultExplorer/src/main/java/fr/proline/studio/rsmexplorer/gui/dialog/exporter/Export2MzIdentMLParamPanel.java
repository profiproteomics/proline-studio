/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.exporter;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class Export2MzIdentMLParamPanel extends JPanel {
    
    private DefaultDialog m_parent =null;
    
    
    private final String m_contact_FN_key = "contact_first_name";
    private JTextField m_contactFirstName;
    private final String m_contact_LN_key = "contact_last_name";
    private JTextField m_contactLastName;
    private final String m_contact_Mail_key = "contact_email";
    private JTextField m_contactEmail;
    private final String m_contact_URL_key = "contact_url";
    private JTextField m_contactURL;
    private final String m_org_name_key = "organization_name";
    private JTextField m_orgName;
    private final String m_org_url_key = "organization_url";
    private JTextField m_orgURL;
        
    private ParameterList m_parameterList;

    public Export2MzIdentMLParamPanel(DefaultDialog parent) {
        m_parent = parent;
        setLayout(new BorderLayout());
        add(createWizardPanel(), BorderLayout.PAGE_START);
        add(createMainPanel(), BorderLayout.CENTER);   
    }
        
    private JPanel createWizardPanel() {
        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 15, 5, 15);

        JLabel titleLabel = new JLabel("MzIdentML parameters");
        titleLabel.setIcon(IconManager.getIcon(IconManager.IconType.WAND_HAT));
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        helpPanel.add(titleLabel, c);
        
        String helpText =NbBundle.getMessage(Export2MzIdentMLDialog.class,"Export2MzIdentMLDialog.help.text");
        JTextArea helpArea = new JTextArea(helpText);        
        helpArea.setForeground(Color.LIGHT_GRAY);
        helpArea.setBackground(Color.WHITE);
        helpArea.setLineWrap(true);
        helpArea.setWrapStyleWord(true);       
        c.gridy++;
        helpPanel.add(helpArea, c);
                
        return helpPanel;
    }
    
    private JPanel  createMainPanel() {
        
        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new java.awt.GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        createParameters();
        m_parameterList.updateIsUsed(NbPreferences.root());
        
        JLabel cFNameLabel = new JLabel("First Name*:");
        c.gridx = 0;
        c.gridy = 0;
        paramPanel.add(cFNameLabel, c);
        c.gridx++;
        c.weightx = 1;
        paramPanel.add(m_contactFirstName, c);
                
        JLabel cLNameLabel = new JLabel("Last Name*:");
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        paramPanel.add(cLNameLabel, c);
        c.gridx++;
        c.weightx = 1;
        paramPanel.add(m_contactLastName, c);
        
                        
        JLabel eMailLabel = new JLabel("Email:");
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        paramPanel.add(eMailLabel, c);
        c.gridx++;
        c.weightx = 1;
        paramPanel.add(m_contactEmail, c);
        
                        
        JLabel cURLLabel = new JLabel("URL:");
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        paramPanel.add(cURLLabel, c);
        c.gridx++;
        c.weightx = 1;
        paramPanel.add(m_contactURL, c);
        
        JLabel orgNameLabel = new JLabel("Organization Name*:");
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        paramPanel.add(orgNameLabel, c);
        c.gridx++;
        c.weightx = 1;
        paramPanel.add(m_orgName, c);
                                
        JLabel orgURLLabel = new JLabel("URL:");
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        paramPanel.add(orgURLLabel, c);
        c.gridx++;
        c.weightx = 1;
        paramPanel.add(m_orgURL, c);              
        
        c.gridx=0;
        c.gridy++;
        c.gridwidth=2;
        c.weighty=1;
        paramPanel.add(Box.createVerticalGlue(),c);
        return paramPanel;
    }
 
    private void createParameters(){
        m_parameterList = new ParameterList(Export2MzIdentMLDialog.MZIDENT_SETTINGS_KEY);
                
        m_contactFirstName = new JTextField(20);
        StringParameter contactFirstNameParameter = new StringParameter(m_contact_FN_key, "Contact First Name", m_contactFirstName, "", new Integer(2), null);
        contactFirstNameParameter.setUsed(false);
        contactFirstNameParameter.setCompulsory(true);
        m_parameterList.add(contactFirstNameParameter);
        
        m_contactLastName = new JTextField(20);
        StringParameter contactLastNameParameter = new StringParameter(m_contact_LN_key, "Contact Last Name", m_contactLastName, "", new Integer(2), null);
        contactLastNameParameter.setUsed(false);
        contactLastNameParameter.setCompulsory(true);
        m_parameterList.add(contactLastNameParameter);
        
        m_contactURL  = new JTextField(20);
        StringParameter contactURLParameter = new StringParameter(m_contact_URL_key, "Contact URL", m_contactURL, "", new Integer(5), null);
        contactURLParameter.setUsed(false);
        contactURLParameter.setCompulsory(false);
        m_parameterList.add(contactURLParameter);
        
        m_contactEmail  = new JTextField(20);
        StringParameter contactEmailParameter = new StringParameter(m_contact_Mail_key, "Contact eMail", m_contactEmail, "", new Integer(5), null);
        contactEmailParameter.setUsed(false);
        contactEmailParameter.setCompulsory(false);
        m_parameterList.add(contactEmailParameter);
        
        
        m_orgName = new JTextField(20);
        StringParameter orgNameParameter = new StringParameter(m_org_name_key, "Organization Name", m_orgName, "", new Integer(2), null);
        orgNameParameter.setUsed(false);
        orgNameParameter.setCompulsory(true);
        m_parameterList.add(orgNameParameter);
        
        m_orgURL  = new JTextField(20);
        StringParameter orgURLParameter = new StringParameter(m_org_url_key, "Organization URL", m_orgURL, "", new Integer(5), null);
        orgURLParameter.setUsed(false);
        orgURLParameter.setCompulsory(false);
        m_parameterList.add(orgURLParameter);
                
    }
         
    protected void saveParameters(Preferences preferences){
        if(preferences == null)
            preferences = NbPreferences.root();
        
        m_parameterList.saveParameters(preferences);

    }
    
    protected boolean checkParameters(){
        ParameterError error =  m_parameterList.checkParameters();
        // report error
        if (error == null) {
            setUsedParams();
            return true;
        } else {
            m_parent.setStatus(true, error.getErrorMessage());
            m_parent.highlight(error.getParameterComponent());
            return false;
        }
    }
    
    protected void loadParameters(){
        SettingsDialog settingsDialog = new SettingsDialog(m_parent, Export2MzIdentMLDialog.MZIDENT_SETTINGS_KEY);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);

        if (settingsDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            if (settingsDialog.isDefaultSettingsSelected()) {
                 m_parameterList.initDefaults();              
            } else {
                try {
                    File settingsFile = settingsDialog.getSelectedFile();
                    FilePreferences filePreferences = new FilePreferences(settingsFile, null, "");

                    Preferences preferences = NbPreferences.root();
                    String[] keys = filePreferences.keys();
                    for (String key : keys) {
                        String value = filePreferences.get(key, null);
                        preferences.put(key, value);
                    }

                    m_parameterList.loadParameters(filePreferences);

                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
                    m_parent.setStatus(true, "Parsing of your Settings File failed");
                }                
            }
        }
           
    }
           
    private void setUsedParams(){
        for (AbstractParameter param : m_parameterList) {
            param.setUsed(!((JTextField)param.getComponent()).getText().isEmpty());
        }
    }
    
        
    public HashMap<String, Object> getExportParams() {

        HashMap exportParams = new HashMap<>();
        HashMap contactMap =  new HashMap<>();
        HashMap orgMap =  new HashMap<>();
        m_parameterList.getValues().forEach((String key, String value) -> {
            if(key.startsWith("contact_")){
                contactMap.put(key.substring("contact_".length()), value);
            } else if(key.startsWith("organization_")){
                orgMap.put(key.substring("organization_".length()), value);
            }
                
            exportParams.put("contact",contactMap);
            exportParams.put("organization",orgMap);
        });
        return exportParams; 
    }
}
