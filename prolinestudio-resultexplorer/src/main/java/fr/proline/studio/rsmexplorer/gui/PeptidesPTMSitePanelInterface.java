/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 22 mars 2019
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;

/**
 *
 * @author Karine XUE
 */
public interface PeptidesPTMSitePanelInterface {

    public void setData(PTMSite peptidesPTMSite, DPeptideInstance pepInst);

    public void setSelectedPeptide(DPeptideInstance inst);

    public PTMSite getSelectedPTMSite();

    public DPeptideInstance getSelectedPeptideInstance();

}
