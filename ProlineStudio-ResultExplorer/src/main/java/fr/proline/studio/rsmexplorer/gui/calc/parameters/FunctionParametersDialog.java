package fr.proline.studio.rsmexplorer.gui.calc.parameters;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class FunctionParametersDialog extends DefaultDialog {
    
    private final ParameterList[] m_parameterList;
    private final AbstractFunction m_function;
    
    public FunctionParametersDialog(String name, Window parent, ParameterList[] parameterList, AbstractFunction function) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_parameterList = parameterList;
        m_function = function;
        
        setTitle(name+" Settings");

        setButtonVisible(BUTTON_HELP, false);

        setInternalComponent(createInternalPanel());

    }
    
    public final JPanel createInternalPanel() {
        
        JPanel containerPanel = new JPanel(new GridBagLayout());
        
        JPanel framedPanel = new JPanel(new GridBagLayout());
        framedPanel.setBorder(BorderFactory.createTitledBorder(""));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        ParameterList parameterList = m_parameterList[0];
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        framedPanel.add(parameterList.getPanel(), c);
        containerPanel.add(framedPanel, c);
        
        
        return containerPanel;
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
