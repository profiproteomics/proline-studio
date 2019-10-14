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
