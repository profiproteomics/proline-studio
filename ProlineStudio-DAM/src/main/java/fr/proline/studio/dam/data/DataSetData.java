package fr.proline.studio.dam.data;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataSetData extends AbstractData {
    
    private Dataset dataset = null;
    private String temporaryName = null;
    private Aggregation.ChildNature temporaryAggregateType;

    public DataSetData(Dataset dataSet) {
        dataType = DataTypes.DATA_SET;

        this.dataset = dataSet;

    }
    public DataSetData(String temporaryName, Aggregation.ChildNature temporaryAggregateType) {
        dataType = DataTypes.DATA_SET;

        this.temporaryName = temporaryName;
        this.temporaryAggregateType = temporaryAggregateType;

    }

    public Dataset getDataset() {
        return dataset;
    }
    
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
        temporaryName = null;
    }
    
    @Override
    public boolean hasChildren() {
        if (dataset != null) {
            return (dataset.getFractionCount()>0);
        }
        return false;
    }
    
    @Override
    public String getName() {
        if (dataset == null) {
            if (temporaryName != null) {
                return temporaryName;
            } else {
                return "";
            }
        } else {
            return dataset.getName();
        }
    }

    public Aggregation.ChildNature getAggregateType() {
        if (dataset == null) {
            return temporaryAggregateType;
        }
        Aggregation aggreation = dataset.getAggregation();
        if (aggreation == null) {
            // should not happen !
            return temporaryAggregateType;
        }
        return aggreation.getChildNature();
    }
    
    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadChildrenDataset(dataset, list);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



    }
}
