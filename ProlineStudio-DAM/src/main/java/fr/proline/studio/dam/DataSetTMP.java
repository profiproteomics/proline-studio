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
    
    
    public Integer id;
    
    public Project project;
    
    public String name;
    
    public String description;
    
    public Integer resultSetId;
    
    public Integer resultSummaryId;
    
    public Integer parentDataSetId;
    
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
