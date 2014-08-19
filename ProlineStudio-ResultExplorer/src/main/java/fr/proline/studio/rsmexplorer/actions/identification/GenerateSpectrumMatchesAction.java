package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.GenerateSpectrumMatchTask;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
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
        
        final DataSetNode node = (DataSetNode) selectedNodes[0];
        final DDataset dataset = node.getDataset();
        Long projectId = dataset.getProject().getId();
        Long resultSummaryId = (dataset.getResultSummary() != null) ? dataset.getResultSummary().getId() : null;
        // TODO : if resultSummaryId != null open a dialog to choose between generate spectrum matches for the whole resultSet or only RSM
        Long resultSetId = dataset.getResultSetId();
        GenerateSpectrumMatchTask task = new GenerateSpectrumMatchTask(null, projectId, resultSetId, resultSummaryId, null);
        AccessServiceThread.getAccessServiceThread().addTask(task);
    }


    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

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

        if (!datasetNode.isLeaf()) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }
}
