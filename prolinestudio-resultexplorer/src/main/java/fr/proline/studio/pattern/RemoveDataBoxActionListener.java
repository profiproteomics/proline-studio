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
package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action to remove the last DataBox
 * @author JM235353
 */
public class RemoveDataBoxActionListener implements ActionListener {

    private final SplittedPanelContainer m_splittedPanel;
    private final AbstractDataBox m_databox;

    public RemoveDataBoxActionListener(SplittedPanelContainer splittedPanel, AbstractDataBox databox) {
        m_splittedPanel = splittedPanel;
        m_databox = databox;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (m_databox.m_previousDataBox != null) {
            m_databox.m_previousDataBox.removeNextDataBox(m_databox);
        }
        m_splittedPanel.removeLastPanel();


    }
}