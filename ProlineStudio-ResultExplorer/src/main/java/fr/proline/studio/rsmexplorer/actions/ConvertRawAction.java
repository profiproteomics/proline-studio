package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.gui.dialog.ConvertRawDialog;
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
        id = "fr.proline.studio.rsmexplorer.actions.ConvertRawAction")
@ActionRegistration(displayName = "#CTL_ConvertRawAction")
/*
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 600)
})
*/
@Messages("CTL_ConvertRawAction=Convert RAW File(s)")
public final class ConvertRawAction extends AbstractAction implements ActionListener, ContextAwareAction {

    private static ConvertRawAction m_action = null;

    public ConvertRawAction() {
        putValue(Action.NAME, NbBundle.getMessage(ConvertRawAction.class, "CTL_ConvertRawAction"));
        m_action = this;
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Frame f = WindowManager.getDefault().getMainWindow();

        ConvertRawDialog dialog = ConvertRawDialog.getDialog(f);
        if (dialog.isVisible()) {
            return;
        }

        dialog.setLocationRelativeTo(f);
        dialog.setVisible(true);

    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new ConvertRawAction();
    }
}
