package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rserver.dialog.ImageViewerTopComponent;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.CheckParameterInterface;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.FunctionParametersDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Image;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.ImageIcon;
import org.openide.windows.WindowManager;

/**
 * Base class for the definition of a Graphic (scatter, histogram...) with its parameters
 * in the data mixer
 * @author JM235353
 */
public abstract class AbstractGraphic implements CheckParameterInterface {

    protected LockedDataGraphicsModel m_graphicsModelInterface;
    protected Image m_generatedImage;
    
    protected ParameterList[] m_parameters = null;

    private boolean m_calculating = false;
    private boolean m_inError = false;
    private String m_errorMessage = null;
    
    protected GraphPanel m_panel;
    
    public AbstractGraphic(GraphPanel panel) {
        m_panel = panel;
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

    public abstract void process(AbstractGraphObject[] graphObjects, GraphicGraphNode graphicGraphNode, boolean display);
    public abstract void generateDefaultParameters(AbstractGraphObject[] graphObjects);
    public abstract void userParametersChanged();
    public abstract AbstractGraphic cloneGraphic(GraphPanel p);
    

        
    protected void display(String dataName, String functionName) {
        if (m_generatedImage != null) {
            ImageViewerTopComponent win = new ImageViewerTopComponent(dataName + " " + functionName, m_generatedImage);
            win.open();
            win.requestActive();
        } else {
            WindowBox windowBox = WindowBoxFactory.getGraphicsWindowBox(dataName, m_graphicsModelInterface);
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(windowBox);
            win.open();
            win.requestActive();
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
        return IconManager.getIcon(IconManager.IconType.WAVE);
    }
    
}

