package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.ResultSet;

/**
 * information needed to display psm for msquery: msQuery and a resultSet and if filled the rsm
 * @author MB243701
 */
public class MsQueryInfoRset {
    private DMsQuery m_msQuery;
    private ResultSet m_resultSet;

    public MsQueryInfoRset(DMsQuery msQuery, ResultSet resultSet) {
        this.m_msQuery = msQuery;
        this.m_resultSet = resultSet;
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

    
}
