package fr.proline.studio.dam.data;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadRsmFromRsetTask;
import java.util.List;

/**
 * Correspond to the ResultSet in the MSI DB
 *
 * @author JM235353
 */
public class ResultSetData extends AbstractData {

    public ResultSet resultSet = null;

    public ResultSetData(ResultSet resultSet, boolean hasChildren) {
        dataType = DataTypes.RESULT_SET;
        this.resultSet = resultSet;
        this.hasChildren = hasChildren;
    }

    @Override
    public String getName() {
        if (resultSet != null) {
            return resultSet.getName();
        }
        return "";
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {

        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadRsmFromRsetTask(callback, resultSet, list));


    }
}
