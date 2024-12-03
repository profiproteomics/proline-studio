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
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.pattern.AbstractDataBox;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PTMPeptidesGraphicView extends JPanel {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    protected AbstractDataBox m_dataBox;

    private final PTMPeptidesGraphicPanel m_internalPanel;
    private final PTMPeptidesGraphicModel m_dataModel;

    public void setSuperCtrl(PTMGraphicCtrlPanel superCtrl) {
        this.m_internalPanel.setSuperCtrl(superCtrl);
    }

    public void setScrollLocation(int ajustedLocation) {
        this.m_internalPanel.setHorizonScrollLocation(ajustedLocation);
        repaint();
    }

    public void setViewPosition(int sequencePosition) {
        this.m_internalPanel.setHorizonScrollLocation(sequencePosition);
        repaint();
    }

    public void setProteinSequence(String sequence) {
        this.m_internalPanel.getPTMPeptidesModel().setProteinSequence(sequence);
        this.m_internalPanel.updateData();
    }

    public PTMPeptidesGraphicView() {
        super();
        m_internalPanel = new PTMPeptidesGraphicPanel();
        m_dataModel = new PTMPeptidesGraphicModel();
        m_internalPanel.setModel(m_dataModel);
        initComponents();
    }

    /**
     * ToolBar at left, PainArea at Center
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        m_internalPanel.setSize(WIDTH - 30, HEIGHT);
        this.add(m_internalPanel, BorderLayout.CENTER);

    }

    /**
     * useful between table-graphic ptm site panel
     *
     * @param i
     */
    public void setSelectedRow(int i) {
        this.m_internalPanel.setSelectedPeptideIndex(i);
    }

    public void setSelectedPTMPeptide(PTMPeptideInstance pep) {
        int row = this.m_dataModel.getPeptideIndex(pep);
        this.m_internalPanel.setSelectedPeptideIndex(row);
        repaint();
    }

    /**
     * used to set next Data Box
     */
    public PTMPeptideInstance getSelectedPTMPeptideInstance() {
        int selectedRowIndex = this.m_internalPanel.getSelectedPeptideIndex();
        if (m_dataModel.getRowCount() > 0 && selectedRowIndex < m_dataModel.getRowCount()) {
            return this.m_dataModel.getPeptideAt(selectedRowIndex);
        }
        return null;
    }

    public int getSelectedIndex() {
        return this.m_internalPanel.getSelectedPeptideIndex();
    }

    public void setData(List<PTMPeptideInstance> peptidesInstances) {
        m_dataModel.setData(peptidesInstances, m_dataBox.getProjectId());
        if (peptidesInstances == null) {
            m_internalPanel.clean();
        } else {
            m_internalPanel.updateData();
            m_dataBox.addDataChanged(PTMPeptideInstance.class, null); //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
            m_dataBox.propagateDataChanged();
        }
        setScrollLocation(this.m_dataModel.getLowerStartInProtSeq());
        this.repaint();
    }

    public void setDataBox(AbstractDataBox dataBox) {
        this.m_dataBox = dataBox;
        m_internalPanel.setDataBox(dataBox);
    }

    public String getSequence() {
        return this.m_dataModel.getProteinSequence();
    }

}
