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
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSitePeptideInstance;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;

import java.awt.Graphics2D;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PeptideAreaCtrl {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    PeptideAreaModel m_mgr;
    PeptideSetView m_pepSetView;

    public PeptideAreaCtrl() {
        m_mgr = new PeptideAreaModel();
        m_pepSetView = new PeptideSetView();
    }

    public void setData(List<PTMSitePeptideInstance> ptmSitePeptide) {
        m_mgr.setPTM(ptmSitePeptide);
        m_pepSetView.setPeptideViewList(m_mgr.getViewPeptideList());
    }

    public void setPTMPepInstanceData(List<PTMPeptideInstance> ptmSitePeptide) {
        m_mgr.setPTMPeptides(ptmSitePeptide);
        m_pepSetView.setPeptideViewList(m_mgr.getViewPeptideList());
    }

    public void setBeginPoint(int x, int y) {
        this.m_pepSetView.setBeginPoint(x, y);
    }

    public void paint(Graphics2D g2, ViewContext viewContext) {
        this.m_pepSetView.paint(g2, viewContext);
    }

    /**
     * from mouse position to find selected peptide,return its index
     *
     * @param x
     * @param y
     * @return -1 when no found
     */
    public int getSelectedIndex(int x, int y) {
        int index = this.m_pepSetView.getSelectedItemIndex(x, y);
        return index;
    }

    public void setSelectedIndex(int i) {
        this.m_mgr.setSelectedIndex(i);
    }

    public int getSelectedIndex() {
        return this.m_mgr.getSelectedIndex();
    }

    public String getToolTipText(int x, int y) {
        return this.m_pepSetView.getToolTipText(x, y);
    }

    public void setRelativeSelected(int relative) {
        this.m_mgr.setRelativeSelected(relative);
    }

}
