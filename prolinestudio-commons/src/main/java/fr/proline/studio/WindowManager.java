package fr.proline.studio;

import javax.swing.*;
import java.awt.event.ActionListener;

public class WindowManager {

    private static WindowManager m_singleton = null;

    private JFrame m_frame;

    private WindowManager() {

    }

    public static WindowManager getDefault() {
        if (m_singleton == null) {
            m_singleton = new WindowManager();
        }
        return m_singleton;
    }

    public JFrame getMainWindow() {
        return m_frame;
    }

    public void setMainWindow(JFrame frame) {
        m_frame = frame;
    }

    //JPM.DOCK
    public void notify(String titleMessage, ImageIcon exceptionIcon, String message, ActionListener a) {

    }
}
