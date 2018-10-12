package fr.proline.studio.filter;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;
import javax.swing.*;

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
            FilterStatus status = f.checkValues();
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
