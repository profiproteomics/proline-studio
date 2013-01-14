package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.UDSConnectionManager;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;
import javax.swing.JOptionPane;

/**
 *
 * @author JM235353
 */
public class DatabaseConnectionDialog extends DefaultDialog {
    
    
    private static DatabaseConnectionDialog singletonDialog = null;
   
    private DatabaseConnectionPanel databaseConnectionPanel = null;
   
    public static DatabaseConnectionDialog getDialog(Window parent) {
        if (singletonDialog != null) {
            return singletonDialog;
        }
        
        singletonDialog = new DatabaseConnectionDialog(parent);

        return singletonDialog;
    }
    
    
    private DatabaseConnectionDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);


        setTitle("Database Connection");


        databaseConnectionPanel = new DatabaseConnectionPanel();
        databaseConnectionPanel.setDialog(this);

        setInternalComponent(databaseConnectionPanel);
    }
    
    @Override
    protected boolean okCalled() {
        
        if (UDSConnectionManager.getUDSConnectionManager().getConnectionStep() == UDSConnectionManager.CONNECTION_DONE) {
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
        
        databaseConnectionPanel.connect(true);

        // dialog will be closed by the databaseConnectionPanel
        // when the connection is established
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
    
    /*public String getAggregateName() {
        return aggregatePanel.getAggregateName();
    }
    
    public int getNbAggregates() {
       return aggregatePanel.getNbAggregates();
    }
    
    public int getAggregateType() {
        return aggregatePanel.getAggregateType();
    }*/
    
}
