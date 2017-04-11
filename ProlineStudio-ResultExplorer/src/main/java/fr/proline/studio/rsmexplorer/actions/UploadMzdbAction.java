package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.gui.dialog.UploadMzdbDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "File",
        id = "fr.proline.studio.rsmexplorer.actions.UploadMzdbAction")
@ActionRegistration(displayName = "#CTL_UploadMzdbAction")

/*
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 500)
})
*/

@Messages("CTL_UploadMzdbAction=Upload mzDB File(s)")
public final class UploadMzdbAction extends AbstractAction implements ActionListener, ContextAwareAction {

    private static UploadMzdbAction m_action = null;

    public UploadMzdbAction() {
        putValue(Action.NAME, NbBundle.getMessage(UploadMzdbAction.class, "CTL_UploadMzdbAction"));
        m_action = this;
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Frame f = WindowManager.getDefault().getMainWindow();

        UploadMzdbDialog dialog = UploadMzdbDialog.getDialog(f);
        if (dialog.isVisible()) {
            return;
        }

        dialog.setLocationRelativeTo(f);
        dialog.setVisible(true);

    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new UploadMzdbAction();
    }
}
