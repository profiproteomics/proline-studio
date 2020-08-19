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
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;


/**
 * Action to see all peptides ions information per quant Channel
 * @author JM235353
 */
public class DisplayXICPeptideIonAction extends AbstractRSMAction {

    public DisplayXICPeptideIonAction(AbstractTree tree) {
        super("Peptides Ions", tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

            actionImpl(dataSetNode);
        }

    }

    private void actionImpl(DataSetNode dataSetNode) {
        
        final DDataset dataset = ((DataSetData) dataSetNode.getData()).getDataset();


        WindowBox wbox = WindowBoxFactory.getXicQuantPeptideIonWindowBox(dataset.getName(), dataset.getName()+" Peptides Ions", dataSetNode.isQuantitation() && !dataSetNode.isQuantSC());
            wbox.setEntryData(dataset.getProject().getId(), dataset);


            // open a window to display the window box
            DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
            WindowManager.getDefault().getMainWindow().displayWindow(win);
        }


    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // only one node selected
        if (selectedNodes.length != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = (AbstractNode) selectedNodes[0];

        // the node must not be in changing state
        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        // must be a dataset 
        if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        DataSetNode datasetNode = (DataSetNode) node;

        if (! (datasetNode.isQuantitation() && !datasetNode.isQuantSC()) ) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }
}