package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.dam.tasks.data.ptm.PTMSitePeptideInstance;
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

    ArrayList<PeptideView> _viewPeptideList;
    int _selectedIndex;

    public PeptideAreaModel() {
        _viewPeptideList = new ArrayList<PeptideView>();
        _selectedIndex = 0;

    }

    public ArrayList<PeptideView> getViewPeptideList() {
        return _viewPeptideList;
    }

    /**
     * set all peptide of the given protein, which has this PTM Site create
     * PeptideView object from PTMSitePeptideInstance
     *
     * @param pPeptide
     */
    public void setPTM(List<PTMSitePeptideInstance> pPeptide) {
        _viewPeptideList = new ArrayList<>();
        for (PTMSitePeptideInstance pep : pPeptide) {
            PeptideView p = new PeptideView(pep);
            _viewPeptideList.add(p);
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
        this._selectedIndex = index;
        PeptideView pv;
        for (int i = 0; i < _viewPeptideList.size(); i++) {
            pv = _viewPeptideList.get(i);
            if (i != index) {
                pv.setSelected(false);
            } else {
                pv.setSelected(true);
            }
        }
    }

    protected void setRelativeSelected(int relative) {
        int index = this._selectedIndex + relative;
        if ((index == -1) || (index == this.getViewPeptideList().size())) {
            return;
        } else {
            this._selectedIndex = index;
            this.setSelectedIndex(index);
        }
    }

    protected int getSelectedIndex() {
        return this._selectedIndex;
    }

}
