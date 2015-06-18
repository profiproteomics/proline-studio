package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MB243701
 */
public class DisplayQuantiAction extends AbstractRSMAction {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    public DisplayQuantiAction() {
        super(NbBundle.getMessage(CreateXICAction.class, "CTL_DisplayQuanti"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {
        final List<Long> listIds = new ArrayList();

        final int nbDataToLoad = QuantitationTree.getCurrentTree().getQuantitationChildCount();
        final int nbSelectedNodes = selectedNodes.length;
        QuantitationTree.getCurrentTree().clearQuantiSelection();
        // load data for properties
        DataLoadedCallback dataLoadedCallback = new DataLoadedCallback(nbDataToLoad) {

            @Override
            public void run() {
                for (int j = 0; j < nbDataToLoad; j++) {
                    AbstractNode qnode = QuantitationTree.getCurrentTree().getQuantitationNode(j);
                    if (qnode != null) {
                        DataSetNode dsnode = (DataSetNode) qnode;
                        if (dsnode.getDataset().getMasterQuantitationChannels() != null) {
                            int nb = dsnode.getDataset().getMasterQuantitationChannels().size();
                            for (int k = 0; k < nb; k++) {
                                DMasterQuantitationChannel mqc = dsnode.getDataset().getMasterQuantitationChannels().get(k);
                                List<DQuantitationChannel> listQC = mqc.getQuantitationChannels();
                                if (listQC != null) {
                                    int nbQC = listQC.size();
                                    for (int q = 0; q < nbQC; q++) {
                                        DQuantitationChannel qc = listQC.get(q);
                                        for (int i = 0; i < nbSelectedNodes; i++) {
                                            AbstractNode node = selectedNodes[i];
                                            if (node instanceof DataSetNode) {
                                                DataSetNode dsNode = (DataSetNode) node;
                                                DDataset ds = dsNode.getDataset();
                                                final Long rsmId = ds.getResultSummaryId();
                                                if (rsmId != null) {
                                                    if (rsmId.longValue() == qc.getIdentResultSummaryId()) {
                                                        listIds.add(dsnode.getDataset().getId());
                                                    }
                                                }
                                            }
                                        }
                                        
                                    }
                                }
                            }
                        }
                    }
                }
                QuantitationTree.getCurrentTree().selectQuantiNodeWithId(listIds);
            }
        };

        for (int j = 0; j < nbDataToLoad; j++) {
            AbstractNode qnode = QuantitationTree.getCurrentTree().getQuantitationNode(j);
            if (qnode != null) {
                qnode.loadDataForProperties(dataLoadedCallback);
            }
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

            DataSetNode datasetNode = (DataSetNode) node;

            if (!datasetNode.isLeaf()) {
                setEnabled(false);
                return;
            } else {
                if (!datasetNode.hasResultSummary()) {
                    setEnabled(false);
                    return;
                }
            }

        }
        setEnabled(true);
    }

    public abstract class DataLoadedCallback implements Runnable {
        private int m_nbData;
        public DataLoadedCallback(int nb) {
            m_nbData = nb;
        }

        @Override
        public abstract void run();
        

    }
}
