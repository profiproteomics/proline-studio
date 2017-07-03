package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rserver.dialog.ImageViewerTopComponent;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.CheckParameterInterface;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.FunctionParametersDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.openide.windows.WindowManager;

/**
 * Base class for the definition of a Graphic (scatter, histogram...) with its parameters
 * in the data analyzer
 * @author JM235353
 */
public abstract class AbstractGraphic implements CheckParameterInterface {

    protected LockedDataGraphicsModel m_graphicsModelInterface;
    protected Image m_generatedImage;
    
    protected ParameterList[] m_parameters = null;

    private boolean m_calculating = false;
    private boolean m_settingsBeingDone = false;
    private boolean m_inError = false;
    private String m_errorMessage = null;
    
    private  ArrayList<SplittedPanelContainer.PanelLayout> m_autoDisplayLayoutDuringProcess = null;
    
    protected GraphPanel m_panel;

    
    public AbstractGraphic(GraphPanel panel) {
        m_panel = panel;
    }
    
    public void cloneInfo(AbstractGraphic src) {
        m_autoDisplayLayoutDuringProcess = src.m_autoDisplayLayoutDuringProcess;
    }

    public void inLinkDeleted() {
        m_parameters = null;
        m_graphicsModelInterface = null;
        m_generatedImage = null;
    }

    
    public abstract String getName();

    public LockedDataGraphicsModel getGraphicsModelInterface() {
        return m_graphicsModelInterface;
    }

    public abstract void process(GraphConnector[] graphObjects, GraphicGraphNode graphicGraphNode, ProcessCallbackInterface callback);
    public abstract void generateDefaultParameters(GraphConnector[] graphObjects);
    public abstract void userParametersChanged();
    public abstract AbstractGraphic cloneGraphic(GraphPanel p);
    

    
    public void setAutoDisplayLayoutDuringProcess( ArrayList<SplittedPanelContainer.PanelLayout> layout) {
        m_autoDisplayLayoutDuringProcess = layout;
    }
    
    public ArrayList<SplittedPanelContainer.PanelLayout> getAutoDisplayLayoutDuringProcess() {
        return m_autoDisplayLayoutDuringProcess;
    }
        
    public void display(String dataName) {
        String functionName = getName();
        if (m_generatedImage != null) {
            ImageViewerTopComponent win = new ImageViewerTopComponent(dataName + " " + functionName, m_generatedImage);
            win.open();
            win.requestActive();
        } else {
            WindowBox windowBox = WindowBoxFactory.getGraphicsWindowBox(dataName, m_graphicsModelInterface, true);
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(windowBox);
            win.open();
            win.requestActive();
        }
    }
    
    public  WindowBox getDisplayWindowBox(String dataName) {
        String functionName = getName();
        if (m_generatedImage != null) {
            return WindowBoxFactory.getImageWindowBox(dataName+" "+functionName, m_generatedImage);
        } else {
            return WindowBoxFactory.getGraphicsWindowBox(dataName, m_graphicsModelInterface, true);
        }
    }
    
    public abstract boolean calculationDone();
    public abstract boolean settingsDone();
    public boolean isCalculating() {
        return m_calculating;
    }
    public boolean inError() {
        return m_inError;
    }
    public boolean isSettingsBeingDone() {
        return m_settingsBeingDone;
    }
    
    protected void setCalculating(boolean v) {
        if (v ^ m_calculating) {
            m_calculating = v;
            m_panel.repaint();
        }
    }

    protected void setInError(CalcError error) {
        if (error == null) {
            setInError(false, null);
        } else {
            setInError(true, error.getFullErrorMessage());
        }
    }
    
    protected void setInError(boolean v, String errorMessage) {
        if (v ^ m_inError) {
            m_inError = v;
            m_panel.repaint();
        }
        m_errorMessage = errorMessage;
    }
    
    public String getErrorMessage() {
        return m_errorMessage;
    }
    
    public boolean settings(GraphConnector[] graphObjects, GraphNode node) {
        
        
        boolean thereIsNoParameterAtStart = (m_parameters == null);
        if (thereIsNoParameterAtStart) {
            generateDefaultParameters(graphObjects);
        }
        if (m_parameters == null) {
            return false;
        }

        m_settingsBeingDone = true;
        try {
            FunctionParametersDialog dialog = new FunctionParametersDialog(getName(), WindowManager.getDefault().getMainWindow(), m_parameters, this, graphObjects);
            dialog.setImageInfo(m_panel, node.getX()-60, node.getY()-40, node.getXEnd()-node.getX()+120, node.getYEnd()-node.getY()+80);
            dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            dialog.setVisible(true);
            if (dialog.getButtonClicked() == FunctionParametersDialog.BUTTON_OK) {
                userParametersChanged();
                return true;
            }
            if (thereIsNoParameterAtStart) {
                m_parameters = null; // JPM.HACK : we do not keep default parameters, to avoid that default parameters are used at the next execution
            }
            return false;
        } finally {
            m_settingsBeingDone = false;
        }
    }

    
    public ImageIcon getIcon() {
        return IconManager.getIcon(IconManager.IconType.PENCIL_RULER);
    }
    
}

