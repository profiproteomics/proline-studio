package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.DatabaseLoadProteinSetsFromProteinTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetProteinGroupComparePanel;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author JM235353
 */
public class DataBoxProteinSetsCmp extends AbstractDataBox {

    public DataBoxProteinSetsCmp() {

        // Name of this databox
        name = "Protein Sets";

        
        // Register Possible in parameters
        // One ProteinMatch
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ProteinMatch.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One ProteinMatch
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(ProteinMatch.class, true);
        registerOutParameter(outParameter);
    
        outParameter = new DataParameter();
        outParameter.addParameter(ResultSummary.class, true);
        registerOutParameter(outParameter);
        
    }
    
    @Override
    public void createPanel() {
        RsetProteinGroupComparePanel p = new RsetProteinGroupComparePanel();
        p.setName(name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged(Class dataType) {
        final ProteinMatch proteinMatch = (ProteinMatch) previousDataBox.getData(false, ProteinMatch.class);

        if (proteinMatch == null) {
            ((RsetProteinGroupComparePanel)m_panel).setData(null, null);
            return;
        }

        ArrayList<ProteinMatch> proteinMatchArray = new ArrayList<>(1);
        proteinMatchArray.add(proteinMatch);
        loadData(proteinMatchArray, null, null, null);

    }

    
    
    public void loadData(final ArrayList<ProteinMatch> proteinMatchArray, ArrayList<Long> resultSetIdArray, String proteinMatchName, final ArrayList<ResultSummary> resultSummaryArray ) {

        //final String searchedText = searchTextBeingDone; //JPM.TODO
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            private boolean proteinSetLoaded = false;

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (!proteinSetLoaded) {
                    // after proteinSet are loaded, we need to load PeptideSet of all proteins of proteinSets
                    proteinSetLoaded = true;

                    ArrayList<ProteinMatch> proteinMatchParameterArray = new ArrayList<>();
                    ArrayList<ResultSummary> resultSummaryParameterArray = new ArrayList<>();
                    
                    int size = proteinMatchArray.size();
                    for (int i=0;i<size;i++) {
                        ProteinMatch proteinMatch = proteinMatchArray.get(i);
                        
                        if (proteinMatch == null) {
                            continue; // should not happen
                        }
                        
                        ProteinSet[] proteinSetArray = proteinMatch.getTransientData().getProteinSetArray();
                        if (proteinSetArray == null) {
                            continue;
                         }

                         // retrieve all ResultSummary
                        int nbProteinSets = proteinSetArray.length;
                        for (int j = 0; j < nbProteinSets; j++) {
                            ProteinSet pset = proteinSetArray[j];
                            ResultSummary rsm = pset.getResultSummary();
                            proteinMatchParameterArray.add(proteinMatch);
                            resultSummaryParameterArray.add(rsm);
                        }
                    
                    }
                    
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadPeptidesInstancesTask(this, getProjectId(), proteinMatchParameterArray, resultSummaryParameterArray));
                    return;
                }

                HashMap<Long, ArrayList<Long>> rsmIdMap = null;
                if (resultSummaryArray != null) {
                    rsmIdMap = new HashMap<>();
                    
                    int size = resultSummaryArray.size();
                    for (int i=0;i<size;i++) {
                        ResultSummary rsm = resultSummaryArray.get(i);
                        Long rsetId = rsm.getResultSet().getId();
                        ArrayList<Long> rsmIdList = rsmIdMap.get(rsetId);
                        if (rsmIdList == null) {
                            rsmIdList = new ArrayList<>();
                            rsmIdMap.put(rsetId, rsmIdList);
                        }
                        rsmIdList.add(rsm.getId());
                    }
                }

                // create a list without twice the same ProteinMatch
                ArrayList<ProteinMatch> proteinMatchCleanedArray = new ArrayList<>();
                int size = proteinMatchArray.size();
                for (int i=0;i<size;i++) {
                    ProteinMatch pm = proteinMatchArray.get(i);
                    if (! proteinMatchCleanedArray.contains(pm)) {
                        proteinMatchCleanedArray.add(pm);
                    }
                }
                
                ((RsetProteinGroupComparePanel) m_panel).setData(proteinMatchCleanedArray, rsmIdMap); 
            }
        };

        // Load data if needed asynchronously
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadProteinSetsFromProteinTask(callback, getProjectId(), proteinMatchArray, resultSetIdArray, proteinMatchName));


    }
    
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(ProteinMatch.class)) {
                ArrayList<ProteinMatch> proteinMatchList = ((RsetProteinGroupComparePanel) m_panel).getComparePanel().getSelectedProteinMatchArray();
                if (getArray) {
                    return proteinMatchList;
                } else {
                    if ((proteinMatchList == null) || (proteinMatchList.isEmpty())) {
                        return null;
                    } else {
                        return proteinMatchList.get(0);
                    }
                }
            } else if (parameterType.equals(ResultSummary.class)) {
                if (getArray) {
                    return ((RsetProteinGroupComparePanel) m_panel).getComparePanel().getResultSummaryList();
                } else {
                    return ((RsetProteinGroupComparePanel) m_panel).getComparePanel().getFirstResultSummary();
                }
            }
        }

        //JPM.TODO
        return super.getData(getArray, parameterType);
    }
    
}
