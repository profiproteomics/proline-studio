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

import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.dto.DDataset;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.AddAggregateDialog;
import java.util.ArrayList;
import javax.swing.tree.DefaultTreeModel;

import fr.proline.studio.WindowManager;

/**
 * Add Aggregate action
 * @author JM235353
 */
public class AggregateAction extends AbstractRSMAction {

    
    public AggregateAction(AbstractTree tree) {
        super("Add Dataset...", tree);
    }

    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        AddAggregateDialog dialog = AddAggregateDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
        if (dialog.getButtonClicked() == AddAggregateDialog.BUTTON_OK) {
            
            // retrieve parameters
            String name = dialog.getAggregateName();
            final int nbAggregates = dialog.getNbAggregates();
            Aggregation.ChildNature aggregateType = dialog.getAggregateType();
            
            int nbNodes = selectedNodes.length;
                  
            for (int iNode=0;iNode<nbNodes;iNode++) {
        
                final AbstractNode n = selectedNodes[iNode];
            
                boolean isParentAProject = (n.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION);

                // check if a child has already the same name with a number suffix
                int suffixNumber = 0;
                int nbChildren = n.getChildCount();
                for (int i = 0; i < nbChildren; i++) {
                    AbstractNode child = (AbstractNode) n.getChildAt(i);
                    String childName = child.toString();

                    if (childName.startsWith(name)) {
                        String suffix = childName.substring(name.length());
                        try {
                            int number = Integer.parseInt(suffix);
                            if (number > suffixNumber) {
                                suffixNumber = number;
                            }
                        } catch (NumberFormatException nfe) {
                            // can happen not a bug
                        }
                    }
                }

                if ((suffixNumber == 0) && (nbAggregates > 1)) {
                    suffixNumber = 1;
                } else if (suffixNumber > 0) {
                    suffixNumber++;
                }
                int suffixStart = suffixNumber;

                final ArrayList<DataSetNode> nodesCreated = new ArrayList<>();

                final IdentificationTree tree = IdentificationTree.getCurrentTree();
                final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                for (int i = 0; i < nbAggregates; i++) {
                    String aggregateName = name;
                    if (suffixNumber > 0) {
                        aggregateName += suffixNumber;
                        suffixNumber++;
                    }
                    DataSetNode datasetNode = new DataSetNode(DataSetData.createTemporaryAggregate(aggregateName));//new DataSetData(aggregateName, Dataset.DatasetType.AGGREGATE, aggregateType));
                    nodesCreated.add(datasetNode);
                    datasetNode.setIsChanging(true);
                    
                    if (isParentAProject) {
                        n.insert(datasetNode, n.getChildCount()-1);
                    } else {
                        n.add(datasetNode);
                    }
                }
                treeModel.nodeStructureChanged(n);

                // expand the parent node to display its children
                tree.expandNodeIfNeeded(n);



                // add to really create the aggregate dataset

                Project project = null;
                DDataset parentDataset = null;
                DataSetNode parentDatasetNode = null;
                if (n.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                    project = ((IdProjectIdentificationNode) n).getProject();
                    parentDataset = null;
                } else if (n.getType() == AbstractNode.NodeTypes.DATA_SET) {
                    parentDatasetNode = ((DataSetNode) n);
                    parentDataset = parentDatasetNode.getDataset();
                    project = parentDataset.getProject();
                }

                final ArrayList<DDataset> createdDatasetList = new ArrayList<>();

                        
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                        if (!success) {
                            return; // should not happen
                        }

                        int nbNodes = nodesCreated.size();
                        for (int i = 0; i < nbNodes; i++) {

                            DataSetNode datasetNode = nodesCreated.get(i);
                            DDataset dataset = createdDatasetList.get(i);
                            datasetNode.setIsChanging(false);
                            ((DataSetData) datasetNode.getData()).setDataset(dataset);
                            treeModel.nodeChanged(datasetNode);
                        }

                    }
                };

                // ask asynchronous loading of data



                DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                task.initCreateDatasetAggregate(project, parentDataset, aggregateType, name, (suffixNumber > 0), suffixStart, suffixStart + nbAggregates - 1, createdDatasetList);
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            }
            }
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }
        
        int nbSelectedNodes = selectedNodes.length;
        
        if (nbSelectedNodes == 0) {
            setEnabled(false);
            return;
        }
        
        for (int i=0;i<nbSelectedNodes;i++) {
            AbstractNode node = selectedNodes[i];
            
            // parent node is being created, we can not add an identification
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }
                
            // we can always add an aggregate directly to a project
            if (node.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                continue;
            }
            
            // we can add an aggregate only to a data set without a ResultSet or a ResultSummary
            if (node.getType() == AbstractNode.NodeTypes.DATA_SET) {
                
                DDataset d = ((DataSetNode) node).getDataset();
                if ((!d.isAggregation()) && (!d.isFolder())) {
                    setEnabled(false);
                    return;
                }
                // not on a merged 
               if(((DataSetNode) node).hasResultSet() || ((DataSetNode) node).hasResultSummary()){
                    setEnabled(false);
                    return;
               }
            }
            
        }

        setEnabled(true);
 
    }
}