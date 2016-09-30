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
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        internalPanel.add(m_parameter.getComponent(), c);

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