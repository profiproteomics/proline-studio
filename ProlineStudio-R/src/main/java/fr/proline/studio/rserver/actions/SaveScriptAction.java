package fr.proline.studio.rserver.actions;

import fr.proline.studio.rserver.command.RScenario;
import fr.proline.studio.rserver.node.RMsnSetNode;
import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RNode.NodeTypes;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class SaveScriptAction extends AbstractRAction  {

    public SaveScriptAction() {
        super("Save Script");
    }
    
    @Override
    public void actionPerformed(RNode[] selectedNodes, int x, int y) {
        
        
        // look for the script path
        String previousVar = null;
        LinkedList<RNode> scriptList = new LinkedList<>();
        RNode node = selectedNodes[0];
        while (true) {
             scriptList.addFirst(node);
             
             node = (RNode) node.getParent();
             if ((node.getType() == RNode.NodeTypes.MSN_SET) && ( ((RMsnSetNode)node).isScriptStart()) ) {
                 break;
             }
             

        }
        
        // create the scenario
        String scriptName = ReplayScriptAction.SCRIPT_NAME;  //JPM.TODO change name
        RScenario scenario = new RScenario(scriptName);
        Iterator<RNode> it = scriptList.iterator();
        while (it.hasNext()) {
             RNode nodeCur = it.next();
             scenario.addCommand(nodeCur.getCommand());
        }
        
        // save the scenario
        StringBuilder scriptSB = new StringBuilder();
        scenario.write(scriptSB);
        Preferences preferences = NbPreferences.root();
        preferences.put(scriptName, scriptSB.toString());
        
        
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        
        if (selectedNodes.length != 1) {
            setEnabled(false);
        }
        
        RNode node = selectedNodes[0];
        
        // one of the parents must be a msnSet
        while (true) {
            if (node == null) {
                setEnabled(false);
                break;
            }
            
            if (node.getType() == NodeTypes.MSN_SET) {
                setEnabled(true);
                break;
            }
            
            node = (RNode) node.getParent();
            
        }
        

    }
    
}
