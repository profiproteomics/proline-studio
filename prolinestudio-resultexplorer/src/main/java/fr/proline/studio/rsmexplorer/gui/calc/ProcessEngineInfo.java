package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import java.util.ArrayList;

/**
 * Class used as information to display results of a process in Data Analyzer
 * @author JM235353
 */
public class ProcessEngineInfo {

    private GraphNode m_graphNode = null;
    private Integer m_processKey = null;
    private String m_processCurrentName = null;
    private ArrayList<SplittedPanelContainer.PanelLayout> m_layout = null;
    private int m_index = -1;

    public ProcessEngineInfo(GraphNode graphNode, boolean bumpKey, String name,  ArrayList<SplittedPanelContainer.PanelLayout> layout, int index) {
        m_graphNode = graphNode;
        m_processKey = ProcessEngine.getProcessEngine().getProcessEngineKey(bumpKey);
        m_processCurrentName = (name!=null) ? name : ProcessEngine.getProcessEngine().getProcessName();
        m_layout = layout;
        m_index = index;
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
    
    public ArrayList<SplittedPanelContainer.PanelLayout> getLayout() {
        return m_layout;
    }
    
    public int getIndex() {
        return m_index;
    }
    
}
