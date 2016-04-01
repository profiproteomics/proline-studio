package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
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
    public String getErrorMessage() {
        return m_graphic.getErrorMessage();
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
            return IconManager.getIcon(IconManager.IconType.SETTINGS);
        }
        
        if (m_graphic.isCalculating()) {
            return IconManager.getIcon(IconManager.IconType.HOUR_GLASS);
        }
        if (m_graphic.inError()) {
            return IconManager.getIcon(IconManager.IconType.EXCLAMATION);
        }
        if (m_graphic.calculationDone()) {
            return IconManager.getIcon(IconManager.IconType.TICK_CIRCLE);
        }
        if (m_graphic.isSettingsBeingDone()) {
            return IconManager.getIcon(IconManager.IconType.SETTINGS);
        }

        return IconManager.getIcon(IconManager.IconType.CONTROL_PAUSE);

    }
    
    @Override
    public boolean canBeProcessed() {
        return ! ((!isConnected()) || (!settingsDone()) || (m_graphic.isCalculating()) || (m_graphic.inError()) || (m_graphic.calculationDone()) || (m_graphic.isSettingsBeingDone()));

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
        
        if (!m_graphic.calculationDone()) {
            return false;
        }
        
        return true;
    }

    @Override
    public void process(ProcessCallbackInterface callback) {
        

        if (!isConnected()) {
            callback.finished(this);
            return;
        }

        if (!settingsDone()) {

            // do settings
            boolean settingsDone = settings();
            m_graphPanel.repaint();

            if (!settingsDone) {
                callback.stopped(this);
            } else {
                callback.reprocess(this);
            }
            return;
        }

        try {
            AbstractConnectedGraphObject[] graphObjectArray = new AbstractConnectedGraphObject[m_inConnectors.size()];
            int i = 0;
            for (GraphConnector connector : m_inConnectors) {
                GraphNode graphNode = connector.getLinkedSourceGraphNode();
                graphObjectArray[i++] = graphNode;
            }

            m_graphic.process(graphObjectArray, this, callback);

        } finally {
            callback.finished(this);
        }
    }

    @Override
    public void askDisplay() {
        
        String dataName = "";
        if (m_inConnectors.size()>0) {
            GraphNode graphNode = m_inConnectors.get(0).getLinkedSourceGraphNode();
            dataName = graphNode.getDataName();
        }

        m_graphic.display(dataName);
    }

    
    @Override
    public boolean settings() {
        
        AbstractConnectedGraphObject[] graphObjectArray = new AbstractConnectedGraphObject[m_inConnectors.size()];
        int i = 0;
        for (GraphConnector connector : m_inConnectors) {
            GraphNode graphNode = connector.getLinkedSourceGraphNode();
            //graphNode.process(false, null);  // need to process previous nodes to be able to do settings  //JPM.TODO
            graphObjectArray[i++] = graphNode;
        }
        
        
        
        boolean settingsChanged = m_graphic.settings(graphObjectArray, this);
        if (settingsChanged) {
            super.propagateSourceChanged();
        }
        
        return settingsChanged;
    }

    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return null;
    }
 
}

