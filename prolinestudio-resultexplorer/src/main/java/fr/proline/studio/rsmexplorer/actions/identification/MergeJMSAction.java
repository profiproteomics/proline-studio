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

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDatasetType.AggregationInformation;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.MergeTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Action to Merge data from a set of Search Results (rset) or Identification Summaries (rsm)
 * @author JM235353
 */
public class MergeJMSAction extends AbstractRSMAction {

    private JMenu m_menu;
    private ConfigurableMergeAction m_aggregationMergeAction;
    private ConfigurableMergeAction m_unionMergeAction;
    
    public MergeJMSAction(AbstractTree tree) {
        super(NbBundle.getMessage(MergeJMSAction.class, "CTL_MergeAction"), tree);
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        Preferences preferences = NbPreferences.root();
        Boolean showHiddenFunctionnality =  preferences.getBoolean("Profi", false);

        m_aggregationMergeAction = new ConfigurableMergeAction(getTree(), MergeTask.Config.AGGREGATION);
        m_unionMergeAction = new ConfigurableMergeAction(getTree(), MergeTask.Config.UNION);
        JMenuItem mergeAggregateItem = new JMenuItem(m_aggregationMergeAction);
        m_menu.add(mergeAggregateItem);
        JMenuItem mergeUnionItem = new JMenuItem(m_unionMergeAction);    
        m_menu.add(mergeUnionItem);

        return m_menu;
    }
    
     @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_aggregationMergeAction.updateEnabled(selectedNodes);
        m_unionMergeAction.updateEnabled(selectedNodes);
        
        boolean isEnabled = m_aggregationMergeAction.isEnabled() ||  m_unionMergeAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }
}

class ConfigurableMergeAction extends AbstractRSMAction {
    
    private MergeTask.Config m_configuration;
    
    public ConfigurableMergeAction(AbstractTree tree, MergeTask.Config config) {
        super(config.getValue(), tree);
        m_configuration = config;
    }
    
    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {

        int nbSelectedNodes = selectedNodes.length;
        for (int i = 0; i < nbSelectedNodes; i++) {
            final AbstractNode node = (AbstractNode) selectedNodes[i];

            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    // check if we can do a merge
                    String error = null;
                    int nbChildren = node.getChildCount();
                    /*if (nbChildren == 1) {
                     error = "Merge on an Dataset with only one Child is not possible";
                     }*/

                    HashSet<Long> checkDifferentResultSetId = new HashSet<>();
                    ArrayList<Long> resultSetIdList = new ArrayList<>();
                    ArrayList<Long> resultSummaryIdList = new ArrayList<>();
                    for (int i = 0; i < nbChildren; i++) {
                        DataSetNode childNode = (DataSetNode) node.getChildAt(i);
                        if (childNode.isChanging()) {
                            error = "Merge is not possible while a child Search Result is being imported or validated";
                            break;
                        }
                        if (!childNode.hasResultSet()) {
                            error = "Merge is not possible : " + childNode.getDataset().getName() + " has no Search Result";
                            break;
                        }

                        Long rsetId = childNode.getDataset().getResultSetId();
                        if (checkDifferentResultSetId.contains(rsetId)) {
                            error = "Merge is not possible : several Search Results are identical";
                            break;
                        }
                        checkDifferentResultSetId.add(rsetId);
                        resultSetIdList.add(rsetId);
                        if (childNode.hasResultSummary()) {
                            resultSummaryIdList.add(childNode.getDataset().getResultSummaryId());
                        }                    }

                    if (error != null) {
                        JOptionPane.showMessageDialog(IdentificationTree.getCurrentTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (resultSummaryIdList.size() == nbChildren) {
                        // we do a merge on resultSummary
                        askMergeService((DataSetNode) node, resultSummaryIdList, true);
                    } else if (resultSummaryIdList.size() > 0) {
                        // not all children have a result summary
                        error = "Merge is not possible : some Search Results are not validated ";
                        JOptionPane.showMessageDialog(IdentificationTree.getCurrentTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // merge on result set
                        askMergeService((DataSetNode) node, resultSetIdList, false);
                    }

                }
            };

            IdentificationTree.getCurrentTree().loadInBackground(node, callback);
        }

    }

    private void askMergeService(final DataSetNode node, final List<Long> idList, final boolean mergeOnRsm) {

        final DDataset dataset = node.getDataset();
        long projectId = dataset.getProject().getId();
        String datasetName = dataset.getName();

        node.setIsChanging(true);
        IdentificationTree tree = IdentificationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(node);

        // used as out parameter for the service
        final Long[] _resultSetId = new Long[1];
        final Long[] _resultSummaryId = new Long[1];

        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    updateDataset(node, dataset, _resultSetId[0], _resultSummaryId[0], getTaskInfo());

                } else {
                    //JPM.TODO : manage error with errorMessage
                    node.setIsChanging(false);

                    treeModel.nodeChanged(node);
                }
            }
        };

        MergeTask task = new MergeTask(callback, projectId, m_configuration);
        if (mergeOnRsm) {
             task.initMergeRsm(idList, datasetName, _resultSetId, _resultSummaryId);
        } else {
            _resultSummaryId[0] = null;
            task.initMergeRset(idList, datasetName, _resultSetId);
        }
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
    }

    private void updateDataset(final DataSetNode datasetNode, DDataset d, Long resultSetId, Long resultSummaryId, TaskInfo taskInfo) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                datasetNode.setIsChanging(false);

                if (datasetNode.hasResultSummary()) {
                    if(MergeTask.Config.AGGREGATION.equals(m_configuration))
                        datasetNode.getDataset().setAggregationInformation(
                            AggregationInformation.IDENTIFICATION_SUMMARY_AGG);
                    else
                        datasetNode.getDataset().setAggregationInformation(
                            AggregationInformation.IDENTIFICATION_SUMMARY_UNION);
                } else {
                    if(MergeTask.Config.AGGREGATION.equals(m_configuration))
                        datasetNode.getDataset().setAggregationInformation(
                            AggregationInformation.SEARCH_RESULT_AGG);
                    else
                        datasetNode.getDataset().setAggregationInformation(
                            AggregationInformation.SEARCH_RESULT_UNION);
                }
             
                
                IdentificationTree tree = IdentificationTree.getCurrentTree();
                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                treeModel.nodeChanged(datasetNode);
            }
        };

        // ask asynchronous loading of data



        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initModifyDatasetForMerge(d, resultSetId, resultSummaryId, taskInfo);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

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
        
        if (nbSelectedNodes<0) {
            setEnabled(false);
            return;
        }

        // check if the user has asked merge at the same time between children and
        // parents, if it is the case, it is forbidden
        HashSet<AbstractNode> allSelelectedNodeSet = new HashSet<>();
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];
            allSelelectedNodeSet.add(node);
        }
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];
            node = (AbstractNode) node.getParent();
            while (node != null) {
                if (allSelelectedNodeSet.contains(node)) {
                    setEnabled(false);
                    return;
                }
                node = (AbstractNode) node.getParent();
            }
        }
        
        
        
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = (AbstractNode) selectedNodes[i];

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
                setEnabled(false);
                return;
            }

            if (!((DataSetData) datasetNode.getData()).getDatasetType().isAggregation()) {
                setEnabled(false);
                return;
            }

            if (datasetNode.hasResultSet()) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true );
    }
    
}