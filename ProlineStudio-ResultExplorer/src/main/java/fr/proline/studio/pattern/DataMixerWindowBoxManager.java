package fr.proline.studio.pattern;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareTableModel;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;

/**
 *
 * @author JM235353
 */
public class DataMixerWindowBoxManager {
    
    private static WindowBox m_windowBox = null;

    
    
    public static void addCompareTableModel(CompareDataInterface model) {
        if (m_windowBox == null) {
            m_windowBox = WindowBoxFactory.getDataMixerWindowBox();
            m_windowBox.setEntryData(-1, model);
            
            // open a window to display the window box
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(m_windowBox);
            win.open();
            win.requestActive();
        } else {
            m_windowBox.setEntryData(-1, model);
            m_windowBox = null;
        }
    }
    
}
