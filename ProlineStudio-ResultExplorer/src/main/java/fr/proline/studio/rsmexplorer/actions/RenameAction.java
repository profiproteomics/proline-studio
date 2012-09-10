/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class RenameAction extends NodeAction {

    private static RenameAction instance = null;

    private RenameAction() {
        putValue(Action.NAME, NbBundle.getMessage(RenameAction.class, "CTL_RenameAction"));
    }

    public static RenameAction getInstance() {
        if (instance == null) {
            instance = new RenameAction();
        }
        return instance;
    }

    @Override
    protected void performAction(Node[] nodes) {

        final RSMNode node = (RSMNode) nodes[0];

        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {

                    Component parentComponent = WindowManager.getDefault().findTopComponent("RSMExplorerTopComponent");
                    String newName = (String) JOptionPane.showInputDialog(
                            parentComponent,
                            "New Name:",
                            "Rename",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            node.getDisplayName());
                    if ((newName != null) && (newName.length() > 0)) {
                        node.setName(newName);
                    }

                }
            });
        } catch (InterruptedException e1) {
        } catch (InvocationTargetException e2) {
        }


    }

    @Override
    protected boolean enable(Node[] nodes) {

        // a node must be selected
        boolean actionEnabled = ((nodes != null) && (nodes.length == 1));

        if (actionEnabled) {
            RSMNode node = (RSMNode) nodes[0];
            actionEnabled = (node.getType() != RSMNode.NodeTypes.TREE_PARENT); // can not rename tree parent node

        }

        return actionEnabled;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}