package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.id.ProjectId;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.CheckParameterInterface;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.FunctionParametersDialog;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
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
    private String m_errorMessage = null;
    
    protected GraphPanel m_panel;
    
    public AbstractFunction(GraphPanel panel) {
        m_panel = panel;
    }

    public void inLinkDeleted() {
        m_parameters = null;
        m_globalTableModelInterface = null;
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
    
    public abstract String getName();
    public abstract int getNumberOfInParameters();
    
    public abstract void process(AbstractGraphObject[] graphObjects, FunctionGraphNode functionGraphNode, boolean display);
    
    
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return m_globalTableModelInterface;
    }


    public abstract void generateDefaultParameters(AbstractGraphObject[] graphObjects);
    
    public abstract void userParametersChanged();
    public abstract AbstractFunction cloneFunction(GraphPanel p);
    

        
    protected void display(String dataName, String functionName) {
        WindowBox windowBox = WindowBoxFactory.getModelWindowBox(dataName, functionName);
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
    
    public boolean settings(AbstractGraphObject[] graphObjects) {
        if (m_parameters == null) {
            generateDefaultParameters(graphObjects);
        }
        if (m_parameters == null) {
            return false;
        }

        FunctionParametersDialog dialog = new FunctionParametersDialog(getName(), WindowManager.getDefault().getMainWindow(), m_parameters, this);
        dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == FunctionParametersDialog.BUTTON_OK) {
            userParametersChanged();
            return true;
        }
        return false;
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
