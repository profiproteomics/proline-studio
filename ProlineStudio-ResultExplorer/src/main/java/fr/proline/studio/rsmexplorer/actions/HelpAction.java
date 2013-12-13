package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.gui.dialog.HelpDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "Help",
id = "fr.proline.studio.rsmexplorer.gui.dialog.HelpAction")
@ActionRegistration(displayName = "#CTL_HelpAction")
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 100)
})
@Messages("CTL_HelpAction=Getting Started")
public final class HelpAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        
        Frame f = WindowManager.getDefault().getMainWindow();
        
        HelpDialog dialog = HelpDialog.getDialog(f);
        if (dialog.isVisible()) {
            return;
        }
        
        dialog.setLocationRelativeTo(f);
        dialog.setVisible(true);
        
        
    }
}
