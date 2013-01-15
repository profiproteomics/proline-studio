/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.data;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.UDSConnectionManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProjectTask;
import java.util.List;

/**
 * User Data for Parent Node of all other Nodes in Result Explorer
 *
 * @author JM235353
 */
public class ParentData extends AbstractData {

    public ParentData() {
        dataType = DataTypes.MAIN;
        
        hasChildren = false;
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        DatabaseProjectTask task = new DatabaseProjectTask(callback);
        task.initLoadProject(UDSConnectionManager.getUDSConnectionManager().getUserName(), list);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);  //JPM.TODO

    }

    public String getName() {
        return "";
    }
}
