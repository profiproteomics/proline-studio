package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.rsmexplorer.gui.dialog.ManageSaveWindowsDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to manage the list of Saved User Windows
 * @author JM235353
 */
public class ManageUserWindowsAction extends AbstractRSMAction {

    private char m_windowType;
    
    public ManageUserWindowsAction(char saveWindowType, AbstractTree tree) {
        super(NbBundle.getMessage(ManageUserWindowsAction.class, "CTL_ManagerUserWindowsAction"), tree);
        m_windowType = saveWindowType;
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        ManageSaveWindowsDialog dialog = ManageSaveWindowsDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
    }
    
    private boolean isForRSM(){
        return m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSM;
    }
    
    private boolean isForRset(){
        return m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET;
    }
    
    private boolean isForQuanti(){
        return m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
        boolean enable = false;
        
        ArrayList<String> savedWindowsList = WindowSavedManager.readSavedWindows();
        int nb = savedWindowsList.size();
        for (int i = 0; i < nb; i++) {
            String wndSaved = savedWindowsList.get(i);
            if (isForRSM() && WindowSavedManager.hasResultSummaryParameter(wndSaved)) {
                enable = true;
                break;
            } else if (isForRset() && WindowSavedManager.hasResultSetParameter(wndSaved)) {
                enable = true;
                break;
            }else if (isForQuanti() && WindowSavedManager.hasQuantiParameter(wndSaved)) {
                enable = true;
                break;
            }
        }
        
        setEnabled(enable);
    }
    
}
