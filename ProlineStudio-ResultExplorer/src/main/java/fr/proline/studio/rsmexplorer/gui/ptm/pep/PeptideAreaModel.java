package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.rsmexplorer.gui.ptm.PtmSitePeptide;
import java.util.ArrayList;

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

    public void setPTM(ArrayList<PtmSitePeptide> pPeptide) {
        _viewPeptideList = new ArrayList<>();
        for (PtmSitePeptide pep : pPeptide) {
            PeptideView p = new PeptideView(pep);
            _viewPeptideList.add(p);
        }
    }

    protected void setSelctedItem(int index) {
        PeptideView pv;
        for (int i = 0; i < _viewPeptideList.size(); i++) {
            pv = _viewPeptideList.get(i);
            if (i != index) {
                pv.setSelected(false);
            }
        }
    }


}
