/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.studio.performancetest;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import org.slf4j.Logger;

/**
 *
 * @author Jean-Philippe
 */
public class PerformanceTestData {
    
    private SpeedNode m_root = new SpeedNode("", null);
    private SpeedNode m_currentNode = m_root;

    
    private Logger m_logger;
    private static Comparator m_comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return (int) (((SpeedNode)o2).m_totalTime - ((SpeedNode)o1).m_totalTime );
            }

        };
    
    public PerformanceTestData(Logger logger) {
        
        m_logger = logger;

    }
    
    public void startTime(String key, boolean global) {

        if (global) {
            SpeedNode node = m_root.getChild(key);
            node.setStartTime(System.currentTimeMillis());
        } else {
            m_currentNode = m_currentNode.getChild(key);

            m_currentNode.setStartTime(System.currentTimeMillis());
        }
        
    }
    
    public void stopTime(String key, boolean global) {

        if (global) {
            SpeedNode node = m_root.getChild(key);
            node.stop(System.currentTimeMillis());
            
        } else {
            SpeedNode currentNode = global ? m_root : m_currentNode;

            currentNode.stop(System.currentTimeMillis());

            // sanity check
            if (!currentNode.m_key.equals(key)) {
                m_logger.debug(" PROBLEM : " + key);
            }

            m_currentNode = m_currentNode.m_parent;
        }

    }
    
    public void displayTime() {
        
        LinkedList<SpeedNode> sortedNodes = new LinkedList();
        m_root.getSortedNodes(sortedNodes); 

        for (SpeedNode curNode : sortedNodes) {
            curNode.display();
            
        }
        
        
    }
    
    
    public void merge(PerformanceTestData testData) {

        merge(m_root, testData.m_root);
        
    }
    
    private void merge(SpeedNode node1, SpeedNode node2) {
        if (node2.m_children != null) {
            for (String childKey : node2.m_children.keySet()) {
                SpeedNode child2 = node2.m_children.get(childKey);
                SpeedNode child1 = (node1.m_children != null) ? node1.m_children.get(childKey) : null;
                if (child1 == null) {
                    child1 = new SpeedNode(childKey, node1);
                    child1.m_totalTime = child2.m_totalTime;
                    child1.m_callNumbers = child2.m_callNumbers;
                    child1.m_minTime = child2.m_minTime;
                    child1.m_maxTime = child2.m_maxTime;
                    node1.addChild(child1);
                } else {
                    child1.m_totalTime += child2.m_totalTime;
                    child1.m_callNumbers += child2.m_callNumbers;
                    if (child2.m_minTime<child1.m_minTime) {
                        child1.m_minTime = child2.m_minTime;
                    }
                    if (child2.m_maxTime>child1.m_maxTime) {
                        child1.m_maxTime = child2.m_maxTime;
                    }
                    
                }
                merge(child1, child2);
            }
        }
    }

    
    
    public class SpeedNode {
        
        private String m_key;
        private String m_fullKey;
        private long m_startTime;
        
        private long m_callNumbers = 0;
        private long m_minTime = Long.MAX_VALUE;
        private long m_maxTime = 0;
        private long m_totalTime = 0;
        
        private SpeedNode m_parent;
        private HashMap<String, SpeedNode> m_children = null;
        
        public SpeedNode(String key, SpeedNode parent) {
            m_key = key;
            m_parent = parent;
            if (parent != null) {
                m_fullKey = parent.getFullKey()+" - "+key;
            } else {
                m_fullKey = key;
            }
        }
        
        public SpeedNode getChild(String key) {
            
            SpeedNode child = null;
            if (m_children == null) {
                m_children = new HashMap<>();
            } else {
                child = m_children.get(key);
            }
            if (child == null) {
                child = new SpeedNode(key, this);
                m_children.put(key, child);
            }
            return child;
        }
        
        public void addChild(SpeedNode child) {
            if (m_children == null) {
                m_children = new HashMap<>();
            }
            m_children.put(child.m_key, child);
        }
        
        public void setStartTime(long startTime) {
            m_startTime = startTime;
        }
        
        public void stop(long stopTime) {
            m_callNumbers++;
            long delay = stopTime - m_startTime;
            if (delay<m_minTime) {
                m_minTime = delay;
            }
            if (delay>m_maxTime)  {
                m_maxTime = delay;
            }
            m_totalTime += delay;
        }
        
        public void getSortedNodes(LinkedList<SpeedNode> list) {
            
            if (m_children != null){
                LinkedList<SpeedNode> childrenList = new LinkedList<>();
                childrenList.addAll(m_children.values());
                Collections.sort(childrenList, m_comparator);

                for (SpeedNode curNode : childrenList) {
                    list.add(curNode);
                    curNode.getSortedNodes(list);
                }
            }
        }
        
        public String getFullKey() {
            return m_fullKey;
        }
        
        public void display() {
            
            m_logger.debug(m_fullKey+" Total:"+m_totalTime+"ms"+"   **** NbCalls:"+m_callNumbers+"  Average:"+m_totalTime/m_callNumbers+"ms  Min:"+m_minTime+"ms  Max:"+m_maxTime+"ms");
        }
        
    }
    
}
