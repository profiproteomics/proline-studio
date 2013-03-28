package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.PropertiesTopComponent;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class PropertiesAction extends AbstractRSMAction {

    public PropertiesAction() {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_PropertiesAction"));
    }

     @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

         String dialogName = null;
         if (selectedNodes.length == 1) {
             RSMNode firstNode = selectedNodes[0];
             String name = firstNode.getData().getName();
             dialogName = "Properties : " + name;
         } else {
             dialogName = "Properties";
         }
         
         
         PropertiesTopComponent win = new PropertiesTopComponent("Properties : "+dialogName);
         win.setNodes(selectedNodes);
         win.open();
         win.requestActive();
         
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;
        
        
        // properties action is enabled only if selected nodes
        // are of the same type and are of type PROJECT or DATA_SET
        RSMNode.NodeTypes currentType = null;
        for (int i=0;i<nbSelectedNodes;i++) {
            RSMNode node = selectedNodes[i];
            
            // node is being created, we can not show properties
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }
            
            RSMNode.NodeTypes type = node.getType();
            if ((currentType != null) && (currentType != type)) {
                setEnabled(false);
                return;
            }
            if ((type!=RSMNode.NodeTypes.PROJECT) && (type!=RSMNode.NodeTypes.DATA_SET)) {
                setEnabled(false);
                return;
            }
            
            currentType = type;
        }
        


        setEnabled(true);


    }
}