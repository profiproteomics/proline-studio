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

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;

/**
 * Dialog to set the filters
 *
 * @author JM235353
 */
public class FilterDialog extends DefaultDialog {

    private static FilterDialog m_singletonDialog = null;

    private Filter[] m_filters;
    private FilterPanel m_filterPanel;

    public static FilterDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new FilterDialog(parent);
        }
        m_singletonDialog.hideInfoPanel();
        return m_singletonDialog;
    }

    private FilterDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Filters");

        setDocumentationSuffix("id.2u6wntf");
        m_filterPanel = new FilterPanel(this);
        setInternalComponent(m_filterPanel);

    }

    public void setFilters(Filter[] filters) {
        m_filters = filters;

        int nb = m_filters.length;
        for (int i = 0; i < nb; i++) {
            Filter f = m_filters[i];
            f.setDefined(f.isUsed());
        }

        m_filterPanel.setFilters(m_filters);
    }

    public Filter[] getFilters() {
        return m_filters;
    }


    @Override
    protected boolean okCalled() {
        for (int i = 0; i < m_filters.length; i++) {
            Filter f = m_filters[i];
            if (!f.isDefined()) {
                continue;
            }
            FilterStatus status = f.checkValues();//check user input valor before register
            if (status != null) {
                setStatus(true, status.getError());
                highlight(status.getComponent());
                return false;
            }
        }

        for (int i = 0; i < m_filters.length; i++) {
            Filter f = m_filters[i];
            f.registerValues();
            f.clearComponents();
        }

        return true;

    }

    @Override
    protected boolean cancelCalled() {
        for (int i = 0; i < m_filters.length; i++) {
            Filter f = m_filters[i];
            f.clearComponents();
        }

        return true;
    }

}
