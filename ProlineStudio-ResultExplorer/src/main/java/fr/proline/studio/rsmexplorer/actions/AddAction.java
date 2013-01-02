package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.rsmexplorer.node.RSMNode;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class AddAction extends AbstractRSMAction {

    private IdentificationAction identificationAction;
    private AggregateAction aggregateAction;
    
    
   public AddAction() {
       super(NbBundle.getMessage(AddAction.class, "CTL_AddAction"));
   }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu((String) getValue(NAME));
        
        identificationAction = new IdentificationAction();
        aggregateAction = new AggregateAction();
        
        JMenuItem identificationItem = new JMenuItem(identificationAction);
        JMenuItem aggregateItem = new JMenuItem(aggregateAction);
        
        menu.add(identificationItem);
        menu.add(aggregateItem);

        return menu;
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        
        identificationAction.updateEnabled(selectedNodes);
        aggregateAction.updateEnabled(selectedNodes);
        
        setEnabled(identificationAction.isEnabled() || aggregateAction.isEnabled());
        
    }

   
}