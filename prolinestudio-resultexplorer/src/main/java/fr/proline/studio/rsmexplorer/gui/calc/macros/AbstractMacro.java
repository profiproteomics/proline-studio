/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
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
