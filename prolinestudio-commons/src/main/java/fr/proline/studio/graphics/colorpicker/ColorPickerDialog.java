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
package fr.proline.studio.graphics.colorpicker;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Dialog to select a color
 * 
 * @author jm235353
 */
public class ColorPickerDialog extends DefaultDialog {

    private ColorPickerPanel m_colorPickerPanel = null;
    
    public ColorPickerDialog(Window parent, Color defaultColor) {
        super(parent);

        setTitle("Color Chooser");
        
        // hide help button and status bar
        setButtonVisible(BUTTON_HELP, false);
        setStatusVisible(false);
    
        JPanel internalPanel = new JPanel(new GridBagLayout());
        internalPanel.setBorder(BorderFactory.createTitledBorder(""));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        m_colorPickerPanel = new ColorPickerPanel(CyclicColorPalette.getPalette());
        m_colorPickerPanel.setColor(defaultColor);
        internalPanel.add(m_colorPickerPanel, c);
        
        setInternalComponent(internalPanel);
    }
    
    public Color getColor() {
        return m_colorPickerPanel.getColor();
    }

}
