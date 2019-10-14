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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;

/**
 * Dialog to add an aggregate
 *
 * @author JM235353
 */
public class AddAggregateDialog extends DefaultDialog {

    private static AddAggregateDialog m_singletonDialog = null;

    private AddAggregatePanel m_aggregatePanel = null;

    public static AddAggregateDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AddAggregateDialog(parent);
        }

        m_singletonDialog.m_aggregatePanel.reinitialize();

        return m_singletonDialog;
    }

    private AddAggregateDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Add Dataset");

        setDocumentationSuffix("id.2p2csry");

        setButtonVisible(BUTTON_DEFAULT, true);

        m_aggregatePanel = new AddAggregatePanel();

        setInternalComponent(m_aggregatePanel);
    }

    @Override
    protected boolean okCalled() {

        String name = m_aggregatePanel.getAggregateName();

        if (name.isEmpty()) {
            setStatus(true, "You must fill the dataset name.");
            highlight(m_aggregatePanel.getNameTextfield());
            return false;
        }

        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    protected boolean defaultCalled() {
        m_aggregatePanel.initDefaults();

        return false;
    }

    public String getAggregateName() {
        return m_aggregatePanel.getAggregateName();
    }

    public int getNbAggregates() {
        return m_aggregatePanel.getNbAggregates();
    }

    public Aggregation.ChildNature getAggregateType() {
        return m_aggregatePanel.getAggregateType();
    }

}
