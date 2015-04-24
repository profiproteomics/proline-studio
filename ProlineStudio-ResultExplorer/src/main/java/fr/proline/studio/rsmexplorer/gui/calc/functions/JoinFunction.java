package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.python.data.Table;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;

/**
 *
 * @author JM235353
 */
public class JoinFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "Join";
    }

    @Override
    public int getNumberOfInParameters() {
        return 2;
    }
    
    @Override
    public GraphNode.NodeState getState() {
        return m_state;
    }

    @Override
    public void process(AbstractGraphObject[] graphObjects) {
        if (m_state == GraphNode.NodeState.READY) {
            return;
        }
        
        Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
        Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
        Table joinedTable = Table.join(t1, t2);
        m_globalTableModelInterface = joinedTable.getModel();
        m_state = GraphNode.NodeState.READY;
    }
    
}
