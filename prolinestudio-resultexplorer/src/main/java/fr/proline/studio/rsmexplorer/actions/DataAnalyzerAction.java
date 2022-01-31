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
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;

import javax.swing.*;
import java.awt.event.ActionEvent;



/**
 * 
 * Action displayed as a menu to open the Data Analyzer window
 * 
 * @author JM235353
 */


public final class DataAnalyzerAction extends AbstractAction {

    public DataAnalyzerAction() {
        putValue(Action.NAME, "Data Analyzer");


        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataAnalyzerWindowBoxManager.openDataAnalyzer();
    }
}
