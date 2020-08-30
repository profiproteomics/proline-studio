package fr.proline.studio;

import fr.proline.studio.dock.AbstractDockFrame;

import javax.swing.*;
import java.awt.event.ActionListener;

public class WindowManager {

    private static WindowManager m_singleton = null;

    private AbstractDockFrame m_frame;

    private WindowManager() {

    }

    public static WindowManager getDefault() {
        if (m_singleton == null) {
            m_singleton = new WindowManager();
        }
        return m_singleton;
    }

    public AbstractDockFrame getMainWindow() {
        return m_frame;
    }

    public void setMainWindow(AbstractDockFrame frame) {
        m_frame = frame;
    }

}
