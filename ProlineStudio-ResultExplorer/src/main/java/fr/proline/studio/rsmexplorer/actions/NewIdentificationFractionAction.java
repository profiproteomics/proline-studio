/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.dam.data.IdentificationFractionData;
import fr.proline.studio.rsmexplorer.node.RSMIdentificationFractionNode;
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
public class NewIdentificationFractionAction extends AbstractRSMAction {

    //private static NewIdentificationFractionAction instance = null;

    public NewIdentificationFractionAction() {
        super(NbBundle.getMessage(NewIdentificationFractionAction.class, "CTL_NewFractionAction"));
    }

    @Override
    public void actionPerformed(RSMNode n) { 
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
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

                        IdentificationFractionData data = new IdentificationFractionData(null);
                        //data.setName(name);  //JPM.TODO
                        RSMIdentificationFractionNode identificationFractionNode = new RSMIdentificationFractionNode(data);
                        // JPM.TODO node.getChildren().add(new Node[]{identificationFractionNode});
                    }

                }
            });
        } catch (InterruptedException e1) {
        } catch (InvocationTargetException e2) {
        }
    }
    
   /* public static NewIdentificationFractionAction getInstance() {
        if (instance == null) {
            instance = new NewIdentificationFractionAction();
        }
        return instance;
    }

    @Override
    protected void performAction(Node[] nodes) {
       /* final RSMNode node = (RSMNode) nodes[0];

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

                        IdentificationFractionData data = new IdentificationFractionData(null);
                        //data.setName(name);  //JPM.TODO
                        RSMIdentificationFractionNode identificationFractionNode = new RSMIdentificationFractionNode(Children.LEAF, Lookups.singleton(data), data);
                        node.getChildren().add(new Node[]{identificationFractionNode});
                    }

                }
            });
        } catch (InterruptedException e1) {
        } catch (InvocationTargetException e2) {
        }*/
/*

    }

    @Override
    protected boolean enable(Node[] nodes) {
        // a node must be selected
        /*boolean actionEnabled = ((nodes != null) && (nodes.length == 1));

        if (actionEnabled) {
            RSMNode node = (RSMNode) nodes[0];
            actionEnabled = (node.getType() == (RSMNode.NodeTypes.IDENTIFICATION) || (node.getType() == RSMNode.NodeTypes.TREE_PARENT));
        }

        return actionEnabled;*/ //JPM.TODO
     /*   return true;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/
}