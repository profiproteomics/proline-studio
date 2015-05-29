package fr.proline.studio.rsmexplorer.gui.calc.functions;

import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.parameters.FunctionParametersDialog;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public abstract class AbstractFunction {
    
    //protected GraphNode.NodeState m_state = GraphNode.NodeState.UNSET;
    protected GlobalTableModelInterface m_globalTableModelInterface;
    
    protected ParameterList[] m_parameters = null;
    
    public abstract String getName();
    public abstract int getNumberOfInParameters();
    
    public abstract void process(AbstractGraphObject[] graphObjects, boolean display);
    
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return m_globalTableModelInterface;
    }

    //public abstract GraphNode.NodeState getState();

    public abstract void generateDefaultParameters(AbstractGraphObject[] graphObjects);
    public abstract ParameterError checkParameters();
    public abstract void userParametersChanged();
    public abstract AbstractFunction cloneFunction();
    
    
    /*public void resetState() {
        m_parameters = null;
        m_globalTableModelInterface = null;
    }*/

        
    protected void display(String name) {
        WindowBox windowBox = WindowBoxFactory.getModelWindowBox(name);
        windowBox.setEntryData(-1, m_globalTableModelInterface);
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(windowBox);
        win.open();
        win.requestActive();
    }
    
    public abstract boolean calculationDone();
    public abstract boolean settingsDone();
    
    public void settings(AbstractGraphObject[] graphObjects) {
        if (m_parameters == null) {
            generateDefaultParameters(graphObjects);
        }
        if (m_parameters == null) {
            return;
        }

        FunctionParametersDialog dialog = new FunctionParametersDialog(getName(), WindowManager.getDefault().getMainWindow(), m_parameters, this);
        dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == FunctionParametersDialog.BUTTON_OK) {
            userParametersChanged();
        }
    }

    
    public ImageIcon getIcon() {
        return IconManager.getIcon(IconManager.IconType.FUNCTION);
    }
    
}
