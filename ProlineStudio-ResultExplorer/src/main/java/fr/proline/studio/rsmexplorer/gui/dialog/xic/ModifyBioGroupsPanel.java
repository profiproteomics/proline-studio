package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.node.xic.RSMBiologicalSampleNode;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.xic.RSMBiologicalGroupNode;
import fr.proline.studio.utils.IconManager;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;

/**
 *
 * @author JM235353
 */
public class ModifyBioGroupsPanel extends JPanel {

    private static ModifyBioGroupsPanel m_singleton = null;
    private ArrayList<JTextField> groupsList;
    private HashMap<JTextField, ArrayList<JTextField>> samplesMap;
    private HashMap<JTextField, ArrayList<DataSetData>> sampleAnalysisMap;

    public static ModifyBioGroupsPanel getDefinePanel(int nbGroups, String defaultGroupPrefix, int nbSamples, final String defaultSamplePrefix) {
        m_singleton = new ModifyBioGroupsPanel(nbGroups, defaultGroupPrefix, nbSamples, defaultSamplePrefix);
        return m_singleton;
    }

    public static ModifyBioGroupsPanel getDefinePanel() {
        return m_singleton;
    }

    private ModifyBioGroupsPanel(int nbGroups, String defaultGroupPrefix, int nbSamples, final String defaultSamplePrefix) {

        JPanel wizardPanel = createWizardPanel();
        JPanel mainPanel = createMainPanel(nbGroups, defaultGroupPrefix, nbSamples, defaultSamplePrefix);
        
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(wizardPanel, c);


        c.gridy++;
        c.weighty = 1;
        
        add(mainPanel, c);

    }
    
