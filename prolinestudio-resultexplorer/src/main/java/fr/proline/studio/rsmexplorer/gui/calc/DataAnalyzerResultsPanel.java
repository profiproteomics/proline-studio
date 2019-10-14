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

import fr.proline.studio.gui.ClosableTabPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.WindowBox;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * Panel used to display results of the Data Analyzer bellow the graph zone.
 * 
 * It contains a JTabbedPane. Each Tab correspond to an execution of the graph.
 * 
 * 
 * @author JM235353
 */
public class DataAnalyzerResultsPanel extends JPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    
    private final JTabbedPane m_tabbedPane = new JTabbedPane(); 
    
    private final HashMap<Integer, WindowBox> m_processKeyToWindowBoxMap = new HashMap<>();
    
    public DataAnalyzerResultsPanel() {
        initComponents();
    }
    
    private void initComponents() {


        setLayout(new BorderLayout());

        add(m_tabbedPane, BorderLayout.CENTER);

    }
    
    
    public void displayGraphNode(ProcessEngineInfo processEngineInfo) {
        
        ArrayList<WindowBox> windowBoxList = processEngineInfo.getGraphNode().getDisplayWindowBox(processEngineInfo.getIndex());
        Integer processEngineKey = processEngineInfo.getProcessKey();
        
        ArrayList<SplittedPanelContainer.PanelLayout> layoutList = processEngineInfo.getLayout();
        
        for (int i = 0; i < windowBoxList.size(); i++) {
            
            WindowBox windowBox = windowBoxList.get(i);
            WindowBox existingWindowBox = m_processKeyToWindowBoxMap.get(processEngineKey);
            if (existingWindowBox != null) {
                AbstractDataBox databox = windowBox.getEntryBox();
                SplittedPanelContainer.PanelLayout layout = layoutList.get(i);
                while (databox != null) {
                    existingWindowBox.addDatabox(databox, layout);
                    ArrayList<AbstractDataBox> list =  databox.getNextDataBoxArray();
                    if ((list != null) && (!list.isEmpty())) {
                        databox = list.get(0);
                        layout = databox.getLayout();
                    } else {
                        databox = null;
                    }
                }
            } else {
                m_processKeyToWindowBoxMap.put(processEngineKey, windowBox);
                String processName = processEngineInfo.getProcessName();

                m_tabbedPane.addTab(null, windowBox.getPanel());
                ClosableTabPanel closableTabPanel = new ClosableTabPanel(m_tabbedPane, processEngineKey + ": " + processName, processEngineKey.toString());
                m_tabbedPane.setTabComponentAt(m_tabbedPane.getTabCount() - 1, closableTabPanel);
                m_tabbedPane.setSelectedIndex(m_tabbedPane.getTabCount() - 1);
            }
        }
        
    }

    
    @Override
    public void addSingleValue(Object v) {
        // not used 
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public void setLoading(int id) {}

    @Override
    public void setLoading(int id, boolean calculating) {}

    @Override
    public void setLoaded(int id) {}

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }
    
}
