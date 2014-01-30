package fr.proline.studio.rserver.actions;


import fr.proline.studio.rserver.command.RScenario;
import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RNode.NodeTypes;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
/**
 *
 * @author JM235353
 */
public class ReplayScriptAction extends AbstractRAction  {

    public static final String SCRIPT_NAME = "SCRIPTR_genericScript";
    
    public ReplayScriptAction() {
        super("Replay Script");
    }
    
    @Override
    public void actionPerformed(RNode[] selectedNodes, int x, int y) {
        
        
        RNode node = selectedNodes[0];
        
        Preferences preferences = NbPreferences.root();
        String script = preferences.get(SCRIPT_NAME, null);
        
        RScenario scenario = new RScenario();
        scenario.read(script);
        
        scenario.play(node);
        
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        
        if (selectedNodes.length != 1) {
            setEnabled(false);
        }
        
        RNode node = selectedNodes[0];
        
        if (node.getType() != NodeTypes.MSN_SET) {
            setEnabled(false);
            return;
        }
        
        Preferences preferences = NbPreferences.root();
        if (preferences.get(SCRIPT_NAME, null) == null) {
            setEnabled(false);
            return;
        }
        
        setEnabled(true);
        
        
        
    }
    
}

