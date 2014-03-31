package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.dto.DDataset;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.dialog.AddAggregateDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMProjectNode;
import fr.proline.studio.rsmexplorer.node.IdentificationTree;
import java.util.ArrayList;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Add Aggregate action
 * @author JM235353
 */
public class AggregateAction extends AbstractRSMAction {

    
    public AggregateAction() {
        super(NbBundle.getMessage(AggregateAction.class, "CTL_AggregateAction"));
    }

    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {


        
        AddAggregateDialog dialog = AddAggregateDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
        if (dialog.getButtonClicked() == AddAggregateDialog.BUTTON_OK) {
            
            // retrieve parameters
            String name = dialog.getAggregateName();
            final int nbAggregates = dialog.getNbAggregates();
            Aggregation.ChildNature aggregateType = dialog.getAggregateType();
            
            int nbNodes = selectedNodes.length;
                  
            for (int iNode=0;iNode<nbNodes;iNode++) {
        
                final RSMNode n = selectedNodes[iNode];
            
                boolean isParentAProject = (n.getType() == RSMNode.NodeTypes.PROJECT);

                // check if a child has already the same name with a number suffix
                int suffixNumber = 0;
                int nbChildren = n.getChildCount();
                for (int i = 0; i < nbChildren; i++) {
                    RSMNode child = (RSMNode) n.getChildAt(i);
                    String childName = child.toString();

                    if (childName.startsWith(name)) {
                        String suffix = childName.substring(name.length());
                        try {
                            int number = Integer.parseInt(suffix);
                            if (number > suffixNumber) {
                                suffixNumber = number;
                            }
                        } catch (NumberFormatException nfe) {
                            // can happen not a bug
                        }
                    }
                }

                if ((suffixNumber == 0) && (nbAggregates > 1)) {
                    suffixNumber = 1;
                } else if (suffixNumber > 0) {
                    suffixNumber++;
                }
                int suffixStart = suffixNumber;

                final ArrayList<RSMDataSetNode> nodesCreated = new ArrayList<>();

                final IdentificationTree tree = IdentificationTree.getCurrentTree();
                final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                for (int i = 0; i < nbAggregates; i++) {
                    String aggregateName = name;
                    if (suffixNumber > 0) {
                        aggregateName += suffixNumber;
                        suffixNumber++;
                    }
                    RSMDataSetNode datasetNode = new RSMDataSetNode(new DataSetData(aggregateName, Dataset.DatasetType.AGGREGATE, aggregateType));
                    nodesCreated.add(datasetNode);
                    datasetNode.setIsChanging(true);
                    
                    if (isParentAProject) {
                        n.insert(datasetNode, n.getChildCount()-1);
                    } else {
                        n.add(datasetNode);
                    }
                }
                treeModel.nodeStructureChanged(n);

                // expand the parent node to display its children
                tree.expandNodeIfNeeded(n);



                // add to really create the aggregate dataset

                Project project = null;
                DDataset parentDataset = null;
                RSMDataSetNode parentDatasetNode = null;
                if (n.getType() == RSMNode.NodeTypes.PROJECT) {
                    project = ((RSMProjectNode) n).getProject();
                    parentDataset = null;
                } else if (n.getType() == RSMNode.NodeTypes.DATA_SET) {
                    parentDatasetNode = ((RSMDataSetNode) n);
                    parentDataset = parentDatasetNode.getDataset();
                    project = parentDataset.getProject();
                }

                final ArrayList<DDataset> createdDatasetList = new ArrayList<>();

                        
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                        if (!success) {
                            return; // should not happen
                        }

                        int nbNodes = nodesCreated.size();
                        for (int i = 0; i < nbNodes; i++) {

                            RSMDataSetNode datasetNode = nodesCreated.get(i);
                            DDataset dataset = createdDatasetList.get(i);
                            datasetNode.setIsChanging(false);
                            ((DataSetData) datasetNode.getData()).setDataset(dataset);
                            treeModel.nodeChanged(datasetNode);
                        }

                    }
                };

                // ask asynchronous loading of data



                DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                task.initCreateDatasetAggregate(project, parentDataset, aggregateType, name, (suffixNumber > 0), suffixStart, suffixStart + nbAggregates - 1, createdDatasetList);
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            }
            }
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        
        int nbSelectedNodes = selectedNodes.length;
        
        if (nbSelectedNodes == 0) {
            setEnabled(false);
            return;
        }
        
        for (int i=0;i<nbSelectedNodes;i++) {
            RSMNode node = selectedNodes[i];
            
            // parent node is being created, we can not add an identification
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }
                
            // we can always add an aggregate directly to a project
            if (node.getType() == RSMNode.NodeTypes.PROJECT) {
                continue;
            }
            
            // we can add an aggregate only to a data set without a ResultSet or a ResultSummary
            if (node.getType() == RSMNode.NodeTypes.DATA_SET) {
                
                DDataset d = ((RSMDataSetNode) node).getDataset();
                if (d.getType() != Dataset.DatasetType.AGGREGATE) {
                    setEnabled(false);
                return;
                }

            }
            
        }

        setEnabled(true);
 
    }
}