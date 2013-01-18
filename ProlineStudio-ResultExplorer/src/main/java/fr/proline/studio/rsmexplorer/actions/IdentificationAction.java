package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.rsmexplorer.gui.dialog.ImportIdentificationDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class IdentificationAction extends AbstractRSMAction {

    public IdentificationAction() {
        super(NbBundle.getMessage(IdentificationAction.class, "CTL_IdentificationAction"));
    }

    @Override
    public void actionPerformed(RSMNode n, int x, int y) { 

        
        ImportIdentificationDialog dialog = ImportIdentificationDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;
        
        // we disallow to add multiple identification for the moment
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }
        
        // we can always add an identification directly to a project
        RSMNode node = selectedNodes[0];
        if (node.getType() == RSMNode.NodeTypes.PROJECT) {
            setEnabled(true);
            return;
        }
        
        // we can add an identification only to a data set without a ResultSet or a ResultSummary
        if (node.getType() == RSMNode.NodeTypes.DATA_SET) {
            RSMDataSetNode dataSetNode = (RSMDataSetNode) node;
        
            setEnabled(!dataSetNode.hasResultSet() && !dataSetNode.hasResultSummary());
            return;  
        }

        setEnabled(false);

    }
    
    
}