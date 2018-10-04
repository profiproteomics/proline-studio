package fr.proline.studio.rsmexplorer.gui.dialog.exporter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;

/**
 * Singleton panel to configure Experiment for Pride export
 * 
 * @author VD225637
 */
public class PrideExpDescPanel extends PrideWizardPanel {
        
    private static PrideExpDescPanel m_panel = null; 
    private JTextField m_expNameTextField;
    private JTextField m_expShortLabelTextField;
    private JTextField m_projectNameTextField;
    
    private String errorMsg;
    
    private JTextField m_contactNameTextField;
    private JTextField m_contactInstitutionTextField;
    
    public static PrideExpDescPanel getPrideExpDescPanel(){
        if(m_panel == null) {
            m_panel = new PrideExpDescPanel();
        }
        return m_panel;
    }

    protected void resetPanel(){
        m_contactInstitutionTextField.setText(null);
        m_contactNameTextField.setText(null);
        m_projectNameTextField.setText(null);
        m_expNameTextField.setText(null);
        m_expShortLabelTextField.setText(null);
    }
    
    private PrideExpDescPanel() {        
        super.initWizardPanels("<html><b>Step 1:</b> Experimental Detail</html>");
    }
    
    @Override
    public HashMap<String, Object> getExportPrideParams(){
        HashMap params = new HashMap();
        if(!StringUtils.isEmpty(m_contactInstitutionTextField.getText().trim()))
            params.put("contact_institution",m_contactInstitutionTextField.getText().trim());
        if(!StringUtils.isEmpty(m_contactNameTextField.getText().trim()))
            params.put("contact_name",m_contactNameTextField.getText().trim());
        if(!StringUtils.isEmpty(m_projectNameTextField.getText().trim()))
            params.put("project_name",m_projectNameTextField.getText().trim());
        if(!StringUtils.isEmpty(m_expNameTextField.getText().trim()))
            params.put("exp_title",m_expNameTextField.getText().trim());
        if(!StringUtils.isEmpty(m_expShortLabelTextField.getText().trim()))
            params.put("exp_short_label",m_expShortLabelTextField.getText().trim());

        return params;
    }

    @Override
    protected Component checkExportPrideParams() {
        errorMsg = null;
        if(StringUtils.isEmpty(m_expNameTextField.getText().trim())){
            errorMsg = "Missing experimental title";
            return m_expNameTextField;
        }
        
        if(StringUtils.isEmpty(m_expShortLabelTextField.getText().trim())){
            errorMsg = "Missing experimental short label";
            return m_expShortLabelTextField;
        }
        
        if(StringUtils.isEmpty(m_contactNameTextField.getText().trim())){
            errorMsg = "Missing contact name";
            return m_contactNameTextField;
        }
        
        if(StringUtils.isEmpty(m_contactInstitutionTextField.getText().trim())){
            errorMsg = "Missing contact institution";
            return m_contactInstitutionTextField;
        }
                
        return null;        
    }
    
    @Override
    protected String getErrorMessage(){
        return errorMsg;
    }
    
    
    @Override
    protected JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();       
//        mainPanel.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel projectNameLabel = new JLabel("Project Name:");
        m_projectNameTextField  = new JTextField(30);
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(projectNameLabel, c);
        c.gridx++;
        c.weightx = 1;
        mainPanel.add(m_projectNameTextField, c);

        
        JLabel experimentNameLabel = new JLabel("Experiment Title* :");
        m_expNameTextField  = new JTextField(30);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        mainPanel.add(experimentNameLabel, c);
        c.gridx++;
        c.weightx = 1;
        mainPanel.add(m_expNameTextField, c);
        
        JLabel expShortNameLabel = new JLabel("Experiment Short Label* :");
        m_expShortLabelTextField  = new JTextField(30);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        mainPanel.add(expShortNameLabel, c);
        c.gridx++;
        c.weightx =1;
        mainPanel.add(m_expShortLabelTextField, c);
        
        JLabel contactNameLabel = new JLabel("Contact Name* :");
        m_contactNameTextField  = new JTextField(30);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        mainPanel.add(contactNameLabel, c);
        c.gridx++;
        c.weightx = 1;
        mainPanel.add(m_contactNameTextField, c);
        
        JLabel contactInstitutionLabel = new JLabel("Contact Institution* :");
        m_contactInstitutionTextField  = new JTextField(30);
        c.gridx = 0;
        c.weightx = 0;
        c.gridy++;
        mainPanel.add(contactInstitutionLabel, c);
        c.gridx++;
        c.weightx = 1;
        mainPanel.add(m_contactInstitutionTextField, c);
                
        c.gridy++;
        c.gridx = 0;
        c.gridwidth=2;
        c.weighty = 1;
        mainPanel.add(Box.createRigidArea(new Dimension(500,200)),c);
        
        return mainPanel;
    }

    
    
}
