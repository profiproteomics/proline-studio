package fr.proline.studio.rsmexplorer.gui.calc.graph;


import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessEngine;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

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
        
        int nbOutParameters = function.getNumberOfOutParameters();
        if (nbOutParameters > 0) {
            m_outConnector = new LinkedList<>();
            for (int i = 0; i < nbOutParameters; i++) {
                m_outConnector.add(new GraphConnector(this, true, i, panel));
            }
        }
        
        int nbInParameters = function.getNumberOfInParameters();
        if (nbInParameters > 0) {
            m_inConnectors = new LinkedList<>();
            for (int i = 0; i < nbInParameters; i++) {
                m_inConnectors.add(new GraphConnector(this, false, i, panel));
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
    public String getFullName(int index) {
        if (index>=0) {
            return getFullName()+" / "+getOutTooltip(index);
        }
        return getFullName();
        
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
        return IconManager.getIcon(IconManager.IconType.CONTROL_PLAY);

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
    public void doAction(int x, int y) {

        if (m_graphNodeAction.isHighlighted()) {
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
                new DisplayInNewWindowAction(this, 0, null).actionPerformed(null);
                return;
            }

            // process
            new ProcessAction(this).actionPerformed(null);
        } else if (m_menuAction.isHighlighted()) {
            m_menuAction.setHighlighted(false);
            JPopupMenu popup = createPopup(m_graphPanel);
            if (popup != null) {
                popup.show(m_graphPanel, x, y);
            }
        }
    }

    @Override
    public boolean canBeProcessed() {
        return ! ((!isConnected(true)) || (!settingsDone()) || (m_function.isCalculating()) || (m_function.inError()) || (m_function.calculationDone()) || (m_function.isSettingsBeingDone()));

    }
    
    @Override
    public void propagateSourceChanged() {
        m_function.inLinkModified();
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
        return (countUnlinkedConnectors <= 0); // can be negative if there are optional in parameters
    }
    
    @Override
    public boolean canAddConnector() {
        if (m_inConnectors == null) {
            return true;
        }
        
        return (m_function.getMaximumNumberOfInParameters()-m_inConnectors.size())>0;
    }
    
    @Override
    public void addFreeConnector() {
        m_inConnectors.add(new GraphConnector(this, false, m_inConnectors.size(), m_graphPanel));
        setConnectorsPosition();
    }
    
    @Override
    public void updateNumberOfInConnections() {

        int nbConnections = m_inConnectors.size();
        int nbInParameters = m_function.getNumberOfInParameters();
        boolean modification = false;
        while ((nbConnections>2) && (nbConnections>nbInParameters)) {
            if ((!m_inConnectors.get(nbConnections-1).isConnected(false) ) && (!m_inConnectors.get(m_inConnectors.size()-2).isConnected(false) ) ) {
                // last two extra connections are not used : remove the last one
                m_inConnectors.removeLast();
                modification = true;
                nbConnections--;
            } else {
                break;
            }
        }
        if (modification) {
            setConnectorsPosition();
        }
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

                    GraphNode graphNode = connector.getLinkedSourceGraphConnector().getGraphNode();
                    if (graphNode.settingsDone() && graphNode.calculationDone()) {
                        countSettingsDone--;
                    }
                }
            }
        }
        return (countSettingsDone <= 0); // can be negative if there are optional in parameters
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
        
        GraphConnector[] graphObjectArray;
        
        if (m_inConnectors != null) {

            int nbLinkedConnectors = 0;
            for (GraphConnector connector : m_inConnectors) {
                GraphConnector srcConnector = connector.getLinkedSourceGraphConnector();
                if (srcConnector != null) {
                    nbLinkedConnectors++;
                }
            }

            graphObjectArray = new GraphConnector[nbLinkedConnectors];
            int i = 0;
            for (GraphConnector connector : m_inConnectors) {
                GraphConnector srcConnector = connector.getLinkedSourceGraphConnector();
                if (srcConnector != null) {
                    graphObjectArray[i++] = srcConnector;
                }
            }
        } else {
            graphObjectArray = new GraphConnector[0];
        }


        m_function.process(graphObjectArray, this, callback);

        
    }

    @Override
    public void askDisplay(int index) {
        m_function.askDisplay(this, index);
    }
    
    @Override
    public ArrayList<WindowBox> getDisplayWindowBox(int index) {
        return m_function.getDisplayWindowBox(this, index);
    }
    
    @Override
    public ArrayList<SplittedPanelContainer.PanelLayout> getAutoDisplayLayoutDuringProcess() {
        return m_function.getAutoDisplayLayoutDuringProcess();
    }

    
    @Override
    public boolean settings() {
        
        GraphConnector[] graphObjectArray;
        if (m_inConnectors != null) {
            
            int nbLinkedConnectors = 0;
            for (GraphConnector connector : m_inConnectors) {
                GraphConnector srcConnector = connector.getLinkedSourceGraphConnector();
                if (srcConnector != null) {
                    nbLinkedConnectors++;
                }
            }
            
            graphObjectArray = new GraphConnector[nbLinkedConnectors];
            int i = 0;
            for (GraphConnector connector : m_inConnectors) {
                GraphConnector srcConnector = connector.getLinkedSourceGraphConnector();
                if (srcConnector != null) {
                    graphObjectArray[i++] = srcConnector;
                }
            }
        } else {
            graphObjectArray = new GraphConnector[0];
        }
        
        
        
        boolean settingsChanged = m_function.settings(graphObjectArray, this);
        if (settingsChanged) {
            m_function.resetError();
            super.propagateSourceChanged();
            if (!ProcessEngine.getProcessEngine().isRunAll()) {
                ProcessEngine.getProcessEngine().runANode(this, m_graphPanel);
            }
        }
        
        return settingsChanged;
    }

    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface(int index) {
        return m_function.getMainGlobalTableModelInterface(index);
    }
 
    public String getOutTooltip(int index) {
        return m_function.getOutTooltip(index);
    }

    @Override
    public String getTooltip(int x, int y) {
        return null;
    }
    
}
