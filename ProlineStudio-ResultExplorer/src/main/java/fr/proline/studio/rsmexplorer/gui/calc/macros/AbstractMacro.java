package fr.proline.studio.rsmexplorer.gui.calc.macros;

import fr.proline.studio.rsmexplorer.gui.calc.DataTree.DataNode;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author JM235353
 */
public abstract class AbstractMacro {
    
    private final ArrayList<DataNode> m_macroNodes = new ArrayList<>();
    private final HashMap<String, DataNode> m_macroNodesReferenceMap = new HashMap<>();
    
    private final HashMap<DataNode, ArrayList<DataNode>> m_linksMap = new HashMap<>();
    
    private final HashMap<DataNode, Integer> m_levelXMap = new HashMap<>();
    private final HashMap<DataNode, Integer> m_levelYMap = new HashMap<>();
    
    public abstract String getName();
    
    private String generateKey(int levelX, int levelY) {
        return levelX+":"+levelY;
    }
    
    public void addNode(DataNode node, int levelX, int levelY) {
        m_macroNodes.add(node);
        m_macroNodesReferenceMap.put(generateKey(levelX, levelY), node);
        m_levelXMap.put(node, levelX);
        m_levelYMap.put(node, levelY);
        
    }
    
    public final void addLink(int levelX1, int levelY1, int levelX2, int levelY2) {
        String key1 = generateKey(levelX1, levelY1);
        String key2 = generateKey(levelX2, levelY2);
        
        DataNode node1 = m_macroNodesReferenceMap.get(key1);
        DataNode node2 = m_macroNodesReferenceMap.get(key2);
        
        ArrayList<DataNode> outNodes = m_linksMap.get(node1);
        if (outNodes == null) {
            outNodes = new ArrayList<>();
            m_linksMap.put(node1, outNodes);
        }
        outNodes.add(node2);
    }

    
    public ArrayList<DataNode> getNodes() {
        return m_macroNodes;
    }
    
    public Integer getLevelX(DataNode node) {
        return m_levelXMap.get(node);
    }
    
    public Integer getLevelY(DataNode node) {
        return m_levelYMap.get(node);
    }

    public ArrayList<DataNode> getLinks(DataNode node) {
        return m_linksMap.get(node);
    }
    
}
