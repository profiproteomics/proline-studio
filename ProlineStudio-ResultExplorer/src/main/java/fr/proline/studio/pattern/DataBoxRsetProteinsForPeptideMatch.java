package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinMatchesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.RsetProteinsPanel;

/**
 * Databox : Proteins for a Peptide Match
 * @author JM235353
 */
public class DataBoxRsetProteinsForPeptideMatch extends AbstractDataBox {
    
    private long m_peptideMatchCurId = -1;

    public DataBoxRsetProteinsForPeptideMatch() {
        super(DataboxType.DataBoxRsetProteinsForPeptideMatch, DataboxStyle.STYLE_RSET);

         // Name of this databox
        m_typeName = "Proteins";
        m_description = "Proteins for a Peptide Match";
        
        // Register Possible in parameters
        // One PeptideMatch
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DPeptideMatch.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple ProteinMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DProteinMatch.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);
       
    }

    
    @Override
    public void createPanel() {
        RsetProteinsPanel p = new RsetProteinsPanel(false);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    

    @Override
    public void dataChanged() {
        final DPeptideMatch peptideMatch = (DPeptideMatch) m_previousDataBox.getData(false, DPeptideMatch.class);

        if (peptideMatch == null) {
            ((RsetProteinsPanel)getDataBoxPanelInterface()).setDataPeptideMatch(null);
            m_peptideMatchCurId = -1;
            return;
        }

        if ((m_peptideMatchCurId!=-1) && (peptideMatch.getId() == m_peptideMatchCurId)) {
            return;
        }
        m_peptideMatchCurId = peptideMatch.getId();
        
        final int loadingId = setLoading();
        
        //final String searchedText = searchTextBeingDone; //JPM.TODO
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                    ((RsetProteinsPanel)getDataBoxPanelInterface()).setDataPeptideMatch(peptideMatch);
                } else {
                    ((RsetProteinsPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }
                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
                
            }
        };

        // Load data if needed asynchronously
        DatabaseProteinMatchesTask task = new DatabaseProteinMatchesTask(callback, getProjectId(), peptideMatch);
        Long taskId = task.getId();
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        registerTask(task);

    }
    private Long m_previousTaskId = null;
    
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null) {
            if (parameterType.equals(DProteinMatch.class)) {
                return ((RsetProteinsPanel) getDataBoxPanelInterface()).getSelectedProteinMatch();
            }
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface)getDataBoxPanelInterface()).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
}
