/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.data;

import fr.proline.studio.dam.data.Data;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseCallback;
import fr.proline.studio.dam.actions.DatabaseLoadIdentificationAction;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 *
 * @author JM235353
 */
public class ProjectData extends Data {
    
    private Project project = null;
    
    public ProjectData(Project project) {
        dataType = Data.DataTypes.PROJECT;
        this.project = project;
    }
    
    public String getName() {
        if (project == null) {
            return "Loading Project...";
        } else {
            return project.getName();
        }
    }
    
    
    public void load(List<Data> list) {
        
        
        final Semaphore waitDataSemaphore = new Semaphore(0, true);


        
         DatabaseCallback callback = new DatabaseCallback() {
            @Override
            public void run() {
                // data is ready
                waitDataSemaphore.release();
            }
        };
        
        //JPM.TODO : create DatabaseAction which fetch needed data for ResultSummary
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadIdentificationAction(callback, project, list));

        // Wait for the end of task
        try {
            waitDataSemaphore.acquire();
        } catch (InterruptedException e) {
            // should not happen
            //JPM.TODO
            
        }
        // now data is loaded
        
        
    }
    
    
    
}
