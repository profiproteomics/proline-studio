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
package fr.proline.studio.parameter;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Dialog to display parameter from a list of ParameterList.
 * A ParameterList generates a panel
 * A list of ParameterList generates a TabbedPane with panels
 * @author JM235353
 */
public class DefaultParameterDialog extends DefaultDialog {
    
    private ArrayList<ParameterList> m_parameterListArray = null;
    private JTabbedPane m_tabbedPane = null;
    
    public DefaultParameterDialog(Window parent, String titleName, ArrayList<ParameterList> parameterListArray) {
        super(parent);

        setTitle(titleName);
        
        m_parameterListArray = parameterListArray;
        
        initInternalPanel();
    }
    
    private void initInternalPanel() {
        
        if (m_parameterListArray.isEmpty()) {
            // should not happen 
            return;
        }
        
        if (m_parameterListArray.size() == 1) {
            JPanel p = m_parameterListArray.get(0).getPanel();
            setInternalComponent(p);
        } else {
            m_tabbedPane = new JTabbedPane(); 
            for (int i=0;i<m_parameterListArray.size();i++) {
                ParameterList parameter = m_parameterListArray.get(i);
                m_tabbedPane.addTab(parameter.toString(), null, parameter.getPanel(), null);
            }
            setInternalComponent(m_tabbedPane);
        }

    }
    
    @Override
    protected boolean okCalled() {

        // check parameters
        ParameterError error = null;
        int indexError = 0;
        for (int i=0;i<m_parameterListArray.size();i++) {
            ParameterList parameter = m_parameterListArray.get(i);
            error = parameter.checkParameters();
            if (error != null) {
                indexError = i;
                break;
            }
        }

        // report error
        if (error != null) {
            if (m_tabbedPane != null) {
                m_tabbedPane.setSelectedIndex(indexError);
            }
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
