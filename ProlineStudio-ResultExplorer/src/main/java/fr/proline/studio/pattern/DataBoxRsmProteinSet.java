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

/**
 *
 * @author JM235353
 */
public class DataBoxRsmProteinSet extends AbstractDataBox {
    
    ResultSummary rsm = null;
    
    public DataBoxRsmProteinSet() {

         // Name of this databox
        name = "Protein Set";
        
        // Register Possible in parameters
        // One ResultSummary
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
   
        // Register possible out parameters
        // One or Multiple ProteinSet
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(ProteinSet.class, true);
        outParameter.addParameter(ResultSummary.class, true);
        registerOutParameter(outParameter);

       
    }
    

    @Override
    public void createPanel() {
        RsmProteinSetPanel p = new RsmProteinSetPanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }
    
    @Override
    public void dataChanged(Class dataType) {
        
        final ResultSummary _rsm = (rsm!=null) ? rsm : (ResultSummary) previousDataBox.getData(false, ResultSummary.class);

        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {
                
                
                
                if (subTask == null) {

                    ProteinSet[] proteinSetArray = _rsm.getTransientData().getProteinSetArray();
                    ((RsmProteinSetPanel)panel).setData(taskId, proteinSetArray);
                } else {
                    ((RsmProteinSetPanel)panel).dataUpdated(subTask);
                }
            }
        };
        

        // ask asynchronous loading of data
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseProteinSetsTask(callback, getProjectId(), _rsm));

       
        
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
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
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        rsm = (ResultSummary) data;
        dataChanged(ResultSummary.class);
    }
}
