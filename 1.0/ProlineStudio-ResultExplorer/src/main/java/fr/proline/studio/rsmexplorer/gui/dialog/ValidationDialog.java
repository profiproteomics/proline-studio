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
    
    private AbstractParameter[] prefilterParameters;
    private AbstractParameter fdrFilterParameter;
    private ObjectParameter<String> fdrOnValueParameter;
    private AbstractParameter proteinFdrFilterParameter;
    
    private JPanel prefiltersSelectedPanel = null;
    private JComboBox prefilterJComboBox = null;
    private JButton addPrefilterButton = null;
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

    private enum DecoyStatus {
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

        initPrefilterPanel();

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
        psmPanel.add(createPreFilterPanel(), c);

        c.gridy++;
        psmPanel.add(createFDRFilterPanel(), c);

        c.gridy++;
        psmPanel.add(createFDREstimatorPanel(), c);

        return psmPanel;
    }

    private JPanel createPreFilterPanel() {
        JPanel prefilterPanel = new JPanel(new GridBagLayout());
        prefilterPanel.setBorder(BorderFactory.createTitledBorder(" Prefilter(s) "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        prefiltersSelectedPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        prefilterPanel.add(prefiltersSelectedPanel, c);


        prefilterJComboBox = new JComboBox(prefilterParameters);
        addPrefilterButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
        addPrefilterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        prefilterPanel.add(prefilterJComboBox, c);

        c.gridx++;
        c.weightx = 0;
        prefilterPanel.add(addPrefilterButton, c);

        c.gridx++;
        c.weightx = 1.0;
        prefilterPanel.add(Box.createHorizontalBox(), c);


        addPrefilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractParameter p = (AbstractParameter) prefilterJComboBox.getSelectedItem();
                p.setUsed(true);
                initPrefilterPanel();
            }
        });

        return prefilterPanel;
    }

    private void initPrefilterPanel() {

        prefiltersSelectedPanel.removeAll();
        prefilterJComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        int nbUsed = 0;
        int nbParameters = prefilterParameters.length;
        for (int i = 0; i < nbParameters; i++) {
            final AbstractParameter p = prefilterParameters[i];
            if (p.isUsed()) {

                c.gridx = 0;
                prefiltersSelectedPanel.add(new JLabel(p.getName() + " " + ((String) p.getAssociatedData())), c);

                c.weightx = 1;
                c.gridx++;
                prefiltersSelectedPanel.add(p.getComponent(), c);

                c.weightx = 0;
                c.gridx++;
                JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS));
                removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
                removeButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        p.setUsed(false);
                        initPrefilterPanel();
                    }
                });
                prefiltersSelectedPanel.add(removeButton, c);

                nbUsed++;

                c.gridy++;
            } else {
                prefilterJComboBox.addItem(p);
            }

        }

        boolean hasUnusedParameters = (nbUsed != nbParameters);
        prefilterJComboBox.setVisible(hasUnusedParameters);
        addPrefilterButton.setVisible(hasUnusedParameters);


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
        proteinSetFilterPanel.setBorder(BorderFactory.createTitledBorder(" Protein Set Filter "));


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
        proteinSetFilterPanel.add(proteinFdrCheckbox, c);

        c.gridx++;
        proteinSetFilterPanel.add(proteinFdrLabel, c);


        c.gridx++;
        proteinSetFilterPanel.add(proteinFdrTextField, c);

        c.gridx++;
        c.weightx = 1.0;
        proteinSetFilterPanel.add(Box.createHorizontalBox(), c);

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



        return proteinSetFilterPanel;
    }

    private void updateproteinFdrObjects(boolean enabled) {
        proteinFdrLabel.setEnabled(enabled);
        proteinFdrTextField.setEnabled(enabled);
        proteinFdrPercentageLabel.setEnabled(enabled);
        proteinFdrFilterParameter.setUsed(enabled);
    }

    private void createParameters() {
        prefilterParameters = new AbstractParameter[6];
        prefilterParameters[0] = new IntegerParameter("RANK", "Rank", new JTextField(6), new Integer(5), new Integer(0), new Integer(10));
        prefilterParameters[0].setAssociatedData("<=");
        prefilterParameters[1] = new IntegerParameter("PEP_SEQ_LENGTH", "Length", new JTextField(6), new Integer(4), new Integer(4), null);
        prefilterParameters[1].setAssociatedData(">=");
        prefilterParameters[2] = new DoubleParameter("SCORE", "Score", new JTextField(6), new Double(0), new Double(0), (Double) null);
        prefilterParameters[2].setAssociatedData(">=");
        prefilterParameters[3] = new DoubleParameter("MASCOT_EVALUE", "e-Value", new JTextField(6), new Double(1), new Double(0), new Double(1));
        prefilterParameters[3].setAssociatedData("<=");
        prefilterParameters[4] = new DoubleParameter("SCORE_IT_P-VALUE", "Identity p-Value", new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        prefilterParameters[4].setAssociatedData("=");   
        prefilterParameters[5] = new DoubleParameter("SCORE_HT_P-VALUE", "Homology p-Value", new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        prefilterParameters[5].setAssociatedData("=");

        for (int i = 0; i < prefilterParameters.length; i++) {
            AbstractParameter p = prefilterParameters[i];
            p.setUsed(false);
            parameterList.add(p);
        }

        fdrTextField = new JTextField(4);
        fdrFilterParameter = new IntegerParameter("expected_fdr", "FDR", fdrTextField, new Integer(5), new Integer(0), new Integer(10));
        fdrFilterParameter.setUsed(false);
        parameterList.add(fdrFilterParameter);

        fdrOnValueComboBox = new JComboBox(FDR_ON_VALUES);
        fdrOnValueParameter = new ObjectParameter<>("expected_fdr_parameter", "FDR Variable", fdrOnValueComboBox, FDR_ON_VALUES,FDR_ON_VALUES_ASSOCIATED_KEYS,  0, null);
        fdrOnValueParameter.setUsed(false);
        parameterList.add(fdrOnValueParameter);

        fdrEstimatorComboBox = new JComboBox(FDR_ESTIMATOR_VALUES);
        ObjectParameter<String> fdrEstimatorParameter = new ObjectParameter<>("use_td_competition", "FDR Estimator", fdrEstimatorComboBox, FDR_ESTIMATOR_VALUES, FDR_ESTIMATOR_VALUES_ASSOCIATED_KEYS, 0, null);
        parameterList.add(fdrEstimatorParameter);

        proteinFdrTextField = new JTextField(4);
        proteinFdrFilterParameter = new IntegerParameter("protein_expected_fdr", "Protein FDR", proteinFdrTextField, new Integer(5), new Integer(0), new Integer(10));
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
