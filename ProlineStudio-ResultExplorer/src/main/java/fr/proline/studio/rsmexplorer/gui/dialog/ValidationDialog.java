package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.*;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Dialog used to validate an identification and ask the creation of a Result
 * Summary from a Result Set
 *
 * @author JM235353
 */
public class ValidationDialog extends DefaultDialog {

    private static ValidationDialog singletonDialog = null;

    private final static String[] FDR_ESTIMATOR_VALUES = { "< Select >", "Separated", "Concataned", "Competition Based"};
    private final static String[] FDR_ON_VALUES = { "< Select >", "Score", "e-Value" };
    
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
        for (int i=0;i<nbParameters;i++) {
            final AbstractParameter p = prefilterParameters[i];
            if (p.isUsed()) {
              
                c.gridx = 0;
                prefiltersSelectedPanel.add(new JLabel(p.getName() + " " + ((String) p.getAssociatedData())), c);
   
                c.weightx = 1;
                c.gridx++;
                prefiltersSelectedPanel.add(p.getComponent(), c );
                
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
                prefiltersSelectedPanel.add(removeButton, c );
                
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
        final JCheckBox fdrCheckbox = new JCheckBox();
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
        final JCheckBox fdrCheckbox = new JCheckBox("");
        proteinFdrLabel = new JLabel("Protein FDR <=");
        fdrCheckbox.setSelected(parameterUsed);
        proteinFdrPercentageLabel = new JLabel(" %");
        
        updateproteinFdrObjects(parameterUsed);
        
        c.gridx = 0;
        c.gridy = 0;
        proteinSetFilterPanel.add(fdrCheckbox, c);
        
        c.gridx++;
        proteinSetFilterPanel.add(proteinFdrLabel, c);
        
        
        c.gridx++;
        proteinSetFilterPanel.add(proteinFdrTextField, c);
        
        c.gridx++;
        c.weightx = 1.0;
        proteinSetFilterPanel.add(Box.createHorizontalBox(), c);
        
        fdrCheckbox.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (fdrCheckbox.isSelected());
                updateproteinFdrObjects(enabled);
            }
        });
        
        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                boolean enabled = (fdrCheckbox.isSelected());
                if (!enabled) {
                    fdrCheckbox.setSelected(true);
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
        prefilterParameters = new AbstractParameter[4];
        prefilterParameters[0] = new IntegerParameter("prefilter.rank", "Rank", new JTextField(6), new Integer(5), new Integer(0), new Integer(10));
        prefilterParameters[0].setAssociatedData("<=");
        prefilterParameters[1] = new IntegerParameter("prefilter.length", "Length", new JTextField(6), new Integer(4), new Integer(4), null);
        prefilterParameters[1].setAssociatedData(">=");
        prefilterParameters[2] = new DoubleParameter("prefilter.score", "Score", new JTextField(6), new Double(0), new Double(0), (Double) null);
        prefilterParameters[2].setAssociatedData(">=");
        prefilterParameters[3] = new DoubleParameter("prefilter.score", "e-Value", new JTextField(6), new Double(0), new Double(0), null);
        prefilterParameters[3].setAssociatedData("<=");
        
        for (int i=0;i<prefilterParameters.length;i++) {
            AbstractParameter p = prefilterParameters[i];
            p.setUsed(false);
            parameterList.add(p);
        }
        
        fdrTextField = new JTextField(4);
        fdrFilterParameter = new IntegerParameter("FDR", "FDR", fdrTextField, new Integer(5), new Integer(0), new Integer(10));
        fdrFilterParameter.setUsed(false);
        parameterList.add(fdrFilterParameter);

        fdrOnValueComboBox = new JComboBox(FDR_ON_VALUES);
        fdrOnValueParameter = new ObjectParameter<>("FDR_Variable", "FDR Variable", fdrOnValueComboBox, FDR_ON_VALUES, 0, null);
        fdrOnValueParameter.setUsed(false);
        fdrOnValueParameter.setInvalidIndex(0);
        parameterList.add(fdrOnValueParameter);
        
        fdrEstimatorComboBox = new JComboBox(FDR_ESTIMATOR_VALUES);
        ObjectParameter<String> fdrEstimatorParameter = new ObjectParameter<>("FDR_Estimator", "FDR Estimator", fdrEstimatorComboBox, FDR_ESTIMATOR_VALUES, 0, null);
        fdrEstimatorParameter.setInvalidIndex(0);
        parameterList.add(fdrEstimatorParameter);
        
        proteinFdrTextField = new JTextField(4);
        proteinFdrFilterParameter = new IntegerParameter("Protein FDR", "Protein_FDR", proteinFdrTextField, new Integer(5), new Integer(0), new Integer(10));
        proteinFdrFilterParameter.setUsed(false);
        parameterList.add(proteinFdrFilterParameter);
        

        
    }
 
    
        @Override
    protected boolean okCalled() {

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
