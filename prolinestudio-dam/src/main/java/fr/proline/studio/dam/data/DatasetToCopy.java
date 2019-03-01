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