    private JPanel createWizardPanel() {
        JPanel wizardPanel = new JPanel();
        wizardPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel wizardLabel = new JLabel("<html><b>Step 2:</b> Modify names and add/remove Biological Samples.</html>");
        wizardLabel.setIcon(IconManager.getIcon(IconManager.IconType.WAND_HAT));
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        wizardPanel.add(wizardLabel, c);
        
        return wizardPanel;
    }
    
    
    private JPanel createMainPanel(int nbGroups, String defaultGroupPrefix, int nbSamples, final String defaultSamplePrefix) {


        
        sampleAnalysisMap = new HashMap<>();

        groupsList = new ArrayList<>(nbGroups);
        samplesMap = new HashMap<>();

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        c.gridx = 0;
        c.gridy = 0;
        JLabel biologicalGroupsLabel = new JLabel("Biological Groups");
        Font boldFont = biologicalGroupsLabel.getFont().deriveFont(Font.BOLD);
        biologicalGroupsLabel.setFont(boldFont);
        mainPanel.add(biologicalGroupsLabel, c);

        c.gridx++;
        JLabel biologicalSamplesLabel = new JLabel("Biological Samples");
        biologicalSamplesLabel.setFont(boldFont);
        mainPanel.add(biologicalSamplesLabel, c);


        c.gridy++;
        for (int i = 0; i < nbGroups; i++) {
            c.gridx = 0;
            final JTextField groupTextField = new JTextField(defaultGroupPrefix + " " + String.valueOf(i), 30);
            c.weightx = 1;
            mainPanel.add(groupTextField, c);
            c.weightx = 0;
            groupsList.add(groupTextField);

            final JButton addButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS)); // added at the end of the group
            final JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);

            final ArrayList<JTextField> samplesList = new ArrayList<>(nbSamples);
            final ArrayList<JButton> deleteButtonList = new ArrayList<>(nbSamples);
            samplesMap.put(groupTextField, samplesList);



            final int _gridyStart = c.gridy;

            for (int j = 0; j < nbSamples; j++) {
                c.gridx = 1;
                final JTextField sampleTextField = new JTextField(defaultSamplePrefix + " " + String.valueOf(j), 30);

                c.weightx = 1;
                mainPanel.add(sampleTextField, c);
                c.weightx = 0;
                samplesList.add(sampleTextField);

                sampleAnalysisMap.put(sampleTextField, new ArrayList<DataSetData>());

                c.gridx++;

                boolean visible = (j == nbSamples - 1);
                JButton deleteSampleButton = createDeleteSampleButton(visible, samplesList, deleteButtonList, sampleTextField, groupTextField, separator, addButton, this);
                deleteButtonList.add(deleteSampleButton);

                c.fill = GridBagConstraints.NONE;

                mainPanel.add(deleteSampleButton, c);
                c.fill = GridBagConstraints.BOTH;




                c.gridy++;
            }

            addButton.setMargin(new Insets(1, 1, 1, 1));
            c.fill = GridBagConstraints.NONE;
            mainPanel.add(addButton, c);
            c.fill = GridBagConstraints.BOTH;

            addButton.addActionListener(new ActionListener() {

                private int m_gridyStart = _gridyStart;

                @Override
                public void actionPerformed(ActionEvent e) {
                    int nb = samplesList.size();
                    c.gridy = m_gridyStart + nb;
                    c.gridx = 1;
                    final JTextField sampleTextField = new JTextField(defaultSamplePrefix + " " + String.valueOf(nb), 30);

                    c.gridwidth = 1;
                    c.weightx = 1;
                    c.weighty = 0;
                    mainPanel.add(sampleTextField, c);
                    c.weightx = 0;
                    samplesList.add(sampleTextField);

                    sampleAnalysisMap.put(sampleTextField, new ArrayList<DataSetData>());

                    deleteButtonList.get(deleteButtonList.size() - 1).setVisible(false);

                    JButton deleteSampleButton = createDeleteSampleButton(true, samplesList, deleteButtonList, sampleTextField, groupTextField, separator, addButton, mainPanel);
                    deleteButtonList.add(deleteSampleButton);
                    c.gridx++;
                    c.fill = GridBagConstraints.NONE;
                    add(deleteSampleButton, c);
                    c.fill = GridBagConstraints.BOTH;

                    deleteButtonList.get(deleteButtonList.size() - 1).setVisible(true);

                    mainPanel.remove(addButton);
                    c.gridy++;
                    c.fill = GridBagConstraints.NONE;
                    mainPanel.add(addButton, c);
                    c.fill = GridBagConstraints.BOTH;


                    mainPanel.revalidate();
                    mainPanel.repaint();
                }
            });

            c.gridy += 100; // let space to add samples

            if (i != nbGroups - 1) {
                // add separator
                c.gridx = 0;
                c.gridwidth = 3;
                mainPanel.add(separator, c);
                c.gridwidth = 1;
                c.gridy++;
            }

        }


        c.gridx = 0;
        c.gridy += 100;
        c.weighty = 1;
        c.gridwidth = 2;
        mainPanel.add(Box.createGlue(), c);

        return mainPanel;
    }

    private JButton createDeleteSampleButton(boolean visible, final ArrayList<JTextField> samplesList, final ArrayList<JButton> deleteButtonList, final JTextField sampleTextField, final JTextField groupTextField, final JSeparator separator, final JButton addButton, final JPanel p) {

        final JButton deleteSampleButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
        deleteSampleButton.setMargin(new Insets(1, 1, 1, 1));
        deleteSampleButton.setVisible(visible);

        deleteSampleButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                p.remove(deleteSampleButton);
                p.remove(sampleTextField);
                samplesList.remove(sampleTextField);
                deleteButtonList.remove(deleteSampleButton);
                if (samplesList.isEmpty()) {
                    samplesMap.remove(groupTextField);
                    p.remove(groupTextField);
                    p.remove(separator);
                    p.remove(addButton);
                } else {
                    deleteButtonList.get(deleteButtonList.size() - 1).setVisible(true);
                }
                p.revalidate();
                p.repaint();
            }
        });


        return deleteSampleButton;
    }

    public RSMNode generateTreeNodes(String quantitationName) {

        RSMDataSetNode rootQuantitationNode = new RSMDataSetNode(new DataSetData(quantitationName, Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION));

        for (int i = 0; i < groupsList.size(); i++) {
            JTextField groupTextField = groupsList.get(i);
            ArrayList<JTextField> sampleList = samplesMap.get(groupTextField);

            RSMBiologicalGroupNode biologicalGroupNode = new RSMBiologicalGroupNode(new DataSetData(groupTextField.getText().toString(), Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            rootQuantitationNode.add(biologicalGroupNode);
              for (int j = 0; j < sampleList.size(); j++) {
                JTextField sampleTextField = sampleList.get(j);
                RSMBiologicalSampleNode biologicalSampleNode = new RSMBiologicalSampleNode(new DataSetData(sampleTextField.getText().toString(), Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
                biologicalGroupNode.add(biologicalSampleNode);

            }

        }

        return rootQuantitationNode;
    }
}