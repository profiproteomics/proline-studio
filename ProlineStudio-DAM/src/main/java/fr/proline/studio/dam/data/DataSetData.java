package fr.proline.studio.dam.data;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataSetData extends AbstractData {
    
    private DataSetTMP dataSet = null;
    private String temporaryName = null;
    private int temporaryAggregateType;

    public DataSetData(DataSetTMP dataSet) {
        dataType = DataTypes.DATA_SET;

        this.dataSet = dataSet;

    }
    public DataSetData(String temporaryName, int temporaryAggregateType) {
        dataType = DataTypes.DATA_SET;

        this.temporaryName = temporaryName;
        this.temporaryAggregateType = temporaryAggregateType;

    }

    public DataSetTMP getDataSet() {
        return dataSet;
    }
    
    public void setDataSet(DataSetTMP dataSet) {
        this.dataSet = dataSet;
        temporaryName = null;
    }
    
    @Override
    public String getName() {
        if (dataSet == null) {
            if (temporaryName != null) {
                return temporaryName;
            } else {
                return "";
            }
        } else {
            return dataSet.getName();
        }
    }

    public int getAggregateType() {
        if (dataSet == null) {
            return temporaryAggregateType;
        }
        return dataSet.aggregateType;
    }
    
    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadChildrenDataset(dataSet, list);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



    }
}
