package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadMSQueriesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.MSQueriesPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * databox to display all msQueries
 * @author MB243701
 */
public class DataBoxMSQueriesForRSM extends AbstractDataBox{

    private ResultSummary m_rsm = null;
    private List<DMsQuery> m_msQueriesList = null;
    private Map<Long, Integer> m_nbPeptideMatchesByMsQueryIdMap;
    
    public DataBoxMSQueriesForRSM() {
        super(DataboxType.DataBoxMSQueriesForRSM, DataboxStyle.STYLE_RSM);

        // Name of this databox
        m_typeName = "MSQueries";
        m_description = "All MSQueries of an Identification Summary";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        
        // Register possible out parameters
        // One or Multiple PeptideMatchId for one msQuery
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DMsQuery.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(ResultSet.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(MsQueryInfoRsm.class, false);
        registerOutParameter(outParameter);

       
    }
    
    
    @Override
    public void createPanel() {
        MSQueriesPanel p = new MSQueriesPanel(true);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        ResultSummary _rsm = (m_rsm!=null) ? m_rsm : m_previousDataBox == null ? null : (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
        final ResultSet _rset =  (_rsm != null)? _rsm.getResultSet() : ( m_previousDataBox == null ? null : (ResultSet) m_previousDataBox.getData(false, ResultSet.class));

        final int loadingId = setLoading();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (subTask == null) {
                    
                    ((MSQueriesPanel)getDataBoxPanelInterface()).setData(taskId,m_msQueriesList, m_nbPeptideMatchesByMsQueryIdMap,  finished);
               } else {
                    ((MSQueriesPanel)getDataBoxPanelInterface()).dataUpdated(subTask, finished);
               }
               
               setLoaded(loadingId);
               
                if (finished) {
                    unregisterTask(taskId);
                }
            }
        };
        // ask asynchronous loading of data
        m_msQueriesList = new ArrayList();
        m_nbPeptideMatchesByMsQueryIdMap= new HashMap<>();
        DatabaseLoadMSQueriesTask task = new DatabaseLoadMSQueriesTask(callback);
        task.initLoadMSQueries( getProjectId(), _rset, m_msQueriesList, m_nbPeptideMatchesByMsQueryIdMap);
        registerTask(task);
    }
    
    @Override
    public void setEntryData(Object data) {
        
        getDataBoxPanelInterface().addSingleValue(data);
        
        if (data instanceof ResultSummary) {
            m_rsm = (ResultSummary) data;
            dataChanged();
        }
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(DMsQuery.class)) {
                return ((MSQueriesPanel)getDataBoxPanelInterface()).getSelectedMsQuery();
            }
            if (parameterType.equals(ResultSummary.class)) {
                return m_rsm;
            }
            if (parameterType.equals(ResultSet.class)) {
                return m_rsm.getResultSet();
            }
            if (parameterType.equals(MsQueryInfoRsm.class)) {
                DMsQuery msq = ((MSQueriesPanel)getDataBoxPanelInterface()).getSelectedMsQuery();
                return new MsQueryInfoRsm(msq, m_rsm);
            }
        }
        return super.getData(getArray, parameterType);
    }
    
    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = { DMsQuery.class, MsQueryInfoRsm.class };
        return classList;
    }

    @Override
    public String getImportantOutParameterValue() {
        DMsQuery q = (DMsQuery) getData(false, DMsQuery.class);
        if (q != null) {
            int id = q.getInitialId();
            return String.valueOf(id);
        }
        return null;
    }
    
}
