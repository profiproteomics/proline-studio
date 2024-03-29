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
package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphGroup;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

/**
 *
 * Manage the processing of the graph of the DataAnalyzer
 * 
 * @author JM235353
 */
public class ProcessEngine implements ProcessCallbackInterface {
    
    private static ProcessEngine m_processEngine = null;
    
    private int m_processEngineKey = 0;
    private String m_currentMacro = null;
    
    private GraphPanel m_panel;
    private JButton m_playButton;
    
    private boolean m_runAll = false;
    
    private final LinkedList<GraphNode> m_processingNodeList = new LinkedList<>();
    
    private ProcessEngine() {  
    }
    
    public static ProcessEngine getProcessEngine() {
        if (m_processEngine == null) {
            m_processEngine = new ProcessEngine();
        }
        
        return m_processEngine;
    }
    
    public void setRunAll(boolean v) {
        m_runAll = v;
    }
    
    public boolean isRunAll() {
        return m_runAll;
    }
    
    public String getProcessName() {
        if (m_currentMacro != null) {
            return m_currentMacro;
        } else {
            return "Results";
        }
    }
    
    public void run(LinkedList<GraphNode> graphNodeArray, GraphPanel panel, JButton playButton) {

        setRunAll(true);
        
        m_processEngineKey++;
        m_panel = panel;
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
    
    public Integer getProcessEngineKey(boolean bumpKey) {
        if (bumpKey) {
            m_processEngineKey++;
        }
        return m_processEngineKey;
    }
    
    public void runANode(GraphNode node, GraphPanel panel) {
        
        setRunAll(false);
        
        m_panel = panel;
        m_playButton = null;
        
        m_processingNodeList.add(node);
        
        processNodes();
    }
    
    private void processNodes() {
        GraphNode firstNode = m_processingNodeList.pollFirst();
        process(firstNode);
    }

    @Override
    public void reprocess(GraphNode node) {
        process(node);
    }
    
    private void process(GraphNode node) {
        
        node.setHighlighted(true);
        m_panel.repaint();
        
        GraphGroup group = node.getGroup();
        if (group != null) {
            m_currentMacro = group.getGroupName();
        } else {
            m_currentMacro = null;
        }
        
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
        m_runAll = false;
    }
    
    @Override
    public void finished(GraphNode node) {
  
        node.setHighlighted(false);
        
         ArrayList<SplittedPanelContainer.PanelLayout> layout = node.getAutoDisplayLayoutDuringProcess();
        if (layout != null) {
            m_panel.displayBelow(node, false, null, layout, -1);
        }
        
        m_panel.repaint();
        
        
        if (m_runAll) {
            LinkedList<GraphNode> nextNodes = node.getOutLinkedGraphNodes();
            if (nextNodes != null) {
                for (GraphNode nextNode : nextNodes) {
                    m_processingNodeList.add(nextNode);
                }
            }
        }
        
        if (! m_processingNodeList.isEmpty()) {
            // invoke Later to let time to correctly do the result display needed
            final GraphNode firstNode = m_processingNodeList.pollFirst();
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    process(firstNode);
                }
            });
            
        } else {
            // processing is finished
            if (m_playButton!= null) {
                m_playButton.setEnabled(true);
                m_playButton = null;
            }
            m_panel = null;
            m_currentMacro = null;
            m_runAll = false;
            
        }
    }




}
