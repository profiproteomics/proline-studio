package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.CreateXICDialog;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class CreateXICAction extends AbstractRSMAction {

    public CreateXICAction() {
        super(NbBundle.getMessage(ConnectAction.class, "CTL_CreateXIC"), false);
    }
    
     @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {
         CreateXICDialog dialog = new CreateXICDialog(WindowManager.getDefault().getMainWindow());
         dialog.setLocation(x, y);
         dialog.setVisible(true);


         if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
         
         }
     }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
         setEnabled(false); // put back
    }
    
}
