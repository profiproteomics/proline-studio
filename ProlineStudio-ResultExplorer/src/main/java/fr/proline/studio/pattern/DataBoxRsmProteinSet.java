/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsmProteinSetPanel;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataBoxRsmProteinSet extends AbstractDataBox {
    
    ResultSummary rsm = null;
    
    public DataBoxRsmProteinSet() {

        // Register Possible in parameters
        // One ResultSummary
        registerInParameterType(null, ResultSummary.class);
        
        // Register possible out parameters
        // One ProteinSet
        registerOutParameterType(null, ProteinSet.class);
        
        // Multiple ProteinSet as a List
        registerOutParameterType(List.class, ProteinSet.class);
       
    }
    

    @Override
    public void createPanel() {
        RsmProteinSetPanel p = new RsmProteinSetPanel();
        p.setName("Protein Groups");
        p.setDataBox(this);
        panel = p;
    }
    
    @Override
    public void dataChanged(AbstractDataBox srcDataBox) {
        
        final ResultSummary _rsm = (rsm!=null) ? rsm : (ResultSummary) srcDataBox.getData(null, ResultSummary.class);

        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {
                
                
                
                if (subTask == null) {

                    ProteinSet[] proteinSetArray = _rsm.getTransientProteinSets();
                    ((RsmProteinSetPanel)panel).setData(taskId, proteinSetArray);
                } else {
                    ((RsmProteinSetPanel)panel).dataUpdated(subTask);
                }
            }
        };
        

        // ask asynchronous loading of data
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseProteinSetsTask(callback, _rsm));

       
        
    }
    
    @Override
    public Object getData(Class arrayParameterType, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(ProteinSet.class)) {
                return ((RsmProteinSetPanel)panel).getSelectedProteinSet();
            }
            if (parameterType.equals(ResultSummary.class)) {
                if (rsm != null) {
                    return rsm;
                }
            }
        }
        return super.getData(arrayParameterType, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        rsm = (ResultSummary) data;
        dataChanged(null);
    }
}
