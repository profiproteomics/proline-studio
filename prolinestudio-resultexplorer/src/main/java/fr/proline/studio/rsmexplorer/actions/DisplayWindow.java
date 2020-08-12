package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.WindowManager;
import fr.proline.studio.rsmexplorer.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DisplayWindow extends AbstractAction implements ActionListener {

    private String m_windowKey = null;

    public DisplayWindow(String actionName, String windowKey) {
        m_windowKey = windowKey;
        putValue(Action.NAME, actionName);
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        MainFrame f = (MainFrame) WindowManager.getDefault().getMainWindow();
        f.displayWindow(m_windowKey);

    }


    @Override
    public boolean isEnabled() {

        return true;
    }


}
