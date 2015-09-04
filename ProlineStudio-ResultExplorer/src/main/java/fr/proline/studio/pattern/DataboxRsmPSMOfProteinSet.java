package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;

/**
 * Load the PSM of a Protein Set
 * @author JM235353
 */
public class DataboxRsmPSMOfProteinSet extends AbstractDataBox {

    private boolean m_finishedLoading = false;
    
    public DataboxRsmPSMOfProteinSet() {
        super(DataboxType.DataboxRsmPSMOfProteinSet);

        // Name of this databox
        m_typeName = "PSM";
        m_description = "All PSM of a Protein Set";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        inParameter.addParameter(DProteinSet.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);
       
    }
    

    @Override
    public void createPanel() {
        PeptideMatchPanel p = new PeptideMatchPanel(true, false, false, false);
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged() {
        
        ResultSummary _rsm = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
        final DProteinSet proteinSet = (DProteinSet) m_previousDataBox.getData(false, DProteinSet.class);

        if (proteinSet == null) {
            ((PeptideMatchPanel) m_panel).setData(-1, null, null, true);
            return;
        }
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {

                    DPeptideMatch[] peptideMatchArray = proteinSet.getTypicalProteinMatch().getPeptideMatches();
                    
                    long[] peptideMatchIdArray = proteinSet.getTypicalProteinMatch().getPeptideMatchesId();
                    ((PeptideMatchPanel)m_panel).setData(taskId, peptideMatchArray, peptideMatchIdArray, finished);
               } else {
                    ((PeptideMatchPanel)m_panel).dataUpdated(subTask, finished);
                }
               
                if (finished) {
                    m_finishedLoading = true;
                    unregisterTask(taskId);
                    //propagateDataChanged(ValuesForStatsAbstract.class);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };
        

        // ask asynchronous loading of data
        registerTask(new DatabaseLoadPeptideMatchTask(callback, getProjectId(), _rsm, proteinSet));

       
        
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(DPeptideMatch.class)) {
                return ((PeptideMatchPanel)m_panel).getSelectedPeptideMatch();
            }
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface)m_panel).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
 

}
