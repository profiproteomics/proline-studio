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

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PTMGraphicCtrlPanel extends JPanel implements DataBoxPanelInterface, SplittedPanelContainer.UserActions {

    DProteinMatch m_selectedProteinMatch;
    List<PTMPeptideInstance> m_ptmPeptideInstances;

    public enum Source {
        PEPTIDE_AREA,
        SEQUENCE
    }

    public enum Message {
        SELECTED,
        SEQUENCE
    }

    private static final Logger m_logger = LoggerFactory.getLogger(PTMGraphicCtrlPanel.class);
    private final PTMPeptidesGraphicView m_ptmPeptideAreaCtrl;
    private final PeptideOnProteinOverviewPanel m_proteinOverviewCtrl;

    protected AbstractDataBox m_dataBox;

    public PTMGraphicCtrlPanel() {
        super();
        m_ptmPeptideAreaCtrl = new PTMPeptidesGraphicView();
        m_ptmPeptideAreaCtrl.setSuperCtrl(this);
        m_proteinOverviewCtrl = new PeptideOnProteinOverviewPanel(this);

        initComponents();
    }

    /**
     * ToolBar at left, PainArea at Center
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        JToolBar toolbar = initToolbar();
        m_ptmPeptideAreaCtrl.setSize(WIDTH - 30, HEIGHT - 50);
        m_proteinOverviewCtrl.setPreferredSize(new Dimension(WIDTH - 30, 50));
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(m_ptmPeptideAreaCtrl, BorderLayout.CENTER);
        centerPanel.add(m_proteinOverviewCtrl, BorderLayout.SOUTH);
        this.add(toolbar, BorderLayout.WEST);
        this.add(centerPanel, BorderLayout.CENTER);
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

    public void setSelectedProtein(DProteinMatch proteinMatch) {
        m_selectedProteinMatch = proteinMatch;
    }

    public void setData(List<PTMPeptideInstance> ptmPepInstances) {
        if (ptmPepInstances == null || ptmPepInstances.isEmpty()) {
            return;
        }
        m_ptmPeptideAreaCtrl.setData(ptmPepInstances);
        m_ptmPeptideInstances = ptmPepInstances;

        if (m_selectedProteinMatch == null) {
            return;
        } else {
            DPeptideInstance[] peptideInstances = m_selectedProteinMatch.getPeptideSet(m_dataBox.getRsmId()).getPeptideInstances();

            if (m_selectedProteinMatch.getDBioSequence() != null) {
                m_proteinOverviewCtrl.setData(m_selectedProteinMatch.getAccession(), m_selectedProteinMatch.getDBioSequence().getSequence(), ptmPepInstances.get(0), ptmPepInstances, peptideInstances);
            } else {
                //sequence of protein begin = 0, end with length
                m_proteinOverviewCtrl.setData(m_selectedProteinMatch.getAccession(), "0" + m_ptmPeptideAreaCtrl.getSequence().length(), ptmPepInstances.get(0), ptmPepInstances, peptideInstances);
            }
        }

        this.repaint();
    }

    /**
     * when selected peptide change, change next databox and table selected row
     */
    protected void valueChanged() {
        m_dataBox.addDataChanged(PTMPeptideInstance.class, null); //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
        m_dataBox.propagateDataChanged();
        this.repaint();
    }

    public void setSelectedPTMPeptide(PTMPeptideInstance pepInstance) {
        this.m_ptmPeptideAreaCtrl.setSelectedPTMPeptide(pepInstance);
        this.m_proteinOverviewCtrl.setSelectedPeptide(pepInstance);
    }

    public PTMPeptideInstance getSelectedPTMPeptideInstance() {
        return this.m_ptmPeptideAreaCtrl.getSelectedPTMPeptideInstance();
    }

    public int getSelectedIndex() {
        return this.m_ptmPeptideAreaCtrl.getSelectedIndex();

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
        m_ptmPeptideAreaCtrl.setDataBox(dataBox);
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

    void onMessage(Source source, Message message) {

        if (source.equals(Source.PEPTIDE_AREA)) {
            if (message.equals(Message.SELECTED)) {
                this.m_proteinOverviewCtrl.setSelectedPeptide(m_ptmPeptideAreaCtrl.getSelectedPTMPeptideInstance());
            }
        } else if (source.equals(Source.SEQUENCE)) {
            if (message.equals(Message.SELECTED)) {
                int selectedOnProtein = m_proteinOverviewCtrl.getSelectedProteinPosition();
                this.m_ptmPeptideAreaCtrl.setViewPosition(selectedOnProtein);
            } else if (message.equals(Message.SEQUENCE)) {
                String sequence = m_proteinOverviewCtrl.getProteinSequence();
                this.m_ptmPeptideAreaCtrl.setProteinSequence(sequence);
                

            }
        }
        repaint();

    }

}
