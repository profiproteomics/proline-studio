package fr.proline.studio.markerbar;

import javax.swing.JPanel;

public class AbstractBar extends JPanel {

    private static final long serialVersionUID = 1L;

    public static enum BarType {

        MARKER_BAR,
        OVERVIEW_BAR
    };
    protected MarkerContainerPanel containerPanel = null;

    public AbstractBar(MarkerContainerPanel containerPanel) {
        this.containerPanel = containerPanel;
    }
}
