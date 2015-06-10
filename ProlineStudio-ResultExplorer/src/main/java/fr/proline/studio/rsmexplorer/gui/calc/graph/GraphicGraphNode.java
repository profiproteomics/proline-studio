package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.AbstractGraphic;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.util.LinkedList;
import javax.swing.ImageIcon;

/**
 * Graph Node representing a Graphic
 * @author JM235353
 */
public class GraphicGraphNode extends GraphNode {
    
    private static final Color FRAME_COLOR = new Color(240,160,40);

    private final AbstractGraphic m_graphic;

    
    public GraphicGraphNode(GraphPanel panel, AbstractGraphic graphic) {
        super(panel);
        m_outConnector = null;
        
        m_graphic = graphic;
        
        m_inConnectors = new LinkedList<>();
        m_inConnectors.add(new GraphConnector(this, false));
    }

    @Override
    public String getFullName() {
        String dataName = getDataName();
        if (dataName == null) {
            return m_graphic.getName();
        }
        return dataName+' '+m_graphic.getName();
    }

    @Override
    public String getDataName() {
        return getPreviousDataName();
    }

    @Override
    public String getTypeName() {
        return m_graphic.getName();
    }
    
    
    @Override
    public Color getFrameColor() {
        return FRAME_COLOR;
    }


    @Override
    public ImageIcon getIcon() {
        return m_graphic.getIcon();
    }
    
    @Override
    public ImageIcon getStatusIcon() {
        
        if (!isConnected()) {
            return IconManager.getIcon(IconManager.IconType.WARNING);
        }
        if (!settingsDone()) {
            return IconManager.getIcon(IconManager.IconType.WARNING);
        }

        return IconManager.getIcon(IconManager.IconType.TICK_CIRCLE);

    }
    
    @Override
    public void propagateSourceChanged() {
        m_graphic.inLinkDeleted();
        super.propagateSourceChanged();
    }

    @Override
    public boolean isConnected() {

        return m_inConnectors.get(0).isConnected();

    }
    
    @Override
    public boolean canSetSettings() {
        if (!isConnected()) {
            return false;
        }

        GraphConnector connector = m_inConnectors.get(0);

        if (connector.isConnected()) {

            GraphNode graphNode = connector.getLinkedSourceGraphNode();
            if (graphNode.settingsDone() && graphNode.calculationDone()) {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public boolean settingsDone() {
        return m_graphic.settingsDone();
    }
    
    @Override
    public boolean calculationDone() {
        if (!isConnected()) {
            return false;
        }
        
        if (!settingsDone()) {
            return false;
        }
        
        return true;
    }

    @Override
    public void process(boolean display) {
        if (!isConnected()) {
            return;
        }

        AbstractGraphObject[] graphObjectArray = new AbstractGraphObject[m_inConnectors.size()];
        int i = 0;
        for (GraphConnector connector : m_inConnectors) {
            GraphNode graphNode = connector.getLinkedSourceGraphNode();
            graphNode.process(false);
            graphObjectArray[i++] = graphNode;
        }
        
        m_graphic.process(graphObjectArray, this, display);
        
    }

    @Override
    public void askDisplay() {
        process(true);
    }

    
    @Override
    public void settings() {
        
        AbstractGraphObject[] graphObjectArray = new AbstractGraphObject[m_inConnectors.size()];
        int i = 0;
        for (GraphConnector connector : m_inConnectors) {
            GraphNode graphNode = connector.getLinkedSourceGraphNode();
            graphNode.process(false);  // need to process previous nodes to be able to do settings
            graphObjectArray[i++] = graphNode;
        }
        
        
        
        boolean settingsChanged = m_graphic.settings(graphObjectArray);
        if (settingsChanged) {
            super.propagateSourceChanged();
        }
    }

    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return null;
    }
 
}

