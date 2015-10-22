package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * display Peptide Matches for a given MsQuey
 * @author MB243701
 */
public class DataboxRsetPSMForMsQuery extends AbstractDataBox{
    
    private DMsQuery m_msQuery;
    private ResultSet m_rset;
    private List<DPeptideMatch> m_peptideMatches;
    
    private boolean m_mergedData;

        
    public DataboxRsetPSMForMsQuery() {
        this(false);
    }
    
    public DataboxRsetPSMForMsQuery(boolean mergedData) {
        
        super(DataboxType.DataBoxRsetPSMForMsQuery);
        
        m_mergedData = mergedData;
        
        // Name of this databox
        m_typeName = "PSM";
        m_description = "All PSM corresponding to a MsQuery for a Search Result";
        
        
        // Register Possible in parameters
        // One MsQuery & rsm & rs, rsm could be null
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(MsQueryInfoRset.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, false);
        registerOutParameter(outParameter);
    }
    
    @Override
    public void createPanel() {
        PeptideMatchPanel p = new PeptideMatchPanel(false, m_mergedData, false, true, true); 
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }


    @Override
    public void dataChanged() {
        long oldMsQId = m_msQuery == null? -1: m_msQuery.getId();
        final MsQueryInfoRset _msqI = (MsQueryInfoRset) m_previousDataBox.getData(false, MsQueryInfoRset.class);
        if (_msqI != null && _msqI.getMsQuery() != null && oldMsQId == _msqI.getMsQuery().getId()){
            return ;
        }
        m_msQuery = _msqI.getMsQuery();
        m_rset = _msqI.getResultSet();

        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {
                   int nb = m_peptideMatches.size();
                    DPeptideMatch[] peptideMatchArray = new DPeptideMatch[nb];
                    long[] peptideMatchIdArray = new long[nb];
                    for(int i=0; i<nb; i++){
                        peptideMatchArray[i] = m_peptideMatches.get(i);
                        peptideMatchIdArray[i] = m_peptideMatches.get(i).getId();
                    }
                    ((PeptideMatchPanel)m_panel).setData(taskId, peptideMatchArray, peptideMatchIdArray, finished);
               } else {
                    ((PeptideMatchPanel)m_panel).dataUpdated(subTask, finished);
                }
               
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };
        

        // ask asynchronous loading of data
        m_peptideMatches = new ArrayList();
        if (m_msQuery != null){
            registerTask(new DatabaseLoadPeptideMatchTask(callback, getProjectId(), m_msQuery, null, m_rset, m_peptideMatches));
        }

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
 
    @Override
    public void setEntryData(Object data) {
        
        m_panel.addSingleValue(data);
        
        if (data instanceof MsQueryInfoRset) {
            MsQueryInfoRset o = (MsQueryInfoRset) data;
            m_msQuery = o.getMsQuery();
            m_panel.addSingleValue(m_msQuery);
            m_rset = o.getResultSet();
            m_panel.addSingleValue(m_rset);
            dataChanged();
        }
    }
}
