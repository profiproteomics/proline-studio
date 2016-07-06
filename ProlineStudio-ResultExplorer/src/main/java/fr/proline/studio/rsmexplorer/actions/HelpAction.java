package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.rsmexplorer.gui.dialog.HelpDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ApplicationSettingsDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "Help",
        id = "fr.proline.studio.rsmexplorer.actions.HelpAction")
@ActionRegistration(displayName = "#CTL_HelpAction")
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 100)
})
@Messages("CTL_HelpAction=Getting Started")
public final class HelpAction extends AbstractAction implements /*LookupListener,*/ ContextAwareAction {

    private static HelpAction m_action = null;

    public HelpAction() {
        putValue(Action.NAME, NbBundle.getMessage(HelpAction.class, "CTL_HelpAction"));

        m_action = this;

        setEnabled(true);
    }

    public static HelpAction getAction() {
        m_action.putValue(Action.NAME, "totoche");
        return m_action;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new HelpAction();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Frame f = WindowManager.getDefault().getMainWindow();

        ApplicationSettingsDialog optionsDialog = ApplicationSettingsDialog.getDialog(f);
        if (optionsDialog.isVisible()) {
            return;
        }

        HelpDialog dialog = HelpDialog.getDialog(f);
        if (dialog.isVisible()) {
            return;
        }

        dialog.setLocationRelativeTo(f);
        dialog.setVisible(true);

    }
}
