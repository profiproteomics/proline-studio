package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.*;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private final static String[] VALIDATION_NAMES = {"FDR Validation", "Irma Validation"};

    JPanel parametersPanel = null;
    private JComboBox validationCombobox = null;
    private int previousValidationIndex = -1;
    
    
    public static ValidationDialog getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new ValidationDialog(parent);
        }

        return singletonDialog;
    }

    public ValidationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Identification Validation");
        //setStatusVisible(false);

        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        internalPanel.add(createSelectValidationPanel(), c);

        c.gridy++;
        c.weighty = 1;
        internalPanel.add(createParametersPanel(), c);

        setInternalComponent(internalPanel);

        validationCombobox.setSelectedIndex(0);
    }

    private JPanel createSelectValidationPanel() {

        JPanel selectPanel = new JPanel(new GridBagLayout());
        selectPanel.setBorder(BorderFactory.createTitledBorder(" Select Validation "));

        JLabel validationLabel = new JLabel("Validation :");
        validationCombobox = new JComboBox(createParameters());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        selectPanel.add(validationLabel, c);

        c.gridx = 1;
        c.weightx = 1;
        selectPanel.add(validationCombobox, c);

                validationCombobox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int validationIndex = validationCombobox.getSelectedIndex();
                if (validationIndex == previousValidationIndex) {
                    return;
                }

                previousValidationIndex = validationIndex;

                initParameters();

                // resize the dialog
                repack();
            }
        });
        
        
        return selectPanel;
    }

    private JPanel createParametersPanel() {

        parametersPanel = new JPanel(new GridBagLayout());
        parametersPanel.setBorder(BorderFactory.createTitledBorder(" Parameters "));


        
        
        return parametersPanel;
    }

    @Override
    protected boolean okCalled() {
        
        // Check values
        ParameterList parameterList = (ParameterList) validationCombobox.getSelectedItem();
        ParameterError error = parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        
        // retrieve values
        HashMap<String, String> values = parameterList.getValues();

        // Save Parameters
        parameterList.saveParameters();
        
        //JPM.TODO
        
        return true;
    }


    @Override
    protected boolean defaultCalled() {
        
        ParameterList parameterList = (ParameterList) validationCombobox.getSelectedItem();
        parameterList.initDefaults();
        
        return false;
    }

    
    
    
    private void initParameters() {


        // remove all parameters
        parametersPanel.removeAll();
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        
        ParameterList parameterList = (ParameterList) validationCombobox.getSelectedItem();
        parametersPanel.add(parameterList.getPanel(), c);

      
    }
 

    
    
    private ParameterList[] createParameters() {
        ParameterList[] plArray = new ParameterList[2];
        plArray[0] = createFDRValidation();
        plArray[1] = createTestValidation();
        return plArray;
    }
    
    private ParameterList createFDRValidation() {
        ParameterList parameterList = new ParameterList("FDR Validation");
        parameterList.add(new IntegerParameter("peptide.fdr", "Peptide FDR", JSlider.class, new Integer(5), new Integer(0), new Integer(10)));
        parameterList.add(new IntegerParameter("min.peptide.length", "Minimum Peptide Length", JSpinner.class, new Integer(6), new Integer(1), null));
        parameterList.add(new IntegerParameter("protein.fdr", "Protein FDR", JSlider.class, new Integer(5), new Integer(0), new Integer(10)));
    
        return parameterList;
    }
    
    private ParameterList createTestValidation() {
        ParameterList parameterList = new ParameterList("Test Validation");
        parameterList.add(new DoubleParameter("score", "Score", JTextField.class, new Double(0), new Double(0), null));
    
        return parameterList;
    }

}
