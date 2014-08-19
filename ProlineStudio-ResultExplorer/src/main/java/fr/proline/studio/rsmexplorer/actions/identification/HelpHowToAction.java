package fr.proline.studio.rsmexplorer.actions.identification;

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
id = "fr.proline.studio.rsmexplorer.actions.HelpHowToAction")
@ActionRegistration(displayName = "#CTL_HelpHowToAction")
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 125)
})
@Messages("CTL_HelpHowToAction=How to")
public final class HelpHowToAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {


        if (Desktop.isDesktopSupported()) { // JDK 1.6.0

            String url = "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:start";

            try {
                Desktop.getDesktop().browse(new URL(url).toURI());
            } catch (Exception ex) {
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(getClass().getSimpleName() + " failed", ex);
            }
        }

    }
}
