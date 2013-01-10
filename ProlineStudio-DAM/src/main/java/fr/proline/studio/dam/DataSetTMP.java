package fr.proline.studio.dam;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Project;
import java.io.Serializable;

/**
 * TMP Class, will be replaced by DataSet in the ORM
 * @author JM235353
 */
public class DataSetTMP {
    
    // Transient Variables not saved in database
    /*@Transient*/ private TransientData transientData = null;
    
    public static final int SAMPLE_ANALYSIS = 0;
    public static final int QUANTITATION_FRACTION = 1;
    public static final int BIOLOGICAL_SAMPLE = 2;
    public static final int BIOLOGICAL_GROUP = 3;
    public static final int OTHER = 4;

    
    public Integer id;
    
    public Project project;
    
    public String name;
    
    public String description;
    
    public Integer resultSetId;
    
    public Integer resultSummaryId;
    
    public Integer parentDataSetId;
    
    public int aggregateType;
    
    public String getName() {
        return name;
    }
    
    public Integer getId() {
        return id;
    }
    
    public Integer getResultSummaryId() {
        return resultSummaryId;
    }
    
    public Integer getResultSetId() {
        return resultSetId;
    }
    
    
    public Integer getProjectId() {
        return project.getId();
    }
    
    public Integer getParentDatasetId() {
        return parentDataSetId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public TransientData getTransientData() {
    	if (transientData == null) {
    		transientData = new TransientData();
    	}
    	return transientData;
    }
    
    /**
     * Transient Data which will be not saved in database Used by the Proline
     * Studio IHM
     *
     * @author JM235353
     */
    public static class TransientData implements Serializable {

        private static final long serialVersionUID = 1L;
        private ResultSummary resultSummary = null;
        private ResultSet resultSet = null;


        protected TransientData() {
        }

        public ResultSummary getResultSummary() {  
            return resultSummary;
        }
        public void setResultSummary(ResultSummary resultSummary) {  
            this.resultSummary = resultSummary;
        }
        
        public ResultSet getResultSet() {  
            return resultSet;
        }
        public void setResultSet(ResultSet resultSet) {  
            this.resultSet = resultSet;
        }
    }

    
}
