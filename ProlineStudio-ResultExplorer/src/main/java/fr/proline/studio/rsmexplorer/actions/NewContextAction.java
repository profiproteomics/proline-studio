/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.dam.data.IdentificationData;
import fr.proline.studio.rsmexplorer.node.RSMContextNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class NewContextAction extends NodeAction {

    private static NewContextAction instance = null;

    private NewContextAction() {
        putValue(Action.NAME, NbBundle.getMessage(NewContextAction.class, "CTL_NewContextAction"));
    }

    public static NewContextAction getInstance() {
        if (instance == null) {
            instance = new NewContextAction();
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
                    String name = (String) JOptionPane.showInputDialog(
                            parentComponent,
                            "Name:",
                            "New Context Name",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            "");
                    if ((name != null) && (name.length() > 0)) {

                        IdentificationData data = new IdentificationData(null);
                        //data.setName(name);  //JPM.TODO
                        RSMContextNode contextNode = new RSMContextNode(Children.LEAF, Lookups.singleton(data), data);
                        node.getChildren().add(new Node[]{contextNode});
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
            actionEnabled = (node.getType() == (RSMNode.NodeTypes.CONTEXT) || (node.getType() == RSMNode.NodeTypes.TREE_PARENT));
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