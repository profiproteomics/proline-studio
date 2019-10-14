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

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.dam.data.DatasetToCopy;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;


/**
 *
 * @author JM235353
 */
public class CopySearchResult extends AbstractRSMAction {

    public CopySearchResult(AbstractTree tree) {
        super("Copy Search Result", tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        final DataSetNode node = (DataSetNode) selectedNodes[0];

        Runnable callback = new Runnable() {
            @Override
            public void run() {
                DatasetToCopy copy = createCopy(node);

                DatasetToCopy.saveDatasetCopied(copy);
            }

        };

        IdentificationTree.getCurrentTree().loadNode(node, callback, true);

    }
    
    private DatasetToCopy createCopy(DataSetNode node) {
        DDataset dataset = ((DataSetData) node.getData()).getDataset();
        long projectId = dataset.getProject().getId();
        Long resultSetId = dataset.getResultSetId();
        DatasetToCopy copy = new  DatasetToCopy();
        copy.setProjectId(projectId);
        copy.setName(dataset.getName());
        Aggregation aggregation = dataset.getAggregation();
        if (aggregation != null) {
            copy.setDatasetType(aggregation.getChildNature());
        }
        int nbChildren = node.getChildCount();
        if (nbChildren == 0) {
            copy.setResultSetId(resultSetId);
        }
        for (int childIndex = 0;childIndex<nbChildren; childIndex++) {
            DataSetNode child = (DataSetNode) node.getChildAt(childIndex);
            copy.addChild(createCopy(child));
        }
        
        return copy;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
        //JPM.TODO: allow multiple selections
        if (selectedNodes.length > 1) {
            setEnabled(false);
            return;
        }
        
        
        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }
        
        int nbSelectedNodes = selectedNodes.length;
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            if (!node.canBeDeleted()) {
                setEnabled(false);
                return;
            }

            if (!(selectedNodes[i] instanceof DataSetNode)) {
                setEnabled(false);
                return;
            }
            
            if (((DataSetNode)selectedNodes[i]).isFolder()) {
                setEnabled(false);
                return;
            }

        }
        
        setEnabled(true);
    }

    
    
}
