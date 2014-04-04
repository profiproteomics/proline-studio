package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.node.IdentificationTree;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class CreateXICDialog extends DefaultDialog {

    private static final int STEP_PANEL_DEFINE_GROUPS = 0;
    private static final int STEP_PANEL_MODIFY_GROUPS = 1;
    private static final int STEP_PANEL_DEFINE_SAMPLE_ANALYSIS = 2;
    private int m_step = STEP_PANEL_DEFINE_GROUPS;
    private static CreateXICDialog m_singletonDialog = null;

    public static CreateXICDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new CreateXICDialog(parent);
        }


        return m_singletonDialog;
    }

    public CreateXICDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Create XIC Quantitation");

        setHelpURL(null); //JPM.TODO

        setButtonVisible(DefaultDialog.BUTTON_DEFAULT, false);
        setStatusVisible(false);

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));

        setInternalComponent(DefineBioGroupsPanel.getDefineBioGroupsPanel());


    }

    @Override
    protected boolean okCalled() {

        if (m_step == STEP_PANEL_DEFINE_GROUPS) {
            
            // check values
            DefineBioGroupsPanel defineBioGroupsPanel = DefineBioGroupsPanel.getDefineBioGroupsPanel();
            String quantitationName = defineBioGroupsPanel.getQuantitationName();
            if (quantitationName.length() == 0) {

                setStatus(true, "You must fill the Quantitation Name");
                highlight(defineBioGroupsPanel.getQuantitationNameTextField());
                return false;

            }
            
            String groupPrefix = defineBioGroupsPanel.getGroupPrefix();
            if (groupPrefix.length() == 0) {

                setStatus(true, "You must fill the Group Prefix");
                highlight(defineBioGroupsPanel.getGroupPrefixTextField());
                return false;

            }
            
            String samplePrefix = defineBioGroupsPanel.getSamplePrefix();
            if (samplePrefix.length() == 0) {

                setStatus(true, "You must fill the Sample Prefix");
                highlight(defineBioGroupsPanel.getSamplePrefixTextField());
                return false;

            }
            
            
            setButtonName(DefaultDialog.BUTTON_OK, "OK");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));

            
            int nbGroups = defineBioGroupsPanel.getGroupNumber();

            int nbSamples = defineBioGroupsPanel.getSampleNumber();


            JScrollPane scrollPane = new JScrollPane();
            DefinePanel definePanel = new DefinePanel(nbGroups, groupPrefix, nbSamples, samplePrefix);
            scrollPane.setViewportView(definePanel);

            replaceInternaleComponent(scrollPane);

            setResizable(true);
            setSize(new Dimension(600, 500));

            revalidate();
            repaint();

            m_step = STEP_PANEL_MODIFY_GROUPS;

            return false;
        } else if (m_step == STEP_PANEL_MODIFY_GROUPS) { // STEP_PANEL_DEFINE_SAMPLE_ANALYSIS

            return true;
        } else {  // STEP_PANEL_DEFINE_SAMPLE_ANALYSIS
            return true;
        }

    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    public static class DefineBioGroupsPanel extends JPanel {

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

    public static class DefinePanel extends JPanel {

        private ArrayList<JTextField> groupsList;
        private HashMap<JTextField, ArrayList<JTextField>> samplesMap;
        private HashMap<JTextField, ArrayList<DataSetData>> sampleAnalysisMap;
        private JList m_sampleAnalysisList = new JList(new DefaultListModel());

        private JTextField m_currentSampleTextField = null;

        public DefinePanel(int nbGroups, String defaultGroupPrefix, int nbSamples, String defaultSamplePrefix) {

            sampleAnalysisMap = new HashMap<>();
            
            groupsList = new ArrayList<>(nbGroups);
            samplesMap = new HashMap<>();


            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);


            c.gridx = 0;
            c.gridy = 0;
            JLabel biologicalGroupsLabel = new JLabel("Biological Groups");
            Font boldFont = biologicalGroupsLabel.getFont().deriveFont(Font.BOLD);
            biologicalGroupsLabel.setFont(boldFont);
            add(biologicalGroupsLabel, c);

            c.gridx++;
            JLabel biologicalSamplesLabel = new JLabel("Biological Samples");
            biologicalSamplesLabel.setFont(boldFont);
            add(biologicalSamplesLabel, c);

            c.gridx = 4;
            JLabel sampleAnalysisLabel = new JLabel("Sample Analysis");
            sampleAnalysisLabel.setFont(boldFont);
            add(sampleAnalysisLabel, c);


            c.gridy++;
            for (int i = 0; i < nbGroups; i++) {
                c.gridx = 0;
                JTextField groupTextField = new JTextField(defaultGroupPrefix + " " + String.valueOf(i));
                add(groupTextField, c);
                groupsList.add(groupTextField);

                ArrayList<JTextField> samplesList = new ArrayList<>(nbSamples);
                samplesMap.put(groupTextField, samplesList);

                for (int j = 0; j < nbSamples; j++) {
                    c.gridx = 1;
                    final JTextField sampleTextField = new JTextField(defaultSamplePrefix + " " + String.valueOf(j));
                    if ((i==0) && (j==0)) {
                        m_currentSampleTextField = sampleTextField;
                    }
                    add(sampleTextField, c);
                    samplesList.add(sampleTextField);
                    sampleAnalysisMap.put(sampleTextField, new ArrayList<DataSetData>());

                    c.gridx++;
                    JButton deleteSampleButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
                    deleteSampleButton.setMargin(new Insets(1, 1, 1, 1));
                    add(deleteSampleButton, c);
                    c.gridx++;
                    JButton sampleAnalysisButton = new JButton(IconManager.getIcon(IconManager.IconType.ARROW_8X7));
                    sampleAnalysisButton.setMargin(new Insets(1, 1, 1, 1));
                    sampleAnalysisButton.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ArrayList<DataSetData> datasetList = sampleAnalysisMap.get(sampleTextField);
                            
                            // data from jlist is copied to previous sampleTextField
                            DefaultListModel model = (DefaultListModel) m_sampleAnalysisList.getModel();
                            if (m_currentSampleTextField != null) {
                                
                                ArrayList<DataSetData> list = sampleAnalysisMap.get(m_currentSampleTextField);
                                list.clear();
                                int size = model.getSize();
                                for (int i=0;i<size; i++) {
                                    list.add((DataSetData) model.get(i));
                                }
                            }
                            
                            
                            // copy data from newly selected sample Text Field to JList
                            m_currentSampleTextField = sampleTextField;
                            ArrayList<DataSetData> list = sampleAnalysisMap.get(sampleTextField);
                            
                            model.clear();
                            for (int i=0;i<list.size();i++) {
                                model.addElement(list.get(i));
                            }
                        }
                        
                    });
                    add(sampleAnalysisButton, c);
                    c.gridy++;
                }

                JButton addButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
                addButton.setMargin(new Insets(1, 1, 1, 1));
                add(addButton, c);

                c.gridy++;


                if (i != nbGroups - 1) {
                    JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
                    c.gridx = 0;
                    c.gridwidth = 3;
                    add(separator, c);
                    c.gridwidth = 1;
                    c.gridy++;
                }
            }

            c.gridheight = c.gridy;
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 4;
            c.gridy = 1;



            JPanel sampleAnalysisPanel = createSampleAnalysisPanel();

            add(sampleAnalysisPanel, c);

        }

        private JPanel createSampleAnalysisPanel() {
            JPanel sampleAnalysisPanel = new JPanel();

            sampleAnalysisPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;


            JScrollPane sampleAnalysisScrollPane = new JScrollPane();
            sampleAnalysisScrollPane.setViewportView(m_sampleAnalysisList);

            sampleAnalysisPanel.add(sampleAnalysisScrollPane, c);



            SelectionTree tree = new SelectionTree(IdentificationTree.getCurrentTree().copyRootNodeForSelection(), m_sampleAnalysisList);
            JScrollPane treeScrollPane = new JScrollPane();
            treeScrollPane.setViewportView(tree);

            c.gridy++;
            sampleAnalysisPanel.add(treeScrollPane, c);

            return sampleAnalysisPanel;
        }

        public static class SelectionTree extends RSMTree implements TreeWillExpandListener {

            private JList m_jlist;

            private SelectionTree(RSMNode top, JList jlist) {

                m_jlist = jlist;

                initTree(top);

                startLoading(top);

            }

            @Override
            protected final void initTree(RSMNode top) {
                super.initTree(top);

                addTreeWillExpandListener(this);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {

                    // display All imported rset on double click
                    RSMNode[] selectedNodes = getSelectedNodes();
                    int nbNodes = selectedNodes.length;
                    for (int i = 0; i < nbNodes; i++) {
                        RSMNode n = selectedNodes[i];
                        if ((n.getType() == RSMNode.NodeTypes.DATA_SET) && ((RSMDataSetNode) n).hasResultSummary()) {
                            // add node to JList
                            DefaultListModel listModel = (DefaultListModel) m_jlist.getModel();
                            listModel.addElement(n.getData());
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

                TreePath path = event.getPath();
                RSMNode nodeExpanded = (RSMNode) path.getLastPathComponent();

                // check if the node contains a GlassHourNode (ie : children are not loaded)
                if (nodeExpanded.getChildCount() > 0) {
                    RSMNode childNode = (RSMNode) nodeExpanded.getChildAt(0);
                    if (childNode.getType() == RSMNode.NodeTypes.HOUR_GLASS) {

                        startLoading(nodeExpanded);
                    }
                }

            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        }
    }
}
