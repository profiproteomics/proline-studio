package fr.proline.studio.rsmexplorer.gui.calc.graph;


import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
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
        m_outConnector = new GraphConnector(this, true, panel);
        
        int nbParameters = function.getNumberOfInParameters();
        if (nbParameters > 0) {
            m_inConnectors = new LinkedList<>();
            for (int i = 0; i < nbParameters; i++) {
                m_inConnectors.add(new GraphConnector(this, false, panel));
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
        
        if (!isConnected(false)) {
            return IconManager.getIcon(IconManager.IconType.WARNING);
        } else if (!isConnected(true)) {
            return null;
        }
        
        if (!settingsDone()) {
            if (canSetSettings()) {
                return IconManager.getIcon(IconManager.IconType.SETTINGS);
            } else {
                return null;
            }
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
        if (m_function.isSettingsBeingDone()) {
            return IconManager.getIcon(IconManager.IconType.SETTINGS);
        }
        return IconManager.getIcon(IconManager.IconType.CONTROL_PAUSE);

    }
    
    @Override
    public ImageIcon getDisplayIcon() {
        if (m_function.calculationDone()) {
            return IconManager.getIcon(IconManager.IconType.TABLE);
        } else {
            return null;
        }
    }
    
    @Override
    public NodeAction possibleAction() {
        if (!isConnected(true)) {
            return NodeAction.NO_ACTION;
        }
        if (!settingsDone()) {
            return canSetSettings() ? NodeAction.STEP_ACTION : NodeAction.NO_ACTION;
        }
        if (m_function.isCalculating()) {
            return NodeAction.NO_ACTION;
        }
        if (m_function.inError()) {
            return NodeAction.ERROR_ACTION;
        }
        if (m_function.calculationDone()) {
            return NodeAction.RESULT_ACTION;
        }
        if (m_function.isSettingsBeingDone()) {
            return NodeAction.NO_ACTION;
        }
        // process
        return NodeAction.STEP_ACTION;
    }
    
    @Override
    public void doAction() {

        m_graphNodeAction.setHighlighted(false);
        
        if (!settingsDone()) {
            // settings
            int nbConnections = (m_inConnectors == null) ? 0 : m_inConnectors.size();
            new SettingsAction(this, nbConnections).actionPerformed(null);
            return;
        }
        if (m_function.inError()) {
            // display error
            new ErrorAction(this).actionPerformed(null);
            return;
        }
        if (m_function.calculationDone()) {
            // display in new window
            int nbConnections = (m_inConnectors == null) ? 0 : m_inConnectors.size();
            new DisplayInNewWindowAction(this, nbConnections).actionPerformed(null);
            return;
        }

        // process
        new ProcessAction(this).actionPerformed(null);
    }

    @Override
    public boolean canBeProcessed() {
        return ! ((!isConnected(true)) || (!settingsDone()) || (m_function.isCalculating()) || (m_function.inError()) || (m_function.calculationDone()) || (m_function.isSettingsBeingDone()));

    }
    
    @Override
    public void propagateSourceChanged() {
        m_function.inLinkDeleted();
        super.propagateSourceChanged();
    }

    @Override
    public boolean isConnected(boolean recursive) {
        int countUnlinkedConnectors = m_function.getNumberOfInParameters();
        if (m_inConnectors != null) {
            for (GraphConnector connector : m_inConnectors) {
                if (connector.isConnected(recursive)) {
                    countUnlinkedConnectors--;
                }
            }
        }
        return (countUnlinkedConnectors == 0);
    }
    
    @Override
    public boolean canSetSettings() {
        if (!isConnected(true)) {
            return false;
        }
        int countSettingsDone = m_function.getNumberOfInParameters();
        if (m_inConnectors != null) {
            for (GraphConnector connector : m_inConnectors) {
                if (connector.isConnected(true)) {

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
        if (!isConnected(true)) {
            return false;
        }
        
        if (!settingsDone()) {
            return false;
        }

        return m_function.calculationDone();
    }


    
    @Override
    public void process(ProcessCallbackInterface callback) {

        if (!isConnected(true)) {
            callback.finished(this);
            return;
        }

        if (!m_function.settingsDone()) {
            
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
        
        AbstractConnectedGraphObject[] graphObjectArray;
        
        if (m_inConnectors != null) {
            graphObjectArray = new AbstractConnectedGraphObject[m_inConnectors.size()];
            int i = 0;
            for (GraphConnector connector : m_inConnectors) {
                GraphNode graphNode = connector.getLinkedSourceGraphNode();
                graphObjectArray[i++] = graphNode;
            }
        } else {
            graphObjectArray = new AbstractConnectedGraphObject[0];
        }
        
        m_function.process(graphObjectArray, this, callback);

        
    }

    @Override
    public void askDisplay() {
        m_function.askDisplay(this);
    }
    
    @Override
    public WindowBox getDisplayWindowBox() {
        return m_function.getDisplayWindowBox(this);
    }
    
    @Override
    public SplittedPanelContainer.PanelLayout getAutoDisplayLayoutDuringProcess() {
        return m_function.getAutoDisplayLayoutDuringProcess();
    }

    
    @Override
    public boolean settings() {
        
        AbstractConnectedGraphObject[] graphObjectArray;
        if (m_inConnectors != null) {
            graphObjectArray = new AbstractConnectedGraphObject[m_inConnectors.size()];
            int i = 0;
            for (GraphConnector connector : m_inConnectors) {
                GraphNode graphNode = connector.getLinkedSourceGraphNode();
                graphObjectArray[i++] = graphNode;
            }
        } else {
            graphObjectArray = new AbstractConnectedGraphObject[0];
        }
        
        
        
        boolean settingsChanged = m_function.settings(graphObjectArray, this);
        if (settingsChanged) {
            m_function.resetError();
            super.propagateSourceChanged();
        }
        
        return settingsChanged;
    }

    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return m_function.getGlobalTableModelInterface();
    }
 

    
}
