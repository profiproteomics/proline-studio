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

import fr.proline.studio.WindowManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.dialog.DataBoxChooserDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action to add a new Databox at the end of the queue
 * @author JM235353
 */
public class AddDataBoxActionListener implements ActionListener {

    private final SplittedPanelContainer m_splittedPanel;
    private final AbstractDataBox m_previousDatabox;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    public AddDataBoxActionListener(SplittedPanelContainer splittedPanel, AbstractDataBox previousDatabox) {
        m_splittedPanel = splittedPanel;
        m_previousDatabox = previousDatabox;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        DataBoxChooserDialog dialog = new DataBoxChooserDialog(WindowManager.getDefault().getMainWindow(), m_previousDatabox, false, null);
        dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            
            AbstractDataBox genericDatabox = dialog.getSelectedDataBox();
            try {

                genericDatabox = DataboxManager.getDataboxNewInstance(genericDatabox);
            } catch (InstantiationException | IllegalAccessException e) {
                // should never happen
                m_logger.error("Error creating new Databox ",e);
            }

            m_previousDatabox.addNextDataBox(genericDatabox);


            genericDatabox.createPanel();

            // add the new panel to the window (below, as a tabb, or as splitted
            if (dialog.addBelow()) {
                m_splittedPanel.registerAddedPanel((JPanel) genericDatabox.getPanel());
            } else if (dialog.addTabbed()) {
                m_splittedPanel.registerAddedPanelAsTab((JPanel) genericDatabox.getPanel());
            } else {
                // splitted
                m_splittedPanel.registerAddedPanelAsSplitted((JPanel) genericDatabox.getPanel());
            }

            // update display of added databox
            final AbstractDataBox _genericDatabox = genericDatabox;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    _genericDatabox.dataChanged();
                }
            });

        }

    }
}