package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;

/**
 *
 * @author JM235353
 */
public class AddAggregateDialog extends DefaultDialog {
    
    
    private static AddAggregateDialog singletonDialog = null;
   
    private AddAggregatePanel aggregatePanel = null;
   
    public static AddAggregateDialog getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new AddAggregateDialog(parent);
        }

        singletonDialog.aggregatePanel.reinitialize();
        
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
            setStatus(true, "You must fill the aggregate name.");
            highlight(aggregatePanel.getNameTextfield());
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
    
    public Aggregation.ChildNature getAggregateType() {
        return aggregatePanel.getAggregateType();
    }
    
}
