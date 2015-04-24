package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 *
 * @author JM235353
 */
public abstract class AbstractFunction {
    
    protected GraphNode.NodeState m_state = GraphNode.NodeState.UNSET;
    protected GlobalTableModelInterface m_globalTableModelInterface;
    
    public abstract String getName();
    public abstract int getNumberOfInParameters();
    
    public abstract void process(AbstractGraphObject[] graphObjects);
    
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return m_globalTableModelInterface;
    }

    public abstract GraphNode.NodeState getState();
    
    public ImageIcon getIcon() {
        return IconManager.getIcon(IconManager.IconType.FUNCTION);
    }
    
}
