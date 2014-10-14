package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.ArrayList;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class ManageUserWindowsAction extends AbstractRSMAction {

    private boolean m_forRSM;
    
    public ManageUserWindowsAction(boolean forRSM) {
        super(NbBundle.getMessage(ManageUserWindowsAction.class, "CTL_ManagerUserWindowsAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
        m_forRSM = forRSM;
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        //JPM.TODO
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
        boolean enable = false;
        
        ArrayList<String> savedWindowsList = WindowSavedManager.readSavedWindows();
        int nb = savedWindowsList.size();
        for (int i = 0; i < nb; i++) {
            String wndSaved = savedWindowsList.get(i);
            if (m_forRSM && WindowSavedManager.hasResultSummaryParameter(wndSaved)) {
                enable = true;
                break;
            } else if (!m_forRSM && WindowSavedManager.hasResultSetParameter(wndSaved)) {
                enable = true;
                break;
            }
        }
        
        setEnabled(enable);
    }
    
}
