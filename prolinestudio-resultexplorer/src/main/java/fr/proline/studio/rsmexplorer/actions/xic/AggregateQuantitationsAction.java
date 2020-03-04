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

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.AggregateQuantitationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.AggregateQuantitationDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class AggregateQuantitationsAction extends AbstractRSMAction {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private int m_nbLoadedQuanti;

    public AggregateQuantitationsAction(AbstractTree tree) {
        super(NbBundle.getMessage(AggregateQuantitationsAction.class, "CTL_AggregateQuantitations"), tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        if (ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject() == null) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "A project should be selected !", "Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Long> quantiId = new ArrayList();
        for (AbstractNode n : selectedNodes) {
            quantiId.add(((DataSetNode) n).getDataset().getId());
        }
        final DDataset[] loadedQuantitations = new DDataset[selectedNodes.length];//to ensure the same order as selected node 

        m_nbLoadedQuanti = 0;
        for (AbstractNode n : selectedNodes) {
            if (!n.isRoot() && DataSetNode.class.isInstance(n)) {
                DataSetNode node = (DataSetNode) n;
                final DataSetData dataset = (DataSetData) node.getData();
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                        if (success) {
                            //loadedQuantitations.add(dataset.getDataset());
                            DDataset quantiDs = dataset.getDataset();
                            long idQuanti = quantiDs.getId();
                            int index = quantiId.indexOf(idQuanti);
                            loadedQuantitations[index] = quantiDs;
                            m_nbLoadedQuanti++;
                            if (m_nbLoadedQuanti == selectedNodes.length) {
                                List<DDataset> loadedSelectedQuanti = Arrays.stream(loadedQuantitations).collect(Collectors.toList());
                                createAggregationDialog(loadedSelectedQuanti, x, y);
                            }
                        }
                    }

                };
                DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
                task.initLoadQuantChannels(ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId(), dataset.getDataset());
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            }
        }

    }

    private void createAggregationDialog(List<DDataset> loadedQuantitations, int x, int y) {
        Long projectID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();

        AggregateQuantitationDialog dialog = AggregateQuantitationDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setQuantitationDatasets(loadedQuantitations);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            final Long[] _xicQuantiDataSetId = new Long[1];

            StringBuffer errorMsg = new StringBuffer("");

            //*** Get experimental design values                
            DataSetData _quantiDS = (DataSetData) dialog.getExperimentalDesignNode().getData();
            String datasetName = _quantiDS.getTemporaryName();
            Map<String, Object> expParams = null;
            try {
                expParams = dialog.getExperimentalDesignParameters();
            } catch (IllegalAccessException iae) {
                errorMsg.append(iae.getMessage());
            }

            Map<String, Object> quantParams = dialog.getQuantiParameters();

            if (!errorMsg.toString().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, errorMsg, "Warning", JOptionPane.ERROR_MESSAGE);
                return;
            }

            m_logger.debug("Will Aggregate Quantitations with params " + quantParams);

            final QuantitationTree tree = QuantitationTree.getCurrentTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            final DataSetNode[] _quantitationNode = {QuantitationTree.getCurrentTree().createQuantitationNode(datasetName)};

            // CallBack for Xic Quantitation Service
            AbstractJMSCallback xicCallback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    if (success) {
                        m_logger.debug(" XIC SUCCESS : " + _xicQuantiDataSetId[0]);
                        QuantitationTree.getCurrentTree().loadDataSet(_xicQuantiDataSetId[0], _quantitationNode[0]);
                    } else {
                        m_logger.debug(" XIC ERROR ");
                        treeModel.removeNodeFromParent(_quantitationNode[0]);
                    }
                }
            };

            AggregateQuantitationTask task = new AggregateQuantitationTask(xicCallback, projectID, datasetName, quantParams, expParams, _xicQuantiDataSetId);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
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

        if (selectedNodes.length <= 1) {
            setEnabled(false);
            return;
        }

        boolean enabled = true;
        for (int k = 0; (k < selectedNodes.length) && enabled; k++) {
            AbstractNode node = (AbstractNode) selectedNodes[k];
            enabled = enabled && !node.isChanging() && (node.getType() == AbstractNode.NodeTypes.DATA_SET);
            if (enabled) {
                DataSetNode datasetNode = (DataSetNode) node;
                enabled = enabled && datasetNode.isQuantXIC();
            }
        }
        setEnabled(enabled);
    }

}
