package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.awt.Color;
import javax.swing.ImageIcon;


/**
 *
 * @author JM235353
 */
public class DataGraphNode extends GraphNode {
    
    private static final Color FRAME_COLOR = new Color(51,128,200);

    
    private TableInfo m_tableInfo = null;


    
    public DataGraphNode(TableInfo tableInfo) {
        m_tableInfo = tableInfo;
        
        m_outConnector = new GraphConnector(this, true);
    }

    @Override
    public String getName() {
        return m_tableInfo.getName();
    }

    @Override
    public Color getFrameColor() {
        return FRAME_COLOR;
    }
    
    @Override
    public ImageIcon getIcon() {
        return m_tableInfo.getIcon();
    }

    @Override
    public void display() {
        WindowBox windowBox = WindowBoxFactory.getModelWindowBox(getName());
        windowBox.setEntryData(-1, m_tableInfo.getModel());
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(windowBox);
        win.open();
        win.requestActive();
    }

    @Override
    public NodeState getState() {
        if (m_state != NodeState.UNSET) {
            return m_state;
        }
        
        if (m_tableInfo.getModel().isLoaded()) {
            m_state = NodeState.READY;
        } else {
            m_state = NodeState.DATA_LOADING;
        }
        
        return m_state;
    }
    
    @Override
    public boolean isConnected() {
        return true;
    }


    @Override
    public void process() {
        // nothing to do
    }

    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return m_tableInfo.getModel();
    }

    
}
