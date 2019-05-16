package fr.proline.studio.rsmexplorer.gui.dialog.spectralcount;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.*;

/**
 *
 * @author JM235353
 */
public class SpectralCountNamePanel extends JPanel {

    private static SpectralCountNamePanel m_panel = null;
    private JTextField m_spectralCountNameTextField;
    private JTextArea m_descriptionTextArea;


    public static SpectralCountNamePanel getSpectralCountNamePanel() {

        if (m_panel == null) {
            m_panel = new SpectralCountNamePanel();
        }

        return m_panel;
    }

    public void reinit() {
        m_spectralCountNameTextField.setText("");
        m_descriptionTextArea.setText("");
    }
    
    private SpectralCountNamePanel() {
        setLayout(new BorderLayout());
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(" Spectral Count "));
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        

        JLabel spectralCountNameLabel = new JLabel("Name:");
        m_spectralCountNameTextField = new JTextField(30);                
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(spectralCountNameLabel, c);            

        c.gridx++;
        c.weightx = 1;
        mainPanel.add(m_spectralCountNameTextField, c);

        JLabel spectralCountDescriptionLabel = new JLabel("Description:");
        m_descriptionTextArea = new JTextArea(10,30);

        JScrollPane scrollPane = new JScrollPane(m_descriptionTextArea);
        c.gridx = 0;
        c.gridy++;
        mainPanel.add(spectralCountDescriptionLabel, c);

        c.gridx++;
        c.weighty = 1;
        mainPanel.add(scrollPane, c);
        
        return mainPanel;
    } 

    public String getSpectralCountName() {
        return m_spectralCountNameTextField.getText().trim();
    }

    public JTextField getNameTextField() {
        return m_spectralCountNameTextField;
    }

    public String getSpectralCountDescription() {
        return m_descriptionTextArea.getText();
    }

    
}
