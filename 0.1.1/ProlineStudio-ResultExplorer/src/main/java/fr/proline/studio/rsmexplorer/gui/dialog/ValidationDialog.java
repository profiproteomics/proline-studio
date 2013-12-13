package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.*;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;

/**
 * Dialog used to validate an identification and ask the creation of a Result
 * Summary from a Result Set
 *
 * @author JM235353
 */
public class ValidationDialog extends DefaultDialog {

    private static ValidationDialog singletonDialog = null;
    
    private final static String[] FDR_ESTIMATOR_VALUES = {null, "Default", "Competition Based"};
    private final static String[] FDR_ESTIMATOR_VALUES_ASSOCIATED_KEYS = {null, "false", "true"};
    private final static String[] FDR_ON_VALUES = {null, "Score", "e-Value", "Identity p-Value", "Homology p-Value"};
    private final static String[] FDR_ON_VALUES_ASSOCIATED_KEYS = {null, "SCORE", "MASCOT_EVALUE", "SCORE_IT_P-VALUE", "SCORE_HT_P-VALUE"};
    
    private ParameterList parameterList;
    
    private AbstractParameter[] psmPrefilterParameters;
    private AbstractParameter[] proteinPrefilterParameters;
    
    private AbstractParameter fdrFilterParameter;
    private ObjectParameter<String> fdrOnValueParameter;
    private AbstractParameter proteinFdrFilterParameter;
    
    private JPanel psmPrefiltersSelectedPanel = null;
    private JComboBox psmPrefilterJComboBox = null;
    private JButton addPsmPrefilterButton = null;
    
    private JPanel proteinPrefiltersSelectedPanel = null;
    private JComboBox proteinPrefilterJComboBox = null;
    private JButton addProteinPrefilterButton = null;
    
    private JLabel fdrLabel = null;
    private JTextField fdrTextField = null;
    private JLabel fdrPercentageLabel = null;
    private JComboBox fdrEstimatorComboBox = null;
    private JLabel proteinFdrLabel = null;
    private JTextField proteinFdrTextField = null;
    private JLabel proteinFdrPercentageLabel = null;
    private JComboBox fdrOnValueComboBox = null;
    private JCheckBox fdrCheckbox = null;
    private JCheckBox proteinFdrCheckbox = null;

    public enum DecoyStatus {
        WAITING,
        HAS_DECOY,
        NO_DECOY
    }
    
    private DecoyStatus hasDecoy  = DecoyStatus.WAITING;
    
