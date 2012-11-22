/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchFromRsetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideMatchPanel;

/**
 *
 * @author JM235353
 */
public class DataBoxRsetPeptide extends AbstractDataBox {

    
    ResultSet rset = null;
    
    public DataBoxRsetPeptide() {

        // Name of this databox
        name = "Peptides";
        
        // Register Possible in parameters
        // One ResultSummary
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ResultSet.class, false);

        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(PeptideMatch.class, true);
        

       
    }
    

    @Override
    public void createPanel() {
        RsetPeptideMatchPanel p = new RsetPeptideMatchPanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }
    
    @Override
    public void dataChanged(AbstractDataBox srcDataBox, Class dataType) {
        
        final ResultSet _rset = (rset!=null) ? rset : (ResultSet) srcDataBox.getData(null, ResultSet.class);

        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {
                
               if (subTask == null) {

                    PeptideMatch[] peptideMatchArray = _rset.getTransientPeptideMatches();
                    ((RsetPeptideMatchPanel)panel).setData(taskId, peptideMatchArray);
               } else {
                    ((RsetPeptideMatchPanel)panel).dataUpdated(subTask);
                }
            }
        };
        

        // ask asynchronous loading of data
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadPeptideMatchFromRsetTask(callback, _rset));

       
        
    }
    
    @Override
    public Object getData(Class arrayParameterType, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(PeptideMatch.class)) {
                return ((RsetPeptideMatchPanel)panel).getSelectedPeptideMatch();
            }
            if (parameterType.equals(ResultSet.class)) {
                if (rset != null) {
                    return rset;
                }
            }
        }
        return super.getData(arrayParameterType, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        rset = (ResultSet) data;
        dataChanged(null, ResultSet.class);
    }
}
