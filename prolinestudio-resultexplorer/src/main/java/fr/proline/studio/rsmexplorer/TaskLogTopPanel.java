package fr.proline.studio.rsmexplorer;

import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.pattern.WindowBoxFactory;

import javax.swing.*;

public class TaskLogTopPanel extends MultiDataBoxViewerTopPanel {

    private static TaskLogTopPanel m_singleton = null;

    public TaskLogTopPanel() {
        super(WindowBoxFactory.getSystemMonitoringWindowBox(),"Logs");

        setName("Logs");
        setToolTipText("Logs");
    }


    public static TaskLogTopPanel getSingleton() {
        if (m_singleton == null) {
            m_singleton = new TaskLogTopPanel();
        }

        return m_singleton;
    }

    /*
    @Override
    protected void componentClosed() {
    }
    //JPM.DOCK
     */


}
