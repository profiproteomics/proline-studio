/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.studio.filter;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JButton;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Button to access to filter dialog from a toolbar
 * @author JM235353
 */
public abstract class FilterButton extends JButton implements ActionListener {
     protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.Commons");
    private FilterTableModelInterface m_tableModelFilterInterface;
    
    public FilterButton(FilterTableModelInterface tableModelFilterInterface) {
        setModelFilterInterface(tableModelFilterInterface);
        
        setIcon(IconManager.getIcon(IconManager.IconType.FUNNEL ));
        setFocusPainted(false);
        setToolTipText("Filter...");

        addActionListener(this);
    }

    public final void setModelFilterInterface(FilterTableModelInterface tableModelFilterInterface) {
        m_tableModelFilterInterface = tableModelFilterInterface;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (!m_tableModelFilterInterface.isLoaded()) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_tableModelFilterInterface, "Data loading", "Filtering is not available while data is loading. Please Wait.");
            dialog.setLocation( getLocationOnScreen().x + getWidth() + 5,  getLocationOnScreen().y + getHeight() + 5);
            dialog.setVisible(true);
            
            if (!dialog.isWaitingFinished()) {
                return;
            }
        }
        
        FilterDialog dialog = FilterDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation( getLocationOnScreen().x + getWidth() + 5,  getLocationOnScreen().y + getHeight() + 5);
        LinkedHashMap<Integer, Filter> filtersMap = m_tableModelFilterInterface.getFilters();
        Filter[] filters = new Filter[filtersMap.size()];
        int index = 0;
        for (Map.Entry<Integer, Filter> entry : filtersMap.entrySet()) {
           filters[index++] = entry.getValue();
        }
        
        
        dialog.setFilters(filters);
        dialog.setVisible(true);

        if (dialog.getButtonClicked() == FilterDialog.BUTTON_OK) {

            boolean filterIsUsed = false;
            int nbFilter = filters.length;
            for (int i = 0; i < nbFilter; i++) {
                if (filters[i].isUsed()) {
                    filterIsUsed = true;
                    break;
                }
            }
            Icon funnelIcon = filterIsUsed ? IconManager.getIcon(IconManager.IconType.FUNNEL_ACTIVATED) : IconManager.getIcon(IconManager.IconType.FUNNEL);
            setIcon(funnelIcon);


            m_tableModelFilterInterface.filter();
            
            filteringDone();
        }
    }
    
    protected abstract void filteringDone();
}
