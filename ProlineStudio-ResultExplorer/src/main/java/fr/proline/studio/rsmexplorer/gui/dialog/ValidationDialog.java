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
public class ValidationDialog extends DefaultDialog {

    private ValidationPanel validationPanel = null;
    
    public ValidationDialog(Window parent) {
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
}
