package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Dataset;
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
import fr.proline.studio.dpm.task.jms.RunXICTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.CreateXICDialog;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.ChildFactory;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
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

    private boolean m_isXiCClone = false;
    private boolean m_referenceDataSetDefined = false;

    static String getMessage(boolean fromExistingXIC, AbstractTree.TreeType sourceTree) {
        if (fromExistingXIC) {
            return NbBundle.getMessage(CreateXICAction.class, "CTL_CreateXIC_Clone");
        } else if (sourceTree.equals(AbstractTree.TreeType.TREE_QUANTITATION)) {
            return NbBundle.getMessage(CreateXICAction.class, "CTL_CreateXIC");
        } else {
            return NbBundle.getMessage(CreateXICAction.class, "CTL_CreateXIC_Ident");
        }
    }

    public CreateXICAction(boolean fromExistingXIC) {
        super(CreateXICAction.getMessage(fromExistingXIC, AbstractTree.TreeType.TREE_QUANTITATION), AbstractTree.TreeType.TREE_QUANTITATION);
        m_isXiCClone = fromExistingXIC;
    }

    public CreateXICAction(AbstractTree.TreeType sourceTree) {
        super(CreateXICAction.getMessage(false, sourceTree), sourceTree);
        m_isXiCClone = false;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        if (ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject() == null) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "A project should be selected !", "Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (m_isXiCClone) {
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
            AbstractNode n = selectedNodes[0];
            DDataset refDataset = null;
            if (!n.isRoot() && DataSetNode.class.isInstance(n)) {
                DataSetNode node = (DataSetNode) n;
                DataSetData dataset = (DataSetData) node.getData();
                Dataset.DatasetType dsType = dataset.getDatasetType();
                if (node.hasResultSummary() && (dsType.equals(Dataset.DatasetType.IDENTIFICATION) || dsType.equals(Dataset.DatasetType.AGGREGATE))) {
                    m_referenceDataSetDefined = true;
                    refDataset = dataset.getDataset();
                } else {
                    m_referenceDataSetDefined = false;
                }
            }
            createXICDialog(refDataset, x, y);
        }
    }

    private void createXICDialog(DDataset dataset, int x, int y) {
        Long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
        CreateXICDialog dialog = CreateXICDialog.getDialog(WindowManager.getDefault().getMainWindow());
        if (m_referenceDataSetDefined) {
            IdentificationTree childTree = IdentificationTree.getCurrentTree().copyDataSetRootSubTree(dataset, dataset.getProject().getId());
            dialog.setSelectableIdentTree(childTree);
            dialog.setReferenceIdentDataset(dataset);
        } else if (m_isXiCClone && dataset.getMasterQuantitationChannels().get(0).getIdentDataset() != null) {
            DDataset refDataset = dataset.getMasterQuantitationChannels().get(0).getIdentDataset();         
            dialog.setReferenceIdentDataset(refDataset);
            IdentificationTree childTree = IdentificationTree.getCurrentTree().copyDataSetRootSubTree(refDataset, refDataset.getProject().getId());
            if (childTree == null) {
                m_logger.debug("Reference dataset not already loaded: creates a new node to dispay in a new IdentificationTree");
                childTree = new IdentificationTree(ChildFactory.createNode(new DataSetData(refDataset)));
            }
            dialog.setSelectableIdentTree(childTree);
        } else {
            dialog.setSelectableIdentTree(null);
            dialog.setReferenceIdentDataset(null);
        }

        dialog.displayExperimentalDesignTree();
        if (m_isXiCClone) {
            dialog.copyClonedDesignTree(dataset);
        }

        dialog.setLocation(x, y);
        dialog.setVisible(true);
        final Long[] _xicQuantiDataSetId = new Long[1];

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            //User specified experimental design dataset
            DataSetData _quantiDS = null;
            Map<String, Object> expParams = null;

            StringBuffer errorMsg = new StringBuffer("");

            Map<String, Object> quantParams = dialog.getQuantiParameters();
            if (quantParams == null) {
                errorMsg.append("Null Quantitation parameters !  ");
            }

            if (dialog.getExperimentalDesignNode() == null) {
                errorMsg.append("No experimental design defined  ");

            } else if (!DataSetData.class.isInstance(dialog.getExperimentalDesignNode().getData())) {
                errorMsg.append("Invalide Quantitation Dataset specified  ");

            } else {

                String registerErrMsg = dialog.registerRawFiles();
                if(registerErrMsg != null && !registerErrMsg.isEmpty())
                    errorMsg.append( registerErrMsg);

                //*** Get experimental design values                
                _quantiDS = (DataSetData) dialog.getExperimentalDesignNode().getData();

                try {
                    expParams = dialog.getExperimentalDesignParameters();
                } catch (IllegalAccessException iae) {
                    errorMsg.append(iae.getMessage());
                }
            }

            if (!errorMsg.toString().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, errorMsg, "Warning", JOptionPane.ERROR_MESSAGE);
                return;
            }

            m_logger.debug(" Will Compute XIC Quanti with on " + ((List) expParams.get("biological_samples")).size() + "samples.");

            final QuantitationTree tree = QuantitationTree.getCurrentTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            final DataSetNode[] _quantitationNode = { QuantitationTree.getCurrentTree().createQuantitationNode(_quantiDS.getName()) };
            
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
           
            RunXICTask task = new RunXICTask(xicCallback, pID, _quantiDS.getName(), quantParams, expParams, dialog.getParamVersion(), _xicQuantiDataSetId);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
           
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

        if (m_isXiCClone) {
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
