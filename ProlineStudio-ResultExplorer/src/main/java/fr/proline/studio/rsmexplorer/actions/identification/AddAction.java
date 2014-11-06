package fr.proline.studio.rsmexplorer.actions.identification;


import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 * Add Action (menu for sub-actions identification and aggregation)
 * @author JM235353
 */
public class AddAction extends AbstractRSMAction {

    private ImportSearchResultAsDatasetAction m_identificationAction;
    private AggregateAction m_aggregateAction;
    
    private JMenu m_menu;
    
   public AddAction() {
       super(NbBundle.getMessage(AddAction.class, "CTL_AddAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
   }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        
        m_identificationAction = new ImportSearchResultAsDatasetAction();
        m_aggregateAction = new AggregateAction();
        
        JMenuItem identificationItem = new JMenuItem(m_identificationAction);
        JMenuItem aggregateItem = new JMenuItem(m_aggregateAction);
        
        m_menu.add(identificationItem);
        m_menu.add(aggregateItem);

        return m_menu;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        
        m_identificationAction.updateEnabled(selectedNodes);
        m_aggregateAction.updateEnabled(selectedNodes);
        
        
        boolean isEnabled = m_identificationAction.isEnabled() || m_aggregateAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }

   
}