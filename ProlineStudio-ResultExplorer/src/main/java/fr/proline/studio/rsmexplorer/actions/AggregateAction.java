package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.dialog.AddAggregateDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMProjectNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.util.ArrayList;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class AggregateAction extends AbstractRSMAction {

    
    public AggregateAction() {
        super(NbBundle.getMessage(AggregateAction.class, "CTL_AggregateAction"));
    }

    
    @Override
    public void actionPerformed(RSMNode n, int x, int y) {

        AddAggregateDialog dialog = AddAggregateDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
        if (dialog.getButtonClicked() == AddAggregateDialog.BUTTON_OK) {
            
            // retrieve parameters
            String name = dialog.getAggregateName();
            final int nbAggregates = dialog.getNbAggregates();
            int aggregateType = dialog.getAggregateType();
            
            // check if a child has already the same name with a number suffix
            int suffixNumber = 0;
            int nbChildren = n.getChildCount();
            for (int i=0;i<nbChildren;i++) {
                RSMNode child = (RSMNode) n.getChildAt(i);
                String childName = child.toString();
                
                if (childName.startsWith(name)) {
                    String suffix = childName.substring(name.length());
                    try {
                        int number = Integer.parseInt(suffix);
                        if (number>suffixNumber) {
                            suffixNumber = number;
                        }
                    } catch (NumberFormatException nfe) {
                        // can happen not a bug
                    }
                }
            }
            
            if ((suffixNumber == 0) && (nbAggregates>1)) {
                suffixNumber = 1;
            } else if (suffixNumber >0) {
                suffixNumber++;
            }
            int suffixStart = suffixNumber;
            
            final ArrayList<RSMDataSetNode> nodesCreated = new ArrayList<RSMDataSetNode>();
            
            RSMTree tree = RSMTree.getTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            for (int i=0;i<nbAggregates;i++) {
                String aggregateName = name;
                if (suffixNumber>0) {
                    aggregateName += suffixNumber;
                    suffixNumber++;
                }
                RSMDataSetNode datasetNode = new RSMDataSetNode(new DataSetData(aggregateName));
                nodesCreated.add(datasetNode);
                datasetNode.setIsChanging(true);
                n.add(datasetNode);
            }
            treeModel.nodeStructureChanged(n);
            

            // add to really create the aggregate dataset
            
            Integer projectId = null;
            Integer parentDatasetId = null;;
            if (n.getType() == RSMNode.NodeTypes.PROJECT) {
                projectId = ((RSMProjectNode)n).getProject().getId();
                parentDatasetId = null; 
            } else if (n.getType() == RSMNode.NodeTypes.DATA_SET) {
                DataSetTMP parentDataset = ((RSMDataSetNode)n).getDataSet();
                projectId = parentDataset.getProjectId();
                parentDatasetId = parentDataset.getId();
            }
            
            final ArrayList<DataSetTMP> createdDatasetList = new ArrayList<>();
            
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask) {
                    
                    if (!success) {
                        return; // should not happen
                    }
                    int nbNodes = nodesCreated.size();
                    for (int i = 0; i < nbNodes; i++) {
                        
                        RSMDataSetNode datasetNode = nodesCreated.get(i);
                        DataSetTMP dataset = createdDatasetList.get(i);
                        datasetNode.setIsChanging(false);
                        ((DataSetData)datasetNode.getData()).setDataSet(dataset);
                        treeModel.nodeChanged(datasetNode);
                    }
                    
                }
            };

            // ask asynchronous loading of data
            
            
            
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initCreateDatasetAggregate(projectId, parentDatasetId,aggregateType,name,(suffixNumber>0), suffixStart, suffixStart+nbAggregates-1, createdDatasetList);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        setEnabled(true);  //JPM.TODO

    }
}