package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;

/**
 *
 * @author JM235353
 */
public class ProcessEngineInfo {

    private GraphNode m_graphNode = null;
    private Integer m_processKey = null;
    private String m_processCurrentName = null;
    SplittedPanelContainer.PanelLayout m_layout = null;

    public ProcessEngineInfo(GraphNode graphNode, boolean bumpKey, String name, SplittedPanelContainer.PanelLayout layout) {
        m_graphNode = graphNode;
        m_processKey = ProcessEngine.getProcessEngine().getProcessEngineKey(bumpKey);
        m_processCurrentName = (name!=null) ? name : ProcessEngine.getProcessEngine().getProcessName();
        m_layout = layout;
    }

    public GraphNode getGraphNode() {
        return m_graphNode;
    }

    public Integer getProcessKey() {
        return m_processKey;
    }
    
    public String getProcessName() {
        return m_processCurrentName;
    }
    
    public SplittedPanelContainer.PanelLayout getLayout() {
        return m_layout;
    }
    
}
