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
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.SpectralCountTask;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.spectralcount.SpectralCountDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    private boolean m_isJMSDefined;

    public SpectralCountAction(boolean isJMSDefined) {
        super(NbBundle.getMessage(SpectralCountAction.class, "CTL_CompareWithSCAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
        this.m_isJMSDefined = isJMSDefined;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, final int x, final int y) {

        final DataSetNode refDatasetNode = (DataSetNode) selectedNodes[0];

        /*DDataset.MergeInformation mergeInfo = refDatasetNode.getDataset().getMergeInformation();
         if (mergeInfo.compareTo(DDataset.MergeInformation.MERGE_IDENTIFICATION_SUMMARY) != 0) {
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

                // check if we can compute SC
                String error = null;
                Map<String, Object> params = new HashMap();
                ArrayList<DDataset> datasetList = new ArrayList<>();
                datasetList.add(refDatasetNode.getDataset()); //first entry is Reference Dataset in data box !

                if (spectralCountDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    ArrayList<DataSetNode> selectedDSNodes = spectralCountDialog.getSelectedRSMDSNodeList();
                    //A Voir :) 
//                   if(selectedDSNodes.contains(refDatasetNode) && selectedDSNodes.size()>1) 
//                        error = " Spectral Count is not possible on reference Node and some of its child";

                    for (DataSetNode dsNode : selectedDSNodes) {
                        //TODO : Verif pas père + fils sélectionnés : Que père ou que fils ?!? 
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
                    params.put(DS_LIST_PROPERTIES, datasetList);

                    ArrayList<DDataset> weightDatasetList = new ArrayList<>();
                    ArrayList<DataSetNode> selectedWeightDSNodes = spectralCountDialog.getSelectedWeightRSMDSNodeList();
                    for (DataSetNode dsNode : selectedWeightDSNodes) {
                        weightDatasetList.add(dsNode.getDataset());
                    }
                    params.put(DS_WEIGHT_LIST_PROPERTIES, weightDatasetList);

                    params.put(DS_NAME_PROPERTIES, spectralCountDialog.getSpectralCountName());
                    params.put(DS_DESCRIPTION_PROPERTIES, spectralCountDialog.getSpectralCountDescription());

                } else { //Cancel / Close was clicked
                    return;
                }

                if (error != null) {
                    JOptionPane.showMessageDialog(IdentificationTree.getCurrentTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                m_logger.debug(" Will Compute SC on " + (datasetList.size()) + " RSMs : " + datasetList);

                /*  WindowBox wbox = WindowBoxFactory.getRsmWSCWindowBox((String)params.get(DS_NAME_PROPERTIES), (String)params.get(DS_NAME_PROPERTIES)+" WSC", false) ;
                 wbox.setEntryData(refDatasetNode.getDataset().getProject().getId(), params);

                 // open a window to display the window box
                 DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                 win.open();
                 win.requestActive(); */
                // Used in acse of computing SC
                final Long[] _quantiDatasetId = new Long[1];
                QuantitationTree tree = QuantitationTree.getCurrentTree();
                final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                final DataSetNode[] _quantitationNode = new DataSetNode[1];

                if (!Map.class.isAssignableFrom(params.getClass())) {
                    throw new IllegalArgumentException("Specified parameter (" + params + ") should be a Map.");
                }

                ArrayList<DDataset> datasetArray = (ArrayList) ((Map) params).get(SpectralCountAction.DS_LIST_PROPERTIES);
                DDataset refDataset = datasetArray.get(0);
                int nb = datasetArray.size() - 1;
                ArrayList<DDataset> datasetRsms = new ArrayList<>(nb);
                for (int i = 1; i <= nb; i++) {
                    datasetRsms.add(datasetArray.get(i));
                }

                String qttDSName = (String) ((Map) params).get(SpectralCountAction.DS_NAME_PROPERTIES);
                String qttDSDescr = (String) ((Map) params).get(SpectralCountAction.DS_DESCRIPTION_PROPERTIES);
                ArrayList<DDataset> datasetWeightRsms = (ArrayList) ((Map) params).get(SpectralCountAction.DS_WEIGHT_LIST_PROPERTIES);
                String[] _spCountJSON = new String[1];

                // add node for the quantitation dataset which will be created
                DataSetData quantitationData = new DataSetData(qttDSName, Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION);

                final DataSetNode quantitationNode = new DataSetNode(quantitationData);
                _quantitationNode[0] = quantitationNode;
                quantitationNode.setIsChanging(true);

                AbstractNode rootNode = (AbstractNode) treeModel.getRoot();
                // before Trash
                treeModel.insertNodeInto(quantitationNode, rootNode, rootNode.getChildCount() - 1);

                // expand the parent node to display its children
                tree.expandNodeIfNeeded(rootNode);

                if (m_isJMSDefined) {
                    // CallBack for SC  Service
                    AbstractJMSCallback scCallback = new AbstractJMSCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success) {
                            runQuantifySC(success, _quantiDatasetId[0], _quantitationNode[0], treeModel);
                        }
                    };
                    fr.proline.studio.dpm.task.jms.SpectralCountTask task = new fr.proline.studio.dpm.task.jms.SpectralCountTask(scCallback, refDatasetNode.getDataset(), datasetRsms, datasetWeightRsms, qttDSName, qttDSDescr, _quantiDatasetId, _spCountJSON);
                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                } else {
                    // CallBack for SC  Service
                    AbstractServiceCallback scCallback = new AbstractServiceCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success) {
                            runQuantifySC(success, _quantiDatasetId[0], _quantitationNode[0], treeModel);
                        }
                    };

                    SpectralCountTask task = new SpectralCountTask(scCallback, refDatasetNode.getDataset(), datasetRsms, datasetWeightRsms, qttDSName, qttDSDescr, _quantiDatasetId, _spCountJSON);
                    AccessServiceThread.getAccessServiceThread().addTask(task);
                }
            }
        };

        spectralCountDialog.setVisible(true);

        IdentificationTree.getCurrentTree().loadInBackground(refDatasetNode, callback);

    }

    private void runQuantifySC(boolean success, Long quantiDsId, final DataSetNode quantitationNode, final DefaultTreeModel treeModel) {
        if (success) {
            m_logger.debug(" SC SUCCESS : " +quantiDsId );
            final ArrayList<DDataset> readDatasetList = new ArrayList<>(1);

            AbstractDatabaseCallback readDatasetCallback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if (success) {
                        final DDataset ds = readDatasetList.get(0);
                        AbstractDatabaseCallback loadQCallback = new AbstractDatabaseCallback() {
                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                ((DataSetData) quantitationNode.getData()).setDataset(ds);
                                quantitationNode.setIsChanging(false);
                                treeModel.nodeChanged(quantitationNode);
                            }
                        };
                        DatabaseDataSetTask loadQTask = new DatabaseDataSetTask(loadQCallback);
                        loadQTask.initLoadQuantitation(ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject(), ds);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(loadQTask);
                    } else {
                        treeModel.removeNodeFromParent(quantitationNode);
                    }
                }
            };

            DatabaseDataSetTask task = new DatabaseDataSetTask(readDatasetCallback);
            task.initLoadDataset(quantiDsId, readDatasetList);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        } else {
            m_logger.debug(" SC ERROR ");
            treeModel.removeNodeFromParent(quantitationNode);
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

        Dataset.DatasetType datasetType = ((DataSetData) datasetNode.getData()).getDatasetType();
        if (datasetType != Dataset.DatasetType.AGGREGATE) {
            setEnabled(false);
            return;
        }

        if (!datasetNode.hasResultSummary()) {
            setEnabled(false);
            return;
        }

        DDataset.MergeInformation mergeInfo = datasetNode.getDataset().getMergeInformation();
        if (mergeInfo.compareTo(DDataset.MergeInformation.MERGE_IDENTIFICATION_SUMMARY) != 0) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }

}
