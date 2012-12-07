package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.*;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadSpectrumsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideSpectrumPanel;

/**
 *
 * @author JM235353
 */
public class DataBoxRsetPeptideSpectrum extends AbstractDataBox {

    public DataBoxRsetPeptideSpectrum() {

        // Name of this databox
        name = "Spectrum";
        
        // Register in parameters
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(PeptideMatch.class, false);
        registerInParameter(inParameter);
        
        inParameter = new DataParameter();
        inParameter.addParameter(PeptideInstance.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        // none
    }

    @Override
    public void createPanel() {
        RsetPeptideSpectrumPanel p = new RsetPeptideSpectrumPanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }

    @Override
    public void dataChanged(Class dataType) {
        final PeptideMatch peptideMatch = (PeptideMatch) previousDataBox.getData(false, PeptideMatch.class);

        if (peptideMatch == null) {
            ((RsetPeptideSpectrumPanel) panel).setData(null);
            return;
        }

        boolean needToLoadData = ((! peptideMatch.getTransientData().getIsMsQuerySet()) ||
                                 (! peptideMatch.getMsQuery().getTransientIsSpectrumSet()));
        
        if (needToLoadData) {
         
                    //final String searchedText = searchTextBeingDone; //JPM.TODO
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {


                ((RsetPeptideSpectrumPanel) panel).setData(peptideMatch);
            }
        };

        // Load data if needed asynchronously
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadSpectrumsTask(callback, peptideMatch));

                 
                 
            
        } else {
            ((RsetPeptideSpectrumPanel) panel).setData(peptideMatch);
        }
    }
}
