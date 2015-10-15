package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;

/**
 * information needed to display psm for msquery: msQuery and a resultSet and if filled the rsm
 * @author MB243701
 */
public class MsQueryInfo {
    private DMsQuery m_msQuery;
    private ResultSet m_resultSet;
    private ResultSummary m_resultSummary;

    public MsQueryInfo(DMsQuery msQuery, ResultSet resultSet) {
        m_msQuery = msQuery;
        m_resultSet = resultSet;
        m_resultSummary = null;
    }
    
    public MsQueryInfo(DMsQuery msQuery, ResultSummary resultSummary) {
        m_msQuery = msQuery;
        m_resultSet = resultSummary.getResultSet();
        m_resultSummary = resultSummary;
    }

    public DMsQuery getMsQuery() {
        return m_msQuery;
    }

    public void setMsQuery(DMsQuery msQuery) {
        this.m_msQuery = msQuery;
    }

    public ResultSet getResultSet() {
        return m_resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.m_resultSet = resultSet;
    }

    public ResultSummary getResultSummary() {
        return m_resultSummary;
    }

    public void setResultSummary(ResultSummary resultSummary) {
        this.m_resultSummary = resultSummary;
    }
    
    
}
