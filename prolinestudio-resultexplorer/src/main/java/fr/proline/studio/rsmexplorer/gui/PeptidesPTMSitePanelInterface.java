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
