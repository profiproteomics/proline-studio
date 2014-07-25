package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.data.ChangeTypicalRule;
import fr.proline.studio.dpm.task.ValidationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.*;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;

/**
 * Dialog used to validate an identification and ask the creation of a Result
 * Summary from a Result Set
 *
 * @author JM235353
 */
public class ValidationDialog extends DefaultDialog {

    private static ValidationDialog m_singletonDialog = null;
    
//    private final static String[] FDR_ESTIMATOR_VALUES = {null, "Default", "Competition Based"};
//    private final static String[] FDR_ESTIMATOR_VALUES_ASSOCIATED_KEYS = {null, "false", "true"};
    private final static String[] FDR_ON_VALUES = {null, "Score", "e-Value", "Identity p-Value", "Homology p-Value"};
    private final static String[] FDR_ON_VALUES_ASSOCIATED_KEYS = {null, "SCORE", "MASCOT_EVALUE", "SCORE_IT_P-VALUE", "SCORE_HT_P-VALUE"};
    
    private final static  String[] SCORING_TYPE_OPTIONS = { "Standard", "Mascot Modified Mudpit" };
    private final static  String[] SCORING_TYPE_VALUES = { "mascot:standard score", "mascot:modified mudpit score" };
    
    private ParameterList m_parameterList;
    
    private AbstractParameter[] m_psmPrefilterParameters;
    private AbstractParameter[] m_proteinPrefilterParameters;
    
    private AbstractParameter m_fdrFilterParameter;
    private ObjectParameter<String> m_fdrOnValueParameter;
    private AbstractParameter m_proteinFdrFilterParameter;
    
    private JPanel m_psmPrefiltersSelectedPanel = null;
    private JComboBox m_psmPrefilterJComboBox = null;
    private JButton m_addPsmPrefilterButton = null;
    
    private JPanel m_proteinPrefiltersSelectedPanel = null;
    private JComboBox m_proteinPrefilterJComboBox = null;
    private JButton m_addProteinPrefilterButton = null;
    
    private JLabel m_fdrLabel = null;
    private JTextField m_fdrTextField = null;
    private JLabel m_fdrPercentageLabel = null;
//    private JComboBox m_fdrEstimatorComboBox = null;
    private JLabel m_proteinFdrLabel = null;
    private JTextField m_proteinFdrTextField = null;
    private JLabel m_proteinFdrPercentageLabel = null;
    private JComboBox m_fdrOnValueComboBox = null;
    private JCheckBox m_fdrCheckbox = null;
    private JCheckBox m_proteinFdrCheckbox = null;

    private JCheckBox m_typicalProteinMatchCheckBox;
    private ChangeTypicalProteinPanel changeTypicalPanel = null;
    
    private JComboBox m_proteinScoringTypeCbx = null;
    
    public enum DecoyStatus {
        WAITING,
        HAS_DECOY,
        NO_DECOY
    }
    
    private DecoyStatus m_hasDecoy  = DecoyStatus.WAITING;
    
