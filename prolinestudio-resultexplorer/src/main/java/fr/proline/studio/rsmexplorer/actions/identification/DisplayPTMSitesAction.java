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
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.WindowManager;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.actions.AbstractDisplayPTMDataAction;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

/**
 *
 * @author JM235353
 */
public class DisplayPTMSitesAction  extends AbstractDisplayPTMDataAction {

    public DisplayPTMSitesAction(boolean isAnnotated, AbstractTree tree) {
        super(true, isAnnotated, tree);
    }

    public DisplayPTMSitesAction(AbstractTree tree) {
       super(true, tree);
    }


    protected void loadWindowBox(DDataset ds, Object data){
        WindowBox wbox = WindowBoxFactory.getPTMDataWindowBox(ds.getName(), true, isAnnotatedPTMsAction());

        // open a window to display the window box
        DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
        WindowManager.getDefault().getMainWindow().displayWindow(win);

        // prepare window box
        wbox.setEntryData(ds.getProject().getId(), data);
    }

}
