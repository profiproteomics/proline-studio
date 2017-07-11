package fr.proline.studio.dam.data;

import fr.proline.core.orm.msi.Enzyme;
import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.SearchSetting;
import fr.proline.core.orm.msi.SearchSettingsSeqDatabaseMap;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;

/**
 * Data for Dataset Node
 *
 * @author JM235353
 */
public class DataSetData extends AbstractData {

    private DDataset m_dataset = null;
    private String m_temporaryName = null;
    private Aggregation.ChildNature m_temporaryAggregateType = null;
    private Dataset.DatasetType m_temporaryDatasetType = null;

    public DataSetData(DDataset dataSet) {
        m_dataType = DataTypes.DATA_SET;
        m_dataset = dataSet;
    }

    public DataSetData(String temporaryName, Dataset.DatasetType temporaryDatasetType, Aggregation.ChildNature temporaryAggregateType) {
        m_dataType = DataTypes.DATA_SET;

        m_temporaryName = temporaryName;
        m_temporaryAggregateType = temporaryAggregateType;
        m_temporaryDatasetType = temporaryDatasetType;

    }

    public DDataset getDataset() {
        return m_dataset;
    }

    public void setDataset(DDataset dataset) {
        m_dataset = dataset;
        m_temporaryName = null;
    }

    @Override
    public boolean hasChildren() {
        if (m_dataset != null) {
            return (m_dataset.getChildrenCount() > 0);
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

    public void setTemporaryName(String name) {
        m_temporaryName = name;
    }

    @Override
    public String toString() {
        return getName();
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
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list, AbstractDatabaseTask.Priority priority, boolean identificationDataset) {

        if (!identificationDataset && m_dataset.isQuantiXIC()) {
            DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
            task.initLoadQuantChannels(m_dataset.getProject().getId(), m_dataset);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        } else {
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);

            task.initLoadChildrenDataset(m_dataset, list, identificationDataset);
            if (priority != null) {
                task.setPriority(priority);
            }
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }

    }

    public static void fetchRsetAndRsmForOneDataset(DDataset d) {
        
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(d.getProject().getId()).createEntityManager();

        try {

            entityManagerMSI.getTransaction().begin();

            Long rsetId = d.getResultSetId();
            if (rsetId != null) {
                ResultSet rsetFound = entityManagerMSI.find(ResultSet.class, rsetId);

                // force initialization of lazy data (data will be needed for the display of properties)
                MsiSearch msiSearch = rsetFound.getMsiSearch();

                if (msiSearch != null) {
                    SearchSetting searchSetting = msiSearch.getSearchSetting();
                    Set<Enzyme> enzymeSet = searchSetting.getEnzymes();
                    Iterator<Enzyme> it = enzymeSet.iterator();
                    while (it.hasNext()) {
                        it.next();
                    }

                    Set<SearchSettingsSeqDatabaseMap> searchSettingsSeqDatabaseMapSet = searchSetting.getSearchSettingsSeqDatabaseMaps();
                    Iterator<SearchSettingsSeqDatabaseMap> itSeqDbMap = searchSettingsSeqDatabaseMapSet.iterator();
                    while (itSeqDbMap.hasNext()) {
                        itSeqDbMap.next();

                    }
                }

                d.setResultSet(rsetFound);
            }

            Long rsmId = d.getResultSummaryId();
            if (rsmId != null) {
                ResultSummary rsmFound = entityManagerMSI.find(ResultSummary.class, rsmId);

                rsmFound.getTransientData().setDDataset(d);
                d.setResultSummary(rsmFound);
            }
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            ;
        } finally {
            entityManagerMSI.close();
        }
    }

}
