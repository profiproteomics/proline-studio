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
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle;

/**
 * 
 * Action displayed as a menu to open the Data Analyzer window
 * 
 * @author JM235353
 */

@ActionID(category = "Window", id = "fr.proline.studio.rsmexplorer.actions.DataAnalyzerAction")
@ActionRegistration(displayName = "#CTL_DataAnalyzerAction")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 100)
})
@NbBundle.Messages("CTL_DataAnalyzerAction=Data Analyzer")
public final class DataAnalyzerAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DataAnalyzerWindowBoxManager.openDataAnalyzer();
    }
}
