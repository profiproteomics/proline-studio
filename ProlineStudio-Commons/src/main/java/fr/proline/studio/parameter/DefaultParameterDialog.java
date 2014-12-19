package fr.proline.studio.parameter;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Window;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class DefaultParameterDialog extends DefaultDialog {
    
    private ParameterList m_parameterList = null;
    
    public DefaultParameterDialog(Window parent, String titleName, ParameterList parameterList) {
        super(parent);

        setTitle(titleName);
        
        m_parameterList = parameterList;
        
        initInternalPanel();
    }
    
    private void initInternalPanel() {
        JPanel p = m_parameterList.getPanel();
        setInternalComponent(p);
    }
    
    @Override
    protected boolean okCalled() {

        // check parameters
        ParameterError error = m_parameterList.checkParameters();

        // report error
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }

        return true;

    }
    
    @Override
    protected boolean cancelCalled() {
        return true;
    }
}
