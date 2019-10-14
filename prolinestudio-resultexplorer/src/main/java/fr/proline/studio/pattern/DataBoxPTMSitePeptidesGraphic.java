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
