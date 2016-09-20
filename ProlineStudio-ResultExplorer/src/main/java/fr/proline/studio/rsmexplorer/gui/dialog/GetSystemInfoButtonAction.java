/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.openide.windows.WindowManager;

/**
 *
 * @author VD225637
 */
public class GetSystemInfoButtonAction extends JButton implements ActionListener {

    public GetSystemInfoButtonAction() {

        setIcon(IconManager.getIcon(IconManager.IconType.INFORMATION));
        setToolTipText("Get System information");

        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SystemInfoDialog dialog = SystemInfoDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.updateInfo();
        dialog.setVisible(true);
    }
}
