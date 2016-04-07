package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author JM235353
 */
public class DataAnalyzerResultsPanel extends JPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    
    private JTabbedPane m_tabbedPane = new JTabbedPane(); 
    
    private HashMap<Integer, WindowBox> m_processKeyToWindowBoxMap = new HashMap<>();
    
    public DataAnalyzerResultsPanel() {
        initComponents();
    }
    
    private void initComponents() {


        setLayout(new BorderLayout());

        add(m_tabbedPane, BorderLayout.CENTER);

    }
    
    
    public void displayGraphNode(ProcessEngineInfo processEngineInfo) {
        
        WindowBox windowBox = processEngineInfo.getGraphNode().getDisplayWindowBox();
        Integer processEngineKey = processEngineInfo.getProcessKey();
        WindowBox existingWindowBox = m_processKeyToWindowBoxMap.get(processEngineKey);
        if (existingWindowBox != null) {
            existingWindowBox.addDatabox(windowBox.getEntryBox());
        } else {
            m_processKeyToWindowBoxMap.put(processEngineKey, windowBox);
            String processName = processEngineInfo.getProcessName();
            m_tabbedPane.addTab(processEngineKey+": "+processName, windowBox.getPanel());
            m_tabbedPane.setSelectedIndex(m_tabbedPane.getTabCount()-1);
        }
        
        
    }

    
    @Override
    public void addSingleValue(Object v) {
        // not used for the moment JPM.TODO ?
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
