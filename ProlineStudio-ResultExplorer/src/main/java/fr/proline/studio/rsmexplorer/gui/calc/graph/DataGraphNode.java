package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.python.data.TableInfo;
import java.awt.Color;
import java.util.LinkedList;

/**
 *
 * @author JM235353
 */
public class DataGraphNode extends GraphNode {
    
    private static final Color FRAME_COLOR = new Color(45,114,178);
    private static final Color BACKGROUND_COLOR = new Color(186,209,239);
    
    private TableInfo m_tableInfo = null;


    
    public DataGraphNode(TableInfo tableInfo) {
        m_tableInfo = tableInfo;
        
        m_outConnector = new GraphConnector(true);
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
    public Color getBackgroundColor() {
        return BACKGROUND_COLOR;
    }



    

    
}
