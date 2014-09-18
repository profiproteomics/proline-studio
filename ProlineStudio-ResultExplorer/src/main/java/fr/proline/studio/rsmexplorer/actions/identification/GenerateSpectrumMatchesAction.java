package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.GenerateSpectrumMatchTask;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;

/**
 * Action to Merge data from a set of Search Results (rset) or Identification Summaries (rsm)
 * @author JM235353
 */
public class GenerateSpectrumMatchesAction extends AbstractRSMAction {

    public GenerateSpectrumMatchesAction() {
        super(NbBundle.getMessage(GenerateSpectrumMatchesAction.class, "CTL_GenerateSpectrumMatchesAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

   
    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {
        
        IdentificationTree tree = IdentificationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        
        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            final DataSetNode node = (DataSetNode) selectedNodes[i];
            node.setIsChanging(true);
            treeModel.nodeChanged(node);
            
            AbstractServiceCallback callback = new AbstractServiceCallback() {

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
            // TODO : if resultSummaryId != null open a dialog to choose between generate spectrum matches for the whole resultSet or only RSM
            Long resultSetId = dataset.getResultSetId();
            GenerateSpectrumMatchTask task = new GenerateSpectrumMatchTask(callback, dataset.getName(), projectId, resultSetId, resultSummaryId, null);
            AccessServiceThread.getAccessServiceThread().addTask(task);
        }
        
        
    }


    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

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

            DataSetNode datasetNode = (DataSetNode) node;

            if (!datasetNode.isLeaf()) {
                setEnabled(false);
                return;
            }
        }
        setEnabled(true);
    }
}
