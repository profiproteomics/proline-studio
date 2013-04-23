/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsmProteinSetPanelTEST;

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
        
        // or one PeptideInstance
        inParameter = new DataParameter();
        inParameter.addParameter(PeptideInstance.class, false);
        registerInParameter(inParameter);
        
   
        // Register possible out parameters
        // One or Multiple ProteinSet
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(ProteinSet.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);

       
    }
    

    @Override
    public void createPanel() {
        RsmProteinSetPanelTEST p = new RsmProteinSetPanelTEST();
        p.setName(name);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged(Class dataType) {
        
        if (dataType == PeptideInstance.class) {

            final PeptideInstance _peptideInstance = (PeptideInstance) previousDataBox.getData(false, PeptideInstance.class);

            
            if (_peptideInstance == null) {
                ((RsmProteinSetPanelTEST) m_panel).setData(null, null, true);
                return;
            }
            
            
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    if (subTask == null) {

                        ProteinSet[] proteinSetArray = _peptideInstance.getTransientData().getProteinSetArray();
                        ((RsmProteinSetPanelTEST) m_panel).setData(taskId, proteinSetArray, finished);
                    } else {
                        ((RsmProteinSetPanelTEST) m_panel).dataUpdated(subTask, finished);
                    }
                }
            };


            // ask asynchronous loading of data
            DatabaseProteinSetsTask task = new DatabaseProteinSetsTask(callback);
            task.initLoadProteinSetForPeptideInstance(getProjectId(), _peptideInstance);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        } else {
        
            final ResultSummary _rsm = (rsm != null) ? rsm : (ResultSummary) previousDataBox.getData(false, ResultSummary.class);


            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {



                    if (subTask == null) {

                        ProteinSet[] proteinSetArray = _rsm.getTransientData().getProteinSetArray();
                        ((RsmProteinSetPanelTEST) m_panel).setData(taskId, proteinSetArray, finished);
                    } else {
                        ((RsmProteinSetPanelTEST) m_panel).dataUpdated(subTask, finished);
                    }
                }
            };


            // ask asynchronous loading of data
            
            DatabaseProteinSetsTask task = new DatabaseProteinSetsTask(callback);
            task.initLoadProteinSets(getProjectId(), _rsm);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(ProteinSet.class)) {
                return ((RsmProteinSetPanelTEST)m_panel).getSelectedProteinSet();
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
