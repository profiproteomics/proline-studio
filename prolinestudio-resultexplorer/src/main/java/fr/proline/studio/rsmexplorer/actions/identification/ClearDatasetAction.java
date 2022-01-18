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

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;

import fr.proline.studio.WindowManager;

/**
 * Remove Identification Summary and potentially Search Result from a dataset
 * @author JM235353
 */
public class ClearDatasetAction extends AbstractRSMAction {
    
    boolean m_fullClear = false;

    public ClearDatasetAction(AbstractTree tree) {
        super("Clear Validation", tree);
    }

    public ClearDatasetAction(AbstractTree tree, boolean fullClear) {
        super(fullClear ? "Clear All" : "Clear Validation", tree);
        m_fullClear = fullClear;
    }

     @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {
        StringBuilder sb = new StringBuilder("The Clear action will delete generated data (Identification Summary");
         if (m_fullClear) {
             sb.append(" and Merged DataSet)");
         } else {
             sb.append(")");
         }
         sb.append("\n(This can not be undone) Are you sure ?");
        String title = "Clear Dataset";
        int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),sb.toString(), title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (n == JOptionPane.YES_OPTION) {
            IdentificationTree tree = IdentificationTree.getCurrentTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

            int nbNodes = selectedNodes.length;
            for (int i = 0; i < nbNodes; i++) {
                final DataSetNode node = (DataSetNode) selectedNodes[i];
                node.setIsChanging(true);
                treeModel.nodeChanged(node);

                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }


                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                        if (success) {
                            DDataset dataset = node.getDataset();
                            dataset.setResultSummaryId(null);
                            dataset.setResultSummary(null);
                            if (m_fullClear && !node.isLeaf())  {
                                dataset.setResultSetId(null);
                                dataset.setResultSet(null);
                            }
                        }
                        node.setIsChanging(false);
                        treeModel.nodeChanged(node);
                    }
                };

                DDataset dataset = node.getDataset();
                DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                task.initClearDataset(dataset, (m_fullClear && !node.isLeaf()) );

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
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            DataSetNode datasetNode = (DataSetNode) node;

            if (datasetNode.isLeaf()) {
                if (!datasetNode.hasResultSummary()) {
                    setEnabled(false);
                    return;
                }
                AbstractNode parentNode = (AbstractNode) datasetNode.getParent();
                if (parentNode.getType() == AbstractNode.NodeTypes.DATA_SET) {
                    DataSetNode parentDatasetNode = (DataSetNode) parentNode;
                    if (parentDatasetNode.isChanging() || parentDatasetNode.hasResultSet() || parentDatasetNode.hasResultSummary()) {
                        // parent merged
                        setEnabled(false);
                        return;
                    }
                }
                
            } else {
                if (!datasetNode.hasResultSummary() && ! datasetNode.hasResultSet()) {
                    setEnabled(false);
                    return;
                } else {

                    if (!datasetNode.hasResultSummary() && !m_fullClear) {
                        setEnabled(false);
                        return;
                    }


                    DDatasetType.AggregationInformation aggInfo = datasetNode.getDataset().getAggregationInformation();
                        if(aggInfo.equals(DDatasetType.AggregationInformation.IDENTIFICATION_SUMMARY_AGG) || aggInfo.equals(DDatasetType.AggregationInformation.IDENTIFICATION_SUMMARY_UNION)){
                        // Identification merged only full clear
                        if(!m_fullClear) {
                            setEnabled(false);
                            return;
                        }
                    }

                    AbstractNode parentNode = (AbstractNode) datasetNode.getParent();
                    if (parentNode.getType() == AbstractNode.NodeTypes.DATA_SET) {
                        DataSetNode parentDatasetNode = (DataSetNode) parentNode;
                        if (parentDatasetNode.isChanging() || parentDatasetNode.hasResultSet() || parentDatasetNode.hasResultSummary()) {
                            // parent merged
                            setEnabled(false);
                            return;
                        }
                    }
                }
            }
            
            
        }
        setEnabled(true);
    }
    
}
