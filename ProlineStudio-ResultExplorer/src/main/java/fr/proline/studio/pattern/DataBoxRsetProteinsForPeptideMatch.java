package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinsFromPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetProteinsForPeptideMatchPanel;

/**
 *
 * @author JM235353
 */
public class DataBoxRsetProteinsForPeptideMatch extends AbstractDataBox {
    

    public DataBoxRsetProteinsForPeptideMatch() {

         // Name of this databox
        name = "Proteins";
        
        // Register Possible in parameters
        // One PeptideMatch
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(PeptideMatch.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple ProteinMatch
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(ProteinMatch.class, true);
        registerOutParameter(outParameter);

       
    }

    
    @Override
    public void createPanel() {
        RsetProteinsForPeptideMatchPanel p = new RsetProteinsForPeptideMatchPanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }
    

    @Override
    public void dataChanged(Class dataType) {
        final PeptideMatch peptideMatch = (PeptideMatch) previousDataBox.getData(false, PeptideMatch.class);

        if (peptideMatch == null) {
            ((RsetProteinsForPeptideMatchPanel)panel).setData(null);
            return;
        }

        
        //final String searchedText = searchTextBeingDone; //JPM.TODO
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {


                ((RsetProteinsForPeptideMatchPanel)panel).setData(peptideMatch);
            }
        };

        // Load data if needed asynchronously
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseProteinsFromPeptideMatchTask(callback, getProjectId(), peptideMatch));


    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null && (parameterType.equals(ProteinMatch.class))) {
            return ((RsetProteinsForPeptideMatchPanel)panel).getSelectedProteinMatch();
        }
        return super.getData(getArray, parameterType);
    }
}
