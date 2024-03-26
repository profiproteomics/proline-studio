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
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;

import java.util.ArrayList;
import java.util.List;

/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
/**
 *
 * @author Karine XUE
 */
public class PeptideAreaModel {

    ArrayList<PeptideView> m_viewPeptideList;
    int m_selectedIndex;

    public PeptideAreaModel() {
        m_viewPeptideList = new ArrayList<>();
        m_selectedIndex = 0;

    }

    public ArrayList<PeptideView> getViewPeptideList() {
        return m_viewPeptideList;
    }



     /**
     * set all peptide of a given protein as PTMPeptideInstance (références: peptide instance + all PTMSite...)
     * Create associated PeptideView object from PTMSitePeptideInstance
     *
     * @param pPeptide
     */
    public void setPTMPeptides(List<PTMPeptideInstance> pPeptide) {
        m_viewPeptideList = new ArrayList<>();

        
        for (PTMPeptideInstance pep : pPeptide) {
            PeptideView p = new PeptideView(pep, false);
            m_viewPeptideList.add(p);
        }
    }
    
    /**
     * prepare repaint
     *
     * @param index
     */
    protected void setSelectedIndex(int index) {
        if (index == -1) {
            return;
        }
        this.m_selectedIndex = index;
        PeptideView pv;
        for (int i = 0; i < m_viewPeptideList.size(); i++) {
            pv = m_viewPeptideList.get(i);
            pv.setSelected(i == index);
        }
    }

    protected void setRelativeSelected(int relative) {
        int index = this.m_selectedIndex + relative;
        if ((index != -1) && (index != this.getViewPeptideList().size())) {
            this.m_selectedIndex = index;
            this.setSelectedIndex(index);
        }
    }

    protected int getSelectedIndex() {
        return this.m_selectedIndex;
    }

}
