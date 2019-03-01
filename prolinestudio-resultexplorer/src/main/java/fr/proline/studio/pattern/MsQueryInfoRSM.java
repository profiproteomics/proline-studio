package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;

/**
 * information needed to display psm for msquery: msQuery and a resultSet and if filled the rsm
 * @author MB243701
 */
public class MsQueryInfoRSM {
    private DMsQuery m_msQuery;
    private ResultSummary m_resultSummary;

    
    public MsQueryInfoRSM(DMsQuery msQuery, ResultSummary resultSummary) {
        this.m_msQuery = msQuery;
        this.m_resultSummary = resultSummary;
    }

    public DMsQuery getMsQuery() {
        return m_msQuery;
    }

    public void setMsQuery(DMsQuery msQuery) {
        this.m_msQuery = msQuery;
    }

    public ResultSet getResultSet() {
        return m_resultSummary.getResultSet();
    }

    public ResultSummary getResultSummary() {
        return m_resultSummary;
    }

    public void setResultSummary(ResultSummary resultSummary) {
        this.m_resultSummary = resultSummary;
    }
    
    
}
