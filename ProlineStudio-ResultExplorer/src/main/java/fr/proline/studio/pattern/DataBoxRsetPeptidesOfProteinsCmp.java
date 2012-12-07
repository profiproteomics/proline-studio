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
import java.util.ArrayList;
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
    public void dataChanged(Class dataType) {

        final List<ProteinMatch> proteinMatchList = (List<ProteinMatch>) previousDataBox.getData(true, ProteinMatch.class);
        List<ResultSummary> resultSummaryList = (List<ResultSummary>) previousDataBox.getData(true, ResultSummary.class);

        if ((proteinMatchList == null) || (proteinMatchList.isEmpty()) || (resultSummaryList == null) || (resultSummaryList.isEmpty())) {
            ((RsetPeptidesOfProteinsCmpPanel) panel).setData(null, null);
            return;
        }

        final ArrayList<ProteinMatch> proteinMatchArrayList = new ArrayList<ProteinMatch>(proteinMatchList);
        final ArrayList<ResultSummary> resultSummaryArrayList = new ArrayList<ResultSummary>(resultSummaryList);
        
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {
  
                
                ((RsetPeptidesOfProteinsCmpPanel) panel).setData(proteinMatchArrayList, resultSummaryArrayList);
            }
        };


        // ask asynchronous loading of data
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadPeptidesInstancesTask(callback, proteinMatchArrayList, resultSummaryArrayList));


    }
}
