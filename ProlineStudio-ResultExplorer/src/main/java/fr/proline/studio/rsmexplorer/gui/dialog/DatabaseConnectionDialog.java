package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.UDSConnectionManagerOLD;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class DatabaseConnectionDialog extends DefaultDialog {
    
    
    private static DatabaseConnectionDialog singletonDialog = null;
   
    private DatabaseConnectionPanel databaseConnectionPanel = null;
   
    public static DatabaseConnectionDialog getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new DatabaseConnectionDialog(parent);
        }
        return singletonDialog;
    }
    
    
    private DatabaseConnectionDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);


        setTitle("Database Connection");

        initInternalPanel();
    }
    
    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());

        databaseConnectionPanel = new DatabaseConnectionPanel();
        databaseConnectionPanel.setDialog(this);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        
        internalPanel.add(databaseConnectionPanel, c); //


        setInternalComponent(internalPanel);
    }
    
    @Override
    protected boolean okCalled() {
        /*
        if (UDSConnectionManagerOLD.getUDSConnectionManager().getConnectionState() == UDSConnectionManagerOLD.CONNECTION_DONE) {
            // we are already connected
            // check if the parameters have changed
            if (! databaseConnectionPanel.checkParametersHaveChanged()) {
                // parameters have not change, nothing to do, we close the dialog
                return true;
            }
            
            // we must need to close all connections before trying to connect
            // with the new parameters
            DatabaseManager.getInstance().closeAll();
            
        }
        
        if (!databaseConnectionPanel.checkParameters()) {
            return false;
        }
        
        
        
        databaseConnectionPanel.connect(true);

        // dialog will be closed by the databaseConnectionPanel
        // when the connection is established*/
        return false; 
    }
    
    @Override
    protected boolean cancelCalled() {
        return true;
    }
       
    @Override
    protected boolean defaultCalled() {
        databaseConnectionPanel.initDefaults();

        return false;
    }
    
    
}
