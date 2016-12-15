package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.utils.MiscellaneousUtils;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
            try {
                Desktop.getDesktop().browse(MiscellaneousUtils.createRedirectTempFile(new File(".").getCanonicalPath()+File.separatorChar+"Documentation"+File.separatorChar+"Proline_UserGuide_1.4RC1.docx.html#id.qsh70q"));
            } catch (Exception ex) {
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(getClass().getSimpleName() + " failed", ex);
            }
        }

    }
}
