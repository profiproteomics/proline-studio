package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.WindowManager;
import fr.proline.studio.rsmexplorer.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExitAction  extends AbstractAction implements ActionListener {

    public ExitAction() {
        putValue(Action.NAME, "Exit");
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        MainFrame f = (MainFrame) WindowManager.getDefault().getMainWindow();
        f.exit();

    }


    @Override
    public boolean isEnabled() {

        return true;
    }


}
