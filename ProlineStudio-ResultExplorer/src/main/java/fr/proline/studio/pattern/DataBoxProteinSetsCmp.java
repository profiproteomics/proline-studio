package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadProteinSetsFromProteinTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetProteinGroupComparePanel;

/**
 *
 * @author JM235353
 */
public class DataBoxProteinSetsCmp extends AbstractDataBox {

    public DataBoxProteinSetsCmp() {

        // Name of this databox
        name = "Protein Groups";

        
        // Register Possible in parameters
        // One ProteinMatch
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ProteinMatch.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One ProteinMatch
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(ProteinMatch.class, false); //JPM.TODO
        registerOutParameter(outParameter);
    
    }
    
    @Override
    public void createPanel() {
        RsetProteinGroupComparePanel p = new RsetProteinGroupComparePanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }

    @Override
    public void dataChanged(AbstractDataBox srcDataBox, Class dataType) {
        final ProteinMatch proteinMatch = (ProteinMatch) srcDataBox.getData(null, ProteinMatch.class);

        if (proteinMatch == null) {
            ((RsetProteinGroupComparePanel)panel).setData(null);
            return;
        }

        
        //final String searchedText = searchTextBeingDone; //JPM.TODO
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {


                ((RsetProteinGroupComparePanel)panel).setData(proteinMatch);
            }
        };

        // Load data if needed asynchronously
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadProteinSetsFromProteinTask(callback, proteinMatch));


    }
    
}
