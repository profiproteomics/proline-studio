package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.RunXICTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.CreateXICDialog;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.*;
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
public class CreateXICAction extends AbstractRSMAction {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private boolean m_fromExistingXIC = false;

    public CreateXICAction() {
        super(NbBundle.getMessage(CreateXICAction.class, "CTL_CreateXIC"), AbstractTree.TreeType.TREE_QUANTITATION);
        m_fromExistingXIC = false;
    }

    public CreateXICAction(boolean fromExistingXIC) {
        super(NbBundle.getMessage(CreateXICAction.class, "CTL_CreateXIC"), AbstractTree.TreeType.TREE_QUANTITATION);
        m_fromExistingXIC = true;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        if (ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject() == null) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "A project should be selected !", "Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (m_fromExistingXIC) {
            final DDataset currentDataset = ((DataSetData) ((DataSetNode) selectedNodes[0]).getData()).getDataset();
            final int posx = x;
            final int posy = y;
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    createXICDialog(currentDataset, posx, posy);
                }
            };
            DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
            task.initLoadQuantChannels(ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId(), currentDataset);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        } else {
            createXICDialog(null, x, y);
        }

    }

    private void createXICDialog(DDataset dataset, int x, int y) {
        Long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
        CreateXICDialog dialog = CreateXICDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.displayDesignTree();
        if (m_fromExistingXIC) {
            dialog.setDefaultDesignTree(dataset);
        }
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        final Long[] _xicQuantiDataSetId = new Long[1];

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            //User specified experimental design dataset
            DataSetData _quantiDS = null;
            Map<String, Object> expParams = null;

            String errorMsg = null;

            Map<String, Object> quantParams = dialog.getQuantiParameters();
            if (quantParams == null) {
                errorMsg = "Null Quantitation parameters !";
            }

            if (dialog.getDesignRSMNode() == null) {
                errorMsg = "No experimental design defined";

            } else if (!DataSetData.class.isInstance(dialog.getDesignRSMNode().getData())) {
                errorMsg = "Invalide Quantitation Dataset specified ";

            } else {

                dialog.registerRawFiles();

                //*** Get experimental design values                
                _quantiDS = (DataSetData) dialog.getDesignRSMNode().getData();

                try {
                    expParams = dialog.getDesignParameters();
                } catch (IllegalAccessException iae) {
                    errorMsg = iae.getMessage();
                }
            }

            if (errorMsg != null) {
                JOptionPane.showMessageDialog(dialog, errorMsg, "Warning", JOptionPane.ERROR_MESSAGE);
                return;
            }

            m_logger.debug(" Will Compute XIC Quanti with on " + ((List) expParams.get("biological_samples")).size() + "samples.");

            QuantitationTree tree = QuantitationTree.getCurrentTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            final DataSetNode[] _quantitationNode = new DataSetNode[1];

            // CallBack for Xic Quantitation Service
            AbstractServiceCallback xicCallback = new AbstractServiceCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    if (success) {
                        m_logger.debug(" XIC SUCCESS : " + _xicQuantiDataSetId[0]);
                        final ArrayList<DDataset> readDatasetList = new ArrayList<>(1);

                        AbstractDatabaseCallback readDatasetCallback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                if (success) {
                                    ((DataSetData) _quantitationNode[0].getData()).setDataset(readDatasetList.get(0));
                                    _quantitationNode[0].setIsChanging(false);
                                    treeModel.nodeChanged(_quantitationNode[0]);
                                } else {
                                    treeModel.removeNodeFromParent(_quantitationNode[0]);
                                }
                            }
                        };

                        DatabaseDataSetTask task = new DatabaseDataSetTask(readDatasetCallback);
                        task.initLoadDataset(_xicQuantiDataSetId[0], readDatasetList);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                    } else {
                        m_logger.debug(" XIC ERROR ");
                        treeModel.removeNodeFromParent(_quantitationNode[0]);
                    }
                }
            };

            RunXICTask task = new RunXICTask(xicCallback, pID, _quantiDS.getName(), quantParams, expParams, _xicQuantiDataSetId);

            // add node for the quantitation dataset which will be created
            DataSetData quantitationData = new DataSetData(_quantiDS.getName(), Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION);

            final DataSetNode quantitationNode = new DataSetNode(quantitationData);
            _quantitationNode[0] = quantitationNode;
            quantitationNode.setIsChanging(true);

            AbstractNode rootNode = (AbstractNode) treeModel.getRoot();
            // before trash
            treeModel.insertNodeInto(quantitationNode, rootNode, rootNode.getChildCount() - 1);

            // expand the parent node to display its children
            tree.expandNodeIfNeeded(rootNode);

            AccessServiceThread.getAccessServiceThread().addTask(task);

        } //End OK entered      
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }

        if (m_fromExistingXIC) {
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
            if (!datasetNode.isQuantXIC()) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);
    }

}
