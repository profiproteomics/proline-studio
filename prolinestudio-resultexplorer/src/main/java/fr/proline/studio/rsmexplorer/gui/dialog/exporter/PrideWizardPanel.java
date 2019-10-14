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
package fr.proline.studio.rsmexplorer.gui.dialog.exporter;

import fr.proline.studio.gui.HelpHeaderPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 *
 * @author VD225637
 */
public abstract class PrideWizardPanel extends JPanel {

    protected void initWizardPanels(String wizardLabel){
        setLayout(new BorderLayout());
        add(new HelpHeaderPanel(wizardLabel), BorderLayout.PAGE_START);
        add(createMainPanel(), BorderLayout.CENTER);
    }
    
    protected abstract JPanel createMainPanel();
    protected abstract HashMap<String, Object> getExportPrideParams();

    //Return component which is invalid
    protected abstract Component checkExportPrideParams();
    //Return message explaining invalid component
    protected abstract String getErrorMessage();
    
}
