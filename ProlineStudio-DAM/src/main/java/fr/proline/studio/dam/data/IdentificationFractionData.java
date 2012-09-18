/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.data;


import fr.proline.studio.dam.data.Data;
import fr.proline.core.orm.uds.IdentificationFraction;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseCallback;
import fr.proline.studio.dam.actions.DatabaseLoadIdentificationAction;
import fr.proline.studio.dam.actions.DatabaseLoadResultSetAction;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 *
 * @author JM235353
 */
public class IdentificationFractionData extends Data {
 
    IdentificationFraction identificationFraction;
    
    public IdentificationFractionData(IdentificationFraction identificationFraction) {
        dataType = Data.DataTypes.CONTEXT;
        
        this.identificationFraction = identificationFraction;
        
    }
    
    public String getName() {
        if (identificationFraction == null) {
            return "";
        } else {
            return identificationFraction.getRawFile().getRawFileName();
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
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadResultSetAction(callback, identificationFraction, list));

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
