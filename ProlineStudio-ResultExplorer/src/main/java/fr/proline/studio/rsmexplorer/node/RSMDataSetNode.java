package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Dataset.DatasetType;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for Dataset
 * @author JM235353
 */
public class RSMDataSetNode extends RSMNode {

   
    
    
    public RSMDataSetNode(AbstractData data) {
        super(NodeTypes.DATA_SET, data);
    }

    @Override
    public ImageIcon getIcon() {
        
        Dataset.DatasetType type = ((DataSetData) getData()).getDatasetType();
        switch(type) {
            case IDENTIFICATION:
                Dataset dataset = ((DataSetData) getData()).getDataset();
                if (dataset != null) {
                    if (dataset.getResultSummaryId() == null) {
                        if (isChanging()) {
                            return getIcon(IconManager.IconType.RSM); // will become a RSM
                        } else {
                            return getIcon(IconManager.IconType.RSET);
                        }
                    } else {
                        return getIcon(IconManager.IconType.RSM);
                    }
                } else {
                    return getIcon(IconManager.IconType.RSET);
                }
                
            case AGGREGATE:
                //Aggregation.ChildNature aggregateType = ((DataSetData) getData()).getAggregateType();
                //JPM.TODO : according to aggregateType type :icon must be different
                return getIcon(IconManager.IconType.VIAL);
            default:
                // sould not happen
                
        }
        
        return null;

    }
    

    
    public Dataset getDataset() {
        return ((DataSetData) getData()).getDataset();
    }
    
    public boolean hasResultSummary() {
        Dataset dataSet = ((DataSetData) getData()).getDataset();
        return (dataSet.getResultSummaryId() != null);
    }
    

    public Integer getResultSummaryId() {
        return ((DataSetData) getData()).getDataset().getResultSummaryId();
    }
    
    public ResultSummary getResultSummary() {
        // getResultSummary() can return null if the resultSummary has not been loaded previously
        Dataset dataSet = ((DataSetData) getData()).getDataset();
        return dataSet.getTransientData().getResultSummary();
    }
    
    
    public boolean hasResultSet() {
        Dataset dataSet = ((DataSetData) getData()).getDataset();
        return (dataSet.getResultSetId() != null);
    }
    
    public Integer getResultSetId() {
        return ((DataSetData) getData()).getDataset().getResultSetId();
    }
    
    public ResultSet getResultSet() {
        // getResultSet() can return null if the resultSet has not been loaded previously
        Dataset dataSet = ((DataSetData) getData()).getDataset();
        return dataSet.getTransientData().getResultSet();
    }
    
    @Override
    public boolean canBeDeleted() {
        
        // for the moment, we can delete only empty DataSet with no leaf
        if (hasResultSet()) {
            return false;
        }
        if (hasResultSummary()) {
            return false;
        }
        
        return isLeaf();
        
    }
    
    public void rename(final String newName) {

        Dataset dataset = getDataset();
        String name = dataset.getName();

        if ((newName != null) && (newName.compareTo(name) != 0)) {

            final RSMDataSetNode datasetNode = this;

            setIsChanging(true);
            dataset.setName(newName + "...");
            ((DefaultTreeModel) RSMTree.getTree().getModel()).nodeChanged(this);


            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    datasetNode.setIsChanging(false);
                    datasetNode.getDataset().setName(newName);
                    ((DefaultTreeModel) RSMTree.getTree().getModel()).nodeChanged(datasetNode);
                }
            };


            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initRenameDataset(dataset, newName);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
    
    @Override
    public Sheet createSheet() {
        Dataset dataset = getDataset();
        
        Sheet sheet = Sheet.createDefault();
        

        
        try {
            Sheet.Set propGroup = Sheet.createPropertiesSet();
            propGroup.setName("Data Set");
            propGroup.setDisplayName("Data Set");

            Property prop = new PropertySupport.Reflection<Integer>(dataset, Integer.class, "getId", null);
            prop.setName("id");
            propGroup.put(prop);
            
            sheet.put(propGroup);

            if (hasResultSet()) {
                sheet.put(createResultSetSheetSet(dataset));
            }

            if (hasResultSummary()) {
                sheet.put(createResultSummarySheetSet(dataset));
            }

        } catch (NoSuchMethodException e) {
            Logger logger = LoggerFactory.getLogger(RSMNode.class);
            logger.error(getClass().getSimpleName() + " properties error ", e);
        }
        

        
        return sheet;
    }
    
    private Sheet.Set createResultSetSheetSet(Dataset dataset) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Result Set");
        propGroup.setDisplayName("Result Set");

        Property prop = new PropertySupport.Reflection<Integer>(dataset, Integer.class, "getResultSetId", null);
        prop.setName("ResultSet id");
        propGroup.put(prop);

     
        return propGroup;
    }
    
    private Sheet.Set createResultSummarySheetSet(Dataset dataset) throws NoSuchMethodException {
        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName("Result Summary");
        propGroup.setDisplayName("Result Summary");
        
        Property prop = new PropertySupport.Reflection<Integer>(dataset, Integer.class, "getResultSummaryId", null);
        prop.setName("ResultSummary id");
        propGroup.put(prop);
        
        return propGroup;
    }
}
