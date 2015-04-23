package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import java.awt.Color;
import java.util.LinkedList;

/**
 *
 * @author JM235353
 */
public class FunctionGraphNode extends GraphNode {
    
    private static final Color FRAME_COLOR = new Color(149,195,95);

    private AbstractFunction m_function;
    
    public FunctionGraphNode(AbstractFunction function) {
        m_function = function;
        m_outConnector = new GraphConnector(this, true);
        
        int nbParameters = function.getNumberOfInParameters();
        if (nbParameters > 0) {
            m_inConnectors = new LinkedList<>();
            for (int i = 0; i < nbParameters; i++) {
                m_inConnectors.add(new GraphConnector(this, false));
            }
        }
    }

    @Override
    public String getName() {
        return "Join";
    }

    @Override
    public Color getFrameColor() {
        return FRAME_COLOR;
    }





    

    
}
