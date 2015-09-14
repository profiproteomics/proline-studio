package fr.proline.studio.search;

import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterTableModelInterfaceV2;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JToggleButton;
import org.jdesktop.swingx.JXTable;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class SearchToggleButton2 extends JToggleButton {

    private ProgressInterface m_progressInterface = null;
    private JXTable m_table = null;
    private FilterTableModelInterfaceV2 m_tableModelFilterInterface = null;
    
    private AdvancedSearchFloatingPanel m_searchPanel = null;
    
    public SearchToggleButton2(ProgressInterface progressInterface, JXTable table, FilterTableModelInterfaceV2 tableModelFilterInterface) {
        
        m_progressInterface = progressInterface;
        m_table = table;
        m_tableModelFilterInterface = tableModelFilterInterface;

        m_searchPanel = new AdvancedSearchFloatingPanel(new Search());
        m_searchPanel.setToggleButton(this);
        
        setIcon(IconManager.getIcon(IconManager.IconType.SEARCH));
        setToolTipText("Search");
        setFocusPainted(false);

        addActionListener(new ActionListener() {

            boolean firstTime = true;

            @Override
            public void actionPerformed(ActionEvent e) {

                if ((m_progressInterface != null) && (!m_progressInterface.isLoaded())) {

                    ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_progressInterface, "Data loading", "Search is not available while data is loading. Please Wait.");
                    dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
                    dialog.setVisible(true);

                    if (!dialog.isWaitingFinished()) {
                        return;
                    }
                }
                
                if (isSelected() && (!m_searchPanel.hasFilters())) {
                    
                    // we must clone filters because we can not use the same that can be used for filtering
                    LinkedHashMap<Integer, Filter> filtersMap = m_tableModelFilterInterface.getFilters();
                    Filter[] filters = new Filter[filtersMap.size()];
                    int index = 0;
                    for (Map.Entry<Integer, Filter> entry : filtersMap.entrySet()) {
                        filters[index++] = entry.getValue().cloneFilter();
                    }

                    m_searchPanel.setFilers(filters);
                }
                m_searchPanel.setVisible(isSelected());

                if (firstTime) {
                    firstTime = false;
                    m_searchPanel.setLocation(getX() + getWidth() + 5, getY() + 5);
                }
            }
        });
    }
    
    public AdvancedSearchFloatingPanel getSearchPanel() {
        return m_searchPanel;
    }
    
    
    private class Search extends AbstractSearch2 {

        @Override
        public void doSearch(Filter f, boolean firstSearch) {
            int modelRowIndex = ((CompoundTableModel) m_table.getModel()).search(m_table, f, firstSearch);
            if (modelRowIndex == -1) {
                return;
            }
            int rowView = m_table.convertRowIndexToView(modelRowIndex);
            m_table.setRowSelectionInterval(rowView, rowView);
            m_table.scrollRowToVisible(rowView);
        }

    }
    
}

