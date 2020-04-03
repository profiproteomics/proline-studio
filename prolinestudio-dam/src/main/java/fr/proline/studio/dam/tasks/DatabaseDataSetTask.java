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
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.MergeMode;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.SearchSetting;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.ExternalDb;
import fr.proline.core.orm.uds.IdentificationDataset;
import fr.proline.core.orm.msi.SearchSettingsSeqDatabaseMap;
import fr.proline.core.orm.msi.Enzyme;
import fr.proline.core.orm.uds.*;

import fr.proline.core.orm.uds.repository.ExternalDbRepository;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.repository.AbstractDatabaseConnector;
import fr.proline.repository.ProlineDatabaseType;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType.QuantitationMethodInfo;
import fr.proline.core.orm.uds.dto.DDatasetType.AggregationInformation;
import fr.proline.core.orm.util.JsonSerializer;
import fr.proline.studio.dam.data.DatasetToCopy;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import java.io.IOException;
import javax.persistence.NoResultException;
import org.netbeans.api.db.explorer.DatabaseException;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;

/**
 * Used to load dataset in two cases : - parent dataset of a project - children
 * of a dataset
 *
 * @author JM235353
 */
public class DatabaseDataSetTask extends AbstractDatabaseTask {

    private Project m_project = null;
    private DDataset m_dataset = null;
    private ArrayList<DDataset> m_datasetList = null;
    private List<AbstractData> m_list = null;
    private ResultSummary m_rsm = null;
    private String m_name = null;
    private DDataset m_parentDataset = null;
    private Long m_resultSetId = null;
    private Long m_resultSummaryId = null;
    private Aggregation.ChildNature m_datasetType;
    private String m_aggregateName;
    private boolean m_hasSuffix = false;
    private int m_suffixStart = 0;
    private int m_suffixStop = 0;
    private Long m_datasetId = null;
    private List<Long> m_dsChildRSMIds = null;
    private List<String> m_dsNames = null;
    private boolean m_identificationDataset;
    private DatasetToCopy m_datasetCopy = null;
    
    private int m_action;

    private final static int LOAD_PARENT_DATASET = 0;
    private final static int LOAD_CHILDREN_DATASET = 1;
    private final static int LOAD_RSET_AND_RSM_OF_DATASET = 2;
    private final static int LOAD_DATASET_FOR_RSM = 3;
    private final static int RENAME_DATASET = 4;
    private final static int CREATE_AGGREGATE_DATASET = 5;
    private final static int CREATE_IDENTIFICATION_DATASET = 6;
    private final static int MODIFY_VALIDATED_DATASET = 7;
    private final static int REMOVE_VALIDATION_OF_DATASET = 8;
    private final static int MODIFY_MERGED_DATASET = 9;
    private final static int EMPTY_TRASH = 10;
    private final static int LOAD_DATASET_AND_RSM_INFO = 11;
    private final static int LOAD_DATASET = 12;
    private final static int CLEAR_DATASET_RSM_AND_RSET = 13;
    private final static int CLEAR_DATASET_RSM = 14;
    private final static int LOAD_QUANTITATION = 15;
    private final static int CREATE_QUANTITATION_FOLDER = 16;
    private final static int CREATE_IDENTIFICATION_FOLDER = 17;
    private final static int PASTE_DATASET = 18;

    private static final Object WRITE_DATASET_LOCK = new Object();

