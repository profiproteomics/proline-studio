/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.data;


import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseCallback;
import fr.proline.studio.dam.actions.DatabaseLoadProjectAction;
import java.util.List;
import java.util.concurrent.Semaphore;


/**
 *
 * @author JM235353
 */
public class ParentData extends Data{
    
    public ParentData() {
        dataType = DataTypes.MAIN;
    }
    
    @Override
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
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadProjectAction(callback, AccessDatabaseThread.getProjectIdTMP(), list));  //JPM.TODO

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
