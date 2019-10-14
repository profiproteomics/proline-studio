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
import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Used to link parameters between them.
 * For instance to display a parameter when another parameter is selected
 * @author jm235353
 */
public abstract class AbstractLinkedParameters {

    private ParameterList m_parameterList = null;

    public AbstractLinkedParameters(ParameterList parameterList) {
        m_parameterList = parameterList;
    }

    public ParameterList getParameterList() {
        return m_parameterList;
    }

    public abstract void valueChanged(String value, Object associatedValue);

    public void showParameter(AbstractParameter parameter, boolean show) {
        JComponent comp = parameter.getComponent(null);
        comp.setVisible(show);
        JLabel l = getParameterList().getAssociatedLabel(comp);
        if (l != null) {
            l.setVisible(show);
        }
        
        m_parameterList.displayModified();
    }

    public void showParameter(AbstractParameter parameter, boolean show, Object value) {
        JComponent comp = parameter.getComponent(value);
        comp.setVisible(show);
        JLabel l = getParameterList().getAssociatedLabel(comp);
        if (l != null) {
            l.setVisible(show);
        }
    }

    public void enableList(boolean v) {
        m_parameterList.enableList(v);
    }

    protected void updateParameterListPanel() {
        JPanel p = getParameterList().getPanel();

        JDialog dialog = getParent(p);
        if (dialog != null) {
            if (dialog instanceof DefaultDialog) {
                ((DefaultDialog) dialog).repack();
            } else {
                dialog.pack();
            }
        } else if (p != null) {
            Container c = p.getParent();
            if (c != null) {
                c.revalidate();
            }
        }
    }

    private JDialog getParent(JComponent c) {
        Container container = c.getParent();
        while (true) {
            if (container == null) {
                return null;
            }
            if (container instanceof JDialog) {
                return (JDialog) container;
            }
            container = container.getParent();
        }
    }

}
