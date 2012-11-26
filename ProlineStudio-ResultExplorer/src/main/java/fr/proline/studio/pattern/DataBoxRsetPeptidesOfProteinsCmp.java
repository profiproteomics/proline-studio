/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptidesOfProteinsCmpPanel;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataBoxRsetPeptidesOfProteinsCmp extends AbstractDataBox {

        public DataBoxRsetPeptidesOfProteinsCmp() {

        // Name of this databox
        name = "Peptides of Proteins";
        
        // Register in parameters
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ProteinMatch.class, false);
        inParameter.addParameter(ResultSummary.class, true);
        registerInParameter(inParameter);
        

        // Register possible out parameters
        // none
        //JPM.TODO
    }
    
    @Override
    public void createPanel() {
        RsetPeptidesOfProteinsCmpPanel p = new RsetPeptidesOfProteinsCmpPanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }

    @Override
    public void dataChanged(AbstractDataBox srcDataBox, Class dataType) {
        
        final ProteinMatch proteinMatch = (ProteinMatch) srcDataBox.getData(false, ProteinMatch.class);
        final List<ResultSummary> resultSummaryList = (List<ResultSummary>) srcDataBox.getData(true, ResultSummary.class);
        
        if ((proteinMatch == null) || (resultSummaryList == null) || (resultSummaryList.isEmpty() )) {
            ((RsetPeptidesOfProteinsCmpPanel) panel).setData(null, null);
            return;
        }
                
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {
                ResultSummary[] rsmArray = new ResultSummary[resultSummaryList.size()];
                resultSummaryList.toArray(rsmArray);
                
               ((RsetPeptidesOfProteinsCmpPanel) panel).setData(proteinMatch, rsmArray);
            }
        };
        

        // ask asynchronous loading of data
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadPeptidesInstancesTask(callback, proteinMatch, resultSummaryList));

       
    }
    
}
