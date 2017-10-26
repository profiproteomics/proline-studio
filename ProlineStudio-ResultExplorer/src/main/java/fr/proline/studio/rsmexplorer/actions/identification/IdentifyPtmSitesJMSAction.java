/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.IdentifyPtmSitesTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * action to generate spectrum matches via JMS 
 * @author MB243701
 */
public class IdentifyPtmSitesJMSAction extends AbstractRSMAction {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    public IdentifyPtmSitesJMSAction(AbstractTree.TreeType selectedTree) {
        super(NbBundle.getMessage(IdentifyPtmSitesJMSAction.class, "CTL_IdentifyPtmSitesAction"), selectedTree);
    }
    
    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {
        
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
        
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        
        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            final DataSetNode node = (DataSetNode) selectedNodes[i];
            node.setIsChanging(true);
            treeModel.nodeChanged(node);
            
            AbstractJMSCallback callback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    node.setIsChanging(false);
                    treeModel.nodeChanged(node);
                }
            };
            
            final DDataset dataset = node.getDataset();
            Long projectId = dataset.getProject().getId();
            Long resultSummaryId = dataset.getResultSummaryId();
            IdentifyPtmSitesTask task = new IdentifyPtmSitesTask(callback, dataset.getName(), projectId, resultSummaryId, null);
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
            
            DataSetNode dataSetNode = (DataSetNode) node;
            if (! dataSetNode.hasResultSet()) {
                setEnabled(false);
                return;
            }


        }
        setEnabled(true);
    }
    
}
