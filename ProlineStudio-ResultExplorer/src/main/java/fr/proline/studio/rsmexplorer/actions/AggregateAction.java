package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class AggregateAction extends AbstractRSMAction {

    public AggregateAction() {
        super(NbBundle.getMessage(AggregateAction.class, "CTL_AggregateAction"));
    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        setEnabled(false);  //JPM.TODO

    }
}