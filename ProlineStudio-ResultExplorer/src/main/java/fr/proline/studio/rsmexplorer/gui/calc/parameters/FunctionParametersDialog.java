package fr.proline.studio.rsmexplorer.gui.calc.parameters;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import java.awt.Dialog;
import java.awt.Window;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class FunctionParametersDialog extends DefaultDialog {
    
    private final ParameterList[] m_parameterList;
    private final AbstractFunction m_function;
    
    public FunctionParametersDialog(Window parent, ParameterList[] parameterList, AbstractFunction function) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_parameterList = parameterList;
        m_function = function;
        
        setTitle("Function Parameters");

        setButtonVisible(BUTTON_HELP, false);

        setInternalComponent(createInternalPanel());

    }
    
    public final JPanel createInternalPanel() {
        ParameterList parameterList = m_parameterList[0];
        return parameterList.getPanel();
    }
    
    @Override
    protected boolean okCalled() {
        
        ParameterError error = m_parameterList[0].checkParameters();
        // report error
        if (error == null) {
            error = m_function.checkParameters();
        }

        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        
        

        return true;
    }
    
    
}
