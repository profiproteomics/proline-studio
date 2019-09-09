/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 19 déc. 2018
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.rsmexplorer.gui.ptm.PTMSitePeptidesGraphicCtrlPanel;

/**
 * This class is very similaire as DataBoxPTMSitePeptides
 *
 * @author Karine XUE
 */
public class DataBoxPTMSitePeptidesGraphic extends DataBoxPTMSitePeptides {

    private ResultSummary m_rsm;

    public DataBoxPTMSitePeptidesGraphic() {
        super();//VDS: could use RSM even in XIC ?

        // Name of this databox
        m_typeName = "Graphic Modification Site's Peptides";
        m_description = "Peptides of a Modification Sites using graphical display.";
    }

    @Override
    public void createPanel() {
        PTMSitePeptidesGraphicCtrlPanel p = new PTMSitePeptidesGraphicCtrlPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }


}
