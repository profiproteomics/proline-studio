package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.id.ProjectId;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.CheckParameterInterface;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.FunctionParametersDialog;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import javax.swing.ImageIcon;
import org.openide.windows.WindowManager;

/**
 * Abstract Parent Function for all functions of the data analyzer
 * @author JM235353
 */
public abstract class AbstractFunction implements CheckParameterInterface {

    protected GlobalTableModelInterface m_globalTableModelInterface;
    
    protected ParameterList[] m_parameters = null;
    
    private boolean m_calculating = false;
    private boolean m_inError = false;
    protected boolean m_settingsBeingDone = false;
    private String m_errorMessage = null;
    
    protected GraphPanel m_panel;
    
    public AbstractFunction(GraphPanel panel) {
        m_panel = panel;
    }

    public void inLinkDeleted() {
        m_parameters = null;
        m_globalTableModelInterface = null;
        
        //inLinkDeletedSubClass();
    }
    
    //public abstract void inLinkDeletedSubClass();
    
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
    
    public abstract String getName();
    public abstract int getNumberOfInParameters();
    
    public abstract void process(AbstractConnectedGraphObject[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback);
    
    public abstract void askDisplay(FunctionGraphNode functionGraphNode);
    
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return m_globalTableModelInterface;
    }


    public abstract void generateDefaultParameters(AbstractConnectedGraphObject[] graphObjects);
    
    public abstract void userParametersChanged();
    public abstract AbstractFunction cloneFunction(GraphPanel p);
    

        
    protected void display(String dataName, String functionName) {
        WindowBox windowBox = WindowBoxFactory.getGenericWindowBox(dataName, functionName, IconManager.IconType.CHALKBOARD, false);
        ProjectId projectId = (ProjectId) m_globalTableModelInterface.getSingleValue(ProjectId.class);
        long id = (projectId!=null) ? projectId.getId() : -1l;
        windowBox.setEntryData(id, m_globalTableModelInterface);
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(windowBox);
        win.open();
        win.requestActive();
    }
    
    public abstract boolean calculationDone();
    public abstract boolean settingsDone();
    public boolean isCalculating() {
        return m_calculating;
    }
    public boolean inError() {
        return m_inError;
    }
    public void resetError() {
        m_inError = false;
        m_errorMessage = null;
    }
    public boolean isSettingsBeingDone() {
        return m_settingsBeingDone;
    }
    
    public boolean settings(AbstractConnectedGraphObject[] graphObjects, GraphNode node) {
        if (m_parameters == null) {
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
            return false;
        } finally {
            m_settingsBeingDone = false;
        }
    }

    
    public ImageIcon getIcon() {
        return IconManager.getIcon(IconManager.IconType.FUNCTION);
    }
    
    public Color getFrameColor() {
        return null;
    }
    
    public String getDataName() {
        return null;
    }
    
    
}
