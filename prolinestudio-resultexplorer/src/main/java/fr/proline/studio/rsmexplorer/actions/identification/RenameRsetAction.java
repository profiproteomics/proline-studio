package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 * Action with a Menu for the Display of Rset (Search Result)
 *
 * @author JM235353
 */
public class RenameRsetAction extends AbstractRSMAction {

    private RenameAction m_renameAction;
    private SetRsetNameAction m_searchNameAction, m_peaklistAction, m_msiAction, m_mascotAction;
    private JMenu m_menu;

    public RenameRsetAction(AbstractTree tree) {
        super(NbBundle.getMessage(RenameRsetAction.class, "CTL_RenameRsetAction"), tree);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));

        m_renameAction = new RenameAction(getTree());

        m_searchNameAction = new SetRsetNameAction(getTree(), ImportManager.SEARCH_RESULT_NAME_SOURCE, "CTL_RenameRsetSearchResultAction");
        m_peaklistAction = new SetRsetNameAction(getTree(), ImportManager.PEAKLIST_PATH_SOURCE, "CTL_RenameRsetPeaklistAction");
        m_msiAction = new SetRsetNameAction(getTree(), ImportManager.MSI_SEARCH_FILE_NAME_SOURCE, "CTL_RenameRsetMsiAction");
        m_mascotAction = new SetRsetNameAction(getTree(), ImportManager.MASCOT_DAEMON_RULE, "CTL_RenameRsetMascotAction");

        JMenuItem renameItem = new JMenuItem(m_renameAction);
        JMenuItem searchNameItem = new JMenuItem(m_searchNameAction);
        JMenuItem peaklistItem = new JMenuItem(m_peaklistAction);
        JMenuItem msiItem = new JMenuItem(m_msiAction);
        JMenuItem mascotItem = new JMenuItem(m_mascotAction);

        m_menu.add(renameItem);

        m_menu.addSeparator();

        m_menu.add(searchNameItem);
        m_menu.add(peaklistItem);
        m_menu.add(msiItem);
        m_menu.add(mascotItem);

        return m_menu;
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_renameAction.updateEnabled(selectedNodes);

        m_searchNameAction.updateEnabled(selectedNodes);
        m_peaklistAction.updateEnabled(selectedNodes);
        m_msiAction.updateEnabled(selectedNodes);

        m_menu.setEnabled(m_renameAction.isEnabled() || m_searchNameAction.isEnabled() || m_peaklistAction.isEnabled() || m_msiAction.isEnabled());
        
        setEnabled(m_menu.isEnabled());

    }

}
