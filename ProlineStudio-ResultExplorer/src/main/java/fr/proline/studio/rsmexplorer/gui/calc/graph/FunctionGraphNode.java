package fr.proline.studio.rsmexplorer.gui.calc.graph;


import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.util.LinkedList;
import javax.swing.ImageIcon;

/**
 * Graph Node representing a Function
 * @author JM235353
 */
public class FunctionGraphNode extends GraphNode {
    
    private static final Color FRAME_COLOR = new Color(149,195,95);

    private final AbstractFunction m_function;

    
    public FunctionGraphNode(AbstractFunction function, GraphPanel panel) {
        super(panel);
        m_function = function;
        m_outConnector = new GraphConnector(this, true);
        
        int nbParameters = function.getNumberOfInParameters();
        if (nbParameters > 0) {
            m_inConnectors = new LinkedList<>();
            for (int i = 0; i < nbParameters; i++) {
                m_inConnectors.add(new GraphConnector(this, false));
            }
        }
    }

    @Override
    public String getFullName() {
        String dataName = getDataName();
        if (dataName == null) {
            return m_function.getName();
        }
        return dataName+' '+m_function.getName();
    }

    @Override
    public String getDataName() {
        String dataName = m_function.getDataName();
        if (dataName != null) {
            return dataName;
        }
        return getPreviousDataName();
    }

    @Override
    public String getTypeName() {
        return m_function.getName();
    }
    
    @Override
    public String getErrorMessage() {
        return m_function.getErrorMessage();
    }
    
    @Override
    public Color getFrameColor() {
        Color frameColor = m_function.getFrameColor();
        if (frameColor != null) {
            return frameColor;
        }
        return FRAME_COLOR;
    }


    @Override
    public ImageIcon getIcon() {
        return m_function.getIcon();
    }
    
    @Override
    public ImageIcon getStatusIcon() {
        
        if (!isConnected()) {
            return IconManager.getIcon(IconManager.IconType.WARNING);
        }
        if (!settingsDone()) {
            return IconManager.getIcon(IconManager.IconType.WARNING);
        }
        
        if (m_function.isCalculating()) {
            return IconManager.getIcon(IconManager.IconType.HOUR_GLASS);
        }
        if (m_function.inError()) {
            return IconManager.getIcon(IconManager.IconType.EXCLAMATION);
        }
        if (m_function.calculationDone()) {
            return IconManager.getIcon(IconManager.IconType.TICK_CIRCLE);
        }
        
        return null;

    }
    
    @Override
    public void propagateSourceChanged() {
        m_function.inLinkDeleted();
        super.propagateSourceChanged();
    }

    @Override
    public boolean isConnected() {
        int countUnlinkedConnectors = m_function.getNumberOfInParameters();
        if (m_inConnectors != null) {
            for (GraphConnector connector : m_inConnectors) {
                if (connector.isConnected()) {
                    countUnlinkedConnectors--;
                }
            }
        }
        return (countUnlinkedConnectors == 0);
    }
    
    @Override
    public boolean canSetSettings() {
        if (!isConnected()) {
            return false;
        }
        int countSettingsDone = m_function.getNumberOfInParameters();
        if (m_inConnectors != null) {
            for (GraphConnector connector : m_inConnectors) {
                if (connector.isConnected()) {

                    GraphNode graphNode = connector.getLinkedSourceGraphNode();
                    if (graphNode.settingsDone() && graphNode.calculationDone()) {
                        countSettingsDone--;
                    }
                }
            }
        }
        return (countSettingsDone == 0);
    }
    
    @Override
    public boolean settingsDone() {
        return m_function.settingsDone();
    }
    
    @Override
    public boolean calculationDone() {
        if (!isConnected()) {
            return false;
        }
        
        if (!settingsDone()) {
            return false;
        }
        
        if (m_function.calculationDone()) {
            return true;
        }
        
        // we try to process
        process(false);
        
        return m_function.calculationDone();
    }

    @Override
    public void process(boolean display) {
        if (!isConnected()) {
            return;
        }

        AbstractGraphObject[] graphObjectArray;
        
        if (m_inConnectors != null) {
            graphObjectArray = new AbstractGraphObject[m_inConnectors.size()];
            int i = 0;
            for (GraphConnector connector : m_inConnectors) {
                GraphNode graphNode = connector.getLinkedSourceGraphNode();
                graphNode.process(false);
                graphObjectArray[i++] = graphNode;
            }
        } else {
            graphObjectArray = new AbstractGraphObject[0];
        }
        
        m_function.process(graphObjectArray, this, display);
        
    }

    @Override
    public void askDisplay() {
        process(true);
    }

    
    @Override
    public void settings() {
        
        AbstractGraphObject[] graphObjectArray;
        if (m_inConnectors != null) {
            graphObjectArray = new AbstractGraphObject[m_inConnectors.size()];
            int i = 0;
            for (GraphConnector connector : m_inConnectors) {
                GraphNode graphNode = connector.getLinkedSourceGraphNode();
                graphNode.process(false);  // need to process previous nodes to be able to do settings
                graphObjectArray[i++] = graphNode;
            }
        } else {
            graphObjectArray = new AbstractGraphObject[0];
        }
        
        
        
        boolean settingsChanged = m_function.settings(graphObjectArray);
        if (settingsChanged) {
            super.propagateSourceChanged();
        }
    }

    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return m_function.getGlobalTableModelInterface();
    }
 
}
