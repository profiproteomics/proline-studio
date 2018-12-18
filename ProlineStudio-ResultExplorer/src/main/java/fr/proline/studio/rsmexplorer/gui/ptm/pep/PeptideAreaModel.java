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

    public PeptideAreaModel() {
        _viewPeptideList = new ArrayList<PeptideView>();

    }

    public ArrayList<PeptideView> getViewPeptideList() {
        return _viewPeptideList;
    }

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
    protected void setSelectedItem(int index) {
        if (index == -1) {
            return;
        }
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

}
