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

    private IdentificationAction m_identificationAction;
    private AggregateAction m_aggregateAction;
    
    private JMenu m_menu;
    
   public AddAction() {
       super(NbBundle.getMessage(AddAction.class, "CTL_AddAction"));
   }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        
        m_identificationAction = new IdentificationAction();
        m_aggregateAction = new AggregateAction();
        
        JMenuItem identificationItem = new JMenuItem(m_identificationAction);
        JMenuItem aggregateItem = new JMenuItem(m_aggregateAction);
        
        m_menu.add(identificationItem);
        m_menu.add(aggregateItem);

        return m_menu;
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        
        m_identificationAction.updateEnabled(selectedNodes);
        m_aggregateAction.updateEnabled(selectedNodes);
        
        
        boolean isEnabled = m_identificationAction.isEnabled() || m_aggregateAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }

   
}