package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.WindowManager;
import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.rsmexplorer.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DisplayWindow extends AbstractAction implements ActionListener {

    private AbstractTopPanel m_topPanel = null;

    public DisplayWindow(String actionName, AbstractTopPanel topPanel) {
        m_topPanel = topPanel;
        putValue(Action.NAME, actionName);
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        MainFrame f = (MainFrame) WindowManager.getDefault().getMainWindow();
        f.displayWindow(m_topPanel);

    }


    @Override
    public boolean isEnabled() {

        return true;
    }


}
