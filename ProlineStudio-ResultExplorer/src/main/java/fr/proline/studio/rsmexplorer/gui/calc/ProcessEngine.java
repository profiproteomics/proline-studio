package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class ProcessEngine implements ProcessCallbackInterface {
    
    private static ProcessEngine m_processEngine = null;
    
    private boolean m_running = false;
    private JPanel m_panel;
    
    private LinkedList<GraphNode> m_processingNodeList = new LinkedList<>();
    
    private ProcessEngine() {
        
    }
    
    public static ProcessEngine getProcessEngine() {
        if (m_processEngine == null) {
            m_processEngine = new ProcessEngine();
        }
        
        return m_processEngine;
    }
    
    public void run(LinkedList<GraphNode> graphNodeArray, JPanel p) {

        m_panel = p;
        
        // look for nodes with no in to initialize the processing
        m_processingNodeList.clear();
        for (GraphNode node : graphNodeArray) {
            if (!node.hasInConnector()) {
                m_processingNodeList.add(node);
            }
        }
        
        processNodes();

    }
    
    private void processNodes() {
        GraphNode firstNode = m_processingNodeList.pollFirst();
        firstNode.process(this);
    }
    
    public void stop() {
        m_processingNodeList.clear();
        m_panel = null;
    }
    
    
    @Override
    public void finished(GraphNode node) {
        
        m_panel.repaint();
        
        LinkedList<GraphNode> nextNodes = node.getOutLinkedGraphNodes();
        if (nextNodes != null) {
            for (GraphNode nextNode : nextNodes) {
                m_processingNodeList.add(nextNode);
            }
        }
        
        if (! m_processingNodeList.isEmpty()) {
            GraphNode firstNode = m_processingNodeList.pollFirst();
            firstNode.process(this);
        } else {
            //JPM.TODO
            // processing is finished
            m_panel = null;
        }
    }
}
