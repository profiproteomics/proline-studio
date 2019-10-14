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
package fr.proline.studio.rsmexplorer.gui.spectrum;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import fr.proline.studio.utils.IconManager;
import javax.swing.JButton;

/**
 * Button to toggle display of intensity/masses from the fragmentation table
 *
 * @author AW
 */
public class HideFragmentsTableIntensityButton extends JButton implements ActionListener {

    private static final long serialVersionUID = 1L;
    private RsetPeptideFragmentationTable m_table = null;
    private boolean m_isPressed = false;

    public HideFragmentsTableIntensityButton(RsetPeptideFragmentationTable table, boolean visible) {

        m_table = table;
        setIcon(IconManager.getIcon(IconManager.IconType.ADD_REMOVE));
        setToolTipText("Show/hide matching fragments intensity...");
        m_isPressed = visible;
        this.setSelected(m_isPressed);
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        m_isPressed = !m_isPressed;
        this.setSelected(m_isPressed);
        m_table.updateFragmentsIntensityVisibility(m_isPressed);
    }

}
