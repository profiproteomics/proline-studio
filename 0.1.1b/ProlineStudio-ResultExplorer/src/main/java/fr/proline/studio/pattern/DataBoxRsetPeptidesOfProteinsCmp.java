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
 * Databox for the comparison of peptides of proteins
 * @author JM235353
 */
public class DataBoxRsetPeptidesOfProteinsCmp extends AbstractDataBox {

    public DataBoxRsetPeptidesOfProteinsCmp() {

        // Name of this databox
        m_name = "Peptides of Proteins";

        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
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
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {

        final List<ProteinMatch> proteinMatchList = (List<ProteinMatch>) m_previousDataBox.getData(true, ProteinMatch.class);
        List<ResultSummary> resultSummaryList = (List<ResultSummary>) m_previousDataBox.getData(true, ResultSummary.class);

        if ((proteinMatchList == null) || (proteinMatchList.isEmpty()) || (resultSummaryList == null) || (resultSummaryList.isEmpty())) {
            ((RsetPeptidesOfProteinsCmpPanel) m_panel).setData(null, null);
            return;
        }

        final ArrayList<ProteinMatch> proteinMatchArrayList = new ArrayList<>(proteinMatchList);
        final ArrayList<ResultSummary> resultSummaryArrayList = new ArrayList<>(resultSummaryList);
        
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
  
                
                ((RsetPeptidesOfProteinsCmpPanel) m_panel).setData(proteinMatchArrayList, resultSummaryArrayList);
            }
        };


        // ask asynchronous loading of data
        registerTask(new DatabaseLoadPeptidesInstancesTask(callback, getProjectId(), proteinMatchArrayList, resultSummaryArrayList));


    }
}
