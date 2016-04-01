package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class ProcessEngine implements ProcessCallbackInterface {
    
    private static ProcessEngine m_processEngine = null;
    
    
    private JPanel m_panel;
    private JButton m_playButton;
    
    private final LinkedList<GraphNode> m_processingNodeList = new LinkedList<>();
    
    private ProcessEngine() {  
    }
    
    public static ProcessEngine getProcessEngine() {
        if (m_processEngine == null) {
            m_processEngine = new ProcessEngine();
        }
        
        return m_processEngine;
    }
    
    public void run(LinkedList<GraphNode> graphNodeArray, JPanel p, JButton playButton) {

        m_panel = p;
        m_playButton = playButton;
        
        // look for nodes with no in to initialize the processing
        m_processingNodeList.clear();
        for (GraphNode node : graphNodeArray) {
            if (!node.hasInConnector()) {
                m_processingNodeList.add(node);
            }
        }
        
        processNodes();

    }
    
    public void runANode(GraphNode node, JPanel p) {
        
        m_panel = p;
        m_playButton = null;
        
        m_processingNodeList.add(node);
        
        processNodes();
    }
    
    private void processNodes() {
        GraphNode firstNode = m_processingNodeList.pollFirst();
        firstNode.setHighlighted(true);
        m_panel.repaint();
        firstNode.process(this);
    }

    @Override
    public void reprocess(GraphNode node) {
        node.process(this);
    }
    
    
    @Override
    public void stopped(GraphNode node) {
        node.setHighlighted(false);
        m_panel.repaint();
        
        m_processingNodeList.clear();

        // processing is finished
        if (m_playButton != null) {
            m_playButton.setEnabled(true);
            m_playButton = null;
        }
        m_panel = null;
    }
    
    @Override
    public void finished(GraphNode node) {
  
        node.setHighlighted(false);
        
        m_panel.repaint();
        
        
        LinkedList<GraphNode> nextNodes = node.getOutLinkedGraphNodes();
        if (nextNodes != null) {
            for (GraphNode nextNode : nextNodes) {
                m_processingNodeList.add(nextNode);
            }
        }
        
        if (! m_processingNodeList.isEmpty()) {
            GraphNode firstNode = m_processingNodeList.pollFirst();
            firstNode.setHighlighted(true);
            m_panel.repaint();
            firstNode.process(this);
        } else {
            // processing is finished
            if (m_playButton!= null) {
                m_playButton.setEnabled(true);
                m_playButton = null;
            }
            m_panel = null;
            
        }
    }



}
