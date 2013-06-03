/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsmPeptidesOfProteinPanel;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class DataBoxRsmPeptidesOfProtein extends AbstractDataBox {
    

    
    public DataBoxRsmPeptidesOfProtein() {

        // Name of this databox
        name = "Peptides";
        
        // Register Possible in parameters
        // One ProteinMatch AND one ResultSummary
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ProteinMatch.class, false);
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple  PeptideInstance
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(PeptideInstance.class, true);
        registerOutParameter(outParameter);
        
        outParameter = new DataParameter();
        outParameter.addParameter(PeptideMatch.class, true);
        registerOutParameter(outParameter);
        
       
    }
    

     
     @Override
    public void createPanel() {
        RsmPeptidesOfProteinPanel p = new RsmPeptidesOfProteinPanel();
        p.setName(name);
        p.setDataBox(this);
        m_panel = p;
    }
    

    @Override
    public void dataChanged(Class dataType) {
        final ProteinMatch proteinMatch = (ProteinMatch) previousDataBox.getData(false, ProteinMatch.class);
        final ResultSummary rsm = (ResultSummary) previousDataBox.getData(false, ResultSummary.class);

        if (proteinMatch == null) {
            ((RsmPeptidesOfProteinPanel) m_panel).setData(null, null);
            return;
        }

        // prepare callback to view new data
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success) {
                    ((RsmPeptidesOfProteinPanel) m_panel).setData(proteinMatch, rsm);
                } else {
                    ((RsmPeptidesOfProteinPanel) m_panel).setData(null, null);
                }
            }
        };

        // Load data if needed asynchronously
        ArrayList<ResultSummary> rsmList = new ArrayList<>(1);
        rsmList.add(rsm);
        DatabaseLoadPeptidesInstancesTask task = new DatabaseLoadPeptidesInstancesTask(callback, getProjectId(), proteinMatch, rsmList);
        Long taskId = task.getId();
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().removeTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
    private Long m_previousTaskId = null;
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null && (parameterType.equals(PeptideInstance.class))) {
            return ((RsmPeptidesOfProteinPanel)m_panel).getSelectedPeptide();
        } else if (parameterType!= null && (parameterType.equals(PeptideMatch.class))) {
            PeptideInstance pi = ((RsmPeptidesOfProteinPanel)m_panel).getSelectedPeptide();
            if (pi != null) {
                return pi.getTransientData().getBestPeptideMatch();
            }
        }
        return super.getData(getArray, parameterType);
    }

}
