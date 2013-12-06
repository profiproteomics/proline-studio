/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.slf4j.LoggerFactory;

@ActionID(category = "Help",
id = "fr.proline.studio.rsmexplorer.actions.HelpProlineAction")
@ActionRegistration(displayName = "#CTL_HelpProlineAction")
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 150, separatorAfter = 175)
})
@Messages("CTL_HelpProlineAction=Proline Help")
public final class HelpProlineAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {


        if (Desktop.isDesktopSupported()) { // JDK 1.6.0

            String url = "http://biodev.extra.cea.fr/docs/proline/doku.php";

            try {
                Desktop.getDesktop().browse(new URL(url).toURI());
            } catch (Exception ex) {
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(getClass().getSimpleName() + " failed", ex);
            }
        }


    }
}
