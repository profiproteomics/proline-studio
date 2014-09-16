package fr.proline.studio.rsmexplorer.tree;

import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDataset.MergeInformation;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import fr.proline.studio.rsmexplorer.actions.identification.PropertiesAction;
import fr.proline.studio.utils.IconManager;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import org.openide.nodes.Sheet;



/**
 * Node for Dataset
 * @author JM235353
 */
public class DataSetNode extends AbstractNode {

   
    
    
    public DataSetNode(AbstractData data) {
        super(NodeTypes.DATA_SET, data);
    }

    @Override
    public ImageIcon getIcon() {
        
        DDataset dataset = ((DataSetData) getData()).getDataset();
        Dataset.DatasetType datasetType = ((DataSetData) getData()).getDatasetType();
        switch(datasetType) {
            case IDENTIFICATION:
                
                if (dataset != null) {
                    if (dataset.getResultSummaryId() == null) {
                        if (isChanging()) {
                            // will become a RSM
                            return getIcon(IconManager.IconType.DATASET_RSM); 
                        } else {
                            return getIcon(IconManager.IconType.DATASET_RSET);
                         }
                    } else {
                        return getIcon(IconManager.IconType.DATASET_RSM);
                    }
                } else {
                    return getIcon(IconManager.IconType.DATASET_RSET);
                }
            case QUANTITATION: {
                if (dataset == null || dataset.getQuantitationMethod() == null)
                    return getIcon(IconManager.IconType.QUANT_XIC);
                if (dataset.getQuantitationMethod().getAbundanceUnit().compareTo("feature_intensity")== 0) { // XIC
                    return getIcon(IconManager.IconType.QUANT_XIC);            
                } else if(dataset.getQuantitationMethod().getAbundanceUnit().compareTo("spectral_counts") == 0) { // Spectral count
                    return getIcon(IconManager.IconType.QUANT_SC);
                } else 
                    return getIcon(IconManager.IconType.QUANT);
            }
            case AGGREGATE:

                //Aggregation.ChildNature aggregateType = ((DataSetData) getData()).getAggregateType();
                //JPM.TODO : according to aggregateType type :icon must be different
                
                if (dataset != null) {
                    if (dataset.getResultSummaryId() != null) {
                        MergeInformation mergeInfo = ((DataSetData) getData()).getDataset().getMergeInformation();
                        if (mergeInfo.compareTo(MergeInformation.MERGE_IDENTIFICATION_SUMMARY) == 0) {
                            return getIcon(IconManager.IconType.DATASET_RSM_MERGED);
                        } else {
                            return getIcon(IconManager.IconType.DATASET_RSM_RSET_MERGED);
                        }
                    }
                    if (dataset.getResultSetId() != null) {
                        return getIcon(IconManager.IconType.DATASET_RSET_MERGED);
                    }
                }
                
                return getIcon(IconManager.IconType.DATASET);
            case TRASH:
                return getIcon(IconManager.IconType.TRASH);
            default:
                return getIcon(IconManager.IconType.QUANT);// sould not happen
                
        }
        
        //return null;

    }
    

    
    public DDataset getDataset() {
        return ((DataSetData) getData()).getDataset();
    }
    
    public void setDataset(DDataset dataset) {
        ((DataSetData) getData()).setDataset(dataset);
    }
    
    public boolean isTrash() {
        DDataset dataset = ((DataSetData) getData()).getDataset();
        if (dataset == null) {
            return false;
        }
        Dataset.DatasetType datasetType = ((DataSetData) getData()).getDatasetType();
        if (datasetType == Dataset.DatasetType.TRASH) {
            return true;
        }
        return false;
    }
    
    public boolean hasResultSummary() {
        DDataset dataSet = ((DataSetData) getData()).getDataset();
        return (dataSet.getResultSummaryId() != null);
    }
    

    public Long getResultSummaryId() {
        return ((DataSetData) getData()).getDataset().getResultSummaryId();
    }
    
