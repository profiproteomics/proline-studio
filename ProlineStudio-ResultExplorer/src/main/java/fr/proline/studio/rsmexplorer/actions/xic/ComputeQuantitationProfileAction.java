/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ComputeQuantProfileTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.QuantProfileXICDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to run the quantitation profile
 *
 * @author MB243701
 */
public class ComputeQuantitationProfileAction extends AbstractRSMAction {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    public ComputeQuantitationProfileAction() {
        super(NbBundle.getMessage(ComputeQuantitationProfileAction.class, "CTL_ComputeQuantitationProfileAction"), AbstractTree.TreeType.TREE_QUANTITATION);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        if (ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject() == null) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "A project should be selected !", "Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //project id
        final Long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
         // dialog with the parameters for quantitation profiler
        QuantProfileXICDialog dialog = QuantProfileXICDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        // retreive parameters
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            //User specified parameters

            String errorMsg = null;

            final Map<String, Object> quantParams = dialog.getQuantParams();
            if (quantParams == null) {
                errorMsg = "Null Quantitation parameters !";
            }

            if (errorMsg != null) {
                JOptionPane.showMessageDialog(dialog, errorMsg, "Warning", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // only on one node
            final AbstractNode node = selectedNodes[0];
            node.setIsChanging(true);
            QuantitationTree tree = QuantitationTree.getCurrentTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            treeModel.nodeChanged(node);
            final DataSetNode datasetNode = (DataSetNode) selectedNodes[0];
            //dataset
            DDataset dataSet = datasetNode.getDataset();
            final String xicName = dataSet.getName() ;
            // call back for loading masterQuantChannel
            AbstractDatabaseCallback masterQuantChannelCallback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if (success) {
                        List<DMasterQuantitationChannel> listMasterQuantChannels = datasetNode.getDataset().getMasterQuantitationChannels();
                        if (listMasterQuantChannels != null && !listMasterQuantChannels.isEmpty()) {
                            Long masterQuantChannelId = new Long(listMasterQuantChannels.get(0).getId());
                            // CallBack for Xic Quantitation Service
                            AbstractServiceCallback xicCallback = new AbstractServiceCallback() {

                                @Override
                                public boolean mustBeCalledInAWT() {
                                    return true;
                                }

                                @Override
                                public void run(boolean success) {
                                    if (success) {
                                    }
                                    node.setIsChanging(false);
                                    treeModel.nodeChanged(node);
                                }
                            };

                            ComputeQuantProfileTask task = new ComputeQuantProfileTask(xicCallback, pID, masterQuantChannelId, quantParams, xicName);
                            AccessServiceThread.getAccessServiceThread().addTask(task);
                        }
                    }
                }
            };
            DatabaseDataSetTask loadTask = new DatabaseDataSetTask(masterQuantChannelCallback);
            loadTask.initLoadQuantitation(dataSet);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(loadTask);

        } //End OK entered         
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

        // must be a quantitation XIC
        Dataset.DatasetType datasetType = ((DataSetData) datasetNode.getData()).getDatasetType();
        if (datasetType != Dataset.DatasetType.QUANTITATION) {
            setEnabled(false);
            return;
        }

        DDataset d = ((DataSetData) datasetNode.getData()).getDataset();
        QuantitationMethod quantitationMethod = d.getQuantitationMethod();
        if (quantitationMethod == null) {
            setEnabled(false);
            return;
        }

        if (quantitationMethod.getAbundanceUnit().compareTo("feature_intensity") != 0) { // XIC
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }
}
