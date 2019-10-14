/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
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
