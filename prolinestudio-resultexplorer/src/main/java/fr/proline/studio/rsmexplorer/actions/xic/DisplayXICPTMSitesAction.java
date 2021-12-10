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
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.WindowManager;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.actions.AbstractDisplayPTMDataAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;

/**
 *
 * @author VD225637
 */
public class DisplayXICPTMSitesAction extends AbstractDisplayPTMDataAction {

    public DisplayXICPTMSitesAction( boolean isAnnotatedPTMDs, AbstractTree tree) {
        super(true, isAnnotatedPTMDs, tree,"Modification");

    }

    public DisplayXICPTMSitesAction(AbstractTree tree) {
       this( false, tree);
    }


    protected void loadWindowBox(DDataset dataSet, Object data) {

        // prepare window box
        WindowBox wbox = WindowBoxFactory.getXicPTMDataWindowBox(dataSet.getName(), true, isAnnotatedPTMsAction());
        wbox.setEntryData(dataSet.getProject().getId(), data);

        // open a window to display the window box
        DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
        WindowManager.getDefault().getMainWindow().displayWindow(win);
    }


    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;
        if (nbSelectedNodes == 0) {
            setEnabled(false);
            return;
        }

        for (int i=0;i<nbSelectedNodes;i++) {
            AbstractNode node = selectedNodes[i];
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET && node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                setEnabled(false);
                return;
            }

            DataSetNode dataSetNode = (DataSetNode) node;
            if (! dataSetNode.hasResultSummary()) {
                setEnabled(false);
                return;
            }
             // must be a quantitation
            if (! (dataSetNode.isQuantitation() && !dataSetNode.isQuantSC()) ) {
                setEnabled(false);
                return;
            }            
        }

        setEnabled(true);
    }

}
