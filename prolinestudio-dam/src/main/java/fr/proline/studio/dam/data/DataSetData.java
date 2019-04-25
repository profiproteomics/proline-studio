package fr.proline.studio.dam.data;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.core.orm.uds.dto.DDatasetType.QuantitationMethodInfo;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;

import javax.persistence.EntityManager;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Data for Dataset Node
 *
 * @author JM235353
 */
public class DataSetData extends AbstractData {

    private DDataset m_dataset = null;
    private String m_temporaryName = null;
    private DDatasetType m_temporaryDatasetType = null;
    private Integer m_channelNumber = null;

    public Integer getChannelNumber() {
        return m_channelNumber;
    }

    public void setChannelNumber(Integer m_channelNumber) {
        this.m_channelNumber = m_channelNumber;
    }
    

    public static DataSetData createTemporaryFolder(String name) {
        DDatasetType type = new DDatasetType(null, Dataset.DatasetType.IDENTIFICATION_FOLDER, null, null);
        return new DataSetData(name, type);
    }

    public static DataSetData createTemporaryIdentification(String name) {
        DDatasetType type = new DDatasetType(null, Dataset.DatasetType.IDENTIFICATION, null, null);
        return new DataSetData(name, type);
    }

    public static DataSetData createTemporaryAggregate(String name) {
        DDatasetType type = new DDatasetType(null, Dataset.DatasetType.AGGREGATE, null, null);
        return new DataSetData(name, type);
    }

    public static DataSetData createTemporaryQuantitation(String name) {
        DDatasetType type = new DDatasetType(null, Dataset.DatasetType.QUANTITATION, null, null);
        return new DataSetData(name, type);
    }

    public DataSetData(DDataset dataSet) {
        m_dataType = DataTypes.DATA_SET;
        m_dataset = dataSet;
    }

    private DataSetData(String temporaryName, DDatasetType temporaryType) {
        m_dataType = DataTypes.DATA_SET;
        m_temporaryName = temporaryName;
        m_temporaryDatasetType = temporaryType;
    }

    public DDataset getDataset() {
        return m_dataset;
    }

    public void setDataset(DDataset dataset) {
        m_dataset = dataset;
        m_temporaryName = null;
        m_temporaryDatasetType = null;
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

    public String getTemporaryName() {
        return m_temporaryName;
    }

    @Override
    public String toString() {
        return getName();
    }

    public DDatasetType getDatasetType() {
        if (m_dataset == null) {
            return m_temporaryDatasetType;
        }
        return m_dataset.getType();
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list, AbstractDatabaseTask.Priority priority, boolean identificationDataset) {

        if (!identificationDataset && m_dataset.getType().getQuantMethodInfo() == QuantitationMethodInfo.FEATURES_EXTRACTION) {
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
        } finally {
            entityManagerMSI.close();
        }
    }

}
