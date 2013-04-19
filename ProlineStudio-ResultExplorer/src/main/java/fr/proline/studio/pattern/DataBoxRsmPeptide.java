package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideMatchPanelTEST;

public class DataBoxRsmPeptide extends AbstractDataBox {

    
    ResultSummary m_rsm = null;
    
    public DataBoxRsmPeptide() {

        // Name of this databox
        name = "PSM";
        
        // Register Possible in parameters
        // One ResultSummary
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(PeptideMatch.class, true);
        registerOutParameter(outParameter);

       
    }
    

    @Override
    public void createPanel() {
        RsetPeptideMatchPanelTEST p = new RsetPeptideMatchPanelTEST(true);
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }
    
    @Override
    public void dataChanged(Class dataType) {
        
        final ResultSummary _rsm = (m_rsm!=null) ? m_rsm : (ResultSummary) previousDataBox.getData(false, ResultSummary.class);

        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {

                    PeptideMatch[] peptideMatchArray = _rsm.getTransientData().getPeptideMatches();
                    ((RsetPeptideMatchPanelTEST)panel).setData(taskId, peptideMatchArray, finished);
               } else {
                    ((RsetPeptideMatchPanelTEST)panel).dataUpdated(subTask, finished);
                }
            }
        };
        

        // ask asynchronous loading of data
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadPeptideMatchTask(callback, getProjectId(), _rsm));

       
        
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(PeptideMatch.class)) {
                return ((RsetPeptideMatchPanelTEST)panel).getSelectedPeptideMatch();
            }
            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }
        }
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        if (data instanceof ResultSummary) {
            m_rsm = (ResultSummary) data;
            dataChanged(ResultSummary.class);
        }
    }
}
