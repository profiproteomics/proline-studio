package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchFromRsetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideMatchPanelTEST;

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
        RsetPeptideMatchPanelTEST p = new RsetPeptideMatchPanelTEST();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }
    
    @Override
    public void dataChanged(Class dataType) {
        
        final ResultSet _rset = (rset!=null) ? rset : (ResultSet) previousDataBox.getData(false, ResultSet.class);

        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {

                    PeptideMatch[] peptideMatchArray = _rset.getTransientPeptideMatches();
                    ((RsetPeptideMatchPanelTEST)panel).setData(taskId, peptideMatchArray, finished);
               } else {
                    ((RsetPeptideMatchPanelTEST)panel).dataUpdated(subTask, finished);
                }
            }
        };
        

        // ask asynchronous loading of data
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadPeptideMatchFromRsetTask(callback, getProjectId(), _rset));

       
        
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(PeptideMatch.class)) {
                return ((RsetPeptideMatchPanelTEST)panel).getSelectedPeptideMatch();
            }
            if (parameterType.equals(ResultSet.class)) {
                if (rset != null) {
                    return rset;
                }
            }
        }
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        if (data instanceof ResultSet) {
            rset = (ResultSet) data;
            dataChanged(ResultSet.class);
        } else if (data instanceof ResultSummary) {
            rset = ((ResultSummary) data).getResultSet();
        }
    }
}
