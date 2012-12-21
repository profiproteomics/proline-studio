package fr.proline.studio.dam.data;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadDataSetTask;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataSetData extends AbstractData {
    
    private DataSetTMP dataSet;

    public DataSetData(DataSetTMP dataSet) {
        dataType = DataTypes.DATA_SET;

        this.dataSet = dataSet;

    }

    public DataSetTMP getDataSet() {
        return dataSet;
    }
    
    @Override
    public String getName() {
        if (dataSet == null) {
            return "";
        } else {
            return dataSet.getName();
        }
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadDataSetTask(callback, dataSet, list));



    }
}