    public DatabaseDataSetTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }

    /**
     * Load all Parent Dataset of a project
     *
     * @param project
     * @param list
     * @param identificationDataset
     */
    public void initLoadParentDataset(Project project, List<AbstractData> list, boolean identificationDataset) {
        setTaskInfo(new TaskInfo("Load " + (identificationDataset ? "Identification" : "Quantitation") + " Data for Project " + project.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_project = project;
        m_list = list;
        m_identificationDataset = identificationDataset;
        m_action = LOAD_PARENT_DATASET;
        setPriority(Priority.HIGH_1);
    }

    /**
     * Load all children dataset of a dataset
     *
     * @param parentDataset
     * @param list
     * @param identificationDataset
     */
    public void initLoadChildrenDataset(DDataset parentDataset, List<AbstractData> list, boolean identificationDataset) {
        setTaskInfo(new TaskInfo("Load Data for Dataset " + parentDataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_project = parentDataset.getProject();
        m_parentDataset = parentDataset;
        m_list = list;
        m_identificationDataset = identificationDataset;
        m_action = LOAD_CHILDREN_DATASET;
        setPriority(Priority.HIGH_1);
    }

    /**
     * Load Rset and Rsm of a dataset
     *
     * @param dataset
     */
    public void initLoadRsetAndRsm(DDataset dataset) {
        setTaskInfo(new TaskInfo("Load Search Result and Identification Summary for Dataset " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_dataset = dataset;

        m_action = LOAD_RSET_AND_RSM_OF_DATASET;
    }

    /**
     * Load Quantitation of a dataset
     *
     * @param project
     * @param dataset
     */
    public void initLoadQuantitation(Project project, DDataset dataset) {
        setTaskInfo(new TaskInfo("Load Quantitation for Dataset " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_dataset = dataset;
        m_project = project;

        m_action = LOAD_QUANTITATION;
    }

    /**
     * Load Rset and Rsm of a dataset list
     *
     * @param datasetList
     */
    public void initLoadRsetAndRsm(ArrayList<DDataset> datasetList) {
        setTaskInfo(new TaskInfo("Load Search Result and Identification Summary for multiple Dataset", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));

        m_datasetList = datasetList;

        m_action = LOAD_RSET_AND_RSM_OF_DATASET;
    }

    /**
     * Load a dataset from its rsm
     *
     * @param rsm
     */
    public void initLoadDatasetForRsm(ResultSummary rsm) {
        setTaskInfo(new TaskInfo("Load data for Identification Summary " + rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_rsm = rsm;
        m_action = LOAD_DATASET_FOR_RSM;
    }

    /**
     * rename a dataset
     *
     * @param dataset
     * @param oldName
     * @param name
     */
    public void initRenameDataset(DDataset dataset, String oldName, String name) {
        setTaskInfo(new TaskInfo("Rename Dataset " + oldName + " to " + name, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_name = name;
        m_dataset = dataset;
        m_action = RENAME_DATASET;
    }

    public void initPasteDatasets(Project project, DDataset parentDataset, DatasetToCopy datasetCopy, ArrayList<DDataset> datasetList) {
        setTaskInfo(new TaskInfo("Paste Dataset ", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_project = project;
        m_parentDataset = parentDataset;
        m_datasetCopy = datasetCopy;
        m_action = PASTE_DATASET;
        m_datasetList = datasetList;
    }
    
    public void initCreateDatasetAggregate(Project project, DDataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, ArrayList<DDataset> datasetList) {
        initCreateDatasetAggregate(project, parentDataset, datasetType, aggregateName, false, 0, 0, datasetList);
    }

    public void initCreateDatasetAggregate(Project project, DDataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, boolean hasSuffix, int suffixStart, int suffixStop, ArrayList<DDataset> datasetList) {
        setTaskInfo(new TaskInfo("Create Dataset " + aggregateName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_project = project;
        m_parentDataset = parentDataset;
        m_datasetType = datasetType;
        m_aggregateName = aggregateName;
        m_hasSuffix = hasSuffix;
        m_suffixStart = suffixStart;
        m_suffixStop = suffixStop;
        m_datasetList = datasetList;
        m_action = CREATE_AGGREGATE_DATASET;
    }
    
    public void initCreateDatasetFolder(Project project, DDataset parentDataset, String folderName, boolean identification, ArrayList<DDataset> datasetList) {
        setTaskInfo(new TaskInfo("Create "+(identification ? "Identification" : "Quantitation")+" Folder " + folderName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_project = project;
        m_parentDataset = parentDataset;
        m_aggregateName = folderName;
        m_datasetList = datasetList;
        m_action = identification ? CREATE_IDENTIFICATION_FOLDER : CREATE_QUANTITATION_FOLDER;

    }

    public void initCreateDatasetForIdentification(Project project, DDataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, Long resultSetId, Long resultSummaryId, ArrayList<DDataset> datasetList, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        m_project = project;
        m_parentDataset = parentDataset;
        m_datasetType = datasetType;
        m_aggregateName = aggregateName;
        m_resultSetId = resultSetId;
        m_resultSummaryId = resultSummaryId;
        m_datasetList = datasetList;
        m_action = CREATE_IDENTIFICATION_DATASET;
    }

    public void initModifyDatasetForValidation(DDataset dataset, Long resultSummaryId, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        m_dataset = dataset;
        m_resultSummaryId = resultSummaryId;
        m_action = MODIFY_VALIDATED_DATASET;
    }

    public void initModifyDatasetToRemoveValidation(DDataset dataset) {
        setTaskInfo(new TaskInfo("Remove Identification Summary from Dataset " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        setPriority(Priority.HIGH_2);
        m_dataset = dataset;
        m_action = REMOVE_VALIDATION_OF_DATASET;
    }

    public void initModifyDatasetForMerge(DDataset dataset, Long resultSetId, Long resultSummaryId, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        m_dataset = dataset;
        m_resultSetId = resultSetId;
        m_resultSummaryId = resultSummaryId;
        m_action = MODIFY_MERGED_DATASET;
    }

    public void initEmptyTrash(DDataset trashDataset, boolean identificationDataset) {
        String type = Dataset.DatasetType.IDENTIFICATION.name();
        if (!identificationDataset) {
            type = Dataset.DatasetType.QUANTITATION.name();
        }
        setTaskInfo(new TaskInfo("Empty Trash " + type, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        setPriority(Priority.HIGH_2);
        m_dataset = trashDataset;
        m_identificationDataset = identificationDataset;
        m_action = EMPTY_TRASH;
    }

    /**
     * Load Dataset with specific ID
     *
     * @param datasetId Dataset Id
     * @param rsmIds
     * @param returnedDatasetList
     * @param returnedDatasetNames
     * @param project project Dataset belongs to
     */
    public void initLoadDatasetAndRSMInfo(Long datasetId, ArrayList<Long> rsmIds, ArrayList<DDataset> returnedDatasetList, ArrayList<String> returnedDatasetNames, Project project) {
        setTaskInfo(new TaskInfo("Load DataSet " + datasetId + " and get RSMs names", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_project = project;
        m_datasetId = datasetId;
        m_dsChildRSMIds = rsmIds;
        m_action = LOAD_DATASET_AND_RSM_INFO;
        m_datasetList = returnedDatasetList;
        m_dsNames = returnedDatasetNames;
        setPriority(Priority.HIGH_1);
    }

    public void initLoadDataset(Long datasetId, ArrayList<DDataset> returnedDatasetList) {
        setTaskInfo(new TaskInfo("Load DataSet " + datasetId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_datasetId = datasetId;
        m_datasetList = returnedDatasetList;
        m_action = LOAD_DATASET;
        setPriority(Priority.HIGH_1);
    }

    /**
     * Load Rset and Rsm of a dataset
     *
     * @param dataset
     * @param rsmAndRset
     */
    public void initClearDataset(DDataset dataset, boolean rsmAndRset) {
        setTaskInfo(new TaskInfo("Clear Dataset " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_dataset = dataset;

        m_action = (rsmAndRset) ? CLEAR_DATASET_RSM_AND_RSET : CLEAR_DATASET_RSM;
    }

    @Override
    public boolean needToFetch() {

        switch (m_action) {

            case LOAD_PARENT_DATASET:
            case LOAD_CHILDREN_DATASET:
                // this task is used only one time for each node
                return true;
            case LOAD_DATASET_FOR_RSM:
                return (m_rsm.getTransientData(TransientMemoryCacheManager.getSingleton()).getDataSet() == null);
            case LOAD_RSET_AND_RSM_OF_DATASET:
                if (m_datasetList != null) {
                    int nbDataSet = m_datasetList.size();
                    for (int i = 0; i < nbDataSet; i++) {
                        DDataset d = m_datasetList.get(i);
                        if (needToFetchRsetAndRsm(d)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return needToFetchRsetAndRsm(m_dataset);
                }
            case RENAME_DATASET:
            case PASTE_DATASET:
            case CREATE_AGGREGATE_DATASET:
            case CREATE_IDENTIFICATION_DATASET:
            case MODIFY_VALIDATED_DATASET:
            case REMOVE_VALIDATION_OF_DATASET:
            case MODIFY_MERGED_DATASET:
            case EMPTY_TRASH:
            case LOAD_DATASET_AND_RSM_INFO:
            case LOAD_DATASET:
            case CLEAR_DATASET_RSM_AND_RSET:
            case CLEAR_DATASET_RSM:
            case LOAD_QUANTITATION:
            case CREATE_QUANTITATION_FOLDER:
            case CREATE_IDENTIFICATION_FOLDER:
                return true; // done one time

        }

        return true; // should never be called
    }

    private boolean needToFetchRsetAndRsm(DDataset dataset) {
        if ((dataset.getResultSetId() != null) && (dataset.getResultSet() == null)) {
            // need to fetch a result set
            return true;
        }
        if ((dataset.getResultSummaryId() != null) && (dataset.getResultSummary() == null)) {
            // need to fetch a result summary
            return true;
        }
        return false;
    }

    @Override
    public boolean fetchData() {

        switch (m_action) {
            case LOAD_PARENT_DATASET:
                return fetchParentsDataSet();
            case LOAD_CHILDREN_DATASET:
                return fetchChildrenDataSet();
            case LOAD_RSET_AND_RSM_OF_DATASET:
                return fetchRsetAndRsm();
            case LOAD_DATASET_FOR_RSM:
                return fetchDatasetForRsm(m_rsm);
            case RENAME_DATASET:
                return renameDataset();
            case PASTE_DATASET:
                return pasteDataset();
            case CREATE_AGGREGATE_DATASET:
                return createDataset(false);
            case CREATE_IDENTIFICATION_DATASET:
                return createDataset(true);
            case MODIFY_VALIDATED_DATASET:
                return modifyDatasetRSM();
            case REMOVE_VALIDATION_OF_DATASET:
                return removeValidationOfDataset();
            case MODIFY_MERGED_DATASET:
                return modifyDatasetRsetAndRsm();
            case EMPTY_TRASH:
                return emptyTrash();
            case LOAD_DATASET_AND_RSM_INFO:
                return fetchDatasetWithIDAndRSMInfo();
            case LOAD_DATASET:
                return fetchDataset();
            case CLEAR_DATASET_RSM_AND_RSET:
            case CLEAR_DATASET_RSM:
                return clearDataset();
            case LOAD_QUANTITATION:
                return fetchQuantitation();
            case CREATE_QUANTITATION_FOLDER:
                return createDatasetFolder(false);
            case CREATE_IDENTIFICATION_FOLDER:
                return createDatasetFolder(true);
        }

        return false; // should never happen
    }

    public boolean fetchParentsDataSet() {

        long projectId = m_project.getId();

        ArrayList<Long> dsIdsList = new ArrayList<>();
        HashMap<Long, DDataset> ddatasetById = new HashMap<>();
        ArrayList<Long> rsetIdList = new ArrayList<>();

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            // ---- load parent DataSet -----
            //JPM.HACK : there is a join done by Hibernate if we read the Aggregation at once,
            // But some Aggregation are null (for identifications) -> identifications are not loaded
            // So we load aggregations afterwards
            List<DDataset> datasetListSelected = new ArrayList(); //Query result
            if (m_identificationDataset) {
                //Get Root DATASETs which isn't quanti (=> ident & trash & ident folder...)
                TypedQuery<DDataset> dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name, d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number) FROM Dataset d WHERE (d.parentDataset IS null) AND d.type<>:quantitationType AND d.type<>:quantitationType2 AND d.project.id=:projectId  ORDER BY d.number ASC", DDataset.class);
                dataSetQuery.setParameter("projectId", projectId);
                dataSetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION);
                dataSetQuery.setParameter("quantitationType2", Dataset.DatasetType.QUANTITATION_FOLDER);
                datasetListSelected = dataSetQuery.getResultList();

            } else {
                //Get Trash for current project
                TypedQuery<DDataset> dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name, d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number) FROM Dataset d WHERE (d.parentDataset IS null) AND (  d.type=:trashType)  AND d.project.id=:projectId  ORDER BY d.number ASC", DDataset.class);
                dataSetQuery.setParameter("projectId", projectId);
                dataSetQuery.setParameter("trashType", Dataset.DatasetType.TRASH);
                datasetListSelected.addAll(dataSetQuery.getResultList());
                
                //Get Quant dataset (with resultSummaryId from masterQuantChannel)
                dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name, d.type, d.childCount, d.resultSetId, mqc.quantResultSummaryId, d.number) FROM Dataset d, MasterQuantitationChannel mqc WHERE (d.parentDataset IS null) AND d.type=:quantitationType  AND d.project.id=:projectId AND mqc.quantDataset.id = d.id ORDER BY d.number ASC", DDataset.class);
                dataSetQuery.setParameter("projectId", projectId);
                dataSetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION);
                datasetListSelected.addAll(dataSetQuery.getResultList());
                
                //Get Quant Folder dataset 
                dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name, d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number) FROM Dataset d WHERE (d.parentDataset IS null) AND d.type=:quantitationType AND d.project.id=:projectId  ORDER BY d.number ASC", DDataset.class);
                dataSetQuery.setParameter("projectId", projectId);
                dataSetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION_FOLDER);
                datasetListSelected.addAll(dataSetQuery.getResultList());
                
                // sort according to dataset.number
                Collections.sort(datasetListSelected, new Comparator<DDataset>() {
                    @Override
                    public int compare(DDataset d2, DDataset d1) {

                        if (d2.isTrash()) {
                            return Integer.MAX_VALUE;
                        } else if (d1.isTrash()) {
                            return Integer.MIN_VALUE;
                        }

                        return d2.getNumber()-d1.getNumber();
                    }
                });
            }

            DDataset trash = null;
            Iterator<DDataset> it = datasetListSelected.iterator();
            while (it.hasNext()) {
                DDataset datasetCur = it.next();
                boolean isQuantiXIC = datasetCur.isQuantitation() && datasetCur.getQuantMethodInfo() == QuantitationMethodInfo.FEATURES_EXTRACTION;
                if (isQuantiXIC) {
                    datasetCur.setChildrenCount(1);
                }
                if (datasetCur.isTrash()) {
                    trash = datasetCur;
                } else {
                    DataSetData dsData = new DataSetData(datasetCur);
                    
                    if (isQuantiXIC) {
                        dsData.setHasChildren(true);
                    }
                    m_list.add(dsData);
                    Long resultSetId = datasetCur.getResultSetId();
                    if (resultSetId != null) {
                        rsetIdList.add(resultSetId);
                    }
                }

                Long id = datasetCur.getId();
                dsIdsList.add(id);
                ddatasetById.put(id, datasetCur);
            }

            // Load Aggregation and QuantitationMethod separately
            if (!m_list.isEmpty()) {
                loadDataSetSuppData(entityManagerUDS, dsIdsList, ddatasetById);
            }

            if (trash != null) {
                m_list.add(new DataSetData(trash));
            }

            if (m_identificationDataset) {
                m_project.getTransientData().setChildrenNumber(m_list.size());
            }

            // add the MSIdb connection to the Netbeans Service
            ExternalDb msiDb = ExternalDbRepository.findExternalByTypeAndProject(entityManagerUDS, ProlineDatabaseType.MSI, entityManagerUDS.merge(m_project));

            try {
                ConnectionManager cm = ConnectionManager.getDefault();
                JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers(DatabaseDataManager.getDatabaseDataManager().getUdsJdbcDriver())[0]; //JPM.WART : same driver for uds and msi
                String udsJdbcUrl = DatabaseDataManager.getDatabaseDataManager().getUdsJdbcURL();
                String msiJdbcUrl = udsJdbcUrl.substring(0, udsJdbcUrl.lastIndexOf('/') + 1) + msiDb.getDbName();

                String dbUser = (String)DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(projectId).getProperty(AbstractDatabaseConnector.PERSISTENCE_JDBC_USER_KEY);
                String dbPassword = (String)DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(projectId).getProperty(AbstractDatabaseConnector.PERSISTENCE_JDBC_PASSWORD_KEY);

                DatabaseConnection dbconn = DatabaseConnection.create(driver, msiJdbcUrl, dbUser, "public", dbPassword, true);

                cm.addConnection(dbconn);
            } catch (DatabaseException e) {

                String message = e.getMessage();
                if ((message == null) || (!message.contains("connection already exists"))) { //JPM.WART : avoid error because the connection already exist
                    m_logger.error(getClass().getSimpleName() + " failed to add UDS connection to Services ", e);
                }
            }

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        completeMergeInfo(projectId, rsetIdList);

        return true;
    }

    public boolean fetchChildrenDataSet() {

        long parentDatasetId = m_parentDataset.getId();

        ArrayList<Long> dsIdsList = new ArrayList<>();
        HashMap<Long, DDataset> ddatasetById = new HashMap<>();
        ArrayList<Long> rsetIdList = new ArrayList<>();

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            //JPM.HACK : there is a join done by Hibernate if we read the Aggregation at once,
            // But some Aggregation are null (for identifications) -> identifications are not loaded
            // So we load aggregations afterwards
             List<DDataset> datasetListSelected = new ArrayList(); //Query result
            if (m_parentDataset.isTrash()) {
               // TypedQuery<DDataset> datasetQuery2 = null;
                if (m_identificationDataset) {
                     //Get child DATASETs which isn't quanti (=> ident & trash & ident folder...)
                    TypedQuery<DDataset> datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId AND d.type<>:quantitationType AND d.type<>:quantitationType2 ORDER BY d.number ASC", DDataset.class);
                    datasetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION);
                    datasetQuery.setParameter("quantitationType2", Dataset.DatasetType.QUANTITATION_FOLDER);
                    datasetQuery.setParameter("parentDatasetId", parentDatasetId);                    
                    datasetListSelected = datasetQuery.getResultList();
                } else {
                    //Get Quant dataset (with resultSummaryId from masterQuantChannel)
                    TypedQuery<DDataset> datasetQuery  = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childCount, d.resultSetId, mqc.quantResultSummaryId, d.number)  FROM Dataset d, MasterQuantitationChannel mqc WHERE d.parentDataset.id=:parentDatasetId AND d.type=:quantitationType AND mqc.quantDataset.id = d.id ORDER BY d.number ASC", DDataset.class);
                    datasetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION);
                    datasetQuery.setParameter("parentDatasetId", parentDatasetId);
                    datasetListSelected.addAll(datasetQuery.getResultList());
                    
                    //Get Quant Folder dataset 
                    datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId AND d.type=:quantitationType2 ORDER BY d.number ASC", DDataset.class);                    
                    datasetQuery.setParameter("quantitationType2", Dataset.DatasetType.QUANTITATION_FOLDER);
                    datasetQuery.setParameter("parentDatasetId", parentDatasetId);
                    datasetListSelected.addAll(datasetQuery.getResultList());
                }

            } else {
                if (m_identificationDataset) {
                    //Get child DATASETs
                   TypedQuery<DDataset> datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId ORDER BY d.number ASC", DDataset.class);
                   datasetQuery.setParameter("parentDatasetId", parentDatasetId);
                   datasetListSelected.addAll(datasetQuery.getResultList());
                } else {
                    //Get child DATASETs (with resultSummaryId from masterQuantChannel)
                    TypedQuery<DDataset> datasetQuery =  entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childCount, d.resultSetId, mqc.quantResultSummaryId, d.number)  FROM Dataset d, MasterQuantitationChannel mqc WHERE d.parentDataset.id=:parentDatasetId AND mqc.quantDataset.id = d.id ORDER BY d.number ASC", DDataset.class);
                    datasetQuery.setParameter("parentDatasetId", parentDatasetId);
                    datasetListSelected.addAll(datasetQuery.getResultList());
                    
                    //Get QuantFolder DATASETs
                    datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.type=:datasetType AND d.parentDataset.id=:parentDatasetId ORDER BY d.number ASC", DDataset.class);
                    datasetQuery.setParameter("datasetType", Dataset.DatasetType.QUANTITATION_FOLDER);
                    datasetQuery.setParameter("parentDatasetId", parentDatasetId);
                    datasetListSelected.addAll(datasetQuery.getResultList());
                }
            }
            
            // sort according to dataset.number
            Collections.sort(datasetListSelected, new Comparator<DDataset>() {
                @Override
                public int compare(DDataset d2, DDataset d1) {

                    if (d2.isTrash()) {
                        return Integer.MAX_VALUE;
                    } else if (d1.isTrash()) {
                        return Integer.MIN_VALUE;
                    }

                    return d2.getNumber()-d1.getNumber();
                }
            });
            
            Iterator<DDataset> itDataset = datasetListSelected.iterator();
            while (itDataset.hasNext()) {
                DDataset datasetCur = itDataset.next();
                m_list.add(new DataSetData(datasetCur));

                Long id = datasetCur.getId();
                dsIdsList.add(id);
                ddatasetById.put(id, datasetCur);
                Long resultSetId = datasetCur.getResultSetId();
                if (resultSetId != null) {
                    rsetIdList.add(resultSetId);
                }
            }
            
            // Load Aggregation and QuantitationMethod separately
            if (!m_list.isEmpty()) 
                loadDataSetSuppData(entityManagerUDS, dsIdsList, ddatasetById);
            

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        completeMergeInfo(m_parentDataset.getProject().getId(), rsetIdList);

        return true;
    }

    /**
     *  Load Aggregation and QuantitationMethod for specied datasats and force XIC dataset to have a children to display the exp. design
     *  dsIdsList = List of Dataset to get information for    
     *  ddatasetById 
     */
    private void loadDataSetSuppData(EntityManager entityManagerUDS, List<Long> dsIdsList, Map<Long,DDataset> ddatasetById ) {

        // Load Aggregation
        Query aggregationQuery = entityManagerUDS.createQuery("SELECT d.id, d.aggregation FROM Dataset d WHERE d.id IN (:listId)");
        aggregationQuery.setParameter("listId", dsIdsList);
        List<Object[]> results = aggregationQuery.getResultList();
        Iterator<Object[]> it = results.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            Long id = (Long) resCur[0];
            Aggregation aggregation = (Aggregation) resCur[1];
            ddatasetById.get(id).setAggregation(aggregation);
        }

        // Load QuantitationMethod
        Query quantitationQuery = entityManagerUDS.createQuery("SELECT d.id, d.method FROM Dataset d WHERE d.id IN (:listId)");
        quantitationQuery.setParameter("listId", dsIdsList);
        results = quantitationQuery.getResultList();
        it = results.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            Long id = (Long) resCur[0];
            QuantitationMethod quantitationMethod = (QuantitationMethod) resCur[1];
            ddatasetById.get(id).setQuantitationMethod(quantitationMethod);
        }
                
        for (Map.Entry<Long, DDataset> entrySet : ddatasetById.entrySet()) {
            DDataset ds = entrySet.getValue();
            if (ds.isQuantitation()){
                if(ds.getQuantMethodInfo() == QuantitationMethodInfo.FEATURES_EXTRACTION) {
                    ds.setChildrenCount(1);
                }
                //Load ObjectTree linked to the dataset
                Dataset datasetDB = entityManagerUDS.find(Dataset.class, entrySet.getKey());
                setDDatasetQuantProperties(ds, datasetDB.getObjectTreeIdByName(), entityManagerUDS);
            }
        }        
    }

    private boolean completeMergeInfo(Long projectId, ArrayList<Long> rsetIdList) {
        if (!m_list.isEmpty() && !rsetIdList.isEmpty()) {
            // fetch if there is a merged rsm
            EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(projectId).createEntityManager();
            try {

                entityManagerMSI.getTransaction().begin();

                HashSet<Long> rsetFromMergedRsm = new HashSet<>();
                HashSet<Long> mergedRsmIds = new HashSet<>();
                HashMap<Long,MergeMode> mergeModeByRSId = new HashMap<>();
                HashMap<Long,MergeMode> mergeModeByRSMId = new HashMap<>();
                                 
                //Get ResultSet merge info & mode
                Query mergeInfoQuery = entityManagerMSI.createQuery("SELECT r.id, r.mergedRsmId, r.serializedProperties FROM ResultSet r WHERE r.id IN (:listId)"); //Add type != search ? 
                mergeInfoQuery.setParameter("listId", rsetIdList);
                List<Object[]> results = mergeInfoQuery.getResultList();
                Iterator<Object[]> itMergedInfo = results.iterator();               
                while (itMergedInfo.hasNext()) {
                    Object[] resCur = itMergedInfo.next();
                    Long rsetId = (Long) resCur[0];
                    Long mergedRsmId = (Long) resCur[1];
                    if (mergedRsmId != null) {
                        rsetFromMergedRsm.add(rsetId);
                        mergedRsmIds.add(mergedRsmId);
                    } else {
                        //result set from import or merge rs
                        String serializedProperties = (String)resCur[2];
                        Map<String, Object> serializedPropertiesMap = JsonSerializer.getMapper().readValue(serializedProperties,Map.class);
                        if(serializedPropertiesMap.containsKey("merge_mode")){
                            String mergeType = serializedPropertiesMap.get("merge_mode").toString().toUpperCase();
                            if(mergeType.equals(MergeMode.UNION.name()))
                                mergeModeByRSId.put(rsetId,MergeMode.UNION);
                            else
                                mergeModeByRSId.put(rsetId,MergeMode.AGGREGATION);
                        }
                    }
                }
                
                if(!mergedRsmIds.isEmpty()){
                    //Get ResultSummary merge mode
                    mergeInfoQuery = entityManagerMSI.createQuery("SELECT rsm.id, rsm.serializedProperties FROM ResultSummary rsm WHERE rsm.id IN (:listId)"); 
                    mergeInfoQuery.setParameter("listId", mergedRsmIds);
                    results = mergeInfoQuery.getResultList();
                    itMergedInfo = results.iterator();               
                    while (itMergedInfo.hasNext()) {
                        Object[] resCur = itMergedInfo.next();
                        Long rsmId = (Long) resCur[0];
                        String serializedProperties = (String)resCur[1];
                        Map<String, Object> serializedPropertiesMap = JsonSerializer.getMapper().readValue(serializedProperties,Map.class);
                        if(serializedPropertiesMap.containsKey("merge_mode")){
                            String mergeType = serializedPropertiesMap.get("merge_mode").toString().toUpperCase();
                            if(mergeType.equals(MergeMode.UNION.name()))
                                mergeModeByRSMId.put(rsmId,MergeMode.UNION);
                            else
                                mergeModeByRSMId.put(rsmId,MergeMode.AGGREGATION);
                        }                    
                    }
                }
                
                Iterator<AbstractData> it = m_list.iterator();
                while (it.hasNext()) {
                    AbstractData data = (AbstractData) it.next();

                    if (data.getDataType() != AbstractData.DataTypes.DATA_SET) {
                        continue;
                    }
                    
                    DDataset dataset = ((DataSetData) data).getDataset();
                    Long rsetId = dataset.getResultSetId();
                    Long rsmId = dataset.getResultSummaryId();
                    dataset.setAggregationInformation(AggregationInformation.UNKNOWN);
                    if (rsetFromMergedRsm.contains(rsetId)) {
                        //Merge RSM
                        if(mergeModeByRSMId.containsKey(rsmId)){
                            if(mergeModeByRSMId.get(rsmId).equals(MergeMode.AGGREGATION))
                                dataset.setAggregationInformation(AggregationInformation.IDENTIFICATION_SUMMARY_AGG);
                            else
                                dataset.setAggregationInformation(AggregationInformation.IDENTIFICATION_SUMMARY_UNION);
                        } 
                    } else if(mergeModeByRSId.containsKey(rsetId)){ //Merge RS
                        if(mergeModeByRSId.get(rsetId).equals(MergeMode.AGGREGATION))
                            dataset.setAggregationInformation(AggregationInformation.SEARCH_RESULT_AGG);
                        else
                            dataset.setAggregationInformation(AggregationInformation.SEARCH_RESULT_UNION);
                    } else {
                        
                    }
                }

                entityManagerMSI.getTransaction().commit();
            } catch (IOException e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerMSI.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerMSI.close();
            }
        }

        return true;
    }

    public boolean fetchRsetAndRsm() {

        long projectId = -1;
        if (m_dataset != null) {
            projectId = m_dataset.getProject().getId();
        } else if (m_datasetList != null) {
            projectId = m_datasetList.get(0).getProject().getId();
        }

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            if (m_datasetList != null) {
                int nbDataset = m_datasetList.size();
                for (int i = 0; i < nbDataset; i++) {
                    fetchRsetAndRsmForOneDataset(entityManagerMSI, m_datasetList.get(i));
                }

            } else if (m_dataset != null) {
                fetchRsetAndRsmForOneDataset(entityManagerMSI, m_dataset);
            }
            entityManagerMSI.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }

    private boolean fetchQuantitation() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_project.getId()).createEntityManager();
        EntityManager entityManagerLCMS = DStoreCustomPoolConnectorFactory.getInstance().getLcMsDbConnector(m_project.getId()).createEntityManager();
        try {

            entityManagerUDS.getTransaction().begin();
            entityManagerMSI.getTransaction().begin();
            entityManagerLCMS.getTransaction().begin();

            // force initialization of lazy data (data will be needed for the display of properties)
            Dataset datasetDB = entityManagerUDS.find(Dataset.class, m_dataset.getId());
            QuantitationMethod quantMethodDB = datasetDB.getMethod();
            List<MasterQuantitationChannel> listMasterQuantitationChannels = datasetDB.getMasterQuantitationChannels();
            List<QuantitationLabel> labels = quantMethodDB.getLabels();            
            // fill the current object with the db object
            quantMethodDB.setLabels(labels);
            m_dataset.setQuantitationMethod(quantMethodDB);
            m_dataset.setDescription(datasetDB.getDescription());

            DatabaseLoadXicMasterQuantTask.fetchDataQuantChannels(m_project.getId(), m_dataset, m_taskError);

            // load ObjectTree linked to the dataset
            setDDatasetQuantProperties(m_dataset, datasetDB.getObjectTreeIdByName(), entityManagerUDS);
            
            entityManagerUDS.getTransaction().commit();
            entityManagerMSI.getTransaction().commit();
            entityManagerLCMS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
                entityManagerMSI.getTransaction().rollback();
                entityManagerLCMS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
            entityManagerMSI.close();
            entityManagerLCMS.close();
        }

        return true;
    }
    
    private void setDDatasetQuantProperties(DDataset ddataset, Map<String, Long> objectTreeIdByName, EntityManager entityManagerUDS){
        if (objectTreeIdByName != null){
            for (Map.Entry<String, Long> entry: objectTreeIdByName.entrySet()) {
                if (entry.getKey().startsWith("quantitation")) {
                    Long objectId = entry.getValue();
                    fr.proline.core.orm.uds.ObjectTree objectTree = entityManagerUDS.find(fr.proline.core.orm.uds.ObjectTree.class, objectId);
                    ddataset.setObjectTree(objectTree);
                }
            }
        }

    }

    private void fetchRsetAndRsmForOneDataset(EntityManager entityManagerMSI, DDataset d) {

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
            Set<String> objTreeByName = rsmFound.getObjectTreeIdByName().keySet(); //to init Lazy map
            
            rsmFound.getTransientData(TransientMemoryCacheManager.getSingleton()).setDDataset(d);
            d.setResultSummary(rsmFound);
        }
    }

    private boolean fetchDatasetForRsm(ResultSummary rsm) {

        Long rsmId = rsm.getId();

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            // load dataset for rsm
            TypedQuery<Dataset> dataSetQuery = entityManagerUDS.createQuery("SELECT d FROM Dataset d WHERE d.resultSummaryId=:rsmId", Dataset.class);
            dataSetQuery.setParameter("rsmId", rsmId);
            Dataset dataSet = dataSetQuery.getSingleResult();
            rsm.getTransientData(TransientMemoryCacheManager.getSingleton()).setDataSet(dataSet);

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }

    private boolean fetchDataset() {

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

        try {
            entityManagerUDS.getTransaction().begin();

            // *** load dataset for specified id
            TypedQuery<DDataset> dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name, d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.id=:dsId", DDataset.class);
            dataSetQuery.setParameter("dsId", m_datasetId);
            DDataset ddataSet = dataSetQuery.getSingleResult();
            m_datasetList.add(ddataSet);

            if (ddataSet.isIdentification() && ddataSet.isAggregation()) {
                // Load Aggregation
                TypedQuery<Aggregation> aggregationQuery = entityManagerUDS.createQuery("SELECT d.aggregation FROM Dataset d WHERE d.id = :dsId", Aggregation.class);
                aggregationQuery.setParameter("dsId", m_datasetId);
                Aggregation aggregation = aggregationQuery.getSingleResult();
                ddataSet.setAggregation(aggregation);
            }

            if (ddataSet.isQuantitation()) {
                // Load QuantitationMethod
                TypedQuery<QuantitationMethod> quantitationQuery = entityManagerUDS.createQuery("SELECT d.method FROM Dataset d WHERE d.id = :dsId", QuantitationMethod.class);
                quantitationQuery.setParameter("dsId", m_datasetId);
                QuantitationMethod quantitationMethod = quantitationQuery.getSingleResult();
                ddataSet.setQuantitationMethod(quantitationMethod);
                
                Dataset datasetDB = entityManagerUDS.find(Dataset.class, m_datasetId);
                setDDatasetQuantProperties(ddataSet, datasetDB.getObjectTreeIdByName(), entityManagerUDS);
            }

            entityManagerUDS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }

    private boolean fetchDatasetWithIDAndRSMInfo() {

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

        try {
            entityManagerUDS.getTransaction().begin();

            // *** load dataset for specified id
            TypedQuery<DDataset> dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.id=:dsId", DDataset.class);
            dataSetQuery.setParameter("dsId", m_datasetId);
            DDataset ddataSet;
            try {
                ddataSet = dataSetQuery.getSingleResult();
                m_datasetList.add(ddataSet);
            } catch (NoResultException e) {
                m_logger.error(getClass().getSimpleName() + " failed -- Dataset with id=" + m_datasetId + " doesn't exist anymore in the database", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            }

            TypedQuery<Aggregation> aggregationQuery = entityManagerUDS.createQuery("SELECT d.aggregation FROM Dataset d WHERE d.id = :dsId", Aggregation.class);
            aggregationQuery.setParameter("dsId", m_datasetId);
            Aggregation aggregation = aggregationQuery.getSingleResult();
            ddataSet.setAggregation(aggregation);

            //m_datasetList.add(ddataSet);
            //****  Get all DS for searched RSMs
            Query rsmDSQuery = entityManagerUDS.createQuery("SELECT d.id, d.resultSummaryId FROM Dataset d WHERE d.resultSummaryId IN (:listId) and d.project.id = :pjId ");
            rsmDSQuery.setParameter("listId", m_dsChildRSMIds);
            rsmDSQuery.setParameter("pjId", m_project.getId());
            Map<Long, List<Long>> dsIdPerRSMIds = new HashMap<>();
            Iterator<Object[]> it = rsmDSQuery.getResultList().iterator();
            while (it.hasNext()) {
                Object[] resCur = it.next();
                Long dsId = (Long) resCur[0];
                Long rsmId = (Long) resCur[1];
                m_logger.debug("--- FOR RSM   " + rsmId + " DS = " + dsId);
                //A RSM could belong to multiple DS
                if (dsIdPerRSMIds.containsKey(rsmId)) {
                    dsIdPerRSMIds.get(rsmId).add(dsId);
                } else {
                    ArrayList<Long> rsmDSs = new ArrayList<>();
                    rsmDSs.add(dsId);
                    dsIdPerRSMIds.put(rsmId, rsmDSs);
                }
            }

            //****  Get all none root DS informations
            Query dsQueries = entityManagerUDS.createQuery("SELECT d.id, d.parentDataset.id, d.name FROM Dataset d WHERE d.project.id = :pjId and d.parentDataset is not null ");
            dsQueries.setParameter("pjId", m_project.getId());
            List<Object[]> results = dsQueries.getResultList();
            Map<Long, Object[]> dsInfoPerDsIds = new HashMap<>();
            Iterator<Object[]> it2 = results.iterator();
            while (it2.hasNext()) {
                Object[] resCur = it2.next();
                Long dsId = (Long) resCur[0];
                Object[] dsInfo = new Object[2];
                dsInfo[0] = (Long) resCur[1];
                dsInfo[1] = (String) resCur[2];
                dsInfoPerDsIds.put(dsId, dsInfo);
            }

            entityManagerUDS.getTransaction().commit();

            //*** Find DS of searched rsm and which belongs to specfied DS childs            
            for (Long nextRSMId : m_dsChildRSMIds) {

                boolean foundDS = false; // Correct DS found
                Iterator<Long> rsmDSsIdIt = dsIdPerRSMIds.get(nextRSMId).iterator();

                while (!foundDS && rsmDSsIdIt.hasNext()) {
                    Long nextDSId = rsmDSsIdIt.next(); // DS in hierarchy to test 
                    Long rsmDSId = nextDSId; // Keep DS assciated to RSM
                    while (!foundDS && nextDSId != null) {
                        if (m_datasetId.equals(nextDSId)) { // current DS RSM is the good one (in correct branch)
                            foundDS = true;
                            //Get initial DS for RSM
                            Object[] rsmDSInfo = dsInfoPerDsIds.get(rsmDSId);
                            if (rsmDSInfo == null) {
                                // RSM DS is root so should be reference DS! 
                                m_dsNames.add(ddataSet.getName());
                            } else {
                                m_dsNames.add((String) rsmDSInfo[1]);
                            }
                        } else {
                            //Get current DS info 
                            Object[] dsInfo = dsInfoPerDsIds.get(nextDSId);
                            if (dsInfo == null) { //No parent, root reached without finding ref DS. Search in other branch
                                nextDSId = null;
                            } else { // Set current DS = parent DS
                                nextDSId = (Long) dsInfo[0];
                            }
                        }
                    }// End search in current branch
                } //End search in all hierarchies for current RSM

            }//End go through all searched RSMs

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }

    private boolean renameDataset() {

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            String renameSQL = "UPDATE Dataset d set d.name= :name where d.id = :datasetId";
            Query renameQuery = entityManagerUDS.createQuery(renameSQL);
            renameQuery.setParameter("datasetId", m_dataset.getId());
            renameQuery.setParameter("name", m_name);
            renameQuery.executeUpdate();

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }

    private boolean clearDataset() {

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            String updateSQL;
            if (m_action == CLEAR_DATASET_RSM_AND_RSET) {
                updateSQL = "UPDATE Dataset d set d.resultSetId=null,d.resultSummaryId=null where d.id = :datasetId";
            } else {
                updateSQL = "UPDATE Dataset d set d.resultSummaryId=null where d.id = :datasetId";
            }
            Query updateQuery = entityManagerUDS.createQuery(updateSQL);
            updateQuery.setParameter("datasetId", m_dataset.getId());
            updateQuery.executeUpdate();

            entityManagerUDS.getTransaction().commit();
            m_dataset.setAggregationInformation(AggregationInformation.UNKNOWN);
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }

    private boolean pasteDataset() {
        synchronized (WRITE_DATASET_LOCK) {

            EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                long projectId = m_datasetCopy.getProjectId();
                Long resultSetId = m_datasetCopy.getResultSetId();
                
                Project mergedProject = entityManagerUDS.find(Project.class, projectId);
                Dataset mergedParentDataset = (m_parentDataset == null) ? null : entityManagerUDS.find(Dataset.class, m_parentDataset.getId());

                Dataset d;
                if (resultSetId != null) {
                    d = new IdentificationDataset();
                    d.setProject(mergedProject);
                    d.setType(Dataset.DatasetType.IDENTIFICATION);
                } else {
                    d = new Dataset(mergedProject);
                    d.setType(Dataset.DatasetType.AGGREGATE);

                    Aggregation aggregation = DatabaseDataManager.getDatabaseDataManager().getAggregation(m_datasetCopy.getDatasetType());
                    Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
                    d.setAggregation(mergedAggregation);
                }
                
                // number of children of the parent
                if (mergedParentDataset != null) {
                    mergedParentDataset.addChild(d);
                } else {
                    int childrenCount = m_project.getTransientData().getChildrenNumber();
                    d.setNumber(childrenCount);
                    m_project.getTransientData().setChildrenNumber(childrenCount + 1);
                }

                d.setName(m_datasetCopy.getName());
                d.setResultSetId(m_datasetCopy.getResultSetId());
                d.setChildrenCount(0); // this dataset has no child for the moment

                pasteDatasetAddChildrenImpl(entityManagerUDS, mergedProject, d, m_datasetCopy);
                
               entityManagerUDS.persist(d);
                if (mergedParentDataset != null) {
                    entityManagerUDS.merge(mergedParentDataset);
                }
                
                // we return only the top dataset created
                DDataset ddataset = new DDataset(d.getId(), d.getProject(), d.getName(), d.getType(), d.getChildrenCount(), d.getResultSetId(), d.getResultSummaryId(), d.getNumber());
                ddataset.setAggregation(d.getAggregation());

                m_datasetList.add(ddataset);
                
                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }

        return true;
    }
    
    private Dataset pasteDatasetAddChildrenImpl(EntityManager entityManagerUDS, Project mergedProject, Dataset d, DatasetToCopy datasetCopy) {
        ArrayList<DatasetToCopy> copiedChildren = datasetCopy.getChildren();
        for (DatasetToCopy copy : copiedChildren) {
            d.addChild(pasteDatasetImpl(entityManagerUDS, mergedProject, copy));
        }
        
        return d;
    }
    
    private Dataset pasteDatasetImpl(EntityManager entityManagerUDS, Project mergedProject, DatasetToCopy datasetCopy) {
        Long resultSetId = datasetCopy.getResultSetId();
        Dataset d;
        if (resultSetId != null) {
            d = new IdentificationDataset();
            d.setProject(mergedProject);
            d.setType(Dataset.DatasetType.IDENTIFICATION);
        } else {
            d = new Dataset(mergedProject);
            d.setType(Dataset.DatasetType.AGGREGATE);

            Aggregation aggregation = DatabaseDataManager.getDatabaseDataManager().getAggregation(m_datasetCopy.getDatasetType());
            Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
            d.setAggregation(mergedAggregation);
        }
        
        d.setName(datasetCopy.getName());
        d.setResultSetId(datasetCopy.getResultSetId());
        d.setChildrenCount(0); // this dataset has no child for the moment
        
        pasteDatasetAddChildrenImpl(entityManagerUDS, mergedProject, d, datasetCopy);
        
        entityManagerUDS.persist(d);
                
        return d;
    }
    
    private boolean createDataset(boolean identificationDataset) {

        synchronized (WRITE_DATASET_LOCK) {

            EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                if (m_hasSuffix) {
                    for (int i = m_suffixStart; i <= m_suffixStop; i++) {
                        String datasetName = m_aggregateName + String.valueOf(i);
                        createDataSetImpl(entityManagerUDS, datasetName, identificationDataset);
                    }

                } else {
                    createDataSetImpl(entityManagerUDS, m_aggregateName, identificationDataset);
                }

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }

        return true;
    }
    
    
            
    private boolean createDatasetFolder(boolean identification) {

        synchronized (WRITE_DATASET_LOCK) {

            EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                createFolderImpl(entityManagerUDS, m_aggregateName, identification);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }

        return true;
    }

    private void createFolderImpl(EntityManager entityManagerUDS, String folderName, boolean identification) {


        Project mergedProject = entityManagerUDS.find(Project.class, m_project.getId());

        Dataset mergedParentDataset = (m_parentDataset == null) ? null : entityManagerUDS.find(Dataset.class, m_parentDataset.getId());

        Dataset d = new Dataset(mergedProject);
        
        if (identification) {
            d.setType(Dataset.DatasetType.IDENTIFICATION_FOLDER);
        } else {
            d.setType(Dataset.DatasetType.QUANTITATION_FOLDER);
        }
        
        Aggregation aggregation = DatabaseDataManager.getDatabaseDataManager().getAggregation(Aggregation.ChildNature.OTHER);
        Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
         d.setAggregation(mergedAggregation);

        d.setName(folderName);
        d.setResultSetId(m_resultSetId);
        d.setResultSummaryId(m_resultSummaryId);
        d.setChildrenCount(0); // this aggregate has no child for the moment

        // number of children of the parent
        if (mergedParentDataset != null) {
            mergedParentDataset.addChild(d);
            /*try {
             m_parentDataset[0].addChild(d);
             } catch (org.hibernate.LazyInitializationException e) {
             // JPM.WART
             // if this exception happens : the children count has not been updated
             // I update it by hand
             m_parentDataset[0].setChildrenCount(mergedParentDataset.getChildrenCount());
                
             }*/
        } else {
            int childrenCount = m_project.getTransientData().getChildrenNumber();
            d.setNumber(childrenCount);
            m_project.getTransientData().setChildrenNumber(childrenCount + 1);
        }

        entityManagerUDS.persist(d);
        if (mergedParentDataset != null) {
            entityManagerUDS.merge(mergedParentDataset);
        }

        DDataset ddataset = new DDataset(d.getId(), d.getProject(), d.getName(), d.getType(), d.getChildrenCount(), d.getResultSetId(), d.getResultSummaryId(), d.getNumber());
        ddataset.setAggregation(d.getAggregation());

        m_datasetList.add(ddataset);
    }
    
    
    private void createDataSetImpl(EntityManager entityManagerUDS, String datasetName, boolean identificationDataset) {

        //JPM.TODO : reuse objects for multiple queries
        //Project mergedProject = entityManagerUDS.merge(m_project);
        Project mergedProject = entityManagerUDS.find(Project.class, m_project.getId());

        Dataset mergedParentDataset = (m_parentDataset == null) ? null : entityManagerUDS.find(Dataset.class, m_parentDataset.getId());

        Dataset d;
        if (identificationDataset) {
            d = new IdentificationDataset();
            d.setProject(mergedProject);
            d.setType(Dataset.DatasetType.IDENTIFICATION);
        } else {
            d = new Dataset(mergedProject);
            d.setType(Dataset.DatasetType.AGGREGATE);

            Aggregation aggregation = DatabaseDataManager.getDatabaseDataManager().getAggregation(m_datasetType);
            Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
            d.setAggregation(mergedAggregation);

        }

        d.setName(datasetName);
        d.setResultSetId(m_resultSetId);
        d.setResultSummaryId(m_resultSummaryId);
        d.setChildrenCount(0); // this aggregate has no child for the moment

        // number of children of the parent
        if (mergedParentDataset != null) {
            mergedParentDataset.addChild(d);
            /*try {
             m_parentDataset[0].addChild(d);
             } catch (org.hibernate.LazyInitializationException e) {
             // JPM.WART
             // if this exception happens : the children count has not been updated
             // I update it by hand
             m_parentDataset[0].setChildrenCount(mergedParentDataset.getChildrenCount());
                
             }*/
        } else {
            int childrenCount = m_project.getTransientData().getChildrenNumber();
            d.setNumber(childrenCount);
            m_project.getTransientData().setChildrenNumber(childrenCount + 1);
        }

        entityManagerUDS.persist(d);
        if (mergedParentDataset != null) {
            entityManagerUDS.merge(mergedParentDataset);
        }

        DDataset ddataset = new DDataset(d.getId(), d.getProject(), d.getName(), d.getType(), d.getChildrenCount(), d.getResultSetId(), d.getResultSummaryId(), d.getNumber());
        ddataset.setAggregation(d.getAggregation());

        m_datasetList.add(ddataset);
    }

    private boolean modifyDatasetRSM() {

        synchronized (WRITE_DATASET_LOCK) {

            EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                Dataset mergedDataset = entityManagerUDS.find(Dataset.class, m_dataset.getId());

                m_dataset.setResultSummaryId(m_resultSummaryId);
                mergedDataset.setResultSummaryId(m_resultSummaryId);

                entityManagerUDS.merge(mergedDataset);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }

        return true;
    }

    private boolean removeValidationOfDataset() {

        synchronized (WRITE_DATASET_LOCK) {

            EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                Dataset mergedDataset = entityManagerUDS.find(Dataset.class, m_dataset.getId());

                m_dataset.setResultSummaryId(null);
                mergedDataset.setResultSummaryId(null);

                m_dataset.setResultSummary(null);
                mergedDataset.getTransientData().setResultSummary(null);

                entityManagerUDS.merge(mergedDataset);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }

        return true;
    }

    private boolean modifyDatasetRsetAndRsm() {

        synchronized (WRITE_DATASET_LOCK) {

            EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                Dataset mergedDataset = entityManagerUDS.find(Dataset.class, m_dataset.getId());

                m_dataset.setResultSetId(m_resultSetId);
                mergedDataset.setResultSetId(m_resultSetId);

                m_dataset.setResultSummaryId(m_resultSummaryId);
                mergedDataset.setResultSummaryId(m_resultSummaryId);

                entityManagerUDS.merge(mergedDataset);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }

        return true;
    }

    private boolean emptyTrash() {
        synchronized (WRITE_DATASET_LOCK) {
            EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                Dataset mergedTrash = entityManagerUDS.find(Dataset.class, m_dataset.getId()); // dataset is the Trash
                removeChildren(entityManagerUDS, mergedTrash, m_identificationDataset);
                List<Dataset> children = mergedTrash.getChildren();
                mergedTrash.setChildren(children);
                mergedTrash.setChildrenCount(children == null ? 0 : children.size());
                if (children != null) {
                    int pos = 0;
                    for (Dataset child : children) {
                        child.setNumber(pos);
                        entityManagerUDS.persist(child);
                    }
                }
                entityManagerUDS.persist(mergedTrash);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error("updateDatasetAndProjectsTree failed", e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }

        return true;
    }

    private void removeChildren(EntityManager entityManagerUDS, Dataset d, boolean identificationTrash) {
        List<Dataset> children = d.getChildren();
        if (children != null) {
            int nbChildren = children.size();
            for (int i = nbChildren - 1; i >= 0; i--) {
                Dataset child = children.get(i);
                removeChildren(entityManagerUDS, child, identificationTrash);
                boolean isDatasetIdentification = (child.getMethod() == null);
                if ((isDatasetIdentification && identificationTrash) || (!isDatasetIdentification && !identificationTrash)) {
                    children.remove(child);
                }
            }
            //children.clear();
        }
        boolean isDatasetIdentification = (d.getMethod() == null);
        if ((isDatasetIdentification && identificationTrash) || (!isDatasetIdentification && !identificationTrash)) {
            entityManagerUDS.remove(d);
        }
    }

    /**
     * Called when the tree Project/Dataset has been modified by the user by
     * Drag & Drop
     *
     * @param databaseObjectsToModify HashMap whose keys can be Project or Parent Dataset
     * @param identificationTree
     * @return
     */
    public static boolean updateDatasetAndProjectsTree(LinkedHashMap<Object, ArrayList<DDataset>> databaseObjectsToModify, boolean identificationTree) {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            Iterator it = databaseObjectsToModify.keySet().iterator();
            while (it.hasNext()) {
                Object parentObject = it.next();
                ArrayList<DDataset> datasetList = databaseObjectsToModify.get(parentObject);

                int nbDataset = datasetList.size();
                ArrayList<Dataset> mergedDatasetList = new ArrayList<>(nbDataset);

                boolean childrenNotLoaded = false;
                for (int i = 0; i < nbDataset; i++) {
                    DDataset dataset = datasetList.get(i);
                    if (dataset == null) {
                        childrenNotLoaded = true; // some children have not been loaded already
                        continue;
                    }
                    Dataset mergedDataset = entityManagerUDS.find(Dataset.class, dataset.getId());
                    mergedDatasetList.add(mergedDataset);
                }

                DDataset parentDataset;
                Dataset mergedParentDataset = null;
                Project parentProject;
                //Project mergedParentProject = null;
                if (parentObject instanceof DDataset) {
                    parentDataset = (DDataset) parentObject;
                    mergedParentDataset = entityManagerUDS.find(Dataset.class, parentDataset.getId());

                    if (mergedParentDataset.getType() == Dataset.DatasetType.TRASH) {
                        if (identificationTree) {
                            // load quanti trash
                            TypedQuery<DDataset> datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId AND d.type=:quantitationType ORDER BY d.number ASC", DDataset.class);
                            datasetQuery.setParameter("parentDatasetId", mergedParentDataset.getId());
                            datasetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION);
                            List<DDataset> quantiInTrash = datasetQuery.getResultList();

                            mergedParentDataset.replaceAllChildren(mergedDatasetList);
                            for (Iterator<DDataset> qt = quantiInTrash.iterator(); qt.hasNext();) {
                                DDataset aQuantiInTrash = qt.next();
                                Dataset wholeDataset = entityManagerUDS.find(Dataset.class, aQuantiInTrash.getId());
                                mergedParentDataset.addChild(wholeDataset);
                            }

                        } else {
                            // load identification trash
                            TypedQuery<DDataset> datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId AND d.type<>:quantitationType ORDER BY d.number ASC", DDataset.class);

                            datasetQuery.setParameter("parentDatasetId", mergedParentDataset.getId());
                            datasetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION);
                            List<DDataset> identificationInTrash = datasetQuery.getResultList();

                            mergedParentDataset.replaceAllChildren(mergedDatasetList);
                            for (Iterator<DDataset> qt = identificationInTrash.iterator(); qt.hasNext();) {
                                DDataset aIdentificationInTrash = qt.next();
                                Dataset wholeDataset = entityManagerUDS.find(Dataset.class, aIdentificationInTrash.getId());
                                mergedParentDataset.addChild(wholeDataset);
                            }
                        }
                        // change number position for children
                        List<Dataset> childs = mergedParentDataset.getChildren();
                        int pos = 0;
                        for (Dataset dataset : childs) {
                            dataset.setNumber(pos);
                            pos++;
                        }
                    } else if (childrenNotLoaded) {
                        for (int i = 0; i < mergedDatasetList.size(); i++) {
                            mergedParentDataset.addChild(mergedDatasetList.get(i));
                        }
                    } else {
                        mergedParentDataset.replaceAllChildren(mergedDatasetList);
                    }

                    parentDataset.setChildrenCount(mergedParentDataset.getChildrenCount());

                } else if (parentObject instanceof Project) {
                    parentProject = (Project) parentObject;
                    if (identificationTree) {
                        parentProject.getTransientData().setChildrenNumber(nbDataset);
                    }

                    // order the children which can have been moved
                    for (int i = 0; i < mergedDatasetList.size(); i++) {
                        Dataset mergedChildDataset = mergedDatasetList.get(i);
                        mergedChildDataset.setNumber(i);
                    }
                }

                for (int i = 0; i < mergedDatasetList.size(); i++) {
                    Dataset mergedDataset = mergedDatasetList.get(i);
                    entityManagerUDS.merge(mergedDataset);
                }
                if (mergedParentDataset != null) {
                    entityManagerUDS.merge(mergedParentDataset);
                }

            }

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error("updateDatasetAndProjectsTree failed", e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(DatabaseDataSetTask.class.getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
}