    public ResultSummary getResultSummary() {
        // getResultSummary() can return null if the resultSummary has not been loaded previously
        DDataset dataSet = ((DataSetData) getData()).getDataset();
        return dataSet.getResultSummary();
    }
    
    
    public boolean hasResultSet() {
        DDataset dataSet = ((DataSetData) getData()).getDataset();
        return (dataSet.getResultSetId() != null);
    }
    
    public Long getResultSetId() {
        return ((DataSetData) getData()).getDataset().getResultSetId();
    }
    
    public ResultSet getResultSet() {
        // getResultSet() can return null if the resultSet has not been loaded previously
        DDataset dataSet = ((DataSetData) getData()).getDataset();
        return dataSet.getResultSet();
    }
    
    @Override
    public boolean isInTrash() {
        if (isTrash()) {
            return true;
        }
        return ((AbstractNode) getParent()).isInTrash();
    }
    
    @Override
    public String toString() {
        //JPM.WART : display Trash instead of TRASH
        if (isTrash()) {
            return "Trash";
        }
        return super.toString();
    }
    
    @Override
    public boolean canBeDeleted() {
        
        // for the moment, we can delete only empty DataSet with no leaf
        if (isChanging()) {
            return false;
        }
        if (isInTrash()) {
            return false;
        }
        
        Enumeration e = children();
        while (e.hasMoreElements()) {
            AbstractNode child = (AbstractNode) e.nextElement();
            if (!child.canBeDeleted()) {
                return false;
            }
        }

        return true;
        
    }
    
    public void rename(final String newName) {

        DDataset dataset = getDataset();
        String name = dataset.getName();

        if ((newName != null) && (newName.compareTo(name) != 0)) {

            final DataSetNode datasetNode = this;

            setIsChanging(true);
            dataset.setName(newName + "...");
            ((DefaultTreeModel) IdentificationTree.getCurrentTree().getModel()).nodeChanged(this);


            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    datasetNode.setIsChanging(false);
                    datasetNode.getDataset().setName(newName);
                    ((DefaultTreeModel) IdentificationTree.getCurrentTree().getModel()).nodeChanged(datasetNode);
                }
            };


            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initRenameDataset(dataset, newName);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
    
    @Override
    public void loadDataForProperties(final Runnable callback) {
        
        // we must load resultSet and resultSummary
        final DDataset dataSet = ((DataSetData) getData()).getDataset();
        

        
        AbstractDatabaseCallback dbCallback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
                callback.run();

                
            }
        };


        // ask asynchronous loading of data
        
        // Task 1 : Load ResultSet and ResultSummary
        DatabaseDataSetTask task1 = new DatabaseDataSetTask(null);
        task1.setPriority(Priority.HIGH_3); // highest priority
        task1.initLoadRsetAndRsm(dataSet);
        
        // Task 2 : Load ResultSet Extra Data
        AbstractDatabaseCallback task2Callback = (dataSet.getResultSummaryId()!=null) ? null : dbCallback;
        DatabaseRsetProperties task2 = new DatabaseRsetProperties(task2Callback, dataSet.getProject().getId(),dataSet);
        task2.setPriority(Priority.HIGH_3); // highest priority
        task1.setConsecutiveTask(task2);
        
        // Task 3 : Count number of Protein Sets for Rsm
        if (dataSet.getResultSummaryId() != null) {
            DatabaseProteinSetsTask task3 = new DatabaseProteinSetsTask(dbCallback);
            task3.initCountProteinSets(dataSet);
            task3.setPriority(Priority.HIGH_3); // highest priority

            task2.setConsecutiveTask(task3);
        }
        
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task1);

    }
    
    
    @Override
    public Sheet createSheet() {
        
        DDataset dataset = getDataset();
        return PropertiesAction.createSheet(dataset);
        
    }
    
    @Override
    public AbstractNode copyNode() {
        if (isTrash()) {
            return null;
        }
        AbstractNode copy = new DataSetNode(getData());
        copyChildren(copy);
        return copy;
    }
}
