/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.gui;

import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.ParameterError;
import java.awt.*;
import javax.swing.*;

/**
 * Dialog for a question yes/no or to get a text input
 * 
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