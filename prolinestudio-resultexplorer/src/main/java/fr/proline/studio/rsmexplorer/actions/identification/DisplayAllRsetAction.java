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
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.WindowManager;

/**
 * Action to display a window with all Search Results (rset)
 * @author JM235353
 */
public class DisplayAllRsetAction extends AbstractRSMAction {

    public DisplayAllRsetAction(AbstractTree tree) {
        super("Display List", tree);
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        
        AbstractNode node = selectedNodes[0];
        
        IdProjectIdentificationNode projectNode = (IdProjectIdentificationNode) node.getParent();
        
        Project p = projectNode.getProject();
        
        String windowName = p.getName()+" : All Imported";
        

        boolean displayed = WindowManager.getDefault().getMainWindow().isDisplayed(windowName);
        if (displayed) {
            WindowManager.getDefault().getMainWindow().toFront(windowName);
        } else {
            // prepare window box
            WindowBox wbox = WindowBoxFactory.getAllResultSetWindowBox(windowName);
            wbox.setEntryData(p.getId(), p);

            // open a window to display the window box
            DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);

            WindowManager.getDefault().getMainWindow().displayWindow(win);
        }


    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
       setEnabled(true); //JPM.TODO
    }
    

    
}
