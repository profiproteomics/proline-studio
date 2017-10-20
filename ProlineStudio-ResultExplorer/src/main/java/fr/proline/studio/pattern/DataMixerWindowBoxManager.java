package fr.proline.studio.pattern;

import fr.proline.studio.id.ProjectId;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.calc.DataAnalyzerPanel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;

/**
 *
 * @author JM235353
 */
public class DataMixerWindowBoxManager {

    private static WindowBox m_windowBox = null;
    private static DataBoxViewerTopComponent m_win = null;

    public static void addTableInfo(TableInfo tableInfo) {

        if (m_windowBox == null) {
            m_windowBox = WindowBoxFactory.getDataAnalyzerWindowBox();
            if (tableInfo != null) {

                GlobalTableModelInterface model = tableInfo.getModel();
                ProjectId projectId = (ProjectId) model.getSingleValue(ProjectId.class);
                long id = (projectId != null) ? projectId.getId() : -1l;

                m_windowBox.setEntryData(id, tableInfo);
            }

            // open a window to display the window box
            m_win = new DataBoxViewerTopComponent(m_windowBox);
            m_win.open();
            m_win.requestActive();
        } else {
            if (m_windowBox.getEntryBox() instanceof DataboxDataAnalyzer) {
                DataboxDataAnalyzer analyzer = (DataboxDataAnalyzer) m_windowBox.getEntryBox();
                if (analyzer.getPanel() instanceof DataAnalyzerPanel) {
                    DataAnalyzerPanel panel = (DataAnalyzerPanel) analyzer.getPanel();
                    panel.resetViewpoint();
                }
            }

            if (tableInfo != null) {

                GlobalTableModelInterface model = tableInfo.getModel();
                ProjectId projectId = (ProjectId) model.getSingleValue(ProjectId.class);
                long id = (projectId != null) ? projectId.getId() : -1l;

                m_windowBox.setEntryData(id, tableInfo);
            }
            if (m_win.isOpened()) {
                m_win.requestActive();
            } else {
                m_win = new DataBoxViewerTopComponent(m_windowBox);
                m_win.open();
                m_win.requestActive();
            }
        }
    }

    public static void openDataMixer() {
        addTableInfo(null);
    }

}
