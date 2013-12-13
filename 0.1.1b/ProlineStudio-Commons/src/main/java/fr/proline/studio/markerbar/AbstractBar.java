package fr.proline.studio.markerbar;

import javax.swing.JPanel;

/**
 * Base class for Marker and overview bar
 * @author JM235353
 */
public class AbstractBar extends JPanel {

    private static final long serialVersionUID = 1L;

    public static enum BarType {

        MARKER_BAR,
        OVERVIEW_BAR
    };
    protected MarkerContainerPanel m_containerPanel = null;

    public AbstractBar(MarkerContainerPanel containerPanel) {
        m_containerPanel = containerPanel;
    }
}
