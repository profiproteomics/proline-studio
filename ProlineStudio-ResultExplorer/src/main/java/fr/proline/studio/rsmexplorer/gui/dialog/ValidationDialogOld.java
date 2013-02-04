package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

/**
 * Dialog used to validate an identification and ask the creation
 * of a Result Summary from a Result Set
 * @author JM235353
 */
public class ValidationDialogOld extends DefaultDialog {

    private ValidationPanel validationPanel = null;
    
    private static ValidationDialogOld singletonDialog = null;
    
    public static ValidationDialogOld getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new ValidationDialogOld(parent);
        }

        return singletonDialog;
    }

    
    
    public ValidationDialogOld(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Identification Validation");
        setStatusVisible(false);

        validationPanel = new ValidationPanel();

        setInternalComponent(validationPanel);


    }
    
    @Override
    protected boolean okCalled() {
        //JPM.TODO
        
        return true;
    }
    
    @Override
    protected boolean defaultCalled() {
        validationPanel.initDefaults();
        
        return false;
    }
    
    public String getDescription() {
        return validationPanel.getDescription();
    }
    
    public int getPeptideFDR() {
        return validationPanel.getPeptideFDR();
    }
    
    public int getPeptideMinPepSequence() {
        return validationPanel.getPeptideMinPepSequence();
    }
    
    public int getProteinFDR() {
        return validationPanel.getProteinFDR();
    }
    
    public int getProteinMinPepSequence() {
        return validationPanel.getProteinMinPepSequence();
    }
}
