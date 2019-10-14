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
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.data.DatasetToCopy;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.HourGlassNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import java.util.ArrayList;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author JM235353
 */
public class PasteSearchResult extends AbstractRSMAction {

    public PasteSearchResult(AbstractTree tree) {
        super("Paste Search Result", tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        
        final IdentificationTree tree = IdentificationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        
        DatasetToCopy copy = DatasetToCopy.getDatasetCopied();

        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        
        final AbstractNode node = selectedNodes[0];
        
        // node is changing
        node.setIsChanging(true);
        
        
        AbstractData data = (AbstractData) node.getUserObject();
        
        DDataset parentDataset = null;
        if (data.getDataType() == AbstractData.DataTypes.DATA_SET) {
            DataSetNode datasetNode = (DataSetNode) selectedNodes[0];
            parentDataset = datasetNode.getDataset();
        }
        
        final ArrayList<DDataset> datasetList = new ArrayList<>(1);
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success) {

                    DDataset d = datasetList.get(0);
//                    Aggregation aggregation = d.getAggregation();
//                    DataSetData data = new DataSetData(d.getName(), d.getType(), (aggregation!=null) ? d.getAggregation().getChildNature() : Aggregation.ChildNature.SAMPLE_ANALYSIS);
//                    data.setDataset(d);
                    DataSetData data = new DataSetData(d);
                    DataSetNode datasetNode = new DataSetNode(data);
                    if (d.getChildrenCount()>0) {
                        datasetNode.add(new HourGlassNode(null));
                    }
                    boolean isParentAProject = (node.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION);
                    if (isParentAProject) {
                        node.insert(datasetNode, node.getChildCount()-1);
                    } else {
                        node.add(datasetNode);
                    }
                    
                    
                    node.setIsChanging(false);
                    treeModel.nodeStructureChanged(node);

                } else {
                    // should not happen
                    node.setIsChanging(false);
                }
            }
        };

        // ask asynchronous loading of data
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);

        task.initPasteDatasets(selectedProject, parentDataset, copy, datasetList);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
        DatasetToCopy copy = DatasetToCopy.getDatasetCopied();
        if (copy == null) {
            setEnabled(false);
            return;
        }
        
        long cloneProjectId = copy.getProjectId();
        
        // only one selection allowed
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
        
        if (cloneProjectId != selectedProject.getId()) {
            // we are not in the same project
            setEnabled(false);
            return;
        }
        
        AbstractNode node = selectedNodes[0];
        
        AbstractData data = (AbstractData) node.getUserObject();
        
        if (data.getDataType() == AbstractData.DataTypes.PROJECT_IDENTIFICATION) {
            setEnabled(true);
            return;
        } else if (data.getDataType() == AbstractData.DataTypes.DATA_SET) {
            DataSetData datasetData = (DataSetData) data;
            if (datasetData.getDatasetType().isIdentification() && datasetData.getDatasetType().isFolder()) {
                setEnabled(true);
                return;
            } else if (datasetData.getDatasetType().isIdentification() && datasetData.getDatasetType().isAggregation()) {
                DDataset dataset =  datasetData.getDataset();
                if (dataset.getResultSetId() == null) {
                    setEnabled(true);
                    return;
                }
                
            }
        }

        
        setEnabled(false);
    }

    
    
}