    public static ValidationDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ValidationDialog(parent);
        }
        
        

        return m_singletonDialog;
    }

    public ValidationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Identification Validation");

        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:rsvalidation");
        
        m_parameterList = new ParameterList("Validation");
        createParameters();
        m_parameterList.updateIsUsed();

        setInternalComponent(createInternalPanel());

        initPsmPrefilterPanel();
        initProteinPrefilterPanel();
        
        restoreScoringTypeParameter();
        
        restoreTypicalProteinParameters();
        

    }

    public synchronized void setHasDecoy(DecoyStatus hasDecoy) {
        m_hasDecoy = hasDecoy;
    }
    public synchronized DecoyStatus getHasDecoy() {
        return m_hasDecoy;
    }
    
    public void setDatasetList(final ArrayList<DDataset> datasetList) {

        setHasDecoy(DecoyStatus.WAITING);
        
        // we need to load ResultSet of dataset to be sure if we
        // can validate on Decoy Resultset or not
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
                int nb = datasetList.size();
                for (int i=0;i<nb;i++) {
                    DDataset dataset = datasetList.get(i);
                    ResultSet rset = dataset.getResultSet();
                    
                    if (rset.getDecoyResultSet() == null) {
                        setHasDecoy(DecoyStatus.NO_DECOY);
                        return;
                    }
                }
                setHasDecoy(DecoyStatus.HAS_DECOY);
            }
        };

        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadRsetAndRsm(datasetList);
        task.setPriority(AbstractDatabaseTask.Priority.HIGH_3); // must be done as fast as possible to avoid to let the use wait
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                                
    }
    
    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        internalPanel.add(createPSMPanel(), c);

        c.gridy++;
        internalPanel.add(createProteinSetFilterPanel(), c);

        c.gridy++;
        internalPanel.add(createTypicalProteinPanel(), c);
        
        
        return internalPanel;
    }

    private JPanel createPSMPanel() {
        JPanel psmPanel = new JPanel(new GridBagLayout());
        psmPanel.setBorder(BorderFactory.createTitledBorder(" PSM"));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        psmPanel.add(createPsmPreFilterPanel(), c);

        c.gridy++;
        psmPanel.add(createFDRFilterPanel(), c);

//        c.gridy++;
//        psmPanel.add(createFDREstimatorPanel(), c);

        return psmPanel;
    }

    private JPanel createPsmPreFilterPanel() {
        JPanel prefilterPanel = new JPanel(new GridBagLayout());
        prefilterPanel.setBorder(BorderFactory.createTitledBorder(" Prefilter(s) "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        m_psmPrefiltersSelectedPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        prefilterPanel.add(m_psmPrefiltersSelectedPanel, c);

        
        
        m_psmPrefilterJComboBox = new JComboBox(m_psmPrefilterParameters);
        m_psmPrefilterJComboBox.setRenderer(new ParameterComboboxRenderer(null));
        m_addPsmPrefilterButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
        m_addPsmPrefilterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        prefilterPanel.add(m_psmPrefilterJComboBox, c);

        c.gridx++;
        c.weightx = 0;
        prefilterPanel.add(m_addPsmPrefilterButton, c);

        c.gridx++;
        c.weightx = 1.0;
        prefilterPanel.add(Box.createHorizontalBox(), c);


        m_addPsmPrefilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractParameter p = (AbstractParameter) m_psmPrefilterJComboBox.getSelectedItem();
                if (p == null) {
                    return;
                }
                p.setUsed(true);
                initPsmPrefilterPanel();
            }
        });

        return prefilterPanel;
    }

    private void initPsmPrefilterPanel() {

        m_psmPrefiltersSelectedPanel.removeAll();
        m_psmPrefilterJComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        int nbUsed = 0;
        boolean putAndInFront = false;
        int nbParameters = m_psmPrefilterParameters.length;
        for (int i = 0; i < nbParameters; i++) {
            final AbstractParameter p = m_psmPrefilterParameters[i];

            if ( (p != null) && (p.isUsed())) {

                c.gridx = 0;
                if (putAndInFront) {
                    m_psmPrefiltersSelectedPanel.add(new JLabel("AND"), c);
                } else {
                    putAndInFront = true;
                    m_psmPrefiltersSelectedPanel.add(new JLabel("   "), c);
                }
                
                c.gridx++;               
                JLabel prefilterNameLabel = new JLabel(p.getName());
                prefilterNameLabel.setHorizontalAlignment(JLabel.RIGHT);
                m_psmPrefiltersSelectedPanel.add(prefilterNameLabel, c);

                if(p.hasComponent()){
                    c.gridx++;
                    JLabel cmpLabel = new JLabel(((String) p.getAssociatedData()));
                    cmpLabel.setHorizontalAlignment(JLabel.CENTER);
                    m_psmPrefiltersSelectedPanel.add(cmpLabel, c);
                
                    c.weightx = 1;
                    c.gridx++;
                    m_psmPrefiltersSelectedPanel.add(p.getComponent(), c);
                } else {
                    c.gridx++;
                    c.gridx++;                                        
                }

                c.weightx = 0;
                c.gridx++;
                JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
                removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
                removeButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        p.setUsed(false);
                        initPsmPrefilterPanel();
                    }
                });
                m_psmPrefiltersSelectedPanel.add(removeButton, c);

                nbUsed++;

                c.gridy++;
            } else {
                m_psmPrefilterJComboBox.addItem(p);
            }

        }

        boolean hasUnusedParameters = (nbUsed != nbParameters);
        m_psmPrefilterJComboBox.setVisible(hasUnusedParameters);
        m_addPsmPrefilterButton.setVisible(hasUnusedParameters);


        repack();
    }

    private JPanel createFDRFilterPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR Filter "));


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        boolean fdrUsed = m_fdrFilterParameter.isUsed();
        m_fdrCheckbox = new JCheckBox();
        m_fdrLabel = new JLabel("Ensure FDR <=");
        m_fdrPercentageLabel = new JLabel("%  on");
        m_fdrCheckbox.setSelected(fdrUsed);

        updateFdrObjects(fdrUsed);


        c.gridx = 0;
        c.gridy = 0;
        fdrPanel.add(m_fdrCheckbox, c);

        c.gridx++;
        fdrPanel.add(m_fdrLabel, c);

        c.gridx++;
        fdrPanel.add(m_fdrTextField, c);

        c.gridx++;

        fdrPanel.add(m_fdrPercentageLabel, c);

        c.gridx++;
        fdrPanel.add(m_fdrOnValueComboBox, c);


        c.gridx++;
        c.weightx = 1.0;
        fdrPanel.add(Box.createHorizontalBox(), c);

        m_fdrCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (m_fdrCheckbox.isSelected());
                updateFdrObjects(enabled);
            }
        });


        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                boolean enabled = (m_fdrCheckbox.isSelected());
                if (!enabled) {
                    m_fdrCheckbox.setSelected(true);
                    updateFdrObjects(true);

                    if (e.getSource().equals(m_fdrTextField)) {
                        m_fdrTextField.requestFocusInWindow();
                    }

                }
            }
        };

        m_fdrLabel.addMouseListener(actionOnClick);
        m_fdrTextField.addMouseListener(actionOnClick);


        return fdrPanel;
    }

    private void updateFdrObjects(boolean enabled) {
        m_fdrLabel.setEnabled(enabled);
        m_fdrTextField.setEnabled(enabled);
        m_fdrPercentageLabel.setEnabled(enabled);
        m_fdrOnValueComboBox.setEnabled(enabled);
        m_fdrFilterParameter.setUsed(enabled);
        m_fdrOnValueParameter.setUsed(enabled);
    }



    private JPanel createProteinSetFilterPanel() {
        JPanel proteinSetFilterPanel = new JPanel(new GridBagLayout());
        proteinSetFilterPanel.setBorder(BorderFactory.createTitledBorder(" Protein Set "));


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        proteinSetFilterPanel.add(createProteinPreFilterPanel(), c);
        
        c.gridy++;
        proteinSetFilterPanel.add(createProteinFDRFilterPanel(), c);
        
        c.gridy++;
        proteinSetFilterPanel.add(createScoringTypePanel(), c);
        
        return proteinSetFilterPanel;
    }

    private JPanel createTypicalProteinPanel() {
        JPanel typicalProteinPanel = new JPanel(new GridBagLayout());
        typicalProteinPanel.setBorder(BorderFactory.createTitledBorder("Set Typical Protein Match "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
//        c.gridwidth = 2;
        m_typicalProteinMatchCheckBox = new JCheckBox("Using rules (in priority order):");
        m_typicalProteinMatchCheckBox.setHorizontalAlignment(SwingConstants.RIGHT);
        typicalProteinPanel.add(m_typicalProteinMatchCheckBox, c);

        m_typicalProteinMatchCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = m_typicalProteinMatchCheckBox.isSelected();
                changeTypicalPanel.enableRules(enabled);
            }
        });
        
        c.gridx=0;
        c.gridy++;
        c.gridwidth = 2;
        changeTypicalPanel = new ChangeTypicalProteinPanel();
        typicalProteinPanel.add(changeTypicalPanel, c);

        return typicalProteinPanel;
    }
    
    private JPanel createProteinFDRFilterPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR Filter "));


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        
        boolean parameterUsed = m_proteinFdrFilterParameter.isUsed();
        m_proteinFdrCheckbox = new JCheckBox("");
        m_proteinFdrLabel = new JLabel("Protein FDR <=");
        m_proteinFdrCheckbox.setSelected(parameterUsed);
        m_proteinFdrPercentageLabel = new JLabel(" %");

        updateproteinFdrObjects(parameterUsed);

        c.gridx = 0;
        c.gridy = 0;
        fdrPanel.add(m_proteinFdrCheckbox, c);

        c.gridx++;
        fdrPanel.add(m_proteinFdrLabel, c);

        c.gridx++;
        fdrPanel.add(m_proteinFdrTextField, c);
        
        c.gridx++;
        fdrPanel.add(m_proteinFdrPercentageLabel, c);

        c.gridx++;
        c.weightx = 1.0;
        fdrPanel.add(Box.createHorizontalBox(), c);

        m_proteinFdrCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (m_proteinFdrCheckbox.isSelected());
                updateproteinFdrObjects(enabled);
            }
        });

        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                boolean enabled = (m_proteinFdrCheckbox.isSelected());
                if (!enabled) {
                    m_proteinFdrCheckbox.setSelected(true);
                    updateproteinFdrObjects(true);
                    if (e.getSource().equals(m_proteinFdrTextField)) {
                        m_proteinFdrTextField.requestFocusInWindow();
                    }
                }
            }
        };

        m_proteinFdrLabel.addMouseListener(actionOnClick);
        m_proteinFdrTextField.addMouseListener(actionOnClick);




        return fdrPanel;
    }
    
    private JPanel createScoringTypePanel() {
        JPanel scoringTypePanel = new JPanel(new GridBagLayout());
        //scoringTypePanel.setBorder(BorderFactory.createTitledBorder(" Scoring "));


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        JLabel proteinScoringTypeLabel = new JLabel("Scoring Type: ");
        m_proteinScoringTypeCbx = new JComboBox(SCORING_TYPE_OPTIONS);

        c.gridx = 0;
        c.gridy = 0;
        scoringTypePanel.add(proteinScoringTypeLabel, c);

        c.gridx++;
        scoringTypePanel.add(m_proteinScoringTypeCbx, c);

        c.gridx++;
        c.weightx = 1.0;
        scoringTypePanel.add(Box.createHorizontalBox(), c);


        return scoringTypePanel;
    }
    
    private JPanel createProteinPreFilterPanel() {
        JPanel prefilterPanel = new JPanel(new GridBagLayout());
        prefilterPanel.setBorder(BorderFactory.createTitledBorder(" Prefilter(s) "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        m_proteinPrefiltersSelectedPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        prefilterPanel.add(m_proteinPrefiltersSelectedPanel, c);

        
        
        m_proteinPrefilterJComboBox = new JComboBox(m_proteinPrefilterParameters);
        m_proteinPrefilterJComboBox.setRenderer(new ParameterComboboxRenderer(null));
        m_addProteinPrefilterButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
        m_addProteinPrefilterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        prefilterPanel.add(m_proteinPrefilterJComboBox, c);

        c.gridx++;
        c.weightx = 0;
        prefilterPanel.add(m_addProteinPrefilterButton, c);

        c.gridx++;
        c.weightx = 1.0;
        prefilterPanel.add(Box.createHorizontalBox(), c);


        m_addProteinPrefilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractParameter p = (AbstractParameter) m_proteinPrefilterJComboBox.getSelectedItem();
                if (p == null) {
                    return;
                }
                p.setUsed(true);
                initProteinPrefilterPanel();
            }
        });

        return prefilterPanel;
    }

    private void initProteinPrefilterPanel() {

        m_proteinPrefiltersSelectedPanel.removeAll();
        m_proteinPrefilterJComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        int nbUsed = 0;
        boolean putAndInFront = false;
        int nbParameters = m_proteinPrefilterParameters.length;
        for (int i = 0; i < nbParameters; i++) {
            final AbstractParameter p = m_proteinPrefilterParameters[i];

            if ( (p != null) && (p.isUsed())) {

                c.gridx = 0;
                if (putAndInFront) {
                    m_proteinPrefiltersSelectedPanel.add(new JLabel("AND"), c);
                } else {
                    putAndInFront = true;
                    m_proteinPrefiltersSelectedPanel.add(new JLabel("   "), c);
                }
                
                c.gridx++;
                JLabel prefilterNameLabel = new JLabel(p.getName());
                prefilterNameLabel.setHorizontalAlignment(JLabel.RIGHT);
                m_proteinPrefiltersSelectedPanel.add(prefilterNameLabel, c);

                c.gridx++;
                JLabel cmpLabel = new JLabel(((String) p.getAssociatedData()));
                cmpLabel.setHorizontalAlignment(JLabel.CENTER);
                m_proteinPrefiltersSelectedPanel.add(cmpLabel, c);
                
                c.weightx = 1;
                c.gridx++;
                m_proteinPrefiltersSelectedPanel.add(p.getComponent(), c);

                c.weightx = 0;
                c.gridx++;
                JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
                removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
                removeButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        p.setUsed(false);
                        initProteinPrefilterPanel();
                    }
                });
                m_proteinPrefiltersSelectedPanel.add(removeButton, c);

                nbUsed++;

                c.gridy++;
            } else {
                m_proteinPrefilterJComboBox.addItem(p);
            }

        }

        boolean hasUnusedParameters = (nbUsed != nbParameters);
        m_proteinPrefilterJComboBox.setVisible(hasUnusedParameters);
        m_addProteinPrefilterButton.setVisible(hasUnusedParameters);


        repack();
    }
    
    
    private void restoreScoringTypeParameter() {
        Preferences preferences = NbPreferences.root();
        
        String scoringType = preferences.get("ValidationScoringType", SCORING_TYPE_OPTIONS[0]);
        m_proteinScoringTypeCbx.setSelectedItem(scoringType);
        
    }
    
    private void restoreTypicalProteinParameters() {
        Preferences preferences = NbPreferences.root();
        
        boolean useTypicalProteinRegex = preferences.getBoolean("UseTypicalProteinRegex", true);
        
        m_typicalProteinMatchCheckBox.setSelected(useTypicalProteinRegex);
        changeTypicalPanel.restoreInitialParameters();
        changeTypicalPanel.enableRules(useTypicalProteinRegex);
    }
    
    private void updateproteinFdrObjects(boolean enabled) {
        m_proteinFdrLabel.setEnabled(enabled);
        m_proteinFdrTextField.setEnabled(enabled);
        m_proteinFdrPercentageLabel.setEnabled(enabled);
        m_proteinFdrFilterParameter.setUsed(enabled);
    }

    private void createParameters() {
        m_psmPrefilterParameters = new AbstractParameter[9];
        m_psmPrefilterParameters[0] = null;
        m_psmPrefilterParameters[1] = new IntegerParameter(ValidationTask.RANK_FILTER_KEY, ValidationTask.RANK_FILTER_NAME, new JTextField(6), new Integer(5), new Integer(0), new Integer(10));
        m_psmPrefilterParameters[1].setAssociatedData("<=");
        m_psmPrefilterParameters[2] = new IntegerParameter(ValidationTask.PEP_LENGTH_FILTER_KEY, ValidationTask.PEP_LENGTH_FILTER_NAME, new JTextField(6), new Integer(4), new Integer(4), null);
        m_psmPrefilterParameters[2].setAssociatedData(">=");
        m_psmPrefilterParameters[3] = new DoubleParameter(ValidationTask.SCORE_FILTER_KEY, ValidationTask.SCORE_FILTER_NAME, new JTextField(6), new Double(0), new Double(0), (Double) null);
        m_psmPrefilterParameters[3].setAssociatedData(">=");
        m_psmPrefilterParameters[4] = new DoubleParameter(ValidationTask.MASCOT_EVAL_FILTER_KEY, ValidationTask.MASCOT_EVAL_FILTER_NAME, new JTextField(6), new Double(1), new Double(0), new Double(1));
        m_psmPrefilterParameters[4].setAssociatedData("<=");
        m_psmPrefilterParameters[5] = new DoubleParameter(ValidationTask.MASCOT_IT_SCORE_FILTER_KEY, ValidationTask.MASCOT_IT_SCORE_FILTER_NAME, new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        m_psmPrefilterParameters[5].setAssociatedData("=");   
        m_psmPrefilterParameters[6] = new DoubleParameter(ValidationTask.MASCOT_HT_SCORE_FILTER_KEY, ValidationTask.MASCOT_HT_SCORE_FILTER_NAME, new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        m_psmPrefilterParameters[6].setAssociatedData("=");
        JCheckBox singlePerQueryCB = new JCheckBox("post FDR");
        m_psmPrefilterParameters[7] = new BooleanParameter(ValidationTask.SINGLE_PSM_QUERY_FILTER_KEY, ValidationTask.SINGLE_PSM_QUERY_FILTER_NAME, singlePerQueryCB, false);
        m_psmPrefilterParameters[7].setAssociatedData(":");        
        m_psmPrefilterParameters[8] = new NoneParameter(ValidationTask.SINGLE_PSM_RANK_FILTER_KEY, ValidationTask.SINGLE_PSM_RANK_FILTER_NAME);
//        m_psmPrefilterParameters[8].setAssociatedData("=");

        for (int i = 0; i < m_psmPrefilterParameters.length; i++) {
            AbstractParameter p = m_psmPrefilterParameters[i];
            if (p == null) {
                continue;
            }
            p.setUsed(false);
            m_parameterList.add(p);
        }

        m_proteinPrefilterParameters = new AbstractParameter[2];
        m_proteinPrefilterParameters[0] = null;
        m_proteinPrefilterParameters[1] = new IntegerParameter(ValidationTask.SPECIFIC_PEP_FILTER_KEY, ValidationTask.SPECIFIC_PEP_FILTER_NAME, new JTextField(6), new Integer(1), new Integer(1), null);
        m_proteinPrefilterParameters[1].setAssociatedData(">=");
        
         for (int i = 0; i < m_proteinPrefilterParameters.length; i++) {
            AbstractParameter p = m_proteinPrefilterParameters[i];
            if (p == null) {
                continue;
            }
            p.setUsed(false);
            m_parameterList.add(p);
        }
        
        
        m_fdrTextField = new JTextField(5);
        m_fdrFilterParameter = new DoubleParameter("expected_fdr", "FDR", m_fdrTextField, new Double(5), new Double(0), new Double(10));
        m_fdrFilterParameter.setUsed(false);
        m_parameterList.add(m_fdrFilterParameter);

        m_fdrOnValueComboBox = new JComboBox(FDR_ON_VALUES);
        m_fdrOnValueParameter = new ObjectParameter<>("expected_fdr_parameter", "FDR Variable", m_fdrOnValueComboBox, FDR_ON_VALUES,FDR_ON_VALUES_ASSOCIATED_KEYS,  0, null);
        m_fdrOnValueParameter.setUsed(false);
        m_parameterList.add(m_fdrOnValueParameter);

//        m_fdrEstimatorComboBox = new JComboBox(FDR_ESTIMATOR_VALUES);
//        ObjectParameter<String> fdrEstimatorParameter = new ObjectParameter<>("use_td_competition", "FDR Estimator", m_fdrEstimatorComboBox, FDR_ESTIMATOR_VALUES, FDR_ESTIMATOR_VALUES_ASSOCIATED_KEYS, 0, null);
//        m_parameterList.add(fdrEstimatorParameter);

        m_proteinFdrTextField = new JTextField(5);
        m_proteinFdrFilterParameter = new DoubleParameter("protein_expected_fdr", "Protein FDR", m_proteinFdrTextField, new Double(5), new Double(0), new Double(10));
        m_proteinFdrFilterParameter.setUsed(false);
        m_parameterList.add(m_proteinFdrFilterParameter);



    }

    public HashMap<String, String> getArguments() {
        return m_parameterList.getValues();
    }
    
    public List<ChangeTypicalRule> getChangeTypicalRules() {
        return changeTypicalPanel.getChangeTypicalRules();
    }
    
    
       
    public String getScoringType() {
        return SCORING_TYPE_VALUES[m_proteinScoringTypeCbx.getSelectedIndex()];
    }
    
    @Override
    protected boolean okCalled() {

        boolean aFdrSelected = m_fdrCheckbox.isSelected() || m_proteinFdrCheckbox.isSelected();

        if (aFdrSelected && (getHasDecoy() == DecoyStatus.WAITING)) {
            // we have not finished to read data for decoy check
            // we are waiting for one second
            setBusy(true);
            javax.swing.Timer t = new Timer(1000, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    okCalled();
                }
            });
            t.setRepeats(false);
            t.start();
            return false;
        }
         
        setBusy(false);
        
        if (aFdrSelected && (getHasDecoy() == DecoyStatus.NO_DECOY)) {
            setStatus(true, "A FDR can not be calculated with no Decoy Data");
            if (m_fdrCheckbox.isSelected()) {
                highlight(m_fdrCheckbox);
            } else {
                highlight(m_proteinFdrCheckbox);
            }
            return false;
        }
            
        // check parameters
        ParameterError error = m_parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }


        // Save Parameters        
        m_parameterList.saveParameters();

        
        Preferences preferences = NbPreferences.root();
        
        // save scoring type
         preferences.put("ValidationScoringType", m_proteinScoringTypeCbx.getSelectedItem().toString());
        
        // save specific Typical Protein parameters
         changeTypicalPanel.savePreference();
        preferences.putBoolean("UseTypicalProteinRegex", m_typicalProteinMatchCheckBox.isSelected());

        
        return true;

    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    protected boolean defaultCalled() {
        m_parameterList.initDefaults();

        return false;
    }
}
