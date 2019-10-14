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
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.utils.HelpUtils;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.slf4j.LoggerFactory;

@ActionID(category = "Help", id = "fr.proline.studio.rsmexplorer.actions.HelpHowToAction")
@ActionRegistration(displayName = "#CTL_HelpHowToAction")
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 125)
})
@Messages("CTL_HelpHowToAction=How to")
public final class HelpHowToAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {


        if (Desktop.isDesktopSupported()) { // JDK 1.6.0
            try {
                Desktop.getDesktop().browse(HelpUtils.createRedirectTempFile("id.qsh70q"));
            } catch (Exception ex) {
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(getClass().getSimpleName() + " failed", ex);
            }
        }

    }
}
