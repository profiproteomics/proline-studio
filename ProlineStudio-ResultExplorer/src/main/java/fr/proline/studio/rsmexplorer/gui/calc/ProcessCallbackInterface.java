package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;

/**
 *
 * @author JM235353
 */
public interface ProcessCallbackInterface {
    
    public void reprocess(GraphNode node);
    
    public void finished(GraphNode node);
    
    public void stopped(GraphNode node);
}
