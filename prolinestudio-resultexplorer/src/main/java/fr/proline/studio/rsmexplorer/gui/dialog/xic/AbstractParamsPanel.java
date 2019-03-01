/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.parameter.ParameterList;
import java.util.Map;
import javax.swing.JPanel;

/**
 *
 * @author CB205360
 */
public abstract class AbstractParamsPanel extends JPanel {
    
    protected ParameterList m_parameterList;
    
    public AbstractParamsPanel() {
    
    }

    public abstract Map<String, Object> getQuantParams();

    public abstract void setQuantParams(Map<String, Object> quantParams);

    public ParameterList getParameterList() {
        return m_parameterList;
    }
    
}
