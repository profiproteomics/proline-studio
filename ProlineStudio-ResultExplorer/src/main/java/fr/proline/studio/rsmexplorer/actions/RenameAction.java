/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;


import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class RenameAction extends AbstractRSMAction {

    //private static RenameAction instance = null;

    public RenameAction() {
        super(NbBundle.getMessage(RenameAction.class, "CTL_RenameAction"));
    }

    /*public static RenameAction getInstance() {
        if (instance == null) {
            instance = new RenameAction();
        }
        return instance;
    }

    @Override
    protected void performAction(Node[] nodes) {
/*
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
*/ //JPM.TODO

  /*  }

    @Override
    protected boolean enable(Node[] nodes) {*/
/*
        // a node must be selected
        boolean actionEnabled = ((nodes != null) && (nodes.length == 1));

        if (actionEnabled) {
            RSMNode node = (RSMNode) nodes[0];
            actionEnabled = (node.getType() != RSMNode.NodeTypes.TREE_PARENT); // can not rename tree parent node

        }

        return actionEnabled;
        return true;*/
  /*  }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/
}