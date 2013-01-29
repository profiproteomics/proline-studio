package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 * Node for Dataset
 * @author JM235353
 */
public class RSMDataSetNode extends RSMNode {

   
    
    
    public RSMDataSetNode(AbstractData data) {
        super(NodeTypes.DATA_SET, data);
    }

    @Override
    public ImageIcon getIcon() {


        
        //JPM.TODO : icon management of Dataset
        int aggreagateType;
        if (isChanging()) {
            aggreagateType = ((DataSetData) getData()).getAggregateType();
            
            switch (aggreagateType) {
                case DataSetTMP.BIOLOGICAL_GROUP:
                    return getIcon(IconManager.IconType.VIAL);
                case DataSetTMP.BIOLOGICAL_SAMPLE:
                    return getIcon(IconManager.IconType.GEL);
                case DataSetTMP.SAMPLE_ANALYSIS:
                    return getIcon(IconManager.IconType.GEL);
            }

            return getIcon(IconManager.IconType.RSET);
            
            
        } else {
            DataSetTMP dataSet = ((DataSetData) getData()).getDataSet();
            aggreagateType = dataSet.aggregateType;

            switch (aggreagateType) {
                case DataSetTMP.BIOLOGICAL_GROUP:
                    return getIcon(IconManager.IconType.VIAL);
                case DataSetTMP.BIOLOGICAL_SAMPLE:
                    return getIcon(IconManager.IconType.GEL);
                case DataSetTMP.SAMPLE_ANALYSIS:
                    return getIcon(IconManager.IconType.GEL);
            }

            if (dataSet.getResultSummaryId() != null) {
                return getIcon(IconManager.IconType.RSM);
            } else if (dataSet.getResultSetId() != null) {
                return getIcon(IconManager.IconType.RSET);
            }
            
        }
        
        
        

        //JPM.TODO : return another icon for OTHER ???
        return getIcon(IconManager.IconType.GEL);

    }
    

    
    public DataSetTMP getDataSet() {
        return ((DataSetData) getData()).getDataSet();
    }
    
    public boolean hasResultSummary() {
        DataSetTMP dataSet = ((DataSetData) getData()).getDataSet();
        return (dataSet.getResultSummaryId() != null);
    }
    

    public Integer getResultSummaryId() {
        return ((DataSetData) getData()).getDataSet().getResultSummaryId();
    }
    
    public ResultSummary getResultSummary() {
        // getResultSummary() can return null if the resultSummary has not been loaded previously
        DataSetTMP dataSet = ((DataSetData) getData()).getDataSet();
        return dataSet.getTransientData().getResultSummary();
    }
    
    
    public boolean hasResultSet() {
        DataSetTMP dataSet = ((DataSetData) getData()).getDataSet();
        return (dataSet.getResultSetId() != null);
    }
    
    public Integer getResultSetId() {
        return ((DataSetData) getData()).getDataSet().getResultSetId();
    }
    
    public ResultSet getResultSet() {
        // getResultSet() can return null if the resultSet has not been loaded previously
        DataSetTMP dataSet = ((DataSetData) getData()).getDataSet();
        return dataSet.getTransientData().getResultSet();
    }
    
    @Override
    public boolean canBeDeleted() {
        
        // for the moment, we can delete only empty DataSet with no leaf
        if (hasResultSet()) {
            return false;
        }
        if (hasResultSummary()) {
            return false;
        }
        
        return isLeaf();
        
    }
    
    
}
