package fr.proline.studio.rsmexplorer.tree;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.core.orm.uds.dto.DDatasetType.AggregationInformation;
import static fr.proline.core.orm.uds.dto.DDatasetType.AggregationInformation.IDENTIFICATION_SUMMARY_AGG;
import static fr.proline.core.orm.uds.dto.DDatasetType.AggregationInformation.IDENTIFICATION_SUMMARY_UNION;
import static fr.proline.core.orm.uds.dto.DDatasetType.AggregationInformation.SEARCH_RESULT_AGG;
import static fr.proline.core.orm.uds.dto.DDatasetType.AggregationInformation.SEARCH_RESULT_UNION;
import static fr.proline.core.orm.uds.dto.DDatasetType.AggregationInformation.UNKNOWN;
import fr.proline.core.orm.uds.dto.DDatasetType.QuantitationMethodInfo;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.DatabaseRsetProperties;
import fr.proline.studio.dam.tasks.DatabaseRsummaryProperties;
import fr.proline.studio.dam.tasks.SubTask;
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

    public DataSetNode(NodeTypes type, AbstractData data) {
        super(type, data);
    }
    
    
    public DataSetNode(AbstractData data) {
        super(NodeTypes.DATA_SET, data);
    }

    @Override
    public ImageIcon getIcon(boolean expanded) {

        DDataset dataset = ((DataSetData) getData()).getDataset();
        DDatasetType datasetType = ((DataSetData) getData()).getDatasetType();
        
        
        if (datasetType.isTrash()) {
            return getIcon(IconManager.IconType.TRASH);
        }
        // Test Folders first since for identification folders, isQuantitation() is true (same for identification folders
        if (datasetType.isFolder()) {
            if (expanded) {
                return getIcon(IconManager.IconType.FOLDER_EXPANDED);
            } else {
                return getIcon(IconManager.IconType.FOLDER);
            }
        }

        if (datasetType.isIdentification()) {
            if (datasetType.isAggregation()) {
                if (dataset != null) {
                    boolean rsmDefined = dataset.getResultSummaryId() != null;
                    AggregationInformation mergeInfo = ((DataSetData) getData()).getDataset().getAggregationInformation();
                    switch (mergeInfo) {
                        case IDENTIFICATION_SUMMARY_AGG:
                            return getIcon(IconManager.IconType.DATASET_RSM_MERGED_AGG);
                        case IDENTIFICATION_SUMMARY_UNION:
                            return getIcon(IconManager.IconType.DATASET_RSM_MERGED_UNION);
                        case SEARCH_RESULT_AGG:
                            if (rsmDefined) {
                                return getIcon(IconManager.IconType.DATASET_RSM_RSET_MERGED_AGG);
                            } else {
                                return getIcon(IconManager.IconType.DATASET_RSET_MERGED_AGG);
                            }
                        case SEARCH_RESULT_UNION:
                            if (rsmDefined) {
                                return getIcon(IconManager.IconType.DATASET_RSM_RSET_MERGED_UNION);
                            } else {
                                return getIcon(IconManager.IconType.DATASET_RSET_MERGED_UNION);
                            }
                        case UNKNOWN:
                            return getIcon(IconManager.IconType.DATASET);
                    }
                }
                return getIcon(IconManager.IconType.DATASET);
            } else if (dataset != null) {
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
        }
        if (datasetType.isQuantitation()) {
            if (dataset == null || datasetType.getQuantMethodInfo() == QuantitationMethodInfo.NONE) {
                return getIcon(IconManager.IconType.QUANT_XIC);
            }
            if (dataset.getQuantMethodInfo() == QuantitationMethodInfo.FEATURES_EXTRACTION) { // XIC
                return getIcon(IconManager.IconType.QUANT_XIC);
            } else if (dataset.getQuantMethodInfo() == QuantitationMethodInfo.SPECTRAL_COUNTING) { // Spectral count
                return getIcon(IconManager.IconType.QUANT_SC);
            } else {
                return getIcon(IconManager.IconType.QUANT);
            }
        }

        return getIcon(IconManager.IconType.QUANT);// sould not happen

    }
    
    public boolean isMerged() {

        DDatasetType datasetType = ((DataSetData) getData()).getDatasetType();
        if (datasetType.isIdentification() && datasetType.isAggregation()) {
            DDataset dataset = ((DataSetData) getData()).getDataset();
            if (dataset != null) {
                if ((dataset.getResultSummaryId() != null) || (dataset.getResultSetId() != null)) {
                    return true;
                }
            }
        } else if (datasetType.isQuantitation()){
            return true; //rsType is Quantitation but, a SC or a XIC is necessarily a merge
        }
        return false;
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
        DDatasetType datasetType = ((DataSetData) getData()).getDatasetType();
        if (datasetType.isTrash()) {
            return true;
        }
        return false;
    }
    
    public boolean isFolder() {
        DDataset dataset = ((DataSetData) getData()).getDataset();
        if (dataset == null) {
            return false;
        }
        DDatasetType datasetType = ((DataSetData) getData()).getDatasetType();
        if (datasetType.isFolder()) {
            return true;
        }
        return false;
    }
    
    public boolean hasResultSummary() {
        DDataset dataSet = ((DataSetData) getData()).getDataset();
        return (dataSet != null) && (dataSet.getResultSummaryId() != null);
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
        return (dataSet != null) && ((dataSet.getResultSetId() != null) || (dataSet.isQuantitation()));
    }
    
    public Long getResultSetId() {
        return ((DataSetData) getData()).getDataset().getResultSetId();
    }
    
    public ResultSet getResultSet() {
        // getResultSet() can return null if the resultSet has not been loaded previously
        DDataset dataSet = ((DataSetData) getData()).getDataset();
        if(dataSet.getResultSet()==null){
            DataSetData.fetchRsetAndRsmForOneDataset(dataSet);
        }
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
    
    /**
     * rename the dataset with the given newName
     * This operation could be done on the IdentificationTree or a QuantitationTree
     * @param newName
     * @param tree 
     */
    public void rename(final String newName, final AbstractTree tree) {

        DDataset dataset = getDataset();
        String name = dataset.getName();

        if ((newName != null) && (newName.compareTo(name) != 0) && !newName.trim().isEmpty()) {

            final DataSetNode datasetNode = this;

            setIsChanging(true);
            dataset.setName(newName + "...");
            if (tree != null) {
                ((DefaultTreeModel) tree.getModel()).nodeChanged(this);
            }


            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    datasetNode.setIsChanging(false);
                    datasetNode.getDataset().setName(newName);
                    if (tree != null) {
                        ((DefaultTreeModel) tree.getModel()).nodeChanged(datasetNode);
                    }
                }
            };


            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initRenameDataset(dataset, name, newName);
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
        // depending of the type
        if (dataSet.isQuantitation()) {
            // Task 1 : Load Quantitation, MasterQuantitationChannels 
            DatabaseDataSetTask task1 = new DatabaseDataSetTask(dbCallback);
            task1.setPriority(Priority.HIGH_3); // highest priority
            task1.initLoadQuantitation(dataSet.getProject(), dataSet);
            
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task1);
        } else { 
        
            // Task 1 : Load ResultSet and ResultSummary
            AbstractDatabaseCallback task1Callback = (dataSet.getResultSetId() != null) ? null : dbCallback;
            DatabaseDataSetTask task1 = new DatabaseDataSetTask(task1Callback);
            task1.setPriority(Priority.HIGH_3); // highest priority
            task1.initLoadRsetAndRsm(dataSet);
        
            // Task 2 : Load ResultSet Extra Data
            if (dataSet.getResultSetId() != null) {
                AbstractDatabaseCallback task2Callback = (dataSet.getResultSummaryId() != null) ? null : dbCallback;
                DatabaseRsetProperties task2 = new DatabaseRsetProperties(task2Callback, dataSet.getProject().getId(), dataSet);
                task2.setPriority(Priority.HIGH_3); // highest priority
                task1.setConsecutiveTask(task2);

                // Task 3 : Count number of Protein Sets for Rsm
                if (dataSet.getResultSummaryId() != null) {
                    DatabaseRsummaryProperties task3 = new DatabaseRsummaryProperties(dbCallback, dataSet.getProject().getId(), dataSet);
                    task3.setPriority(Priority.HIGH_3); // highest priority
                    task2.setConsecutiveTask(task3);
                }
            }

            AccessDatabaseThread.getAccessDatabaseThread().addTask(task1);
        }

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
    
    /**
     * return true in case of DatasetNode is a quantitation node (XIC or spectral count)
     * @return 
     */
    public boolean isQuantitation(){
       DDatasetType datasetType = ((DataSetData) getData()).getDatasetType();
       return  (datasetType.isQuantitation()) ;
    }
    
    /**
     * return true if it's a quantitation Spectral Count
     * @return 
     */
    public boolean isQuantSC() {
        if (isQuantitation()) {
            DDataset d =  ((DataSetData) getData()).getDataset();
            return d.getQuantMethodInfo() == DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING;
        } else {
            return false;
        }
    }
    
    /**
     * return true if it's a quantitation XIC
     * @return 
     */
    public boolean isQuantXIC() {
        if (isQuantitation()) {
            DDataset d =  ((DataSetData) getData()).getDataset();
            return d.getQuantMethodInfo() == DDatasetType.QuantitationMethodInfo.FEATURES_EXTRACTION;
        }else {
            return false;
        }
    }
    
    /**
     * return the parent dataset of the current node, if the parent is a merged dataset
     * @return 
     */
    public DDataset getParentMergedDataset ()  {
        if (this.getParent() instanceof DataSetNode) {
            DataSetNode parentNode = (DataSetNode)this.getParent();
            if (parentNode.getDataset().getAggregationInformation() != null) {
                return parentNode.getDataset();
            }else {
                return null;
            }
        }else {
            return null;
        }
    }

    @Override
    public Sheet createSheet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
