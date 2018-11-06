package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.gui.WizardPanel;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class DefineQuantParamsPanel extends JPanel {
    
    private static DefineQuantParamsPanel m_singleton = null;
    
    private AbstractGenericQuantParamsPanel m_internalQuantPanel = null;
    
    private  JButton m_toggleButton = null;
    private JPanel m_mainPanel ;
    
    private boolean m_completePanel; //todo: change to m_simplifiedPanel to simplify ...
    
    private GridBagConstraints m_internalPanelGBC = null;
    
    private DefineQuantParamsPanel() {
        setLayout(new BorderLayout());
 
        
        JPanel wizardPanel = new WizardPanel("<html><b>Step 3:</b> Specify quantitation parameters.</html>");
        m_mainPanel = createMainPanel();

        add(wizardPanel, BorderLayout.PAGE_START);
        add(m_mainPanel, BorderLayout.CENTER);
    }
           
    public String getParamsVersion(){
        return "2.0";
    }
    
    public AbstractGenericQuantParamsPanel getParamsPanel() {
        return m_internalQuantPanel;     
    }
    

    public static DefineQuantParamsPanel getDefineQuantPanel() {
        if (m_singleton == null) {
            m_singleton = new DefineQuantParamsPanel();
        }

        return m_singleton;
    }
    
    private JPanel createMainPanel() {
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        m_toggleButton = new JButton(IconManager.getIcon(IconManager.IconType.OPTIONS_MORE));
        m_toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                // check if user also wants to erase locally
                String infoText = m_completePanel ? "Switching to short XIC Parameters panel will reset the filled values." : "Switching to complete XIC Parameters panel will reset the filled values.";
                InfoDialog exitDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "XIC Parameters Reset", infoText);
                exitDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                exitDialog.setVisible(true);

                if (exitDialog.getButtonClicked() == OptionDialog.BUTTON_OK) {
                   setIsSimplifiedPanel(m_completePanel); //if current is complete, switch to simplified panel.
                }
            }

        });
        
        
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(m_toggleButton, c);
        
        c.gridx++;
        c.weightx = 1;
        mainPanel.add(Box.createHorizontalGlue(), c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weighty = 1;
        Preferences preferences = NbPreferences.root();
        boolean completePanel = ! preferences.getBoolean(AbstractGenericQuantParamsPanel.XIC_SIMPLIFIED_PARAMS, true);

        m_internalQuantPanel = getParamsPanel(completePanel, true);
        mainPanel.add(m_internalQuantPanel, c);  
        m_internalPanelGBC = c;
        
        return mainPanel;
    }
    
    protected void setIsSimplifiedPanel(boolean isSimplified){
        if(m_completePanel == isSimplified) { //have to change Panel 
            m_completePanel = !m_completePanel;
            m_toggleButton.setIcon(m_completePanel ? IconManager.getIcon(IconManager.IconType.OPTIONS_LESS) : IconManager.getIcon(IconManager.IconType.OPTIONS_MORE));
            m_mainPanel.remove(m_internalQuantPanel);
            m_internalQuantPanel = getParamsPanel(m_completePanel, true);
            m_mainPanel.add(m_internalQuantPanel, m_internalPanelGBC);
            m_mainPanel.revalidate();
            m_mainPanel.repaint();
        }        
    }
    
    private AbstractGenericQuantParamsPanel getParamsPanel(boolean completePanel, boolean readValues) {
        m_completePanel = completePanel;
        updateButton(m_completePanel);
        if (m_completePanel) {
            return new DefineQuantParamsCompletePanel(false, readValues);
        } else {
            return new DefineQuantParamsSimplifiedPanel(false, readValues);
        }
    }
    
    private void updateButton(boolean completePanel) {
        if (completePanel) {
            m_toggleButton.setText("Simplified Parameters");
        } else {
            m_toggleButton.setText("Advanced Parameters");
        }
    }
    
    
}
