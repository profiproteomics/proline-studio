package fr.proline.studio.rserver.actions;


import fr.proline.studio.rserver.command.*;
import fr.proline.studio.rserver.node.RNode;

/**
 *
 * @author JM235353
 */
public class NormalizeMsnSetAction extends AbstractRAction {

    public NormalizeMsnSetAction() {
        super("Normalize");
    }
    
    @Override
    public void actionPerformed(RNode[] selectedNodes, int x, int y) {
        
        RScenario scenario = new RScenario();

        String[] parameters = {GenericCommand.IN_VARIABLE};
        final GenericCommand cmd = new GenericCommand("NormalizeCenterReduction", "Normalized", "Nomalize(" + AbstractCommand.PREVIOUS_NODE + ")", parameters, RVar.MSN_SET);

        scenario.addCommand(cmd);

        scenario.play(selectedNodes[0]);

        
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        
        RNode parentNode = selectedNodes[0];
        
        setEnabled(parentNode.getType() == RNode.NodeTypes.MSN_SET);
    }
    
    
    

    
    
}
