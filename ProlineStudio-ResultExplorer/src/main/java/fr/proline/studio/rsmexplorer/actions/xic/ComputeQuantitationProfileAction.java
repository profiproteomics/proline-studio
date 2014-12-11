/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ComputeQuantProfileTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
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
        final int posx = x;
        final int posy = y;
        // only on one node
        final AbstractNode node = selectedNodes[0];
        final DataSetNode datasetNode = (DataSetNode) selectedNodes[0];
        //dataset
        final DDataset dataSet = datasetNode.getDataset();
        
        // call back for loading masterQuantChannel
        AbstractDatabaseCallback masterQuantChannelCallback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (success) {
                    // check if profilizer has already been launched
                    if (dataSet.getPostQuantProcessingConfig() != null) {
                        String message = "Compute Quantitation Profile has already been launched.\nDo you want to relaunch it?";
                        OptionDialog yesNoDialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Compute Quant. Profile", message);
                        yesNoDialog.setLocation(posx, posy);
                        yesNoDialog.setVisible(true);
                        if (yesNoDialog.getButtonClicked() != DefaultDialog.BUTTON_OK) {
                            return;
                        }
                    }    
                    
                    // dialog with the parameters for quantitation profiler
                    QuantProfileXICDialog dialog = QuantProfileXICDialog.getDialog(WindowManager.getDefault().getMainWindow());
                    dialog.setLocation(posx, posy);
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
                        node.setIsChanging(true);
                        QuantitationTree tree = QuantitationTree.getCurrentTree();
                        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                        treeModel.nodeChanged(node);
                        //dataset
                        final String xicName = dataSet.getName() ;
                        
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
                    } //End OK entered   
                }
            }
        };
                
        DatabaseDataSetTask loadTask = new DatabaseDataSetTask(masterQuantChannelCallback);
        loadTask.initLoadQuantitation(ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject(), dataSet);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(loadTask);
          
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }
        
        
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
        if (! datasetNode.isQuantXIC()) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }
}
