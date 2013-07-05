package fr.proline.studio.dam.data;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import java.util.List;

/**
 * Data for Dataset Node
 * @author JM235353
 */
public class DataSetData extends AbstractData {
    
    private Dataset m_dataset = null;
    private String m_temporaryName = null;
    private Aggregation.ChildNature m_temporaryAggregateType = null;
    private Dataset.DatasetType m_temporaryDatasetType = null;

    public DataSetData(Dataset dataSet) {
        m_dataType = DataTypes.DATA_SET;

        m_dataset = dataSet;

    }
    public DataSetData(String temporaryName, Dataset.DatasetType temporaryDatasetType, Aggregation.ChildNature temporaryAggregateType) {
        m_dataType = DataTypes.DATA_SET;

        m_temporaryName = temporaryName;
        m_temporaryAggregateType = temporaryAggregateType;
        m_temporaryDatasetType = temporaryDatasetType;

    }

    public Dataset getDataset() {
        return m_dataset;
    }
    
    public void setDataset(Dataset dataset) {
        this.m_dataset = dataset;
        m_temporaryName = null;
    }
    
    @Override
    public boolean hasChildren() {
        if (m_dataset != null) {
            return (m_dataset.getChildrenCount()>0);
        }
        return false;
    }
    
    @Override
    public String getName() {
        if (m_dataset == null) {
            if (m_temporaryName != null) {
                return m_temporaryName;
            } else {
                return "";
            }
        } else {
            return m_dataset.getName();
        }
    }

    public Aggregation.ChildNature getAggregateType() {
        if (m_dataset == null) {
            return m_temporaryAggregateType;
        }
        Aggregation aggreation = m_dataset.getAggregation();
        if (aggreation == null) {
            return m_temporaryAggregateType;
        }
        return aggreation.getChildNature();
    }
    
    public Dataset.DatasetType getDatasetType() {
        if (m_dataset == null) {
            return m_temporaryDatasetType;
        }
        return m_dataset.getType();
    }
    
    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadChildrenDataset(m_dataset, list);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



    }
}
