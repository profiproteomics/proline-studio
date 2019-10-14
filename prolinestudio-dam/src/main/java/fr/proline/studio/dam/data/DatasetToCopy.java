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
package fr.proline.studio.dam.data;

import fr.proline.core.orm.uds.Aggregation;
import java.util.ArrayList;

/**
 * Used for a copy/paste action
 *
 * @author JM235353
 */
public class DatasetToCopy {

    private static DatasetToCopy m_datasetCopied = null;
    
    private ArrayList<DatasetToCopy> m_children = new ArrayList<>();
    private long m_projectId;
    private Long m_resultSetId = null;
    private Aggregation.ChildNature m_datasetType = null;
    private String m_datasetName = null;

    public DatasetToCopy() {

    }
    
    public static void saveDatasetCopied(DatasetToCopy datasetCopied) {
        m_datasetCopied = datasetCopied;
    }
    public static DatasetToCopy getDatasetCopied() {
        return m_datasetCopied;
    }
    

    public void setProjectId(long projectId) {
        m_projectId = projectId;
    }

    public long getProjectId() {
        return m_projectId;
    }

    public void setResultSetId(Long resultSetId) {
        m_resultSetId = resultSetId;
    }

    public Long getResultSetId() {
        return m_resultSetId;
    }
    
    public void setDatasetType(Aggregation.ChildNature datasetType) {
        m_datasetType = datasetType;
    }

    public Aggregation.ChildNature getDatasetType() {
        return m_datasetType;
    }

    public void setName(String datasetName) {
        m_datasetName = datasetName;
    }

    public String getName() {
        return m_datasetName;
    }
    
    public void addChild(DatasetToCopy dataset) {
        m_children.add(dataset);
    }

    public ArrayList<DatasetToCopy> getChildren() {
        return m_children;
    }

  }