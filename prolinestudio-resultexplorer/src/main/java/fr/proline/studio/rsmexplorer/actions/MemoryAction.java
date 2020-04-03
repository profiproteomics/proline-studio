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


import fr.proline.studio.rsmexplorer.gui.dialog.MemoryDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "Window", id = "fr.proline.studio.rsmexplorer.actions.MemoryAction")
@ActionRegistration(displayName = "#CTL_MemoryAction")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 51)
})
@Messages("CTL_MemoryAction=Memory Usage")
public final class MemoryAction extends AbstractAction implements ActionListener {

    public MemoryAction() {
        putValue(Action.NAME, NbBundle.getMessage(MemoryAction.class, "CTL_MemoryAction"));
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        Frame f = WindowManager.getDefault().getMainWindow();
        MemoryDialog dialog = MemoryDialog.getDialog(f);
        if (dialog.isVisible()) {
            return;
        }

        dialog.setLocationRelativeTo(f);
        dialog.setVisible(true);
   
    }

    
    @Override
     public boolean isEnabled() {
        return true;
    }
     

}
