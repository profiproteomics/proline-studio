package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;
import javax.swing.JOptionPane;

/**
 *
 * @author JM235353
 */
public class AddAggregateDialog extends DefaultDialog {
    
    
    private static AddAggregateDialog singletonDialog = null;
   
    private AddAggregatePanel aggregatePanel = null;
   
    public static AddAggregateDialog getDialog(Window parent) {
        if (singletonDialog != null) {
            return singletonDialog;
        }
        
        singletonDialog = new AddAggregateDialog(parent);

        return singletonDialog;
    }
    
    
    private AddAggregateDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);


        setTitle("Add Aggregate");


        aggregatePanel = new AddAggregatePanel();

        setInternalComponent(aggregatePanel);
    }
    
    @Override
    protected boolean okCalled() {
        
        String name = aggregatePanel.getAggregateName();
       
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(aggregatePanel, "You must fill the aggregate name.", "Warning", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
    
    @Override
    protected boolean cancelCalled() {
        return true;
    }
       
    @Override
    protected boolean defaultCalled() {
        aggregatePanel.initDefaults();

        return false;
    }
    
    public String getAggregateName() {
        return aggregatePanel.getAggregateName();
    }
    
    public int getNbAggregates() {
       return aggregatePanel.getNbAggregates();
    }
    
    public int getAggregateType() {
        return aggregatePanel.getAggregateType();
    }
    
}
