/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.search;

import fr.proline.studio.WindowManager;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterMapInterface;
import fr.proline.studio.filter.FilterTableModelInterface;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

import org.jdesktop.swingx.JXTable;

/**
 * Button to add to start a search.
 * This button use 
 * @author JM235353
 */
public class SearchToggleButton extends JToggleButton {

    private ProgressInterface m_progressInterface = null;

    // for search on table
    private JXTable m_table = null;
    private FilterTableModelInterface m_tableModelFilterInterface = null;

    // for generic search
    SearchInterface m_searchInterface = null;
    FilterMapInterface m_filterMapInterface = null;

    private AdvancedSearchFloatingPanel m_searchPanel = null;

    /**
     * Constructor for search on JXTable
     * @param progressInterface
     * @param table
     * @param tableModelFilterInterface
     */
    public SearchToggleButton(ProgressInterface progressInterface, JXTable table, FilterTableModelInterface tableModelFilterInterface) {
        init(progressInterface, table, tableModelFilterInterface);
        initGraphic(new Search());
    }

    /**
     * Constructor for generic search
     *
     * @param applySearchInterface
     * @param progressInterface
     * @param searchInterface
     * @param filterMapInterface
     */
    public SearchToggleButton(ApplySearchInterface applySearchInterface, ProgressInterface progressInterface, SearchInterface searchInterface, FilterMapInterface filterMapInterface) {
        
        m_progressInterface = progressInterface;
        m_searchInterface = searchInterface;
        m_filterMapInterface = filterMapInterface;

        initGraphic(applySearchInterface);

    }

    public final void init(ProgressInterface progressInterface, JXTable table, FilterTableModelInterface tableModelFilterInterface) {
        m_progressInterface = progressInterface;
        m_table = table;
        m_tableModelFilterInterface = tableModelFilterInterface;

    }

    private void initGraphic(ApplySearchInterface search) {
        
        m_searchPanel = new AdvancedSearchFloatingPanel(search);
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
                        setSelected(false);
                        return;
                    }
                }

                if (isSelected() && (!m_searchPanel.hasFilters())) {

                    // we must clone filters because we can not use the same that can be used for filtering
                    LinkedHashMap<Integer, Filter> filtersMap = (m_filterMapInterface!=null) ? m_filterMapInterface.getFilters() : m_tableModelFilterInterface.getFilters();
                    Filter[] filters = new Filter[filtersMap.size()];
                    int index = 0;
                    for (Map.Entry<Integer, Filter> entry : filtersMap.entrySet()) {
                        filters[index++] = entry.getValue().cloneFilter4Search();
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

    
    private class Search implements ApplySearchInterface {


        @Override
        public void doSearch(Filter f, boolean firstSearch) {
            int modelRowIndex = m_tableModelFilterInterface.search(m_table, f, firstSearch);
            if (modelRowIndex == -1) {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "No result found", "Search", JOptionPane.INFORMATION_MESSAGE );
                return;
            }
            int rowView = m_table.convertRowIndexToView(modelRowIndex);
            m_table.setRowSelectionInterval(rowView, rowView);
            m_table.scrollRowToVisible(rowView);
        }

    }

    
}

