package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.SpectralCountTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.spectralcount.SpectralCountDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.util.ArrayList;
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
public class SpectralCountAction extends AbstractRSMAction {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    final static public String DS_NAME_PROPERTIES = "dsName";
    final static public String DS_DESCRIPTION_PROPERTIES = "dsDescription";
    final static public String DS_LIST_PROPERTIES = "dsList";
    final static public String DS_WEIGHT_LIST_PROPERTIES = "dsWeightList";


    public SpectralCountAction() {
        super(NbBundle.getMessage(SpectralCountAction.class, "CTL_CompareWithSCAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);

    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, final int x, final int y) {

        final DataSetNode refDatasetNode = (DataSetNode) selectedNodes[0];

        /*DDataset.AggregationInformation mergeInfo = refDatasetNode.getDataset().getAggregationInformation();
         if (mergeInfo.compareTo(DDataset.AggregationInformation.MERGE_IDENTIFICATION_SUMMARY) != 0) {
         InfoDialog exitDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Warning", "Spectral Count on a Merge of Search Results has not been tested.\nDo you want to proceed ?");
         exitDialog.setButtonName(OptionDialog.BUTTON_OK, "Yes");
         exitDialog.setButtonName(OptionDialog.BUTTON_CANCEL, "No");
         exitDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
         exitDialog.setVisible(true);

         if (exitDialog.getButtonClicked() == OptionDialog.BUTTON_CANCEL) {
         // No clicked
         return;
         }
         }*/
        //Create Child Tree to select RSM to compute SC for
        final IdentificationTree childTree = IdentificationTree.getCurrentTree().copyDataSetRootSubTree(refDatasetNode.getDataset(), refDatasetNode.getDataset().getProject().getId());
        final SpectralCountDialog spectralCountDialog = new SpectralCountDialog(WindowManager.getDefault().getMainWindow(), childTree);

        spectralCountDialog.setLocation(x, y);

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                m_logger.debug(" Choose RSM for Weighted SC");

                String qttDSName = "";
                String qttDSDescr = "";
                ArrayList<DDataset> datasetList = new ArrayList<>();
                ArrayList<DDataset> weightDatasetList = new ArrayList<>();
                
                // check if we can compute SC
                String error = null;
                if (spectralCountDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    
                    ArrayList<DataSetNode> selectedDSNodes = spectralCountDialog.getSelectedRSMDSNodeList();
                    for (DataSetNode dsNode : selectedDSNodes) {
                        if (!dsNode.hasResultSummary()) {
                            error = " Spectral Count is not possible on Search result (" + dsNode.getDataset().getName() + ").  Identification Summary should be created first";
                            break;
                        }
                        if (dsNode.isChanging()) {
                            error = " Spectral Count is not possible while import or validation is on going. (Search result " + dsNode.getDataset().getName() + ")";
                            break;
                        }
                        datasetList.add(dsNode.getDataset());
                    }

                    ArrayList<DataSetNode> selectedWeightDSNodes = spectralCountDialog.getSelectedWeightRSMDSNodeList();
                    for (DataSetNode dsNode : selectedWeightDSNodes) {
                        weightDatasetList.add(dsNode.getDataset());
                    }

                    qttDSName = spectralCountDialog.getSpectralCountName();
                    qttDSDescr = spectralCountDialog.getSpectralCountDescription();

                } else { //Cancel / Close was clicked
                    return;
                }

                if (error != null) {
                    JOptionPane.showMessageDialog(IdentificationTree.getCurrentTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                m_logger.debug(" Will Compute SC on " + (datasetList.size()) + " RSMs : " + datasetList);
                
                final Long[] _quantiDatasetId = new Long[1];
                QuantitationTree tree = QuantitationTree.getCurrentTree();
                final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                final DataSetNode[] _quantitationNode = new DataSetNode[1];

                // add node for the quantitation dataset which will be created
                DataSetData quantitationData = DataSetData.createTemporaryQuantitation(qttDSName); //new DataSetData(qttDSName, Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION);
                final DataSetNode quantitationNode = new DataSetNode(quantitationData);
                _quantitationNode[0] = quantitationNode;
                quantitationNode.setIsChanging(true);

                AbstractNode rootNode = (AbstractNode) treeModel.getRoot();
                // before Trash
                treeModel.insertNodeInto(quantitationNode, rootNode, rootNode.getChildCount() - 1);
                // expand the parent node to display its children
                tree.expandNodeIfNeeded(rootNode);

                // CallBack for SC  Service
                AbstractJMSCallback scCallback = new AbstractJMSCallback() {
                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        if (success) {
                            m_logger.debug(" SC SUCCESS : " + _quantiDatasetId[0]);
                            QuantitationTree.getCurrentTree().loadDataSet(_quantiDatasetId[0], _quantitationNode[0]);

                        } else {
                            m_logger.debug(" SC ERROR ");
                            treeModel.removeNodeFromParent(_quantitationNode[0]);
                        }
                    }
                };
                SpectralCountTask task = new SpectralCountTask(scCallback, refDatasetNode.getDataset(), datasetList, weightDatasetList, qttDSName, qttDSDescr, _quantiDatasetId);
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

            }
        };

        spectralCountDialog.setVisible(true);
        IdentificationTree.getCurrentTree().loadInBackground(refDatasetNode, callback);
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }

        if (selectedNodes.length != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = (AbstractNode) selectedNodes[0];

        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        DataSetNode datasetNode = (DataSetNode) node;

        //VDS : Allow on Ident DS !!!??? Comment next part
//        Dataset.DatasetType datasetType = ((DataSetData) datasetNode.getData()).getDatasetType();
//        if (datasetType != Dataset.DatasetType.AGGREGATE) {
//            setEnabled(false);
//            return;
//        }

        if (!datasetNode.hasResultSummary()) {
            setEnabled(false);
            return;
        }

        //VDS: ALLOW On Merge RS- Comment next part
//        DDataset.AggregationInformation mergeInfo = datasetNode.getDataset().getAggregationInformation();
//        if (mergeInfo.compareTo(DDataset.AggregationInformation.MERGE_IDENTIFICATION_SUMMARY) != 0) {
//            setEnabled(false);
//            return;
//        }

        setEnabled(true);
    }

}
