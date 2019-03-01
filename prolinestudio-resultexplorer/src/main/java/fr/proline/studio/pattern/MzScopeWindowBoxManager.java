package fr.proline.studio.pattern;

import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;

/**
 *
 * @author MB243701
 */
public class MzScopeWindowBoxManager {
    
    private static WindowBox m_windowBox = null;
    private static DataBoxViewerTopComponent m_win = null;
    
    public static void addMzdbScope(MzScopeInterface mzScopeInterface) {
        if (m_windowBox == null) {
            m_windowBox = WindowBoxFactory.getMzScopeWindowBox();
            m_windowBox.setEntryData(-1, mzScopeInterface);
            
            // open a window to display the window box
            m_win = new DataBoxViewerTopComponent(m_windowBox);
            m_win.open();
            m_win.requestActive();
        } else {
            m_windowBox.setEntryData(-1, mzScopeInterface);
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
