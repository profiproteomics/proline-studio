/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.ResultSet;

/**
 * information needed to display psm for msquery: msQuery and a resultSet and if filled the rsm
 * @author MB243701
 */
public class MsQueryInfoRset {
    private MsQuery m_msQuery;
    private ResultSet m_resultSet;

    public MsQueryInfoRset(MsQuery msQuery, ResultSet resultSet) {
        this.m_msQuery = msQuery;
        this.m_resultSet = resultSet;
    }
    

    public MsQuery getMsQuery() {
        return m_msQuery;
    }

    public void setMsQuery(MsQuery msQuery) {
        this.m_msQuery = msQuery;
    }

    public ResultSet getResultSet() {
        return m_resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.m_resultSet = resultSet;
    }

    
}
