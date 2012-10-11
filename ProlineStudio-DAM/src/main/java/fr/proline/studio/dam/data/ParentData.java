/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.data;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadProjectTask;
import java.util.List;

/**
 * User Data for Parent Node of all other Nodes in Result Explorer
 *
 * @author JM235353
 */
public class ParentData extends AbstractData {

    public ParentData() {
        dataType = DataTypes.MAIN;
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadProjectTask(callback, AccessDatabaseThread.getProjectIdTMP(), list));  //JPM.TODO

    }

    public String getName() {
        return "";
    }
}
