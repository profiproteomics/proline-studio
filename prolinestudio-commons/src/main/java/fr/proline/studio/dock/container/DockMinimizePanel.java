package fr.proline.studio.dock.container;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class DockMinimizePanel extends JPanel {

    HashMap<DockContainer, JLabel> m_componentMap = new HashMap<>();

    public DockMinimizePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setBackground(Color.white);

        setVisible(false);
    }

    public void add(DockContainer component) {

        JLabel l = new JLabel(component.getTitle());

        add(l);


        m_componentMap.put(component, l);

        if (!isVisible()) {
            setVisible(true);
        }

        revalidate();
        repaint();
    }

    public void remove(DockContainer component) {

        JLabel l = m_componentMap.remove(component);

        remove(l);

        if (m_componentMap.isEmpty()) {
            setVisible(false);
        }
    }


}
