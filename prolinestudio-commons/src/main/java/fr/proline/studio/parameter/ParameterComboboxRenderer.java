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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Renderer for Paramters displayed in a combobox
 *
 * @author JM235353
 * @param <E>
 */
public class ParameterComboboxRenderer<E> extends DefaultListCellRenderer {

    private AbstractParameterToString<E> m_paramToString = null;

    public ParameterComboboxRenderer(AbstractParameterToString<E> paramToString) {
        m_paramToString = paramToString;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value == null) {
            l.setText("< Select >");
            return l;
        }

        String display;
        if (m_paramToString != null) {
            display = m_paramToString.toString((E) value);
        } else {
            display = value.toString();
        }
        l.setText(display);

        return l;
    }
}
