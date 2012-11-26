/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinsFromProteinSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsmProteinsOfProteinSetPanel;



/**
 *
 * @author JM235353
 */
public class DataBoxRsmProteinsOfProteinSet extends AbstractDataBox {
    

    public DataBoxRsmProteinsOfProteinSet() {

         // Name of this databox
        name = "Proteins";
        
        // Register Possible in parameters
        // One ProteinSet
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ProteinSet.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One ProteinMatch
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(ProteinMatch.class, true);
        registerOutParameter(outParameter);

       
    }

    @Override
    public void createPanel() {
        RsmProteinsOfProteinSetPanel p = new RsmProteinsOfProteinSetPanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }
    
    @Override
    public void dataChanged(AbstractDataBox srcDataBox, Class dataType) {
        final ProteinSet proteinSet = (ProteinSet) srcDataBox.getData(false, ProteinSet.class);

        //final String searchedText = searchTextBeingDone; //JPM.TODO
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {


                ((RsmProteinsOfProteinSetPanel)panel).setData(proteinSet, null /*
                         * searchedText
                         */); //JPM.TODO
            }
        };

        // Load data if needed asynchronously
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseProteinsFromProteinSetTask(callback, proteinSet));


    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null && (parameterType.equals(ProteinMatch.class))) {
            return ((RsmProteinsOfProteinSetPanel)panel).getSelectedProteinMatch();
        }
        return super.getData(getArray, parameterType);
    }
}
