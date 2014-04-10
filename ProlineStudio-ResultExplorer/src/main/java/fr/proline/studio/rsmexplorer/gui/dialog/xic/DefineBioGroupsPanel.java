package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.*;

public class DefineBioGroupsPanel extends JPanel {

    private static DefineBioGroupsPanel m_panel = null;
    private JTextField m_quantitationNameTextField;
    private JTextField m_samplePrefixTextField;
    private JSpinner m_nbSamplesSpinner;
    private JTextField m_groupPrefixTextField;
    private JSpinner m_nbGroupsSpinner;

    public static DefineBioGroupsPanel getDefineBioGroupsPanel() {
        if (m_panel == null) {
            m_panel = new DefineBioGroupsPanel();
        }

        return m_panel;
    }

    private DefineBioGroupsPanel() {

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        JLabel quantitationNameLabel = new JLabel("Quantitation Name:");
        m_quantitationNameTextField = new JTextField(30);
        c.gridx = 0;
        c.gridy = 0;
        add(quantitationNameLabel, c);

        c.gridx++;
        c.weightx = 1;
        add(m_quantitationNameTextField, c);


        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        add(createBiologicalGroupPanel(), c);

        c.gridy++;
        add(createBiologicalSamplePanel(), c);



    }

    private JPanel createBiologicalGroupPanel() {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        p.setBorder(BorderFactory.createTitledBorder(" Biological Groups "));

        JLabel defaultPrefixLabel = new JLabel("Default Prefix:");
        JLabel numberLabel = new JLabel("Number of Groups:");


        m_groupPrefixTextField = new JTextField();
        m_nbGroupsSpinner = new JSpinner();
        m_nbGroupsSpinner.setModel(new SpinnerNumberModel(1, 1, 10000, 1));

        c.gridx = 0;
        c.gridy = 0;
        p.add(defaultPrefixLabel, c);

        c.gridx++;
        c.weightx = 1;
        p.add(m_groupPrefixTextField, c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        p.add(numberLabel, c);

        c.gridx++;
        c.weightx = 1;
        p.add(m_nbGroupsSpinner, c);
        c.weightx = 0;

        return p;
    }

    private JPanel createBiologicalSamplePanel() {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        p.setBorder(BorderFactory.createTitledBorder(" Biological Samples "));

        JLabel defaultPrefixLabel = new JLabel("Default Prefix:");
        JLabel numberLabel = new JLabel("Number of Samples:");

        m_samplePrefixTextField = new JTextField();
        m_nbSamplesSpinner = new JSpinner();
        m_nbSamplesSpinner.setModel(new SpinnerNumberModel(1, 1, 10000, 1));

        c.gridx = 0;
        c.gridy = 0;
        p.add(defaultPrefixLabel, c);

        c.gridx++;
        c.weightx = 1;
        p.add(m_samplePrefixTextField, c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        p.add(numberLabel, c);

        c.gridx++;
        c.weightx = 1;
        p.add(m_nbSamplesSpinner, c);
        c.weightx = 0;

        return p;
    }

    public String getQuantitationName() {
        return m_quantitationNameTextField.getText().trim();
    }

    public JTextField getQuantitationNameTextField() {
        return m_quantitationNameTextField;
    }

    public String getSamplePrefix() {
        return m_samplePrefixTextField.getText().trim();
    }

    public JTextField getSamplePrefixTextField() {
        return m_samplePrefixTextField;
    }

    public String getGroupPrefix() {
        return m_groupPrefixTextField.getText().trim();
    }

    public JTextField getGroupPrefixTextField() {
        return m_groupPrefixTextField;
    }

    public int getSampleNumber() {
        return ((Integer) m_nbSamplesSpinner.getValue()).intValue();
    }

    public int getGroupNumber() {
        return ((Integer) m_nbGroupsSpinner.getValue()).intValue();
    }
}