    public static ValidationDialog getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new ValidationDialog(parent);
        }
        
        

        return singletonDialog;
    }

    public ValidationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Identification Validation");

        parameterList = new ParameterList("Validation");
        createParameters();
        parameterList.updateIsUsed();

        setInternalComponent(createInternalPanel());

        initPsmPrefilterPanel();
        initProteinPrefilterPanel();

    }

    public synchronized void setHasDecoy(DecoyStatus hasDecoy) {
        this.hasDecoy = hasDecoy;
    }
    public synchronized DecoyStatus getHasDecoy() {
        return hasDecoy;
    }
    
    public void setDatasetList(final ArrayList<Dataset> datasetList) {

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
                    Dataset dataset = datasetList.get(i);
                    ResultSet rset = dataset.getTransientData().getResultSet();
                    
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

        c.gridy++;
        psmPanel.add(createFDREstimatorPanel(), c);

        return psmPanel;
    }

    private JPanel createPsmPreFilterPanel() {
        JPanel prefilterPanel = new JPanel(new GridBagLayout());
        prefilterPanel.setBorder(BorderFactory.createTitledBorder(" Prefilter(s) "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        psmPrefiltersSelectedPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        prefilterPanel.add(psmPrefiltersSelectedPanel, c);

        
        
        psmPrefilterJComboBox = new JComboBox(psmPrefilterParameters);
        psmPrefilterJComboBox.setRenderer(new ParameterComboboxRenderer(null));
        addPsmPrefilterButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
        addPsmPrefilterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        prefilterPanel.add(psmPrefilterJComboBox, c);

        c.gridx++;
        c.weightx = 0;
        prefilterPanel.add(addPsmPrefilterButton, c);

        c.gridx++;
        c.weightx = 1.0;
        prefilterPanel.add(Box.createHorizontalBox(), c);


        addPsmPrefilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractParameter p = (AbstractParameter) psmPrefilterJComboBox.getSelectedItem();
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

        psmPrefiltersSelectedPanel.removeAll();
        psmPrefilterJComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        int nbUsed = 0;
        boolean putAndInFront = false;
        int nbParameters = psmPrefilterParameters.length;
        for (int i = 0; i < nbParameters; i++) {
            final AbstractParameter p = psmPrefilterParameters[i];

            if ( (p != null) && (p.isUsed())) {

                c.gridx = 0;
                if (putAndInFront) {
                    psmPrefiltersSelectedPanel.add(new JLabel("AND"), c);
                } else {
                    putAndInFront = true;
                    psmPrefiltersSelectedPanel.add(new JLabel("   "), c);
                }
                
                c.gridx++;
                JLabel prefilterNameLabel = new JLabel(p.getName());
                prefilterNameLabel.setHorizontalAlignment(JLabel.RIGHT);
                psmPrefiltersSelectedPanel.add(prefilterNameLabel, c);

                c.gridx++;
                JLabel cmpLabel = new JLabel(((String) p.getAssociatedData()));
                cmpLabel.setHorizontalAlignment(JLabel.CENTER);
                psmPrefiltersSelectedPanel.add(cmpLabel, c);
                
                c.weightx = 1;
                c.gridx++;
                psmPrefiltersSelectedPanel.add(p.getComponent(), c);

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
                psmPrefiltersSelectedPanel.add(removeButton, c);

                nbUsed++;

                c.gridy++;
            } else {
                psmPrefilterJComboBox.addItem(p);
            }

        }

        boolean hasUnusedParameters = (nbUsed != nbParameters);
        psmPrefilterJComboBox.setVisible(hasUnusedParameters);
        addPsmPrefilterButton.setVisible(hasUnusedParameters);


        repack();
    }

    private JPanel createFDRFilterPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR Filter "));


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        boolean fdrUsed = fdrFilterParameter.isUsed();
        fdrCheckbox = new JCheckBox();
        fdrLabel = new JLabel("Ensure FDR <=");
        fdrPercentageLabel = new JLabel("%  on");
        fdrCheckbox.setSelected(fdrUsed);

        updateFdrObjects(fdrUsed);


        c.gridx = 0;
        c.gridy = 0;
        fdrPanel.add(fdrCheckbox, c);

        c.gridx++;
        fdrPanel.add(fdrLabel, c);

        c.gridx++;
        fdrPanel.add(fdrTextField, c);

        c.gridx++;

        fdrPanel.add(fdrPercentageLabel, c);

        c.gridx++;
        fdrPanel.add(fdrOnValueComboBox, c);


        c.gridx++;
        c.weightx = 1.0;
        fdrPanel.add(Box.createHorizontalBox(), c);

        fdrCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (fdrCheckbox.isSelected());
                updateFdrObjects(enabled);
            }
        });


        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                boolean enabled = (fdrCheckbox.isSelected());
                if (!enabled) {
                    fdrCheckbox.setSelected(true);
                    updateFdrObjects(true);

                    if (e.getSource().equals(fdrTextField)) {
                        fdrTextField.requestFocusInWindow();
                    }

                }
            }
        };

        fdrLabel.addMouseListener(actionOnClick);
        fdrTextField.addMouseListener(actionOnClick);


        return fdrPanel;
    }

    private void updateFdrObjects(boolean enabled) {
        fdrLabel.setEnabled(enabled);
        fdrTextField.setEnabled(enabled);
        fdrPercentageLabel.setEnabled(enabled);
        fdrOnValueComboBox.setEnabled(enabled);
        fdrFilterParameter.setUsed(enabled);
        fdrOnValueParameter.setUsed(enabled);
    }

    private JPanel createFDREstimatorPanel() {
        JPanel fdrEstimatorPanel = new JPanel(new GridBagLayout());


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);



        c.gridx = 0;
        c.gridy = 0;
        fdrEstimatorPanel.add(new JLabel("FDR Estimator"), c);

        c.gridx++;
        fdrEstimatorPanel.add(fdrEstimatorComboBox, c);

        c.gridx++;
        c.weightx = 1.0;
        fdrEstimatorPanel.add(Box.createHorizontalBox(), c);

        return fdrEstimatorPanel;
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
        
        return proteinSetFilterPanel;
    }

    private JPanel createProteinFDRFilterPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR Filter "));


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        
        boolean parameterUsed = proteinFdrFilterParameter.isUsed();
        proteinFdrCheckbox = new JCheckBox("");
        proteinFdrLabel = new JLabel("Protein FDR <=");
        proteinFdrCheckbox.setSelected(parameterUsed);
        proteinFdrPercentageLabel = new JLabel(" %");

        updateproteinFdrObjects(parameterUsed);

        c.gridx = 0;
        c.gridy = 0;
        fdrPanel.add(proteinFdrCheckbox, c);

        c.gridx++;
        fdrPanel.add(proteinFdrLabel, c);


        c.gridx++;
        fdrPanel.add(proteinFdrTextField, c);

        c.gridx++;
        c.weightx = 1.0;
        fdrPanel.add(Box.createHorizontalBox(), c);

        proteinFdrCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (proteinFdrCheckbox.isSelected());
                updateproteinFdrObjects(enabled);
            }
        });

        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                boolean enabled = (proteinFdrCheckbox.isSelected());
                if (!enabled) {
                    proteinFdrCheckbox.setSelected(true);
                    updateproteinFdrObjects(true);
                    if (e.getSource().equals(proteinFdrTextField)) {
                        proteinFdrTextField.requestFocusInWindow();
                    }
                }
            }
        };

        proteinFdrLabel.addMouseListener(actionOnClick);
        proteinFdrTextField.addMouseListener(actionOnClick);




        return fdrPanel;
    }
    
    private JPanel createProteinPreFilterPanel() {
        JPanel prefilterPanel = new JPanel(new GridBagLayout());
        prefilterPanel.setBorder(BorderFactory.createTitledBorder(" Prefilter(s) "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        proteinPrefiltersSelectedPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        prefilterPanel.add(proteinPrefiltersSelectedPanel, c);

        
        
        proteinPrefilterJComboBox = new JComboBox(proteinPrefilterParameters);
        proteinPrefilterJComboBox.setRenderer(new ParameterComboboxRenderer(null));
        addProteinPrefilterButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
        addProteinPrefilterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        prefilterPanel.add(proteinPrefilterJComboBox, c);

        c.gridx++;
        c.weightx = 0;
        prefilterPanel.add(addProteinPrefilterButton, c);

        c.gridx++;
        c.weightx = 1.0;
        prefilterPanel.add(Box.createHorizontalBox(), c);


        addProteinPrefilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractParameter p = (AbstractParameter) proteinPrefilterJComboBox.getSelectedItem();
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

        proteinPrefiltersSelectedPanel.removeAll();
        proteinPrefilterJComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        int nbUsed = 0;
        boolean putAndInFront = false;
        int nbParameters = proteinPrefilterParameters.length;
        for (int i = 0; i < nbParameters; i++) {
            final AbstractParameter p = proteinPrefilterParameters[i];

            if ( (p != null) && (p.isUsed())) {

                c.gridx = 0;
                if (putAndInFront) {
                    proteinPrefiltersSelectedPanel.add(new JLabel("AND"), c);
                } else {
                    putAndInFront = true;
                    proteinPrefiltersSelectedPanel.add(new JLabel("   "), c);
                }
                
                c.gridx++;
                JLabel prefilterNameLabel = new JLabel(p.getName());
                prefilterNameLabel.setHorizontalAlignment(JLabel.RIGHT);
                proteinPrefiltersSelectedPanel.add(prefilterNameLabel, c);

                c.gridx++;
                JLabel cmpLabel = new JLabel(((String) p.getAssociatedData()));
                cmpLabel.setHorizontalAlignment(JLabel.CENTER);
                proteinPrefiltersSelectedPanel.add(cmpLabel, c);
                
                c.weightx = 1;
                c.gridx++;
                proteinPrefiltersSelectedPanel.add(p.getComponent(), c);

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
                proteinPrefiltersSelectedPanel.add(removeButton, c);

                nbUsed++;

                c.gridy++;
            } else {
                proteinPrefilterJComboBox.addItem(p);
            }

        }

        boolean hasUnusedParameters = (nbUsed != nbParameters);
        proteinPrefilterJComboBox.setVisible(hasUnusedParameters);
        addProteinPrefilterButton.setVisible(hasUnusedParameters);


        repack();
    }
    
    
    private void updateproteinFdrObjects(boolean enabled) {
        proteinFdrLabel.setEnabled(enabled);
        proteinFdrTextField.setEnabled(enabled);
        proteinFdrPercentageLabel.setEnabled(enabled);
        proteinFdrFilterParameter.setUsed(enabled);
    }

    private void createParameters() {
        psmPrefilterParameters = new AbstractParameter[7];
        psmPrefilterParameters[0] = null;
        psmPrefilterParameters[1] = new IntegerParameter("RANK", "Rank", new JTextField(6), new Integer(5), new Integer(0), new Integer(10));
        psmPrefilterParameters[1].setAssociatedData("<=");
        psmPrefilterParameters[2] = new IntegerParameter("PEP_SEQ_LENGTH", "Length", new JTextField(6), new Integer(4), new Integer(4), null);
        psmPrefilterParameters[2].setAssociatedData(">=");
        psmPrefilterParameters[3] = new DoubleParameter("SCORE", "Score", new JTextField(6), new Double(0), new Double(0), (Double) null);
        psmPrefilterParameters[3].setAssociatedData(">=");
        psmPrefilterParameters[4] = new DoubleParameter("MASCOT_EVALUE", "e-Value", new JTextField(6), new Double(1), new Double(0), new Double(1));
        psmPrefilterParameters[4].setAssociatedData("<=");
        psmPrefilterParameters[5] = new DoubleParameter("SCORE_IT_P-VALUE", "Identity p-Value", new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        psmPrefilterParameters[5].setAssociatedData("=");   
        psmPrefilterParameters[6] = new DoubleParameter("SCORE_HT_P-VALUE", "Homology p-Value", new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        psmPrefilterParameters[6].setAssociatedData("=");

        for (int i = 0; i < psmPrefilterParameters.length; i++) {
            AbstractParameter p = psmPrefilterParameters[i];
            if (p == null) {
                continue;
            }
            p.setUsed(false);
            parameterList.add(p);
        }

        proteinPrefilterParameters = new AbstractParameter[2];
        proteinPrefilterParameters[0] = null;
        proteinPrefilterParameters[1] = new IntegerParameter("SPECIFIC_PEP", "Specific Peptides", new JTextField(6), new Integer(1), new Integer(1), null);
        proteinPrefilterParameters[1].setAssociatedData(">=");
        
         for (int i = 0; i < proteinPrefilterParameters.length; i++) {
            AbstractParameter p = proteinPrefilterParameters[i];
            if (p == null) {
                continue;
            }
            p.setUsed(false);
            parameterList.add(p);
        }
        
        
        fdrTextField = new JTextField(5);
        fdrFilterParameter = new DoubleParameter("expected_fdr", "FDR", fdrTextField, new Double(5), new Double(0), new Double(10));
        fdrFilterParameter.setUsed(false);
        parameterList.add(fdrFilterParameter);

        fdrOnValueComboBox = new JComboBox(FDR_ON_VALUES);
        fdrOnValueParameter = new ObjectParameter<>("expected_fdr_parameter", "FDR Variable", fdrOnValueComboBox, FDR_ON_VALUES,FDR_ON_VALUES_ASSOCIATED_KEYS,  0, null);
        fdrOnValueParameter.setUsed(false);
        parameterList.add(fdrOnValueParameter);

        fdrEstimatorComboBox = new JComboBox(FDR_ESTIMATOR_VALUES);
        ObjectParameter<String> fdrEstimatorParameter = new ObjectParameter<>("use_td_competition", "FDR Estimator", fdrEstimatorComboBox, FDR_ESTIMATOR_VALUES, FDR_ESTIMATOR_VALUES_ASSOCIATED_KEYS, 0, null);
        parameterList.add(fdrEstimatorParameter);

        proteinFdrTextField = new JTextField(5);
        proteinFdrFilterParameter = new DoubleParameter("protein_expected_fdr", "Protein FDR", proteinFdrTextField, new Double(5), new Double(0), new Double(10));
        proteinFdrFilterParameter.setUsed(false);
        parameterList.add(proteinFdrFilterParameter);



    }

    public HashMap<String, String> getArguments() {
        return parameterList.getValues();
    }
    
    @Override
    protected boolean okCalled() {

        boolean aFdrSelected = fdrCheckbox.isSelected() || proteinFdrCheckbox.isSelected();

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
            if (fdrCheckbox.isSelected()) {
                highlight(fdrCheckbox);
            } else {
                highlight(proteinFdrCheckbox);
            }
            return false;
        }
            
        // check parameters
        ParameterError error = parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }

        // retrieve values
        //HashMap<String, String> values = parameterList.getValues();

        // Save Parameters        
        parameterList.saveParameters();

        return true;

    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    protected boolean defaultCalled() {
        parameterList.initDefaults();

        return false;
    }
}
