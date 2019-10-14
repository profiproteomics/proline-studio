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
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.PeptidesPTMSitePanelInterface;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.PTMMarkCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequence;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PTMSitePeptidesGraphicCtrlPanel extends JPanel implements DataBoxPanelInterface, SplittedPanelContainer.UserActions, PeptidesPTMSitePanelInterface {

    private static Logger logger = LoggerFactory.getLogger(PTMSitePeptidesGraphicCtrlPanel.class);
    protected PTMSitePeptidesGraphicDataMgr m_dataMgr;

    protected PTMSitePeptidesGraphicViewPanel m_paintArea;
    protected PTMMarkCtrl m_ctrlMark;
    protected ProteinSequence m_ctrlSequence;
    protected PeptideAreaCtrl m_ctrlPeptideArea;

    protected AbstractDataBox m_dataBox;
    protected PTMSite m_currentPTMSite = null;

    public PTMSitePeptidesGraphicCtrlPanel() {
        //super(false);
        super();
        m_dataMgr = new PTMSitePeptidesGraphicDataMgr();
        m_ctrlMark = new PTMMarkCtrl();
        m_ctrlSequence = new ProteinSequence();
        m_ctrlPeptideArea = new PeptideAreaCtrl();
        m_paintArea = new PTMSitePeptidesGraphicViewPanel(this, m_ctrlMark, m_ctrlSequence, m_ctrlPeptideArea);
        initComponents();
    }

    /**
     * ToolBar at left, PainArea at Center
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        m_paintArea.setSize(WIDTH - 30, HEIGHT);
        JToolBar toolbar = initToolbar();
        this.add(toolbar, BorderLayout.WEST);
        this.add(m_paintArea, BorderLayout.CENTER);

    }

    /**
     * Export image button in ToolBar
     *
     * @return
     */
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        ExportButton exportImageButton = new ExportButton("Graphic", this);
        toolbar.add(exportImageButton);

        return toolbar;
    }

    @Override
    public void setData(PTMSite peptidesPTMSite, DPeptideInstance pepInst) {
        //logger.debug(this.getClass().getName() + " setData ->");
        if (peptidesPTMSite == null) {
            return;
        }
        if ((peptidesPTMSite.equals(m_currentPTMSite))) {
            return;
        }
        m_dataMgr.setData(peptidesPTMSite);
        if (peptidesPTMSite == null) {
            this.m_paintArea.clean();
        } else {
            this.m_currentPTMSite = peptidesPTMSite;

            this.m_ctrlSequence.setData(m_dataMgr.getProteinSequence());
            this.m_ctrlSequence.setPTMSequencePosition(m_dataMgr.getPTMSiteSeqPos());
            this.m_ctrlMark.setData(m_dataMgr.getAllPtmMarks());
            this.m_ctrlPeptideArea.setData(m_dataMgr.getPTMSitePeptideInstances());

            this.m_ctrlPeptideArea.setSelectedIndex(0);
            this.m_paintArea.setIsDataLoaded(true);
            this.m_paintArea.setRowCount(this.m_dataMgr.getRowCount());
            this.m_paintArea.setSequenceLength(m_dataMgr.getProteinSequence().length());
            int ajustedLocation = m_dataMgr.getBeginBestFit();
            this.m_paintArea.setScrollLocation(ajustedLocation);
            valueChanged();
        }
        this.repaint();
    }

    public PTMSite getSelectedPTMSite() {
        return this.m_currentPTMSite;
    }

    /**
     * used to set next Data Box
     */
    @Override
    public DPeptideInstance getSelectedPeptideInstance() {
        int selectedRowIndex = this.m_paintArea.getSelectedPeptideIndex();
        //logger.debug("getSelectedPeptideInstance selectRowIndex: " + selectedRowIndex);
        return this.m_dataMgr.getSelectedDPeptideInstance(selectedRowIndex);
    }

    @Override
    public void setSelectedPeptide(DPeptideInstance pep) {
        int row = this.m_dataMgr.getPeptideIndex(pep);
        this.m_paintArea.setSelectedPeptideIndex(row);
        repaint();
    }

    /**
     * when selected petptied change, change next databox and table selected row
     */
    protected void valueChanged() {
        m_dataBox.propagateDataChanged(PTMSite.class);
        this.repaint();
    }

    /**
     * useful between table-graphic ptm site panel
     *
     * @param i
     */
    public void setSelectedRow(int i) {
        this.m_ctrlPeptideArea.setSelectedIndex(i);
        this.m_paintArea.setSelectedPeptideIndex(i);
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    @Override
    public void addSingleValue(Object v) {
        //Nothing to Do
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public void setLoading(int id) {
        //Nothing to Do
    }

    @Override
    public void setLoading(int id, boolean calculating) {
        //Nothing to Do
    }

    @Override
    public void setLoaded(int id) {
        //Nothing to Do
    }

}
