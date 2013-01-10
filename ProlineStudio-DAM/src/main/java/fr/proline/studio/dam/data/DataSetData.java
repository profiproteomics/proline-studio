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
    
    private DataSetTMP dataSet;
    private String temporaryName;

    public DataSetData(DataSetTMP dataSet) {
        dataType = DataTypes.DATA_SET;

        this.dataSet = dataSet;

    }
    public DataSetData(String temporaryName) {
        dataType = DataTypes.DATA_SET;

        this.temporaryName = temporaryName;

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

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadChildrenDataset(dataSet, list);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



    }
}
