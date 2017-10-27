package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;

/**
 * Callback used to follow the process of the graph of the Data Analyzer
 * 
 * @author JM235353
 */
public interface ProcessCallbackInterface {
    
    public void reprocess(GraphNode node);
    
    public void finished(GraphNode node);
    
    public void stopped(GraphNode node);
}
