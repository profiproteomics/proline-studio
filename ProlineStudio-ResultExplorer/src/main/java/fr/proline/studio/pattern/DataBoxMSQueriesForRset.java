package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadMSQueriesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.MSQueriesPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * databox to display all msQueries
 * @author MB243701
 */
public class DataBoxMSQueriesForRset extends AbstractDataBox{

    private ResultSet m_rset = null;
    private List<DMsQuery> m_msQueriesList = null;
    private Map<Long, Integer> m_nbPeptideMatchesByMsQueryIdMap;
    
    public DataBoxMSQueriesForRset() {
        super(DataboxType.DataBoxMSQueriesForRset);

        // Name of this databox
        m_typeName = "MSQueries";
        m_description = "All MSQueries of a Search Result";
        
        // Register Possible in parameters
        // One ResultSet
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSet.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatchId for one msQuery
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DMsQuery.class, false);
        registerOutParameter(outParameter);
        
        
        outParameter = new GroupParameter();
        outParameter.addParameter(ResultSet.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(MsQueryInfoRset.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, false);
        registerOutParameter(outParameter);
       
    }
    
    
    @Override
    public void createPanel() {
        MSQueriesPanel p = new MSQueriesPanel(false);
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final ResultSet _rset =   ((m_rset!=null) ? m_rset : m_previousDataBox == null ? null : (ResultSet) m_previousDataBox.getData(false, ResultSet.class));

        final int loadingId = setLoading();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (subTask == null) {
                    
                    ((MSQueriesPanel)m_panel).setData(taskId,m_msQueriesList, m_nbPeptideMatchesByMsQueryIdMap,  finished);
               } else {
                    ((MSQueriesPanel)m_panel).dataUpdated(subTask, finished);
               }
               
               setLoaded(loadingId);
               
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };
        // ask asynchronous loading of data
        m_msQueriesList = new ArrayList();
        m_nbPeptideMatchesByMsQueryIdMap = new HashMap<>();
        DatabaseLoadMSQueriesTask task = new DatabaseLoadMSQueriesTask(callback);
        task.initLoadMSQueries( getProjectId(), _rset, m_msQueriesList, m_nbPeptideMatchesByMsQueryIdMap);
        registerTask(task);
    }
    
    @Override
    public void setEntryData(Object data) {
        
        m_panel.addSingleValue(data);
        
        if (data instanceof ResultSet) {
            m_rset = (ResultSet) data;
            dataChanged();
        }
    }
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(DMsQuery.class)) {
                return ((MSQueriesPanel)m_panel).getSelectedMsQuery();
            }
            if (parameterType.equals(ResultSet.class)) {
                return m_rset;
            }
            if (parameterType.equals(MsQueryInfoRset.class)) {
                DMsQuery msq = ((MSQueriesPanel)m_panel).getSelectedMsQuery();
                return new MsQueryInfoRset(msq, m_rset);
            }
            if(parameterType.equals(CompareDataInterface.class)){
                return ((GlobalTabelModelProviderInterface) m_panel).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface)m_panel).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
    
}
