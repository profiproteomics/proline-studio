package fr.proline.studio.pattern;


import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;

/**
 *
 * @author JM235353
 */
public class DataMixerWindowBoxManager {
    
    private static WindowBox m_windowBox = null;
    private static DataBoxViewerTopComponent m_win = null;
    
    
    public static void addTableInfo(TableInfo tableInfo) {
        

        if (m_windowBox == null) {
            m_windowBox = WindowBoxFactory.getDataMixerWindowBoxV2();
            m_windowBox.setEntryData(-1, tableInfo);
            
            // open a window to display the window box
            m_win = new DataBoxViewerTopComponent(m_windowBox);
            m_win.open();
            m_win.requestActive();
        } else {
            m_windowBox.setEntryData(-1, tableInfo);
            if (m_win.isOpened()) {
                m_win.requestActive();
            } else {
                m_win = new DataBoxViewerTopComponent(m_windowBox);
                m_win.open();
                m_win.requestActive();
            } 
        }
    }
    
}
