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
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataboxRsmPeptidesOfProtein extends AbstractDataBox {
    

    
    public DataboxRsmPeptidesOfProtein() {

        // Name of this databox
        name = "Peptides";
        
        // Register Possible in parameters
        // One ResultSummary
        registerInParameterType(null, ProteinMatch.class);
        
        // Register possible out parameters
        // One PeptideInstance or PeptideMatch
        registerOutParameterType(null, PeptideInstance.class);
        registerOutParameterType(null, PeptideMatch.class);
        
        // Multiple ProteinSet as a List
        registerOutParameterType(List.class, PeptideInstance.class);
       
    }
    

     
     @Override
    public void createPanel() {
        RsmPeptidesOfProteinPanel p = new RsmPeptidesOfProteinPanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }
    

    @Override
    public void dataChanged(AbstractDataBox srcDataBox) {
        final ProteinMatch proteinMatch = (ProteinMatch) srcDataBox.getData(null, ProteinMatch.class);
        final ResultSummary rsm = (ResultSummary) srcDataBox.getData(null, ResultSummary.class);

        if (proteinMatch == null) {
            ((RsmPeptidesOfProteinPanel) panel).setData(null);
            return;
        }

        // prepare callback to view new data
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {

                if (success) {
                    ((RsmPeptidesOfProteinPanel) panel).setData(proteinMatch);
                } else {
                    ((RsmPeptidesOfProteinPanel) panel).setData(null);
                }
            }
        };

        // Load data if needed asynchronously
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadPeptidesInstancesTask(callback, proteinMatch, rsm));


    }
    
    @Override
    public Object getData(Class arrayParameterType, Class parameterType) {
        if (parameterType!= null && (parameterType.equals(PeptideInstance.class))) {
            return ((RsmPeptidesOfProteinPanel)panel).getSelectedPeptide();
        } else if (parameterType!= null && (parameterType.equals(PeptideMatch.class))) {
            PeptideInstance pi = ((RsmPeptidesOfProteinPanel)panel).getSelectedPeptide();
            if (pi != null) {
                return pi.getTransientBestPeptideMatch();
            }
        }
        return super.getData(arrayParameterType, parameterType);
    }

}
