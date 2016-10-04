package fr.proline.studio.gui;

import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.ParameterError;
import java.awt.*;
import javax.swing.*;

/**
 * Dialog for a question yes/no or to get a text input
 * @author jm235353
 */
public class ParameterDialog extends DefaultDialog {
    
    private AbstractParameter m_parameter;
    private Object m_value;

    public ParameterDialog(Window parent, String title, AbstractParameter parameter) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
      
        m_parameter = parameter;        
        
        setStatusVisible(false);
        setTitle(title);

        setButtonVisible(BUTTON_HELP, false);

        setInternalComponent(this.initInternalPanel());

    }

    private JPanel initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new FlowLayout());
        
        internalPanel.add(m_parameter.getComponent());
        m_parameter.getComponent().setToolTipText("Selection only affects current renaming.");
        
        internalPanel.setPreferredSize(new Dimension(240, 80));
        
        internalPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createTitledBorder("According to")));

        return internalPanel;
    }
    
    
    @Override
    protected boolean okCalled() {
        
        ParameterError error = m_parameter.checkParameter();
        
        if(error!=null){
            setStatus(true, "Parameter Error Encountered");
            highlight(m_parameter.getComponent());
            return false;
        }else{
            this.m_value = m_parameter.getObjectValue();
        }
    
        return true;
    }

    public Object getParameterValue(){
        return this.m_value;
    }
    
    

}