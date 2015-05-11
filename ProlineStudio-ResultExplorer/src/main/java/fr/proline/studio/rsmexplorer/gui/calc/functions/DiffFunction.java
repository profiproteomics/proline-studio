package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;

/**
 *
 * @author JM235353
 */
public class DiffFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "Diff";
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
    public AbstractFunction cloneFunction() {
        return new DiffFunction();
    }
    
    @Override
    public void process(AbstractGraphObject[] graphObjects) {
        if (m_state == GraphNode.NodeState.READY) {
            return;
        }
        
        Table t1 = new Table(graphObjects[0].getGlobalTableModelInterface());
        Table t2 = new Table(graphObjects[1].getGlobalTableModelInterface());
        Table diffTable = Table.diff(t1, t2);
        m_globalTableModelInterface = diffTable.getModel();
        m_state = GraphNode.NodeState.READY;
    }
    
    @Override
    public void generateDefaultParameters(AbstractGraphObject[] graphObjects) {
        //JPM.TODO
    }
    
    @Override
    public ParameterError checkParameters() {
        //JPM.TODO
        return null;
    }
    
    @Override
    public void userParametersChanged() {
        //JPM.TODO
    }
    
}
