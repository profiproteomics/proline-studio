/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.RetrieveBioSeqTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class RetrieveBioSeqJMSAction extends AbstractRSMAction {
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    public RetrieveBioSeqJMSAction(AbstractTree.TreeType selectedTree) {
        super(NbBundle.getMessage(RetrieveBioSeqJMSAction.class, "CTL_RetrieveBioSeq"), selectedTree);
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }

        // note : we can ask for the retrieve bioSequence on multiple ResultSummaries in one time
        int nbSelectedNodes = selectedNodes.length;
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            // parent node is being created, we can not validate it (for the moment)
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            // parent node must be a dataset
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            // parent node must have a ResultSummary
            DataSetNode dataSetNode = (DataSetNode) node;
            if (!dataSetNode.hasResultSummary()) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
       
        AbstractTree.TreeType source = getSourceTreeType();
        AbstractTree tree;
        switch (source) {
            case TREE_IDENTIFICATION:
                tree = IdentificationTree.getCurrentTree(); 
                break;
            case TREE_QUANTITATION:
                tree = QuantitationTree.getCurrentTree(); 
                break;
            default:
                m_logger.warn("Unexpected source tree for this action ! "+source.name());
                return; 
        }
        
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        List<DataSetNode> datasets = new ArrayList<>();
        List<Long> rsmIds = new ArrayList<>();

        int nbNodes = selectedNodes.length;
        final List<ResultSummary> rsms2Clean = new ArrayList<>();

        for (int i = 0; i < nbNodes; i++) {
            final DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];
            datasets.add(dataSetNode);
            dataSetNode.setIsChanging(true);
            treeModel.nodeChanged(dataSetNode);

            final DDataset d = dataSetNode.getDataset();
            ResultSummary rsm = d.getResultSummary();
            if(rsm != null)
                rsms2Clean.add(rsm);
            rsmIds.add(d.getResultSummaryId());
        }

        Long projectId = ((DataSetNode) selectedNodes[0]).getDataset().getProject().getId();

        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                IdentificationTree tree = IdentificationTree.getCurrentTree();
                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                for (DataSetNode dataSetNode : datasets) {
                    dataSetNode.setIsChanging(false);
                    treeModel.nodeChanged(dataSetNode);
                }
                
                for (ResultSummary rsm : rsms2Clean)
                    rsm.getTransientData().setProteinSetArray(null);

            }
        };

        RetrieveBioSeqTask task = new RetrieveBioSeqTask(callback,rsmIds, projectId, true);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

    }

}
