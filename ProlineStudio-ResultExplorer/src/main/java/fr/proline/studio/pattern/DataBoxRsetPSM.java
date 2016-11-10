package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;

/**
 * Databox for all PSM of a ResultSet (Search Result)
 * @author JM235353
 */
public class DataBoxRsetPSM extends AbstractDataBox {

    
    private ResultSet m_rset = null;

    private boolean m_mergedData;
    
    public DataBoxRsetPSM() {
        this(false);
    }
    
    public DataBoxRsetPSM(boolean mergedData) {
        super(DataboxType.DataBoxRsetPSM, DataboxStyle.STYLE_RSET);

        m_mergedData = mergedData;
        
        // Name of this databox
        m_typeName = "PSM";
        m_description = "All PSM of a Search Result";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSet.class, false);
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
        PeptideMatchPanel p = new PeptideMatchPanel(false, m_mergedData, true, true, false);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }
    
    @Override
    public void dataChanged() {
        
        final ResultSet _rset = (m_rset!=null) ? m_rset : (ResultSet) m_previousDataBox.getData(false, ResultSet.class);

        final int loadingId = setLoading();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {


                    DPeptideMatch[] peptideMatchArray = _rset.getTransientData().getPeptideMatches();
                    long[] peptideMatchIdArray = _rset.getTransientData().getPeptideMatchIds();
                    
                    ((PeptideMatchPanel)getDataBoxPanelInterface()).setData(taskId, peptideMatchArray, peptideMatchIdArray, finished);
               } else {
                    ((PeptideMatchPanel)getDataBoxPanelInterface()).dataUpdated(subTask, finished);
               }
               
               setLoaded(loadingId);
               
                if (finished) {
                    unregisterTask(taskId);

                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };
        

        // ask asynchronous loading of data
        DatabaseLoadPeptideMatchTask task = new DatabaseLoadPeptideMatchTask(callback, getProjectId(), _rset);
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
        if (parameterType!= null ) {
            if (parameterType.equals(DPeptideMatch.class)) {
                return ((PeptideMatchPanel)getDataBoxPanelInterface()).getSelectedPeptideMatch();
            }
            if (parameterType.equals(ResultSet.class)) {
                if (m_rset != null) {
                    return m_rset;
                }
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
 
    @Override
    public void setEntryData(Object data) {
        
        
        
        if (data instanceof ResultSet) {
            m_rset = (ResultSet) data;
            getDataBoxPanelInterface().addSingleValue(m_rset);
            dataChanged();
        } else if (data instanceof ResultSummary) {
            m_rset = ((ResultSummary) data).getResultSet();
            getDataBoxPanelInterface().addSingleValue(m_rset);
        }
    }
    
    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DPeptideMatch.class};
        return classList;
    }

    @Override
    public String getImportantOutParameterValue() {
        DPeptideMatch p = (DPeptideMatch) getData(false, DPeptideMatch.class);
        if (p != null) {
            Peptide peptide = p.getPeptide();
            if (peptide != null) {
                return peptide.getSequence();
            }
        }
        return null;
    }
}
