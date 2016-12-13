package fr.proline.studio.filter;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import org.openide.util.Exceptions;

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

    public FilterDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Filters");

        try {
            setHelpURL(new File(".").getCanonicalPath() + File.separatorChar + "Documentation" + File.separatorChar + "Proline_UserGuide_1.4RC1.docx.html#id.2u6wntf");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        setInternalComponent(createFilterPanel());

    }

    public void setFilers(Filter[] filters) {
        m_filters = filters;

        int nb = m_filters.length;
        for (int i = 0; i < nb; i++) {
            Filter f = m_filters[i];
            f.setDefined(f.isUsed());
        }

        initPrefilterSelectedPanel();
    }

    public Filter[] getFilters() {
        return m_filters;
    }

    private JPanel createFilterPanel() {

        m_filterPanel = new FilterPanel(this);

        return m_filterPanel;

    }

    public void initPrefilterSelectedPanel() {

        m_filterPanel.setFilers(m_filters);

        repack();

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
