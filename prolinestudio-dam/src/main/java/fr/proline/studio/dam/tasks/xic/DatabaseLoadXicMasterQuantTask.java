/* 
 * Copyright (C) 2019
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
package fr.proline.studio.dam.tasks.xic;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.msi.dto.*;
import fr.proline.core.orm.msi.dto.MasterQuantProteinSetProperties.MasterQuantProteinSetProfile;
import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.GroupSetup;
import fr.proline.core.orm.uds.MasterQuantitationChannel;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.QuantitationLabel;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType.QuantitationMethodInfo;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.core.orm.uds.SampleAnalysis;
import fr.proline.core.orm.util.JsonSerializer;
import fr.proline.studio.corewrapper.data.QuantPostProcessingParams;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.performancetest.PerformanceTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseLoadXicMasterQuantTask extends AbstractDatabaseSlicerTask {

    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 1000;

    // different possible subtasks
    public static final int NO_SUB_TASK = 0;// <<----- get in sync

    public static final int SUB_TASK_PROTEIN_SET = 0;
    public static final int NBR_SUB_TASK_PROTEIN = 1; // <<----- get in sync

    public static final int SUB_TASK_PEPTIDE_INSTANCE = 0;
    public static final int NBR_SUB_TASK_PEPTIDE = 1; // <<----- get in sync

    public static final int SUB_TASK_PEPTIDE_ION = 0;
    public static final int NBR_SUB_TASK_PEPTIDE_ION = 1;

    public static final int SUB_TASK_REPORTER_ION = 0;
    public static final int NBR_SUB_TASK_REPORTER_ION = 1;// <<----- get in sync

    public static final int SUB_TASK_PEPTIDE = 0;
    public static final int SUB_TASK_MSQUERY = 1;
    public static final int SUB_TASK_PROTEINSET_NAME_LIST = 2;
    public static final int NBR_SUB_TASK_PSM = 3;// <<----- get in sync


    // data kept for sub tasks
    private List<Long> m_proteinSetIds = null;
    private List<Long> m_resultSetIds = null;

    private List<Long> m_peptideInstanceIds = null;

    private List<Long> m_masterQuantPeptideIonIds = null;

    private Long m_projectId;
    private DDataset m_dataset;
    private List<DMasterQuantProteinSet> m_masterQuantProteinSetList;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;
    private DMasterQuantProteinSet m_dMasterQuantProteinSet;
    private Long[] m_peptideInstanceIdArray;
    private DMasterQuantPeptide m_masterQuantPeptide;
    private DMasterQuantPeptideIon m_specifiedMasterQuantPeptideIon;
    private List<DMasterQuantPeptideIon> m_masterQuantPeptideIonList;
    private List<MasterQuantReporterIon> m_masterQuantReporterIonList;
    private List<Long> m_masterQuantReporterIonIds = null;

    private HashMap<Long, DQuantitationChannel> m_quantitationChannelsMap;
    private ArrayList<Long> m_childrenDatasetIds;

    private DMasterQuantPeptide m_masterQuantPeptideForPSM;
    private Map<Long, List<Long>> m_psmIdPerQC;
    private List<DPeptideMatch> m_peptideMatchList;
    private boolean m_xic;

    // data kept for sub tasks
    private ArrayList<Long> m_peptideMatchIds = null;
    private HashMap<Long, DPeptideMatch> m_peptideMatchMap = null;
    private HashMap<Long, ArrayList<DPeptideMatch>> m_peptideMatchSequenceMatchArrayMap = null;

    private int action;
    private static final int LOAD_PROTEIN_SET_FOR_XIC = 0;
    private static final int LOAD_PEPTIDE_FOR_XIC = 1;
    private static final int LOAD_QUANT_CHANNELS_FOR_XIC = 2;
    private static final int LOAD_PEPTIDE_FOR_PROTEIN_SET = 3;
    private static final int LOAD_PEPTIDE_ION_FOR_XIC = 4;
    private static final int LOAD_PEPTIDE_ION_FOR_PEPTIDE = 5;
    private static final int LOAD_PSM_FOR_PEPTIDE = 6;
    private static final int LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES = 7;
    private static final int LOAD_PEPTIDE_ION_FOR_AGGREGATE_PEPTIDE_ION = 8;
    private static final int LOAD_REPORTER_ION_FOR_PEPTIDE_ION = 9;
    private static final int LOAD_REPORTER_ION_FOR_QUANT = 10;

    public DatabaseLoadXicMasterQuantTask(AbstractDatabaseCallback callback) {
        super(callback);

    }

    public void initLoadProteinSets(long projectId, DDataset dataset, List<DMasterQuantProteinSet> masterQuantProteinSetList) {
        init(NBR_SUB_TASK_PROTEIN, new TaskInfo("Load Protein Sets of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantProteinSetList = masterQuantProteinSetList;
        action = LOAD_PROTEIN_SET_FOR_XIC;
    }

    public void initLoadQuantChannels(long projectId, DDataset dataset) {
        init(NO_SUB_TASK, new TaskInfo("Load Quant Channels of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        action = LOAD_QUANT_CHANNELS_FOR_XIC;
    }

    public void initLoadPeptides(long projectId, DDataset dataset, List<DMasterQuantPeptide> masterQuantPeptideList, boolean xic) {
        init(NBR_SUB_TASK_PEPTIDE, new TaskInfo("Load Peptides of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));

        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideList = masterQuantPeptideList;
        m_xic = xic;
        action = LOAD_PEPTIDE_FOR_XIC;
    }

    public void initLoadPeptides(long projectId, DDataset dataset, DMasterQuantProteinSet masterQuantProteinSet, List<DMasterQuantPeptide> masterQuantPeptideList, boolean xic) {
        String proteinSetName = "";
        if (masterQuantProteinSet != null) {
            proteinSetName = getMasterQuantProteinSetName(masterQuantProteinSet);
        }
        init(NO_SUB_TASK, new TaskInfo("Load Peptides of proteinSet " + proteinSetName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideList = masterQuantPeptideList;
        m_dMasterQuantProteinSet = masterQuantProteinSet;
        m_xic = xic;
        action = LOAD_PEPTIDE_FOR_PROTEIN_SET;
    }

    public void initLoadPeptides(long projectId, DDataset dataset, Long[] peptideInstanceIdArray, List<DMasterQuantPeptide> masterQuantPeptideList, boolean xic) {

        init(NBR_SUB_TASK_PEPTIDE, new TaskInfo("Load Peptides from peptide Instances ", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideList = masterQuantPeptideList;
        m_peptideInstanceIdArray = peptideInstanceIdArray;
        m_xic = xic;
        action = LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES;
    }

    public void initLoadPeptideIons(long projectId, DDataset dataset, DMasterQuantPeptide masterQuantPeptide, List<DMasterQuantPeptideIon> masterQuantPeptideIonList) {
        String peptideName = "";
        if (masterQuantPeptide != null && masterQuantPeptide.getPeptideInstance() != null && masterQuantPeptide.getPeptideInstance().getPeptide() != null) {
            peptideName = masterQuantPeptide.getPeptideInstance().getPeptide().getSequence();
        }
        init(NO_SUB_TASK, new TaskInfo("Load Peptides Ions of peptide " + peptideName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptide = masterQuantPeptide;
        m_masterQuantPeptideIonList = masterQuantPeptideIonList;
        action = LOAD_PEPTIDE_ION_FOR_PEPTIDE;
    }

    public void initLoadParentPeptideIons(long projectId, DDataset dataset, DMasterQuantPeptide masterQuantPeptide, DMasterQuantPeptideIon aggregatedMasterQuantPeptide,List<DMasterQuantPeptideIon> masterQuantPeptideIonList, HashMap<Long, DQuantitationChannel> quantitationChannelsMap, ArrayList<Long> childrenDatasetIds) {
        String peptideName = "";
        if (aggregatedMasterQuantPeptide != null && aggregatedMasterQuantPeptide.getRepresentativePepMatch() != null && aggregatedMasterQuantPeptide.getRepresentativePepMatch().getPeptide() != null) {
            peptideName = aggregatedMasterQuantPeptide.getRepresentativePepMatch().getPeptide().getSequence();
        }

        init(NO_SUB_TASK, new TaskInfo("Load Parents Peptides Ions of peptide " + peptideName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptide = masterQuantPeptide;
        m_specifiedMasterQuantPeptideIon = aggregatedMasterQuantPeptide;
        m_masterQuantPeptideIonList = masterQuantPeptideIonList;
        m_quantitationChannelsMap = quantitationChannelsMap;
        m_childrenDatasetIds = childrenDatasetIds;
        action = LOAD_PEPTIDE_ION_FOR_AGGREGATE_PEPTIDE_ION;
    }

    public void initLoadPeptideIons(long projectId, DDataset dataset, List<DMasterQuantPeptideIon> masterQuantPeptideIonList) {
        init(NBR_SUB_TASK_PEPTIDE_ION, new TaskInfo("Load Peptides Ions of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideIonList = masterQuantPeptideIonList;
        action = LOAD_PEPTIDE_ION_FOR_XIC;
    }

    public void initLoadPSMForPeptide(long projectId, DDataset dataset, DMasterQuantPeptide masterQuantPeptide, List<DPeptideMatch> listPeptideMatch, Map<Long, List<Long>> psmPerQC) {
        String peptideName = "";
        if (masterQuantPeptide != null && masterQuantPeptide.getPeptideInstance() != null && masterQuantPeptide.getPeptideInstance().getPeptide() != null) {
            peptideName = masterQuantPeptide.getPeptideInstance().getPeptide().getSequence();
        }
        init(NBR_SUB_TASK_PSM, new TaskInfo("Load Peptide Match of peptide " + peptideName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideForPSM = masterQuantPeptide;
        m_psmIdPerQC = psmPerQC;
        m_peptideMatchList = listPeptideMatch;
        action = LOAD_PSM_FOR_PEPTIDE;
    }

    public void initLoadReporterIons(long projectId, DDataset dataset, List<MasterQuantReporterIon> masterQuantReporterIonList) {
        init(NBR_SUB_TASK_REPORTER_ION, new TaskInfo("Load Peptide Matches Reporter Ions od Quanti " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantReporterIonList = masterQuantReporterIonList;
        action = LOAD_REPORTER_ION_FOR_QUANT;
    }

    public void initLoadReporterIons(long projectId, DDataset dataset, DMasterQuantPeptideIon masterQuantPeptideIon, List<MasterQuantReporterIon> masterQuantReporterIonList) {
        String peptideName = "";
        if (masterQuantPeptideIon != null && masterQuantPeptideIon.getPeptideInstance() != null && masterQuantPeptideIon.getPeptideInstance().getPeptide() != null) {
            peptideName = masterQuantPeptideIon.getPeptideInstance().getPeptide().getSequence()+"-"+masterQuantPeptideIon.getCharge()+"+";
        }
        init(NO_SUB_TASK, new TaskInfo("Load Peptide Matches Reporter Ions of peptide ion " + peptideName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_specifiedMasterQuantPeptideIon = masterQuantPeptideIon;
        m_masterQuantReporterIonList = masterQuantReporterIonList;
        action = LOAD_REPORTER_ION_FOR_PEPTIDE_ION;
    }

    @Override
    // must be implemented for all AbstractDatabaseSlicerTask 
    public void abortTask() {
        super.abortTask();
        switch (action) {
            case LOAD_PROTEIN_SET_FOR_XIC:
                m_masterQuantProteinSetList = null;
                break;
            case LOAD_QUANT_CHANNELS_FOR_XIC:
                m_dataset = null;
                break;
            case LOAD_PEPTIDE_FOR_XIC, LOAD_PEPTIDE_FOR_PROTEIN_SET, LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES:
                m_masterQuantPeptideList = null;
                break;
            case LOAD_PEPTIDE_ION_FOR_PEPTIDE:
            case LOAD_PEPTIDE_ION_FOR_XIC:
            case LOAD_PEPTIDE_ION_FOR_AGGREGATE_PEPTIDE_ION:
                m_masterQuantPeptideIonList = null;
                break;
            case LOAD_PSM_FOR_PEPTIDE:
                m_peptideMatchList = null;
                break;
            case LOAD_REPORTER_ION_FOR_QUANT:
            case LOAD_REPORTER_ION_FOR_PEPTIDE_ION:
                m_masterQuantReporterIonList=null;
        }
    }

    @Override
    public boolean fetchData() {
        try {
            if (action == LOAD_QUANT_CHANNELS_FOR_XIC) {
                if (needToFetch()) {
                    m_taskError = fetchDataQuantChannels(m_projectId, m_dataset);
                    return (m_taskError == null);
                }
            } else if (action == LOAD_PROTEIN_SET_FOR_XIC) {
                if (needToFetch()) {
                    // first data are fetched
                    if (m_dataset.getMasterQuantitationChannels() == null || m_dataset.getMasterQuantitationChannels().isEmpty()) {
                        m_taskError = fetchDataQuantChannels(m_projectId, m_dataset);
                    }
                    if ((m_dataset.getMapAlignments() == null || m_dataset.getMapAlignments().isEmpty())) {
                        m_taskError = DatabaseLoadLcMSTask.fetchDataMainTaskAlignmentForXic(m_projectId, m_dataset);
                    }
                    return fetchDataProteinMainTask();
                } else {
                    // fetch data of SubTasks
                    return fetchProteinSetDataSubTask();
                }

            } else if (action == LOAD_PEPTIDE_FOR_XIC) {
                if (needToFetch()) {
                    // first data are fetched
                    if (m_dataset.getMasterQuantitationChannels() == null || m_dataset.getMasterQuantitationChannels().isEmpty()) {
                        m_taskError = fetchDataQuantChannels(m_projectId, m_dataset);
                    }
                    if ((m_dataset.getMapAlignments() == null || m_dataset.getMapAlignments().isEmpty())) {
                        DatabaseLoadLcMSTask.fetchDataMainTaskAlignmentForXic(m_projectId, m_dataset);
                    }
                    return fetchDataPeptideMainTask(m_xic, null);
                } else {
                    // fetch data of SubTasks
                    return fetchPeptideInstanceDataSubTask(m_xic);
                }

            } else if (action == LOAD_PEPTIDE_FOR_PROTEIN_SET) {
                if (needToFetch()) {
                    // first data are fetched
                    //fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
                    if ((m_dataset.getMapAlignments() == null || m_dataset.getMapAlignments().isEmpty())) {
                        m_taskError = DatabaseLoadLcMSTask.fetchDataMainTaskAlignmentForXic(m_projectId, m_dataset);
                    }
                    return fetchDataPeptideForProteinSetMainTask(m_xic);
                }

            } else if (action == LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES) {
                if (needToFetch()) {
                    // first data are fetched
                    //fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
                    if ((m_dataset.getMapAlignments() == null || m_dataset.getMapAlignments().isEmpty())) {
                        m_taskError = DatabaseLoadLcMSTask.fetchDataMainTaskAlignmentForXic(m_projectId, m_dataset);
                    }
                    m_logger.info(" --- CALL LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES NBR "+m_peptideInstanceIdArray.length);
                    return fetchDataPeptideMainTask(m_xic, m_peptideInstanceIdArray);
                } else {
                    // fetch data of SubTasks
                    return fetchPeptideInstanceDataSubTask(m_xic);
                }
            } else if (action == LOAD_PEPTIDE_ION_FOR_XIC) {
                if (needToFetch()) {
                    // first data are fetched
                    if (m_dataset.getMasterQuantitationChannels() == null || m_dataset.getMasterQuantitationChannels().isEmpty()) {
                        m_taskError = fetchDataQuantChannels(m_projectId, m_dataset);
                    }
                    if ((m_dataset.getMapAlignments() == null || m_dataset.getMapAlignments().isEmpty())) {
                        m_taskError = DatabaseLoadLcMSTask.fetchDataMainTaskAlignmentForXic(m_projectId, m_dataset);
                    }
                    return fetchDataPeptideIonMainTask();
                } else {
                    // fetch data of SubTasks
                    return fetchPeptideIonDataSubTask();
                }

            } else if (action == LOAD_PEPTIDE_ION_FOR_PEPTIDE) {
                if (needToFetch()) {
                    // first data are fetched
                    //fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
                    if ((m_dataset.getMapAlignments() == null || m_dataset.getMapAlignments().isEmpty())) {
                        m_taskError = DatabaseLoadLcMSTask.fetchDataMainTaskAlignmentForXic(m_projectId, m_dataset);
                    }
                    return fetchDataPeptideIonForPeptideMainTask();
                }

            } else if (action == LOAD_PEPTIDE_ION_FOR_AGGREGATE_PEPTIDE_ION) {
                if (needToFetch()) {
                /*if ((m_dataset.getMapAlignments() == null || m_dataset.getMapAlignments().isEmpty())) {  //JPM.PARENT.TODO : useful ?
                    m_taskError = DatabaseLoadLcMSTask.fetchDataMainTaskAlignmentForXic(m_projectId, m_dataset);
                }*/
                    return fetchDataPeptideIonForAggregatePeptideIonMainTask();
                }
            } else if (action == LOAD_PSM_FOR_PEPTIDE) {
                if (needToFetch()) {
                    // first data are fetched
                    //fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
                    return fetchDataPSMForPeptideMainTask();
                } else {
                    return fetchPSMForPeptideSubTask();
                }

            } else if (action == LOAD_REPORTER_ION_FOR_PEPTIDE_ION) {
                if (needToFetch()) {
                    if ((m_dataset.getMapAlignments() == null || m_dataset.getMapAlignments().isEmpty())) {
                        m_taskError = DatabaseLoadLcMSTask.fetchDataMainTaskAlignmentForXic(m_projectId, m_dataset);
                    }
                    return fetchDataReporterIonForPeptideIonMainTask();
                }
            } else if (action == LOAD_REPORTER_ION_FOR_QUANT) {
                if (needToFetch()) {
                    // first data are fetched
                    if (m_dataset.getMasterQuantitationChannels() == null || m_dataset.getMasterQuantitationChannels().isEmpty()) {
                        m_taskError = fetchDataQuantChannels(m_projectId, m_dataset);
                    }
                    if ((m_dataset.getMapAlignments() == null || m_dataset.getMapAlignments().isEmpty())) {
                        m_taskError = DatabaseLoadLcMSTask.fetchDataMainTaskAlignmentForXic(m_projectId, m_dataset);
                    }

                    return fetchDataReporterIonForQuantMainTask();
                } else
                    return fetchReporterIonForQuanDataSubTask();
            }
        } finally {
              m_logger.info(" - Display Perf !!! Commented ");
//            m_logger.info("!!!! Display Perf !!! ");
//            PerformanceTest.displayTimeCurrentThread();  //VDS for debug only !
//            PerformanceTest.clearTimeCurrentThread();
//            m_logger.info("!!!! Display Perf DONE !!! ");
        }

        return true; // should not happen
    }

    private boolean fetchPSMForPeptideSubTask() {
        SubTask subTask = m_subTaskManager.getNextSubTask();
        if (subTask == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            switch (subTask.getSubTaskId()) {
                case SUB_TASK_PEPTIDE:
                    fetchPeptide(entityManagerMSI, subTask);
                    break;
                case SUB_TASK_MSQUERY:
                    fetchMsQuery(entityManagerMSI, subTask);
                    break;
                case SUB_TASK_PROTEINSET_NAME_LIST:
                    fetchProteinSetName(entityManagerMSI, subTask);
                    break;
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

    @Override
    public boolean needToFetch() {
        switch (action) {
            case LOAD_PROTEIN_SET_FOR_XIC:
                return (m_masterQuantProteinSetList == null || m_masterQuantProteinSetList.isEmpty());
            case LOAD_QUANT_CHANNELS_FOR_XIC:
                return true;
            case LOAD_PEPTIDE_FOR_XIC:
            case LOAD_PEPTIDE_FOR_PROTEIN_SET:
            case LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES:
                return (m_masterQuantPeptideList == null || m_masterQuantPeptideList.isEmpty());
            case LOAD_PEPTIDE_ION_FOR_XIC:
            case LOAD_PEPTIDE_ION_FOR_PEPTIDE:
            case LOAD_PEPTIDE_ION_FOR_AGGREGATE_PEPTIDE_ION:
                return (m_masterQuantPeptideIonList == null || m_masterQuantPeptideIonList.isEmpty());
            case LOAD_PSM_FOR_PEPTIDE:
                return (m_peptideMatchList == null || m_peptideMatchList.isEmpty());
            case LOAD_REPORTER_ION_FOR_QUANT:
            case LOAD_REPORTER_ION_FOR_PEPTIDE_ION:
                return (m_masterQuantReporterIonList == null || m_masterQuantReporterIonList.isEmpty());
        }
        return false; // should not happen 
    }

    /**
     * load list quantitation channels
     *
     * @param projectId
     * @param dataset
     * @return
     */
    public static TaskError fetchDataQuantChannels(Long projectId, DDataset dataset) {
        long start = System.currentTimeMillis();
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(projectId).createEntityManager();
        EntityManager entityManagerLCMS = DStoreCustomPoolConnectorFactory.getInstance().getLcMsDbConnector(projectId).createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            entityManagerMSI.getTransaction().begin();
            entityManagerLCMS.getTransaction().begin();
            // load DDataset
            Dataset datasetDB = entityManagerUDS.find(Dataset.class, dataset.getId());
            // force initialization of lazy data (data will be needed for the display of properties)
            QuantitationMethod quantMethodDB = datasetDB.getMethod();
            List<MasterQuantitationChannel> listMasterQuantitationChannels = datasetDB.getMasterQuantitationChannels();

            // fill the current object with the db object
            dataset.setQuantitationMethod(quantMethodDB);
            // load labels lazydata
            List<QuantitationLabel> labels = quantMethodDB.getLabels();
            labels.size();
            dataset.setDescription(datasetDB.getDescription());
            List<DMasterQuantitationChannel> masterQuantitationChannels = null;

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                masterQuantitationChannels = new ArrayList<>();
                for (MasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    // load the list of quantitation channel linked to this masterQuantitationChannel
                    List<DQuantitationChannel> listDQuantChannels = createDQuantChannelsForMQC(projectId, masterQuantitationChannel, entityManagerMSI, entityManagerUDS, entityManagerLCMS);

                    DMasterQuantitationChannel dMaster = new DMasterQuantitationChannel(masterQuantitationChannel.getId(), masterQuantitationChannel.getName(),
                            resultSummaryId, listDQuantChannels, masterQuantitationChannel.getDataset(), masterQuantitationChannel.getSerializedProperties());

                    // load the dataset for which the id is stored in the serialized properties
                    //Since V2 : ref dataset and resultSummary identification are stored in masterQuantChannel
                    DDataset identDataset = null;
                    Dataset identDatasetDB = masterQuantitationChannel.getIdentDataset();
                    if (identDatasetDB != null) {
                        identDataset = new DDataset(identDatasetDB.getId(), identDatasetDB.getProject(), identDatasetDB.getName(), identDatasetDB.getType(),
                                identDatasetDB.getChildrenCount(), identDatasetDB.getResultSetId(), identDatasetDB.getResultSummaryId(), identDatasetDB.getNumber());
                    }
                    Long identResultSummaryId = masterQuantitationChannel.getIdentResultSummaryId();

                    dMaster.setIdentDataset(identDataset);
                    dMaster.setIdentResultSummaryId(identResultSummaryId);

                    // add into the list
                    masterQuantitationChannels.add(dMaster);
                } // end of the for all MasterQuantChannel

                // Set the ResultSummary of the Dataset as the ResultSummary of the first MasterQuantitationChannel
                MasterQuantitationChannel masterQuantitationChannel = listMasterQuantitationChannels.get(0);
                Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();
                if (dataset.getResultSummary() == null || dataset.getResultSetId() == null || dataset.getResultSet() == null) {
                    ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, resultSummaryId);
                    //noinspection unused: to init Lazy map
                    Set<String> objTreeByName = rsm.getObjectTreeIdByName().keySet();
                    if(dataset.getResultSummaryId() == null)
                        dataset.setResultSummaryId(resultSummaryId);
                    if(dataset.getResultSummary() == null)
                        dataset.setResultSummary(rsm);
                    if(dataset.getResultSet() == null)
                        dataset.setResultSet(rsm.getResultSet());                    
                    if(dataset.getResultSetId()== null)
                        dataset.setResultSetId(rsm.getResultSet().getId());                    
                }
            } //End if at least 1 MQC exits

            dataset.setMasterQuantitationChannels(masterQuantitationChannels);
            // load groupSetup
            Set<GroupSetup> groupSetupSet = datasetDB.getGroupSetups();
            if (groupSetupSet != null && !groupSetupSet.isEmpty()) {
                GroupSetup groupSetup = groupSetupSet.iterator().next();
                List<BiologicalGroup> listBiolGroup = groupSetup.getBiologicalGroups();
                for (BiologicalGroup biolGroup : listBiolGroup) {
                    List<BiologicalSample> listBiologicalSamples = biolGroup.getBiologicalSamples();
                    for (BiologicalSample bioSample : listBiologicalSamples) {
                        for (SampleAnalysis splAnalysis : bioSample.getSampleAnalyses()) {
                            splAnalysis.getDataset();
                        }
                    }
                }
                dataset.setGroupSetup(groupSetup);
            }

            // sort qCh by BiologicalGroup/BiologicalSample
            if (!dataset.getMasterQuantitationChannels().isEmpty()) {
                List<DQuantitationChannel> listQch = dataset.getMasterQuantitationChannels().get(0).getQuantitationChannels();
                if (dataset.getGroupSetup() != null && !dataset.getGroupSetup().getBiologicalGroups().isEmpty()) {
                    List<DQuantitationChannel> sortedQch = new ArrayList<>();
                    List<BiologicalGroup> listBiolGroup = dataset.getGroupSetup().getBiologicalGroups();
                    for (BiologicalGroup biolGroup : listBiolGroup) {
                        List<BiologicalSample> listBiologicalSamples = biolGroup.getBiologicalSamples();
                        for (BiologicalSample sample : listBiologicalSamples) {
                            List<QuantitationChannel> listQchSample = sample.getQuantitationChannels(); // are sorted by number
                            for (QuantitationChannel qchS : listQchSample) {
                                for (DQuantitationChannel dqch : listQch) {
                                    if (dqch.getId() == qchS.getId()) {
                                        dqch.setBiologicalGroupId(biolGroup.getId());
                                        dqch.setBiologicalGroupName(biolGroup.getName());
                                        sortedQch.add(dqch);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    // replace by sortedList
                    dataset.getMasterQuantitationChannels().get(0).setQuantitationChannels(sortedQch);
                }
            }

            // load ObjectTree corresponding to the QUANTITATION.*_CONFIG
            Map<String, Long> objectTreeIdByName = datasetDB.getObjectTreeIdByName();
            // load ObjectTree linked to the dataset
            if (objectTreeIdByName != null) {
                for (Map.Entry<String, Long> entry : objectTreeIdByName.entrySet()) {
                    if (entry.getKey().startsWith("quantitation") || entry.getKey().equals("proline.low_level_config")) {
                        Long objectId = entry.getValue();
                        fr.proline.core.orm.uds.ObjectTree objectTree = entityManagerUDS.find(fr.proline.core.orm.uds.ObjectTree.class, objectId);
                        dataset.setObjectTree(objectTree);
                    }
                }
            }

            entityManagerMSI.getTransaction().commit();
            entityManagerUDS.getTransaction().commit();
            entityManagerLCMS.getTransaction().commit();
            m_logger.info("fetchQuantChannels took " + (System.currentTimeMillis() - start) + " ms");
        } catch (Exception e) {
            String trace2String = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
            m_logger.error("fetchDataQuantChannels failed: {}, \n StackTrace:\n{}", e, trace2String);
            TaskError taskError = new TaskError(e);
            try {
                entityManagerMSI.getTransaction().rollback();
                entityManagerUDS.getTransaction().rollback();
                entityManagerLCMS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(DatabaseLoadXicMasterQuantTask.class.getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return taskError;
        } finally {
            entityManagerLCMS.close();
            entityManagerMSI.close();
            entityManagerUDS.close();
        }
        return null;

    }

    public static TaskError fetchDataset(Long projectId, ArrayList<Long> childrenDatasetId, ArrayList<DDataset> m_childrenDatasetList) {

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            String query = "SELECT d "
                    + "FROM fr.proline.core.orm.uds.Dataset d "
                    + "WHERE d.id IN (:list) ";

            TypedQuery<Dataset> queryDataset = entityManagerUDS.createQuery(query, Dataset.class);
            queryDataset.setParameter("list", childrenDatasetId);

            List<Dataset> datasets = queryDataset.getResultList();

            for (Dataset d : datasets) {
                DDataset dDataset = new DDataset(d.getId(), d.getProject(), d.getName(), d.getType(),
                        d.getChildrenCount(), d.getResultSetId(), d.getResultSummaryId(), d.getNumber());

                TaskError taskError = fetchDataQuantChannels(projectId, dDataset);
                if (taskError != null) {
                    return taskError;
                }

                m_childrenDatasetList.add(dDataset);

            }

            entityManagerUDS.getTransaction().commit();
        } catch (Exception e) {

            String trace2String = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
            m_logger.error("fetchDataQuantChannels failed: {}, \n StackTrace:\n{}", e, trace2String);
            TaskError taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(DatabaseLoadXicMasterQuantTask.class.getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return taskError;
        } finally {
            entityManagerUDS.close();
        }
        return null;
    }


    public static TaskError fetchDataQuantChannels(HashSet<Long> ids, HashMap<Long, DQuantitationChannel> quantitationChannelsMap) {

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            String query = "SELECT qc "
                        + "FROM fr.proline.core.orm.uds.QuantitationChannel qc "
                        + "WHERE qc.id IN (:list) ";


            TypedQuery<QuantitationChannel> queryLinkedRs = entityManagerUDS.createQuery(query, QuantitationChannel.class);
            queryLinkedRs.setParameter("list", ids);

            List<QuantitationChannel> quantitationChannels = queryLinkedRs.getResultList();


            for (QuantitationChannel qc : quantitationChannels) {
                quantitationChannelsMap.put(qc.getId(), new DQuantitationChannel(qc));
            }


            entityManagerUDS.getTransaction().commit();
        } catch (Exception e) {

            String trace2String = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
            m_logger.error("fetchDataQuantChannels failed: {}, \n StackTrace:\n{}", e, trace2String);
            TaskError taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(DatabaseLoadXicMasterQuantTask.class.getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return taskError;
        } finally {
            entityManagerUDS.close();
        }
        return null;
    }


    static private List<DQuantitationChannel> createDQuantChannelsForMQC(Long projectId, MasterQuantitationChannel masterQuantitationChannel, EntityManager entityManagerMSI, EntityManager entityManagerUDS, EntityManager entityManagerLCMS) {

        List<QuantitationChannel> listQuantitationChannels = masterQuantitationChannel.getQuantitationChannels();
        List<DQuantitationChannel> listDQuantChannels = new ArrayList<>();
        for (QuantitationChannel qc : listQuantitationChannels) {
            DQuantitationChannel dqc = new DQuantitationChannel(qc);
            // search resultFileName and raw path
            String resultFileName = "";
            String rawPath = "";
            Long rsId = null;
            String queryMsi = "SELECT msi.resultFileName, pl.path, rsm.resultSet.id  "
                    + "FROM MsiSearch msi, Peaklist pl, ResultSet rs, ResultSummary rsm "
                    + " WHERE rsm.id=:rsmId AND rsm.resultSet.id = rs.id AND rs.msiSearch.id = msi.id "
                    + "AND msi.peaklist.id = pl.id ";
            Query qMsi = entityManagerMSI.createQuery(queryMsi);
            qMsi.setParameter("rsmId", qc.getIdentResultSummaryId());
            try {
                Object[] res = (Object[]) qMsi.getSingleResult();
                resultFileName = (String) res[0];
                rawPath = (String) res[1];
                if (resultFileName != null && resultFileName.contains(".")) {
                    resultFileName = resultFileName.substring(0, resultFileName.indexOf('.'));
                }
                rsId = (Long) res[2];
            } catch (NoResultException | NonUniqueResultException ignored) {

            }

            // take the dataset name as qch name TODO IF NOT DEFINE ABOVE
            String rawFileIdentifier = null;
            String queryQCName = "SELECT ds.name, run.rawFile.identifier "
                    + "FROM fr.proline.core.orm.uds.Dataset ds, fr.proline.core.orm.uds.QuantitationChannel qc, fr.proline.core.orm.uds.Run run "
                    + "WHERE ds.resultSummaryId = qc.identResultSummaryId AND "
                    + "qc.id=:qChId AND ds.project.id=:projectId AND "
                    + "qc.run.id=run.id ";
            Query  queryQCNameQ = entityManagerUDS.createQuery(queryQCName);
            queryQCNameQ.setParameter("qChId", qc.getId());
            queryQCNameQ.setParameter("projectId", projectId);
            try {
                Object[] res = (Object[]) queryQCNameQ.getSingleResult();
                resultFileName = (String) res[0];
                rawFileIdentifier = (String) res[1];
            } catch (NoResultException | NonUniqueResultException ignored) {

            }
            dqc.setResultFileName(resultFileName);
            dqc.setRawFilePath(rawPath);
            dqc.setRawFileIdentifier(rawFileIdentifier);
            // search for run_identification rawFileName (mzdb fileName) in UDS

            String mzdbFile = "";
            try {
                //mzdbFile = (String) queryMzdb.getSingleResult();
                mzdbFile = qc.getRun().getRawFile().getMzDbFileName();
            } catch (Exception e) {
                String trace2String = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
                m_logger.warn("Error while retrieving mzdb file: {}, \n StackTrace:\n{}", e, trace2String);
            }
            dqc.setMzdbFileName(mzdbFile);
            // search for raw map in LCMS database
            String queryLcms = "SELECT pmrm.rawMap.id "
                    + "FROM fr.proline.core.orm.lcms.Map  m, ProcessedMap pm, ProcessedMapRawMapMapping pmrm "
                    + "WHERE m.id =:processedMapId "
                    + "AND m.id = pm.id "
                    + "AND pm.id = pmrm.id.processedMapId ";
            TypedQuery<Long> queryRawMapLcms = entityManagerLCMS.createQuery(queryLcms, Long.class);
            queryRawMapLcms.setParameter("processedMapId", qc.getLcmsMapId());
            try {
                Long rawMapId = queryRawMapLcms.getSingleResult();
                dqc.setLcmsRawMapId(rawMapId);
            } catch (NoResultException | NonUniqueResultException ignored) {

            }
            if (rsId != null) {
                ResultSet rsetFound = entityManagerMSI.find(ResultSet.class, rsId);
                dqc.setIdentRs(rsetFound);
            } else {
                dqc.setIdentRs(null);
            }
            // search if a dataset with rsmId, rsId exists 
            String queryIdentDsS = "SELECT ds.id FROM Dataset ds WHERE ds.resultSetId=:rsId AND ds.resultSummaryId=:rsmId AND ds.project.id=:projectId ";
            TypedQuery<Long> queryIdentDs = entityManagerUDS.createQuery(queryIdentDsS, Long.class);
            queryIdentDs.setParameter("rsId", rsId);
            queryIdentDs.setParameter("rsmId", qc.getIdentResultSummaryId());
            queryIdentDs.setParameter("projectId", projectId);

            try {
                Long identDsId = queryIdentDs.getSingleResult();
                dqc.setIdentDatasetId(identDsId);
            } catch (NoResultException | NonUniqueResultException e) {
                dqc.setIdentDatasetId((long) -1);
            }
            // load biologicalSample
            dqc.setBiologicalSample(qc.getBiologicalSample());

            listDQuantChannels.add(dqc);
        } //End go through QuantChannel
        return listDQuantChannels;
    }

    /**
     * Fetch first data to display proteinSet (all Master Protein Sets,
     * quantProteinSet
     *
     * @return
     */
    private boolean fetchDataProteinMainTask() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            PerformanceTest.startTime("fetchDataProteinMainTask");
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();
            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary
                    if (resultSummaryId != null) {
                        PerformanceTest.startTime("fetchDataProteinMainTask.getProtList");
                        // retrieve the proteinSet list with isValidated = true
                        Query proteinSetsQuery = entityManagerMSI.createQuery("SELECT ps.id  FROM PeptideSet pepset JOIN pepset.proteinSet as ps "
                                + " WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true ORDER BY pepset.score DESC");
                        if (m_dataset.getQuantMethodInfo() == QuantitationMethodInfo.SPECTRAL_COUNTING) {
                            proteinSetsQuery = entityManagerMSI.createQuery("SELECT ps.id  FROM PeptideSet pepset JOIN pepset.proteinSet as ps "
                                    + " WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true AND ps.masterQuantComponentId is not null ORDER BY pepset.score DESC");
                        }
                        proteinSetsQuery.setParameter("rsmId", resultSummaryId);
                        List<Long> proteinSets = (List<Long>)proteinSetsQuery.getResultList();
                        PerformanceTest.stopTime("fetchDataProteinMainTask.getProtList");
                        if (proteinSets != null && !proteinSets.isEmpty()) {
                            // for each proteinSet, retrieve the DMasterQuantProteinSet
                            int nbProteinSet = proteinSets.size();
//                            m_logger.debug("------- > fetchDataProteinMainTask : get ProtSet with MQPS: " + nbProteinSet);
                            m_proteinSetIds = new ArrayList<>(nbProteinSet);
                            m_resultSetIds = new ArrayList<>(nbProteinSet);
                            for (Long id : proteinSets) {
                                m_proteinSetIds.add(id);
                                m_resultSetIds.add(resultSummaryId);
                                DMasterQuantProteinSet dMasterQuantProteinSet = new DMasterQuantProteinSet();
                                dMasterQuantProteinSet.setProteinSetId(id);
                                dMasterQuantProteinSet.setId(-1);
                                dMasterQuantProteinSet.setQuantResultSummaryId(resultSummaryId);
                                m_masterQuantProteinSetList.add(dMasterQuantProteinSet);
                            }

                            //fetchProteinSetData(entityManagerMSI, m_proteinSetIds);
                            // slice the task and get the first one
                            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEIN_SET, m_proteinSetIds.size(), SLICE_SIZE*4);

                            // execute the first slice now
                            fetchProteinSetData(subTask, entityManagerMSI);

                        } // end proteinSets != null
                    }// end resultSummaryId null

                } // end of the for

            }
            PerformanceTest.stopTime("fetchDataProteinMainTask");
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }

    /**
     * load all quant peptides for a xic
     *
     * @param xic: specific if quanti is xic, otherwise it is spectral count
     * @param peptideInstanceIdArray: Ids of peptide Instance to get data for. May be null in which case all peptide Instance of rsm will be considered.
     * @return
     */
    private boolean fetchDataPeptideMainTask(boolean xic, Long[] peptideInstanceIdArray) {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        long start = System.currentTimeMillis();
        try {
            entityManagerMSI.getTransaction().begin();
            boolean pepInstSpecified = (peptideInstanceIdArray!= null);   
            boolean mqPepCreatedForSpecifiedPepInstance = true;
            m_peptideInstanceIds = new ArrayList<>();
            if (pepInstSpecified) {
                m_peptideInstanceIds.addAll(Arrays.asList(peptideInstanceIdArray));
                mqPepCreatedForSpecifiedPepInstance = false;
            }
            
            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();
            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long quantResultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();
                    if (quantResultSummaryId != null ) {
                        List<Long> currentMQChPepInstanceIds = new ArrayList<>();
                        
                        //--- Get PeptideInstance Ids
                        if (!pepInstSpecified) { //get quant RSM peptide instance Ids
                            String queryPep = "SELECT pi.id "
                                + "FROM fr.proline.core.orm.msi.PeptideInstance pi "
                                + "WHERE pi.resultSummary.id=:rsmId ";
                                                    
                            queryPep += "ORDER BY pi.id ASC";
                            Query peptidesQuery = entityManagerMSI.createQuery(queryPep);
                            peptidesQuery.setParameter("rsmId", quantResultSummaryId);
                            List<Long> listIds = (List<Long>) peptidesQuery.getResultList();
                            currentMQChPepInstanceIds.addAll(listIds);
                            m_peptideInstanceIds.addAll(currentMQChPepInstanceIds);
                            m_logger.info("### loading XIC for peptides ... query returns {} peptide instances", m_peptideInstanceIds.size());
                        } else if(!mqPepCreatedForSpecifiedPepInstance){      
                            //Only for first MasterQuantChanel : Add specified peptideInstance to create DMasterQuantPeptide for.
                            // VDS FIXME : This means that they will be associated to first quantResultSummaryId !!!
                            mqPepCreatedForSpecifiedPepInstance = true;
                            currentMQChPepInstanceIds.addAll(m_peptideInstanceIds);
                        }
                        
                        //--- Create corresponding DMasterQuantPeptide
                        List<DMasterQuantPeptide> listDMasterQuantPeptideFake = new ArrayList<>();
                        for (Long m_peptideInstanceId : currentMQChPepInstanceIds) {
                            DMasterQuantPeptide f = new DMasterQuantPeptide(-1, -1, -1, "", quantResultSummaryId);
                            f.setPeptideInstanceId(m_peptideInstanceId);
                            listDMasterQuantPeptideFake.add(f);
                        }
                        m_masterQuantPeptideList.addAll(listDMasterQuantPeptideFake);

                        if(!pepInstSpecified) {
                            // load MasterQuantPeptide without PeptideInstance : only if not pepInstSpecified
                            //SELECT mqc.id ,pi.id
                            //FROM object_tree ot ,  master_quant_component mqc
                            //LEFT JOIN peptide_instance pi ON mqc.id  = pi.master_quant_component_id 
                            //WHERE mqc.result_summary_id = 52 AND
                            //ot.id = mqc.object_tree_id AND
                            //ot.schema_name like 'object_tree.label_free_quant_peptides' AND pi.id is null
                            String queryNonIndent = "SELECT mqc.id "
                                    + "FROM fr.proline.core.orm.msi.ObjectTree ot, fr.proline.core.orm.msi.MasterQuantComponent mqc "
                                    + "WHERE mqc.resultSummary.id=:rsmId AND "
                                    + "NOT EXISTS (select 1 from fr.proline.core.orm.msi.PeptideInstance pi where pi.resultSummary.id=:rsmId AND pi.masterQuantComponentId = mqc.id ) AND "
                                    + "ot.id =  mqc.objectTreeId AND "
                                    + "ot.schema.name like 'object_tree.label_free_quant_peptides' ";
                            Query quantPeptideNonIdentQuery = entityManagerMSI.createQuery(queryNonIndent);
                            quantPeptideNonIdentQuery.setParameter("rsmId", quantResultSummaryId);
                            List<Long> listPepNonIdentIds = (List<Long>) quantPeptideNonIdentQuery.getResultList();
                            // in order to add these master quantPeptides to the peptideInstance to load, we add the -masterQuantPeptideId to load in the peptideInstanceId...
                            for (Long pepNonIdentId : listPepNonIdentIds) {
                                DMasterQuantPeptide f = new DMasterQuantPeptide(pepNonIdentId, -1, -1, "", quantResultSummaryId);
                                f.setPeptideInstanceId(-pepNonIdentId);
                                m_peptideInstanceIds.add(-pepNonIdentId);
                                m_masterQuantPeptideList.add(f);
                            }
                        }
                        
                    }// end resultSummaryId null
                } // end of the for
                m_logger.debug(" fetchDataMainTaskAlignmentForXic before subtask took "+(System.currentTimeMillis()-start)+" ms");
                // slice the task and get the first one
                SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE_INSTANCE, m_peptideInstanceIds.size(), SLICE_SIZE);
                // execute the first slice now
                fetchPeptideInstanceData(subTask, entityManagerMSI, xic);

            } //end masterquantchannels exist

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
            m_logger.error("fetchDataMainTaskAlignmentForXic total took "+(System.currentTimeMillis()-start)+" ms");
            entityManagerMSI.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }

    /**
     * load all quant peptides ions for a xic
     *
     * @return
     */
    private boolean fetchDataPeptideIonMainTask() {
        long start = System.currentTimeMillis();
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {//for each channel
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary all peptide id for this channel
                    if (resultSummaryId != null) {
                        // load masterQuantPeptideIon
                        String queryPepIon = "SELECT mqpi.id "
                                + "FROM MasterQuantPeptideIon mqpi "
                                + "WHERE mqpi.resultSummary.id=:rsmId "
                                + "ORDER BY mqpi.id ASC";
                        Query peptideIonsQuery = entityManagerMSI.createQuery(queryPepIon);
                        peptideIonsQuery.setParameter("rsmId", resultSummaryId);

                        m_masterQuantPeptideIonIds = (List<Long>) peptideIonsQuery.getResultList();

                        for (Long m_masterQuantPeptideIonId : m_masterQuantPeptideIonIds) {//for each id, create a DMasterQuantPeptideIon
                            DMasterQuantPeptideIon mqpi = new DMasterQuantPeptideIon();
                            mqpi.setId(m_masterQuantPeptideIonId);
                            m_masterQuantPeptideIonList.add(mqpi);
                        }

                        m_logger.info("Peptide Ions ids fetched in = " + (System.currentTimeMillis() - start) + " ms");

                        // slice the task and get the first one
                        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE_ION, m_masterQuantPeptideIonIds.size(), SLICE_SIZE);

                        // execute the first slice now
                        if (subTask != null) {
                            fetchPeptideIonData(subTask, entityManagerMSI);
                        }

                    }// end resultSummaryId null

                } // end of the for

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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }

    /**
     * load all quant peptides for a proteinSet
     *
     * @return
     */
    private boolean fetchDataPeptideForProteinSetMainTask(boolean xic) {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary
                    if (resultSummaryId != null) {
                        // load peptideInstance
                        Long protSetId = m_dMasterQuantProteinSet.getProteinSetId();
                        String queryPep = "SELECT pi.id "
                                + "FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem pspi, "
                                + "fr.proline.core.orm.msi.PeptideSet pepSet "
                                + "WHERE pi.resultSummary.id=:rsmId AND pi.id = pspi.peptideInstance.id AND "
                                + "pspi.peptideSet.id = pepSet.id AND pepSet.proteinSet.id=:proteinSetId "
                                + "ORDER BY pi.id ASC";
                        Query peptidesQuery = entityManagerMSI.createQuery(queryPep);
                        peptidesQuery.setParameter("rsmId", resultSummaryId);
//                            peptidesQuery.setParameter("proteinSetId", (m_dProteinSet == null ? -1 : m_dProteinSet.getId()));
                        peptidesQuery.setParameter("proteinSetId", protSetId);
                        m_peptideInstanceIds = (List<Long>) peptidesQuery.getResultList();

                        List<DMasterQuantPeptide> listDMasterQuantPeptideFake = new ArrayList<>();
                        for (Long m_peptideInstanceId : m_peptideInstanceIds) {
                            DMasterQuantPeptide f = new DMasterQuantPeptide(-1, -1, -1, "", resultSummaryId);
                            f.setPeptideInstanceId(m_peptideInstanceId);
                            listDMasterQuantPeptideFake.add(f);
                        }
                        m_masterQuantPeptideList.addAll(listDMasterQuantPeptideFake);

                        fetchPeptideInstanceData(entityManagerMSI, m_peptideInstanceIds, xic);

                    }// end resultSummaryId null

                } // end of the for

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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }

    /**
     * load peptideInstace data for XIC, for a given list of Ids
     *
     * @param entityManagerMSI
     * @param listPeptideInstanceIds
     * @return
     */
    private boolean fetchPeptideInstanceData(EntityManager entityManagerMSI, List<Long> listPeptideInstanceIds, boolean xic) throws Exception{
        PerformanceTest.startTime("PeptideXIC.fetchPeptideInstanceData");
        HashMap<Long, Peptide> peptideMap = new HashMap<>();
        // map qcId rsmId
        Map<Long, Long> rsmIdVsQcId = new HashMap<>();
        List<Long> rsmIdList = new ArrayList<>();
        List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();
        for (DMasterQuantitationChannel masterQuantChannel : listMasterQuantitationChannels) {
            List<DQuantitationChannel> listQc = masterQuantChannel.getQuantitationChannels();
            for (DQuantitationChannel qc : listQc) {
                rsmIdVsQcId.put(qc.getIdentResultSummaryId(), qc.getId());
                rsmIdList.add(qc.getIdentResultSummaryId());
            }            
        }

        boolean isBestIonSummarizingMethod = mayBestIonPropertySet();                
        // load dPeptideInstance and PeptideMatch
        Map<Long, DPeptideInstance> peptideInstanceById = new HashMap<>();
        String querySelect;
        if (xic) {
            querySelect = "SELECT  pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, p, pm.serializedProperties, mqpi.elutionTime "
                    + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                    + "fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi "
                    + "WHERE pi.id IN (:listId) AND "
                    + "pi.bestPeptideMatchId=pm.id AND "
                    + "pm.peptideId=p.id  AND  pi.id = mqpi.peptideInstance.id "
                    + "ORDER BY pm.score DESC";
        } else { // Spectral Count
            querySelect = "SELECT  pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, p, pm.serializedProperties "
                    + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                    + "fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p "
                    + "WHERE pi.id IN (:listId) AND "
                    + "pi.bestPeptideMatchId=pm.id AND "
                    + "pm.peptideId=p.id "
                    + "ORDER BY pm.score DESC";
        }
        PerformanceTest.startTime("fetchPeptideInstanceData.Query");
        Query query = entityManagerMSI.createQuery(querySelect);
        query.setParameter("listId", listPeptideInstanceIds);

        HashMap<Long, DPeptideMatch> peptideMatchMap = new HashMap<>();
        Iterator<Object[]> itPeptidesQuery = query.getResultList().iterator();
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
            PeptideInstance pi = (PeptideInstance) resCur[0];
            Float elutionTime = (xic) ? (Float) resCur[13] : pi.getElutionTime();
            DPeptideInstance dpi = new DPeptideInstance(pi);
            dpi.setElutionTime(elutionTime);
            dpi.setResultSummary(pi.getResultSummary());
            Long pmId = (Long) resCur[1];

            DPeptideMatch pm = peptideMatchMap.get(pmId);
            if (pm == null) {
                Integer pmRank = (Integer) resCur[2];
                Integer pmCharge = (Integer) resCur[3];
                Float pmDeltaMoz = (Float) resCur[4];
                Double pmExperimentalMoz = (Double) resCur[5];
                Integer pmMissedCleavage = (Integer) resCur[6];
                Float pmScore = (Float) resCur[7];
                Long pmResultSetId = (Long) resCur[8];
                Integer pmCdPrettyRank = (Integer) resCur[9];
                Integer pmSdPrettyRank = (Integer) resCur[10];
                String pmSerializedProp = (String) resCur[12];
                pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank, pmSerializedProp);
                pm.setRetentionTime(elutionTime);
                peptideMatchMap.put(pmId, pm);
            }

            Peptide p = (Peptide) resCur[11];
            if(!p.getTransientData().isPeptideReadablePtmStringLoaded() || ( (p.getPtmString() != null || !p.getPtmString().isEmpty()) && p.getTransientData().getDPeptidePtmMap() == null) ) {
                p.getTransientData().setPeptideReadablePtmStringLoaded();
                peptideMap.put(p.getId(), p);
            }

            dpi.setBestPeptideMatch(pm);
            dpi.setPeptide(p);
            pm.setPeptide(p);

            peptideInstanceById.put(dpi.getId(), dpi);
        }
        PerformanceTest.stopTime("fetchPeptideInstanceData.Query");

//        PerformanceTest.startTime("fetchPeptideInstanceData.peptideMap");
        if(!peptideMap.isEmpty()) {
//            m_logger.debug(" fetchPeptideInstanceData Call Read PTM tasks 1 ....");
            DatabasePTMsTask.fillReadablePTMDataForPeptides(entityManagerMSI, m_dataset.getResultSetId(), peptideMap, null);
            DatabasePTMsTask.fillPeptidePTMForPeptides(entityManagerMSI, peptideMap, null);
        }
//        PerformanceTest.stopTime("fetchPeptideInstanceData.peptideMap");

        int nbMP = m_masterQuantPeptideList.size();
        //  load MasterQuantPeptide and list of QuantPeptide
//        PerformanceTest.startTime("queryDMasterQuantPeptideStr");
        String queryDMasterQuantPeptideStr = "SELECT q.id, q.selectionLevel, q.objectTreeId,  q.serializedProperties,  pi.resultSummary.id, pi.id "
                + "FROM MasterQuantComponent q, PeptideInstance pi "
                + " WHERE pi.id IN (:listPeptideInstanceId) AND  q.id = pi.masterQuantComponentId "
                + " ORDER BY q.id ASC ";
        Query mqPeptideQ = entityManagerMSI.createQuery(queryDMasterQuantPeptideStr);
        mqPeptideQ.setParameter("listPeptideInstanceId", listPeptideInstanceIds);
        List<Object[]> resultListMQPeptide = mqPeptideQ.getResultList();
        List<Long> listObjectTreeId = new ArrayList<>();
        List<Long> listPeptideId = new ArrayList<>();
        List<DMasterQuantPeptide> listMasterQP = new ArrayList<>();
//        PerformanceTest.stopTime("queryDMasterQuantPeptideStr");

//        PerformanceTest.startTime("PeptideXIC.FORresultListMQPeptide");
        //Call fetchPeptideIonData once with all Ids
        List<Long> bestMQPepIonIds = new ArrayList<>();
        Map<Long, DMasterQuantPeptide> masterQPepByBestPepIonId = new HashMap<>();
        m_masterQuantPeptideIonList = new ArrayList<>(); // for fetchPeptideIonData call

        for (Object[] resCur : resultListMQPeptide) {
            int i = 0;
            long masterQuantPeptideId = (long) resCur[i++];
            int selectionLevel = (int) resCur[i++];
            long objectTreeId = (long) resCur[i++];
            String serializedProp = (String) resCur[i++];
            long quantRSId = (long) resCur[i++];
            long peptideInstanceId = (long) resCur[i];
            DMasterQuantPeptide masterQuantPeptide = new DMasterQuantPeptide(masterQuantPeptideId, selectionLevel, objectTreeId, serializedProp, quantRSId);
            //search index of peptideInstance
            DPeptideInstance pi = peptideInstanceById.get(peptideInstanceId);
            masterQuantPeptide.setPeptideInstance(pi);
            masterQuantPeptide.setPeptideInstanceId(peptideInstanceId);
            listObjectTreeId.add(objectTreeId);
            listPeptideId.add(pi.getPeptideId());
            listMasterQP.add(masterQuantPeptide);
            
            //Update MQPeptide representative PeptideMatch
            if(isBestIonSummarizingMethod){
                MasterQuantPeptideProperties mqPepProp = masterQuantPeptide.getMasterQuantPeptideProperties();
                if(mqPepProp != null && mqPepProp.getMqPepIonAbundanceSummarizingConfig() !=null){
                    List<Long> mqpepIonIds  =  mqPepProp.getMqPepIonAbundanceSummarizingConfig().getSelectedMasterQuantPeptideIonIds();
                    //Should have only one ! Use first in any case
                    if(!mqpepIonIds.isEmpty()){
                        //Get MQPepIon RepresentativePeptideMatch
                        Long bestMQPepIonId = mqpepIonIds.get(0);
                        if(bestMQPepIonId != null && bestMQPepIonId >0){
                            DMasterQuantPeptideIon mqpi = new DMasterQuantPeptideIon();
                            mqpi.setId(bestMQPepIonId);
                            //save information for fetchPeptideIonData call
                            bestMQPepIonIds.add(bestMQPepIonId);
                            m_masterQuantPeptideIonList.add(mqpi);
                            masterQPepByBestPepIonId.put(bestMQPepIonId, masterQuantPeptide);
                        }
                    }
                }
            }
        } //End for query result

        //Get Peptide Ion info only once
//        PerformanceTest.startTime("PeptideXIC.fetchPeptideIonData");
        fetchPeptideIonData(entityManagerMSI, bestMQPepIonIds);
        for(DMasterQuantPeptideIon mqpi : m_masterQuantPeptideIonList){
            masterQPepByBestPepIonId.get(mqpi.getId()).setRepresentativePepMatch(mqpi.getRepresentativePepMatch());
        }
//        PerformanceTest.stopTime("PeptideXIC.fetchPeptideIonData");
//        PerformanceTest.stopTime("PeptideXIC.FORresultListMQPeptide");

        // load PSM count for each peptide instance in the different quantChannels
//        PerformanceTest.startTime("PeptideXIC.listPeptideId");
        Map<Long, Map<Long, Integer>> identPSMPerQCIdPerPepId = new HashMap<>();
        if (!listPeptideId.isEmpty()) {
            String queryCountPSM = "SELECT pi.peptideMatchCount, pi.resultSummary.id, pi.peptide.id "
                    + "FROM PeptideInstance pi "
                    + "WHERE pi.resultSummary.id in (:listRsm) AND pi.peptide.id in (:listPepIds) ";
            Query queryPSM = entityManagerMSI.createQuery(queryCountPSM);
            queryPSM.setParameter("listRsm", rsmIdList);
            queryPSM.setParameter("listPepIds", listPeptideId);
            Iterator<Object[]> resultListPSMIt = queryPSM.getResultList().iterator();
            while (resultListPSMIt.hasNext()) {
                Object[] nb = resultListPSMIt.next();
                int psm = (Integer) nb[0];
                Long rsmId = ((Long) nb[1]);
                Long qcId = rsmIdVsQcId.get(rsmId);
                Long peptideId = ((Long) nb[2]);
                Map<Long, Integer> identPSMPerQCId = identPSMPerQCIdPerPepId.get(peptideId);
                if (identPSMPerQCId == null) {
                    identPSMPerQCId = new HashMap<>();
                }
                identPSMPerQCId.put(qcId, psm);
                identPSMPerQCIdPerPepId.put(peptideId, identPSMPerQCId);
            }
        }
//        PerformanceTest.stopTime("PeptideXIC.listPeptideId");
        List<ObjectTree> listOt = new ArrayList<>();
//        PerformanceTest.startTime("PeptideXIC.listObjectTreeId");
        if (!listObjectTreeId.isEmpty()) {
            String otQuery = "SELECT ot FROM fr.proline.core.orm.msi.ObjectTree ot WHERE id IN (:listId) ";
            TypedQuery<ObjectTree> queryObjectTree = entityManagerMSI.createQuery(otQuery, ObjectTree.class);
            queryObjectTree.setParameter("listId", listObjectTreeId);
            listOt = queryObjectTree.getResultList();
        }
        for (DMasterQuantPeptide masterQuantPeptide : listMasterQP) {
            int index = -1;
            for (int k = 0; k < nbMP; k++) {
                if (m_masterQuantPeptideList.get(k).getPeptideInstanceId() == masterQuantPeptide.getPeptideInstanceId()) {
                    index = k;
                    break;
                }
            }
            // list of quantPeptide
            String quantPeptideData = ""; //ObjectTree.clobData
            ObjectTree ot = null;
            // search objectTree
            for (ObjectTree objectTree : listOt) {
                if (objectTree.getId() == masterQuantPeptide.getObjectTreeId()) {
                    ot = objectTree;
                    break;
                }
            }
            if (ot != null) {
                quantPeptideData = ot.getClobData();
            }
            Map<Long, DQuantPeptide> quantProteinSetByQchIds = null;
            if (quantPeptideData != null && !quantPeptideData.isEmpty()) {
                quantProteinSetByQchIds = masterQuantPeptide.parseQuantPeptideFromProperties(quantPeptideData);
            }
            // ident PSM
            Map<Long, Integer> identPSMPerQCId = identPSMPerQCIdPerPepId.get(masterQuantPeptide.getPeptideInstance().getPeptideId());
            if (identPSMPerQCId != null) {
                for (Entry<Long, DQuantPeptide> entrySet : quantProteinSetByQchIds.entrySet()) {
                    Long qcId = entrySet.getKey();
                    DQuantPeptide quantPeptide = entrySet.getValue();
                    Integer psm = identPSMPerQCId.get(qcId);
                    if (psm != null) {
                        quantPeptide.setIdentPeptideMatchCount(psm);
                    }
                }
            }
            masterQuantPeptide.setQuantPeptideByQchIds(quantProteinSetByQchIds);
            masterQuantPeptide.setCluster(getPeptideCluster(masterQuantPeptide.getId()));
            // update the list 
            m_masterQuantPeptideList.set(index, masterQuantPeptide);
        }
//        PerformanceTest.stopTime("PeptideXIC.listObjectTreeId");
        // non quantified peptides instances :
        // no master quantPeptide: build a fake masterQuantPeptide to display the peptideInstance
        Map<Long, DPeptideInstance> extendedPeptideInstanceById = null;
        peptideMap.clear();
        PerformanceTest.startTime("PeptideXIC.FORnbMP");
        for (int i = 0; i < nbMP; i++) {
            DMasterQuantPeptide masterQuantPeptide = m_masterQuantPeptideList.get(i);
            if (masterQuantPeptide.getId() == -1) {
                if (extendedPeptideInstanceById == null) {
                    extendedPeptideInstanceById = new HashMap<>();

                    String querySelect2 = "SELECT  pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, p "
                            + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                            + "fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p  "
                            + "WHERE pi.id IN (:listId) AND  "
                            + "pi.bestPeptideMatchId=pm.id AND "
                            + "pm.peptideId=p.id  "
                            + "ORDER BY pm.score DESC";

                    Query query2 = entityManagerMSI.createQuery(querySelect2);
                    query2.setParameter("listId", listPeptideInstanceIds);
                    List<Object[]> resultList2 = query2.getResultList();

                    //HashMap<Long, DPeptideMatch> peptideMatchMap = new HashMap<>();
                    itPeptidesQuery = resultList2.iterator();
                    while (itPeptidesQuery.hasNext()) {
                        Object[] resCur = itPeptidesQuery.next();
                        PeptideInstance pi = (PeptideInstance) resCur[0];
                        DPeptideInstance dpi = new DPeptideInstance(pi); //JPM.TODO
                        dpi.setResultSummary(pi.getResultSummary());
                        Long pmId = (Long) resCur[1];

                        DPeptideMatch pm = peptideMatchMap.get(pmId);
                        if (pm == null) {
                            Integer pmRank = (Integer) resCur[2];
                            Integer pmCharge = (Integer) resCur[3];
                            Float pmDeltaMoz = (Float) resCur[4];
                            Double pmExperimentalMoz = (Double) resCur[5];
                            Integer pmMissedCleavage = (Integer) resCur[6];
                            Float pmScore = (Float) resCur[7];
                            Long pmResultSetId = (Long) resCur[8];
                            Integer pmCdPrettyRank = (Integer) resCur[9];
                            Integer pmSdPrettyRank = (Integer) resCur[10];
                            String pmSerializedProp = (String) resCur[11];
                            pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank, pmSerializedProp);

                            if (!xic) {
                                pm.setRetentionTime(pi.getElutionTime());
                            }
                            peptideMatchMap.put(pmId, pm);
                        }

                        Peptide p = (Peptide) resCur[12];
                        if(!p.getTransientData().isPeptideReadablePtmStringLoaded() || ( (p.getPtmString() != null || !p.getPtmString().isEmpty()) && p.getTransientData().getDPeptidePtmMap() == null) ) {
                            p.getTransientData().setPeptideReadablePtmStringLoaded();
                            peptideMap.put(p.getId(), p);
                        }

                        dpi.setBestPeptideMatch(pm);
                        dpi.setPeptide(p);
                        pm.setPeptide(p);

                        extendedPeptideInstanceById.put(dpi.getId(), dpi);

                    }
                }

                //search index of peptideInstance
                DPeptideInstance peptideInstance = null;
                PerformanceTest.startTime("PeptideXIC.masterQuantPeptide.getPeptideInstanceId");
                if (masterQuantPeptide.getPeptideInstanceId() > 0 && listPeptideInstanceIds.contains(masterQuantPeptide.getPeptideInstanceId())) {
                    peptideInstance = extendedPeptideInstanceById.get(masterQuantPeptide.getPeptideInstanceId());
                    DMasterQuantPeptide o = new DMasterQuantPeptide(-1, -1, -1, null, masterQuantPeptide.getQuantResultSummaryId());
                    o.setPeptideInstance(peptideInstance);
                    o.setPeptideInstanceId(masterQuantPeptide.getPeptideInstanceId());

                    // load PSM count for each peptide instance in the different quantChannels
//                    PerformanceTest.startTime("PeptideXIC.queryCountPSM.query");
                    String queryCountPSM = "SELECT pi.peptideMatchCount, pi.resultSummary.id FROM PeptideInstance pi "
                            + "WHERE pi.resultSummary.id in (:listRsm) AND pi.peptide.id =:pepId ";
                    Query queryPSM = entityManagerMSI.createQuery(queryCountPSM);
                    queryPSM.setParameter("listRsm", rsmIdList);
                    queryPSM.setParameter("pepId", peptideInstance.getPeptideId());
                    Map<Long, DQuantPeptide> quantProteinSetByQchIds = new HashMap<>();
                    List<Object[]> resultListPSM = queryPSM.getResultList();
//                    PerformanceTest.stopTime("PeptideXIC.queryCountPSM.query");

                    for (Iterator<Object[]> iterator = resultListPSM.iterator(); iterator.hasNext(); ) {
                        Object[] nb = iterator.next();
                        int psm = ((Integer) nb[0]);
                        Long rsmId = ((Long) nb[1]);
                        Long qcId = rsmIdVsQcId.get(rsmId);
                        DQuantPeptide quantPeptide = new DQuantPeptide(Float.NaN, Float.NaN, -1, null, qcId);
                        quantPeptide.setIdentPeptideMatchCount(psm);
                        quantProteinSetByQchIds.put(qcId, quantPeptide);
                        o.setQuantPeptideByQchIds(quantProteinSetByQchIds);
                    }
                    // update the list 
                    m_masterQuantPeptideList.set(i, o);
                }
                PerformanceTest.stopTime("PeptideXIC.masterQuantPeptide.getPeptideInstanceId");
            }
        } //End for all mqPep

        PerformanceTest.stopTime("PeptideXIC.FORnbMP");
        if(!peptideMap.isEmpty()) {
            m_logger.debug("fetchPeptideInstanceData Call Read PTM 2");
            DatabasePTMsTask.fillReadablePTMDataForPeptides(entityManagerMSI, m_dataset.getResultSetId(), peptideMap, null);
            DatabasePTMsTask.fillPeptidePTMForPeptides(entityManagerMSI, peptideMap, null);
        }

        // load masterQuantPeptide not linked to a peptideInstance
        String queryDMasterQuantPeptide = "SELECT  new fr.proline.core.orm.msi.dto.DMasterQuantPeptide"
                + "(q.id, q.selectionLevel, q.objectTreeId,  q.serializedProperties,  q.resultSummary.id) "
                + "FROM MasterQuantComponent q "
                + " WHERE q.id=:masterQuantPeptideId";
        TypedQuery<DMasterQuantPeptide> masterQuantPeptideQuery = entityManagerMSI.createQuery(queryDMasterQuantPeptide, DMasterQuantPeptide.class);
        for (Long pepInstanceId : listPeptideInstanceIds) {
            if (pepInstanceId < 0) { // peptideInstanceId contains the -masterQuantPeptideId to load

                masterQuantPeptideQuery.setParameter("masterQuantPeptideId", -pepInstanceId);
                try {
                    DMasterQuantPeptide masterQuantPeptide = masterQuantPeptideQuery.getSingleResult();
                    if (masterQuantPeptide != null) {
                        DPeptideInstance fakePeptideInstance = new DPeptideInstance(null);
                        ResultSummary rs = new ResultSummary();
                        rs.setId(masterQuantPeptide.getQuantResultSummaryId());
                        fakePeptideInstance.setResultSummary(rs);
                        masterQuantPeptide.setPeptideInstance(fakePeptideInstance);
                        masterQuantPeptide.setPeptideInstanceId(-1);
                        // list of quantPeptide
                        String quantPeptideData = ""; //ObjectTree.clobData
                        ObjectTree ot = entityManagerMSI.find(ObjectTree.class, masterQuantPeptide.getObjectTreeId()); // get the objectTree from id.
                        if (ot != null) {
                            quantPeptideData = ot.getClobData();
                        }
                        // no ident psm count as it's not linked to a peptide instance
                        Map<Long, DQuantPeptide> quantProteinSetByQchIds = null;
                        if (quantPeptideData != null && !quantPeptideData.isEmpty()) {
                            quantProteinSetByQchIds = masterQuantPeptide.parseQuantPeptideFromProperties(quantPeptideData);
                        }
                        masterQuantPeptide.setQuantPeptideByQchIds(quantProteinSetByQchIds);
                        masterQuantPeptide.setCluster(getPeptideCluster(masterQuantPeptide.getId()));
                    }

                    int index = -1;
                    for (int k = 0; k < nbMP; k++) {
                        if (m_masterQuantPeptideList.get(k).getId() == masterQuantPeptide.getId()) {
                            index = k;
                            break;
                        }
                    }

                    // update the list 
                    m_masterQuantPeptideList.set(index, masterQuantPeptide);

                } catch (NoResultException | NonUniqueResultException ignored) {

                }
            }
        }

        ArrayList<Long> peptideMatchIds = new ArrayList<>(peptideMatchMap.size());
        peptideMatchIds.addAll(peptideMatchMap.keySet());
        fetchProteinSetName(entityManagerMSI, peptideMatchIds, peptideMatchMap);

       PerformanceTest.stopTime("PeptideXIC.fetchPeptideInstanceData");
//       PerformanceTest.displayTimeAllThreads();
        return true;
    }

    private boolean mayBestIonPropertySet(){
        boolean isBestIonSummarizingMethod = false;
        Map<String, Object> postProcessingParam = new HashMap<>();
        try {
            //Get PeptideIonAbundance Summarizer method.
            postProcessingParam = m_dataset.getPostQuantProcessingConfigAsMap();
        } catch (Exception ignored) {
        }
                
//        if(postProcessingParam == null)
//            isBestIonSummarizingMethod = false; //If no ComputePostProcessing, information may exist anyway ... Default is Sum...
//        else
        if(postProcessingParam != null && !postProcessingParam.isEmpty() && postProcessingParam.containsKey(QuantPostProcessingParams.PEP_ION_ABUNDANCE_SUMMARIZING_METHOD)){
            String method = postProcessingParam.get(QuantPostProcessingParams.PEP_ION_ABUNDANCE_SUMMARIZING_METHOD).toString();
            isBestIonSummarizingMethod = QuantPostProcessingParams.ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_KEY.equals(method);
        }
        return isBestIonSummarizingMethod;
    }
    
    private DCluster getPeptideCluster(Long masterQuantPeptideId) {
        // cluster info 
        if (m_dMasterQuantProteinSet != null) {
            try {
                MasterQuantProteinSetProperties prop = m_dMasterQuantProteinSet.getMasterQuantProtSetProperties();
                if (prop != null) {
                    HashMap<String, List<MasterQuantProteinSetProfile>> mqProtSetProfilesByGroupSetupNumber = prop.getMqProtSetProfilesByGroupSetupNumber();
                    if (mqProtSetProfilesByGroupSetupNumber != null) {
                        for (Entry<String, List<MasterQuantProteinSetProfile>> entry : mqProtSetProfilesByGroupSetupNumber.entrySet()) {
                            List<MasterQuantProteinSetProfile> listMasterQuantProteinSetProfile = entry.getValue();
                            if (listMasterQuantProteinSetProfile != null) {
                                int nbP = listMasterQuantProteinSetProfile.size();
                                for (int i = 0; i < nbP; i++) {
                                    MasterQuantProteinSetProfile profile = listMasterQuantProteinSetProfile.get(i);
                                    if (profile.getMqPeptideIds() != null && profile.getMqPeptideIds().contains(masterQuantPeptideId)) {
                                        return new DCluster(i + 1, profile.getAbundances(), profile.getRatios());
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed while retrieving MasterQuantProtSetProperties ", e);
            }
        }
        return null;
    }

    /**
     * *
     * load ProteinSet data for a given subTask
     *
     * @param subTask
     * @param entityManagerMSI
     * @return
     */
    private boolean fetchProteinSetData(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfProteinSetIds = subTask.getSubList(m_proteinSetIds);
        return fetchProteinSetData(entityManagerMSI, m_dataset, m_masterQuantProteinSetList, sliceOfProteinSetIds);
    }

    /**
     * subTask to load proteinSet
     *
     * @return
     */
    private boolean fetchProteinSetDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            switch (slice.getSubTaskId()) {
                case SUB_TASK_PROTEIN_SET:
                    fetchProteinSetData(slice, entityManagerMSI);
                    break;
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

    /**
     * load proteinSet data for a given proteinSet Ids list
     *
     * @param entityManagerMSI
     * @param proteinSetIds
     * @return
     */
    public static boolean fetchProteinSetData(EntityManager entityManagerMSI, DDataset dataset, List<DMasterQuantProteinSet> masterQuantProteinSetList, List<Long> proteinSetIds) {
        int nbMQP = masterQuantProteinSetList.size();

        m_logger.debug("fetchProteinSetData for " + proteinSetIds.size() + " of " +nbMQP+" masterQuantProteinSetList ");
        PerformanceTest.startTime("fetchProteinSetData subtask");

//        PerformanceTest.startTime("fetchProteinSetData queryDMasterQuantProteinSet");
        String queryDMasterQuantProteinSet = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantProteinSet"
                + "(q.id,  q.selectionLevel, q.objectTreeId,  q.serializedProperties,  p.resultSummary.id,  p.id) "
                + " FROM MasterQuantComponent q,  ProteinSet p "
                + " WHERE p.id IN (:listId) AND   q.id = p.masterQuantComponentId "
                + " ORDER BY q.id ASC ";
        TypedQuery<DMasterQuantProteinSet> masterQuantProteinSetsQuery = entityManagerMSI.createQuery(queryDMasterQuantProteinSet, DMasterQuantProteinSet.class);
        masterQuantProteinSetsQuery.setParameter("listId", proteinSetIds);
        //masterQuantProteinSetsQuery.setParameter("rsmId", resultSummaryId); // NO NEED, and even if this constaint is added, the query become slow
        List<DMasterQuantProteinSet> listResult = masterQuantProteinSetsQuery.getResultList();
//        PerformanceTest.stopTime("fetchProteinSetData queryDMasterQuantProteinSet");

//        PerformanceTest.startTime("fetchProteinSetData createQueries");
        Query allProteinSetQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinSet(ps.id, ps.representativeProteinMatchId, ps.resultSummary.id, ps.serializedProperties) "
                +" FROM ProteinSet ps WHERE ps.id in (:psIds)");
        String queryAllProteinMatch = "SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession,  pm.score, pm.peptideCount, pm.resultSet.id, pm.description, pm.geneName, pm.serializedProperties) "
                + "FROM ProteinMatch pm "
                + "WHERE pm.id in (:pmIds)";
        Query allProteinMatchQuery = entityManagerMSI.createQuery(queryAllProteinMatch);

        // Get nbr Peptide quantified and nbr total peptide (identified)
        String queryStrCountNbPepAndQuantPep = "SELECT protSet.id, count(pi.id) , pepSet.peptideCount "
                + "FROM fr.proline.core.orm.msi.ProteinSet protSet,  fr.proline.core.orm.msi.PeptideSet pepSet JOIN pepSet.peptideSetPeptideInstanceItems pspi JOIN pspi.peptideInstance pi, fr.proline.core.orm.msi.MasterQuantComponent mqc "
                + "WHERE pepSet.resultSummaryId=protSet.resultSummary.id AND pepSet.proteinSet.id=protSet.id AND protSet.id in (:proteinSetIds) AND "
                + "pi.masterQuantComponentId IS NOT NULL AND "
                + "mqc.id = pi.masterQuantComponentId AND "
                + "mqc.selectionLevel > 1"
                + "GROUP BY protSet.id, pepSet.peptideCount ";
        Query queryCountPepAndQuantPep = entityManagerMSI.createQuery(queryStrCountNbPepAndQuantPep);

        // Get nbr total peptide (identified). Use when no peptide quantified -> NO pi.masterQuantComponentId
        String queryStrAllCountNbPep = "SELECT  protSet.id, pepSet.peptideCount "
                + "FROM fr.proline.core.orm.msi.ProteinSet protSet, fr.proline.core.orm.msi.PeptideSet pepSet "
                + "WHERE pepSet.resultSummaryId= protSet.resultSummary.id AND protSet.id in (:proteinSetIds)  AND pepSet.proteinSet.id= protSet.id  ";
        Query allQueryCountPep = entityManagerMSI.createQuery(queryStrAllCountNbPep);

        String queryStrPepCountByProtMatch = "SELECT ps.peptideCount, pspmm.proteinMatch.id "
                + "FROM PeptideSetProteinMatchMap pspmm, PeptideSet ps "
                + "WHERE  pspmm.resultSummary.id=:rsmId  AND ps.id = pspmm.peptideSet.id ";
        Query queryPepCountByProtMatch = entityManagerMSI.createQuery(queryStrPepCountByProtMatch);

        String queryStrProtSetStatus = "SELECT ps.id, pspmi.proteinMatch.id, pspmi.isInSubset, ps.representativeProteinMatchId, ps.isValidated, pspmi.resultSummary.id "
                + " FROM ProteinSet ps JOIN  ProteinSetProteinMatchItem pspmi ON ps.id = pspmi.id.proteinSetId"
                + " WHERE ps.resultSummary.id in (:rsmIds) order by ps.id, pspmi.proteinMatch.id";
        Query queryProtSetStatus = entityManagerMSI.createQuery(queryStrProtSetStatus);

//        PerformanceTest.stopTime("fetchProteinSetData createQueries");
        List<DQuantitationChannel> listQC = new ArrayList<>();
        Map<Long, Map<Long, String>> protMatchStatusByIdByQcId = new HashMap<>();
        Map<Long, Map<Long, Integer>> protMatchPepNumberByIdByQcId = new HashMap<>();

        //Get ProtMatches PepCount and Status in all Quant Channels RSMs
        if (dataset != null && dataset.getMasterQuantitationChannels() != null && !dataset.getMasterQuantitationChannels().isEmpty()) {
            listQC = dataset.getMasterQuantitationChannels().get(0).getQuantitationChannels();
            List<Long> rsmsIds = listQC.stream().map(aQch -> aQch.getIdentResultSummaryId()).distinct().toList();
            Map<Long, ArrayList<Long>> qchIdsByRsmIds = new HashMap<>();
            for(DQuantitationChannel nextQch : listQC){
                Long rsmId  = nextQch.getIdentResultSummaryId();
                if(!qchIdsByRsmIds.containsKey(rsmId))
                    qchIdsByRsmIds.put(rsmId, new ArrayList<>());
                qchIdsByRsmIds.get(rsmId).add(nextQch.getId());
            }

            PerformanceTest.startTime("fetchProteinSetData queryProtSetStatus");
            queryProtSetStatus.setParameter("rsmIds", rsmsIds);
            List<Object[]> rStatus = queryProtSetStatus.getResultList();

            Long proteinMatchId;
            Boolean isInSubset;
            Long typProteinMatchId;
            Boolean isProtSetValidated;
            Long rsmId;
            for(Object[] resultStatV2 : rStatus) {
                proteinMatchId = (Long) resultStatV2[1];
                isInSubset = (Boolean) resultStatV2[2];
                typProteinMatchId = (Long) resultStatV2[3];
                isProtSetValidated = (Boolean) resultStatV2[4];
                rsmId = (Long)resultStatV2[5];

                String protMatchStatus;
                if (isInSubset) {
                    protMatchStatus = "Subset";
                } else if (typProteinMatchId.equals(proteinMatchId)) { //is the typical
                    protMatchStatus = "Typical";
                } else {
                    protMatchStatus = "Sameset";
                }
                if (!isProtSetValidated) {
                    protMatchStatus = "Invalid " + protMatchStatus;
                }
                ArrayList<Long> qChIds = qchIdsByRsmIds.get(rsmId);
                for(Long qChId : qChIds) {
                    if (!protMatchStatusByIdByQcId.containsKey(qChId))
                        protMatchStatusByIdByQcId.put(qChId, new HashMap<>());
                    protMatchStatusByIdByQcId.get(qChId).put(proteinMatchId, protMatchStatus);
                }
            }
            PerformanceTest.stopTime("fetchProteinSetData queryProtSetStatus");

            for (DQuantitationChannel qch : listQC) {
                PerformanceTest.startTime("fetchProteinSetData queryPepCountByProtMatch");
                Long identQCRsmId = qch.getIdentResultSummaryId();
                Map<Long, Integer> pepNumberByProtMatchId = new HashMap<>();
                queryPepCountByProtMatch.setParameter("rsmId", identQCRsmId);

                List<Object[]> rPepNumber = queryPepCountByProtMatch.getResultList();
                Long pepCountProteinMatchId;
                Integer pepNumber;
                for (Object[] resPep : rPepNumber) {
                    pepCountProteinMatchId = (Long) resPep[1];
                    pepNumber = (Integer) resPep[0];
                    pepNumberByProtMatchId.put(pepCountProteinMatchId, pepNumber);
                }

                protMatchPepNumberByIdByQcId.put(qch.getId(), pepNumberByProtMatchId);
                PerformanceTest.stopTime("fetchProteinSetData queryPepCountByProtMatch");
            }
        }

        m_logger.debug(" fetchProteinSetData -- Get DMasterQuantProteinSet info and associated DQuantProteinSet for each Quant Channel");
        List<Long> mqProtSetProteinSetIds = listResult.stream().map(mqProtSet -> Long.valueOf(mqProtSet.getProteinSetId())).collect(Collectors.toList());

        PerformanceTest.startTime("fetchProteinSetData queryCountPepAndQuantPepv2");
        Map<Long, List<Integer>> countByProtSetId = new HashMap<>();
        // nb PeptideInstance and nbPeptide quantified
        queryCountPepAndQuantPep.setParameter("proteinSetIds", mqProtSetProteinSetIds);
        Iterator<Object[]> resultList = queryCountPepAndQuantPep.getResultList().iterator();
        while (resultList.hasNext()) {
            Object[] nb = resultList.next();
            List<Integer> counts = new ArrayList<>();
            Long protSetId =((Long) nb[0]);
            counts.add(((Long) nb[1]).intValue());
            counts.add((Integer) nb[2]);
            countByProtSetId.put(protSetId, counts);
        }
        PerformanceTest.stopTime("fetchProteinSetData queryCountPepAndQuantPepv2");

        PerformanceTest.startTime("fetchProteinSetData queryCountPepv2");
        allQueryCountPep.setParameter("proteinSetIds", mqProtSetProteinSetIds);
        Map<Long, Integer> pepCountByProtSetId = new HashMap<>();
        Iterator<Object[]> rl = allQueryCountPep.getResultList().iterator();
        while (rl.hasNext()) {
            Object[] aResult = rl.next();
            pepCountByProtSetId.put((Long)aResult[0], (Integer) aResult[1]);
        }
        PerformanceTest.stopTime("fetchProteinSetData queryCountPepv2");

//        PerformanceTest.startTime("fetchProteinSetData DMasterQuantProteinSet DProtSet");
        allProteinSetQuery.setParameter("psIds", mqProtSetProteinSetIds);
        Map<Long, DProteinSet> dProtSetById = new HashMap<>();
        List<Long> protMatchIds = new ArrayList<>();
        Iterator<DProteinSet> prtSets =  allProteinSetQuery.getResultList().iterator();
        while (prtSets.hasNext()){
            DProteinSet nextProtSet =prtSets.next();
            dProtSetById.put(nextProtSet.getId(), nextProtSet);
            protMatchIds.add(nextProtSet.getProteinMatchId());
        }

//        PerformanceTest.stopTime("fetchProteinSetData DMasterQuantProteinSet DProtSet");

//        PerformanceTest.startTime("fetchProteinSetData DMasterQuantProteinSet proteinMatchQuery");
        allProteinMatchQuery.setParameter("pmIds", protMatchIds);
        Iterator<DProteinMatch> prtMatches =  allProteinMatchQuery.getResultList().iterator();
        Map<Long, DProteinMatch> dProtMatchById = new HashMap<>();
        while (prtMatches.hasNext()){
            DProteinMatch nextProtMatch = prtMatches.next();
            dProtMatchById.put(nextProtMatch.getId(), nextProtMatch);
        }
        for(DProteinSet nextProtSet : dProtSetById.values()) {

            DProteinMatch typicalProteinMatch = dProtMatchById.get(nextProtSet.getProteinMatchId());
            if (typicalProteinMatch == null) {
                m_logger.debug("--- NOT FOUND  DProteinMatch for ProtSet " + nextProtSet.getId());
            }
            nextProtSet.setTypicalProteinMatch(typicalProteinMatch);
        }
//        PerformanceTest.stopTime("fetchProteinSetData DMasterQuantProteinSet proteinMatchQuery");

        //Get DMasterQuantProteinSet info and associated DQuantProteinSet for each Quant Channel
        for (DMasterQuantProteinSet masterQuantProteinSet : listResult) {

            Long protSetId = masterQuantProteinSet.getProteinSetId();
            DProteinSet dProteinSet = dProtSetById.get(protSetId);


            PerformanceTest.startTime("fetchProteinSetData QuantProteinSetList");
            // load QuantProteinSetList
            String quantProtSetdata = ""; //ObjectTree.clobData
            ObjectTree ot = entityManagerMSI.find(ObjectTree.class, masterQuantProteinSet.getObjectTreeId()); // get the objectTree from id.
            if (ot != null) {
                quantProtSetdata = ot.getClobData();
            }
            Map<Long, DQuantProteinSet> quantProteinSetByQchIds = null;
            if (quantProtSetdata != null && !quantProtSetdata.isEmpty()) {
                quantProteinSetByQchIds = masterQuantProteinSet.parseQuantProteinSetFromProperties(quantProtSetdata);
            }
            masterQuantProteinSet.setQuantProteinSetByQchIds(quantProteinSetByQchIds);
            masterQuantProteinSet.setProteinSet(dProteinSet);
            PerformanceTest.stopTime("fetchProteinSetData QuantProteinSetList");

//            PerformanceTest.startTime("fetchProteinSetData CountPepAndQuantPep");
            // nb PeptideInstance and nbPeptide quantified
            int nbPep = 0;
            int nbPepQuant = 0;
            if (countByProtSetId.containsKey(protSetId)) {
                List<Integer> counts = countByProtSetId.get(protSetId);
                nbPepQuant = counts.get(0);
                nbPep = counts.get(1);
            }

            //try to get nbPepQuant from masterQuantProteinSet
            MasterQuantProteinSetProperties mqPSprop = masterQuantProteinSet.getMasterQuantProtSetProperties();
            if(mqPSprop!= null){
                nbPepQuant = mqPSprop.getSelectedMasterQuantPeptideIds().size();
            }
//            PerformanceTest.stopTime("fetchProteinSetData CountPepAndQuantPep");

            //If queryCountPepAndQuantPep return an empty result (or empty nbPep ?! not sure it's possible)
//            PerformanceTest.startTime("fetchProteinSetData queryCountPep");
            if (nbPep == 0 && pepCountByProtSetId.containsKey(protSetId)) {
                nbPep = pepCountByProtSetId.get(protSetId);
            }
            masterQuantProteinSet.setNbPeptides(nbPep);
            masterQuantProteinSet.setNbQuantifiedPeptides(nbPepQuant);
//            PerformanceTest.stopTime("fetchProteinSetData queryCountPep");

            // load status and peptideNumber by QcId
//            PerformanceTest.startTime("fetchProteinSetData Status");
            Map<Long, String> quantStatusByQchIds = new HashMap<>();
            for (DQuantitationChannel qch : listQC) {
                String status = "Undefined";
                Integer pepNumber = -1; //0;
                quantStatusByQchIds.put(qch.getId(), status);
                if (masterQuantProteinSet.getQuantProteinSetByQchIds().containsKey(qch.getId())) {
                    DQuantProteinSet protSetQch = masterQuantProteinSet.getQuantProteinSetByQchIds().get(qch.getId());
                    Long pmId = protSetQch.getProteinMatchId();// proteinMatchId in this qch
                    if (protMatchStatusByIdByQcId.containsKey(qch.getId())) {
                        Map<Long, String> protMatchStatusByIdPepMatch = protMatchStatusByIdByQcId.get(qch.getId());
                        if (protMatchStatusByIdPepMatch.containsKey(pmId)) {
                            status = protMatchStatusByIdPepMatch.get(pmId);
                        }
                    }

                    if(protSetQch.getPeptidesCount()<= 0 && protMatchPepNumberByIdByQcId.containsKey(qch.getId())) {
                        Map<Long, Integer> protMatchPepNumberByIdPepMatch = protMatchPepNumberByIdByQcId.get(qch.getId());
                        if (protMatchPepNumberByIdPepMatch.containsKey(pmId)) {
                            pepNumber = protMatchPepNumberByIdPepMatch.get(pmId);
                            protSetQch.setPeptidesCount(pepNumber);
                        }
                    }
                }
                quantStatusByQchIds.put(qch.getId(), status);

            }
            masterQuantProteinSet.setQuantStatusByQchIds(quantStatusByQchIds);
//            PerformanceTest.stopTime("fetchProteinSetData Status");

            // update in the list
            int index = -1;
            for (int k = 0; k < nbMQP; k++) {
                if (masterQuantProteinSetList.get(k).getProteinSetId() == dProteinSet.getId()) {
                    index = k;
                    break;
                }
            }
            masterQuantProteinSetList.set(index, masterQuantProteinSet);
        }
        PerformanceTest.stopTime("fetchProteinSetData subtask");
        return true;
    }

    /**
     * subTask to load peptide
     *
     * @return
     */
    private boolean fetchPeptideInstanceDataSubTask(boolean xic) {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            switch (slice.getSubTaskId()) {
                case SUB_TASK_PEPTIDE_INSTANCE:
                    m_logger.debug(" --- CALL LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES SUB_TASK_PEPTIDE_INSTANCE ");
                    fetchPeptideInstanceData(slice, entityManagerMSI, xic);
                    break;
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

    /**
     * *
     * load Peptide data for a given subTask
     *
     * @param subTask
     * @param entityManagerMSI
     * @return
     */
    private boolean fetchPeptideInstanceData(SubTask subTask, EntityManager entityManagerMSI, boolean xic) throws Exception {
        List<Long> sliceOfPeptideIds = subTask.getSubList(m_peptideInstanceIds);
        return fetchPeptideInstanceData(entityManagerMSI, sliceOfPeptideIds, xic);
    }

    /**
     * *
     * load PeptideIons data for a given subTask
     *
     * @param subTask
     * @param entityManagerMSI
     * @return
     */
    private boolean fetchPeptideIonData(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfPeptideIonIds = subTask.getSubList(m_masterQuantPeptideIonIds);
        return fetchPeptideIonData(entityManagerMSI, sliceOfPeptideIonIds);
    }

    /**
     * subTask to load peptideIon
     *
     * @return
     */
    private boolean fetchPeptideIonDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            switch (slice.getSubTaskId()) {
                case SUB_TASK_PEPTIDE_ION:
                    fetchPeptideIonData(slice, entityManagerMSI);
                    break;
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

    /**
     * load all peptides ions for a petide
     *
     * @return
     */
    private boolean fetchDataPeptideIonForPeptideMainTask() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary
                    if (resultSummaryId != null) {
                        // load peptideIon Ids
                        String queryPepIon = "SELECT mqpi.id "
                                + "FROM MasterQuantPeptideIon mqpi "
                                + "WHERE mqpi.masterQuantPeptideId=:masterQuantPeptideId AND mqpi.resultSummary.id=:rsmId "
                                + "ORDER BY mqpi.charge ASC";
                        Query peptidesIonQuery = entityManagerMSI.createQuery(queryPepIon);
                        peptidesIonQuery.setParameter("rsmId", resultSummaryId);
                        peptidesIonQuery.setParameter("masterQuantPeptideId", (m_masterQuantPeptide == null ? -1 : m_masterQuantPeptide.getId()));
                        m_masterQuantPeptideIonIds = (List<Long>) peptidesIonQuery.getResultList();
                        MasterQuantPeptideProperties property = m_masterQuantPeptide.getMasterQuantPeptideProperties();
                        
                        Map<Long,Integer> seqLevelByIonId = new HashMap<>();
                        if(property != null && property.getMqPepIonAbundanceSummarizingConfig() != null&& property.getMqPepIonAbundanceSummarizingConfig().getMqPeptideIonSelLevelById() != null)
                            seqLevelByIonId = property.getMqPepIonAbundanceSummarizingConfig().getMqPeptideIonSelLevelById();
                                                
                        for (Long m_masterQuantPeptideIonId : m_masterQuantPeptideIonIds) {
                            DMasterQuantPeptideIon mqpi = new DMasterQuantPeptideIon();
                            mqpi.setId(m_masterQuantPeptideIonId);
                            Integer level = seqLevelByIonId.get(mqpi.getId());
                            if(level != null)
                                mqpi.setUsedInPeptide(level >=2); //Should specify used value only in known case.
                            m_masterQuantPeptideIonList.add(mqpi);
                        }
                        
                        if (!m_masterQuantPeptideIonIds.isEmpty()) {
                            fetchPeptideIonData(entityManagerMSI, m_masterQuantPeptideIonIds);
                        }

                    }// end resultSummaryId null

                } // end of the for

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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }

    private boolean fetchDataReporterIonForQuantMainTask(){
        boolean fetchSucess =true;
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        long start = System.currentTimeMillis();
        long step1 = 0;
        m_masterQuantReporterIonIds = new ArrayList<>();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary
                    if (resultSummaryId != null) {
                        // load reporter Ion Ids
                        String queryPepIon = "SELECT mqri.id "
                        + "FROM MasterQuantReporterIon mqri, MasterQuantPeptideIon  mqpi "
                        + "WHERE mqri.resultSummary.id=:rsmId and mqri.masterQuantPeptideIon = mqpi";

                        Query  reporterAndPepIonQuery = entityManagerMSI.createQuery(queryPepIon);
                        reporterAndPepIonQuery.setParameter("rsmId", resultSummaryId);
                        List<Long> listIds = (List<Long>) reporterAndPepIonQuery.getResultList();
                        m_masterQuantReporterIonIds = listIds;

                        step1 = System.currentTimeMillis();
                        for (Long mqRid : m_masterQuantReporterIonIds) {
                            MasterQuantReporterIon mqRepIon = new MasterQuantReporterIon();
                            mqRepIon.setId(mqRid);
                            m_masterQuantReporterIonList.add(mqRepIon);
                        }

                        long end = System.currentTimeMillis();
                        m_logger.info("Reporter Ions ids fetched in = " + (end - start) + " ms. Query : "+(step1-start));

                        // slice the task and get the first one
                        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_REPORTER_ION, m_masterQuantReporterIonIds.size(), SLICE_SIZE);

                        // execute the first slice now
                        if (subTask != null) {
                            fetchSucess= fetchReporterIonData(subTask, entityManagerMSI);
                        }
                    }// end resultSummaryId null

                } // end of the for

            }
            long end = System.currentTimeMillis();
            m_logger.debug(" fetchDataReporterIonForQuantMainTask took "+(end-start)+" ms. Query: "+(step1-start)+"ms");
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            fetchSucess = false;
        } finally {
            entityManagerMSI.close();
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return fetchSucess;
    }

    /**
     * load all reporter ions for a peptide ion
     *
     * @return
     */
    private boolean fetchDataReporterIonForPeptideIonMainTask() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        boolean fetchSuccess = true;
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary
                    if (resultSummaryId != null) {
                        // load reporter Ion Ids
                        String queryPepIon = "SELECT mqri "
                                + "FROM MasterQuantReporterIon mqri "
                                + "WHERE mqri.masterQuantPeptideIon.id=:masterQuantPeptideIonId AND mqri.resultSummary.id=:rsmId ";
                        TypedQuery<MasterQuantReporterIon>  reporterIonQuery = entityManagerMSI.createQuery(queryPepIon, MasterQuantReporterIon.class);
                        reporterIonQuery.setParameter("rsmId", resultSummaryId);
                        reporterIonQuery.setParameter("masterQuantPeptideIonId", (m_specifiedMasterQuantPeptideIon == null ? -1 : m_specifiedMasterQuantPeptideIon.getId()));
                        List<MasterQuantReporterIon> listMasterQuantReporterIon=  reporterIonQuery.getResultList();

                        for(MasterQuantReporterIon mRepIOn : listMasterQuantReporterIon){
                            mRepIOn.getTransientData().setDMasterQuantPeptideIon(m_specifiedMasterQuantPeptideIon);
                            m_masterQuantReporterIonList.add(mRepIOn);
                        }

                        if (!m_masterQuantReporterIonList.isEmpty()) {
                            fetchSuccess = fetchReporterIonData(entityManagerMSI, m_masterQuantReporterIonList);
                        }

                    }// end resultSummaryId null

                } // end of the for

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
            fetchSuccess = false;
        } finally {
            entityManagerMSI.close();
        }

        // set priority as low for the possible subtasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return fetchSuccess;
    }


    private boolean fetchDataPeptideIonForAggregatePeptideIonMainTask()  {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();


            m_masterQuantPeptideIonList.clear();


            // read serializedpropeterties of the aggregated master quand peptide ion

            String queryPepIon = "SELECT mqpi.serializedProperties "
                                + "FROM MasterQuantPeptideIon mqpi "
                                + "WHERE mqpi.id=:id ";
            Query peptidesIonQuery = entityManagerMSI.createQuery(queryPepIon, String.class);
            peptidesIonQuery.setParameter("id", m_specifiedMasterQuantPeptideIon.getId());

            String serializedProperties = (String) peptidesIonQuery.getSingleResult();
            Map<String, Object> serializedPropertiesMap = JsonSerializer.getMapper().readValue(serializedProperties,Map.class);

            Map<String, ArrayList<Integer>> aggMasterQuantPeptideIonMap = (Map<String, ArrayList<Integer>>) serializedPropertiesMap.get("aggregated_master_quant_peptide_ion_id_map");

            //HashMap<Long, ArrayList<Long>> masterQuantPeptideIonIdByQC = new HashMap<>();
            //m_masterQuantPeptideIonByQC

            // look to master quant peptide ions to load
            HashSet<Long> masterQuantPeptideIonIds = new HashSet<>();

            for (String quantChannelIdString : aggMasterQuantPeptideIonMap.keySet()) {
                //Long quantChannelId = Long.valueOf(quantChannelIdString);
                ArrayList<Integer> parentMasterQuantIdList =  aggMasterQuantPeptideIonMap.get(quantChannelIdString);
                for (Integer id : parentMasterQuantIdList) {
                    masterQuantPeptideIonIds.add(id.longValue());
                }

            }


            m_masterQuantPeptideIonIds = new ArrayList<Long>(masterQuantPeptideIonIds);
            MasterQuantPeptideProperties property = m_masterQuantPeptide.getMasterQuantPeptideProperties();

            Map<Long, Integer> seqLevelByIonId = new HashMap<>();
            if (property != null && property.getMqPepIonAbundanceSummarizingConfig() != null && property.getMqPepIonAbundanceSummarizingConfig().getMqPeptideIonSelLevelById() != null) {
                seqLevelByIonId = property.getMqPepIonAbundanceSummarizingConfig().getMqPeptideIonSelLevelById();
            }

            for (Long m_masterQuantPeptideIonId : m_masterQuantPeptideIonIds) {
                DMasterQuantPeptideIon mqpi = new DMasterQuantPeptideIon();
                mqpi.setId(m_masterQuantPeptideIonId);
                Integer level = seqLevelByIonId.get(mqpi.getId());
                if (level != null) {
                    mqpi.setUsedInPeptide(level >= 2); //Should specify used value only in known case.
                }
                m_masterQuantPeptideIonList.add(mqpi);
            }

            if (!m_masterQuantPeptideIonIds.isEmpty()) {
                fetchPeptideIonData(entityManagerMSI, m_masterQuantPeptideIonIds);
            }

            HashSet<Long> channelIdsToLoad = new HashSet<>();
            for (DMasterQuantPeptideIon masterQuandPeptideIon : m_masterQuantPeptideIonList) {
                Set<Long> channelIds = masterQuandPeptideIon.getQuantPeptideIonByQchIds().keySet();
                for (Long channelId : channelIds) {
                    if (!m_quantitationChannelsMap.containsKey(channelId)) {
                        channelIdsToLoad.add(channelId);
                    }
                }
            }
            if (! channelIdsToLoad.isEmpty()) {
                m_taskError = fetchDataQuantChannels(channelIdsToLoad, m_quantitationChannelsMap);
            }

            if (! m_childrenDatasetIds.isEmpty()) {
                ArrayList<DDataset> childrenDatasetList = new ArrayList<>();
                m_taskError = fetchDataset(m_projectId, m_childrenDatasetIds, childrenDatasetList);
                for (DMasterQuantPeptideIon masterQuandPeptideIon : m_masterQuantPeptideIonList) {
                    Long rsmId = masterQuandPeptideIon.getResultSummary().getId();
                    for (DDataset d : childrenDatasetList) {
                        if (d.getResultSummaryId().longValue() == rsmId.longValue()) {
                            masterQuandPeptideIon.getResultSummary().getTransientData(null).setDDataset(d);
                        }
                    }

                }
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }

    /**
     * load peptideIon data for a given list of Ids
     *
     * @param entityManagerMSI
     * @param listMasterPeptideIonsIds
     * @return
     */
    private boolean fetchPeptideIonData(EntityManager entityManagerMSI, List<Long> listMasterPeptideIonsIds) {
        // list of MasterQuantPeptideIons
        int nbM = m_masterQuantPeptideIonList.size();

        PerformanceTest.startTime("fetchPeptideIonData");

        String queryMasterPeptideIons = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon(pi, mqpi, p, pm) "
                + "FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi, "
                + "fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideMatch pm "
                + "WHERE mqpi.id IN (:listId) AND pi.id = mqpi.peptideInstance.id AND  "
                + "mqpi.peptideId=p.id AND mqpi.bestPeptideMatchId = pm.id";

        TypedQuery<DMasterQuantPeptideIon> queryMasterIons = entityManagerMSI.createQuery(queryMasterPeptideIons, DMasterQuantPeptideIon.class);
        queryMasterIons.setParameter("listId", listMasterPeptideIonsIds);
        List<DMasterQuantPeptideIon> resultListIons = new ArrayList<>();
        if (listMasterPeptideIonsIds != null && !listMasterPeptideIonsIds.isEmpty()) {
            resultListIons = queryMasterIons.getResultList();
        }
        List<Integer> indexes = new ArrayList<>();
        List<Long> listObjectTreeId = new ArrayList<>();

        ArrayList<Long> peptideInstanceIds = new ArrayList<Long>(resultListIons.size());
        HashMap<Long, DPeptideInstance> peptideInstanceMap = new HashMap<>();
        //VDS TODO ? To get representativePepMatch in DMasterQuantPeptideIon dataset instead of leaf :
        // call DatabaseLoadPeptideMatchTask - fetchPSMForPeptideInstanceMainTask to load peptide instance's peptide matches
        // and get best peptide match with same charge than masterQuant peptide ion....  If exist. Not the case if 
        // a merge with aggregation has been done.
        for (DMasterQuantPeptideIon mQuantPeptideIon : resultListIons) {
            int i = 0;
            DPeptideInstance pi = mQuantPeptideIon.getPeptideInstance();
            peptideInstanceIds.add(pi.getId());
            Long keyPi = pi.getId();
            if (peptideInstanceMap.containsKey(keyPi)) {
                // replace peptide instance in mQuantPeptideIon, to limit number of PeptideInstance instances
                pi = peptideInstanceMap.get(keyPi);
                mQuantPeptideIon.setPeptideInstance(pi);
            } else {
                peptideInstanceMap.put(keyPi, pi);
            }

            listObjectTreeId.add(mQuantPeptideIon.getMasterQuantComponent().getObjectTreeId());

            // update the list
            int index = -1;
            for (int k = 0; k < nbM; k++) {
                if (m_masterQuantPeptideIonList.get(k).getId() == mQuantPeptideIon.getId()) {
                    index = k;
                    break;
                }
            }
            //Get used in peptide information from initial MQPeptideIon (if it was set...)
            if(m_masterQuantPeptideIonList.get(index).isUsedInPeptide() != null)
                mQuantPeptideIon.setUsedInPeptide(m_masterQuantPeptideIonList.get(index).isUsedInPeptide());
            m_masterQuantPeptideIonList.set(index, mQuantPeptideIon);
            indexes.add(index);
        } // end for

        /////////////////////////////////
        HashMap<Long, Peptide> peptideMap = new HashMap<>();
        // load dPeptideInstance and PeptideMatch
        String querySelect = "SELECT  pi.id, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties "
                + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                + "fr.proline.core.orm.msi.PeptideMatch pm "
                + "WHERE pi.id IN (:listId) AND  "
                + "pi.bestPeptideMatchId=pm.id "
                + "ORDER BY pm.score DESC";

        HashMap<Long, DPeptideMatch> peptideMatchMap = new HashMap<>();

        Query query = entityManagerMSI.createQuery(querySelect);
        query.setParameter("listId", peptideInstanceIds);
        List<Object[]> resultList = (List<Object[]>) query.getResultList();
        for (Object[] resCur : resultList) {
            Long piId = (Long) resCur[0];

            DPeptideInstance dpi = peptideInstanceMap.get(piId);

            //dpi.setResultSummary(pi.getResultSummary());
            Long pmId = (Long) resCur[1];

            DPeptideMatch pm = peptideMatchMap.get(pmId);
            if (pm == null) {
                Integer pmRank = (Integer) resCur[2];
                Integer pmCharge = (Integer) resCur[3];
                Float pmDeltaMoz = (Float) resCur[4];
                Double pmExperimentalMoz = (Double) resCur[5];
                Integer pmMissedCleavage = (Integer) resCur[6];
                Float pmScore = (Float) resCur[7];
                Long pmResultSetId = (Long) resCur[8];
                Integer pmCdPrettyRank = (Integer) resCur[9];
                Integer pmSdPrettyRank = (Integer) resCur[10];
                String pmSerializedProp = (String) resCur[11];
                pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank, pmSerializedProp);
                peptideMatchMap.put(pmId, pm);
            }

            Peptide p = dpi.getPeptide();
            if(!p.getTransientData().isPeptideReadablePtmStringLoaded() || ( (p.getPtmString() != null || !p.getPtmString().isEmpty()) && p.getTransientData().getDPeptidePtmMap() == null) ) {
                p.getTransientData().setPeptideReadablePtmStringLoaded();
                peptideMap.put(p.getId(), p);
            }
            dpi.setBestPeptideMatch(pm);
            pm.setPeptide(p);

        }

        if(!peptideMap.isEmpty()) {
            DatabasePTMsTask.fillReadablePTMDataForPeptides(entityManagerMSI, m_dataset.getResultSetId(), peptideMap, null);
            DatabasePTMsTask.fillPeptidePTMForPeptides(entityManagerMSI, peptideMap, null);
        }

        ArrayList<Long> peptideMatchIds = new ArrayList<>(peptideMatchMap.size());
        peptideMatchIds.addAll(peptideMatchMap.keySet());

        fetchProteinSetName(entityManagerMSI, peptideMatchIds, peptideMatchMap);

        fetchPeptideIonAbundances(listObjectTreeId, entityManagerMSI, indexes);

        // peptideIons without peptide
        indexes = new ArrayList<>();
        listObjectTreeId = new ArrayList<>();
        String queryPeptideIonWithoutPeptide = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon(mqpi) "
                + "FROM  fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi "
                + "WHERE mqpi.id IN (:listId) AND mqpi.peptideInstance is NULL  "
                + "ORDER BY mqpi.id ASC";
        TypedQuery<DMasterQuantPeptideIon> queryMasterIons2 = entityManagerMSI.createQuery(queryPeptideIonWithoutPeptide, DMasterQuantPeptideIon.class);
        queryMasterIons2.setParameter("listId", listMasterPeptideIonsIds);
        List<DMasterQuantPeptideIon> resultListIons2 = new ArrayList<>();
        if (listMasterPeptideIonsIds != null && !listMasterPeptideIonsIds.isEmpty()) {
            resultListIons2 = queryMasterIons2.getResultList();
        }
        for (DMasterQuantPeptideIon mQuantPeptideIon : resultListIons2) {
            listObjectTreeId.add(mQuantPeptideIon.getMasterQuantComponent().getObjectTreeId());

            // update the list
            int index = -1;
            for (int k = 0; k < nbM; k++) {
                if (m_masterQuantPeptideIonList.get(k).getId() == mQuantPeptideIon.getId()) {
                    index = k;
                    break;
                }
            }
            m_masterQuantPeptideIonList.set(index, mQuantPeptideIon);
            indexes.add(index);
        } // end for

        fetchPeptideIonAbundances(listObjectTreeId, entityManagerMSI, indexes);
        PerformanceTest.stopTime("fetchPeptideIonData");
        return true;
    }

    /**
     * *
     * load reporter Ion data for a given list
     *
     * @param subTask
     * @param entityManagerMSI
     * @return
     */
    private boolean fetchReporterIonData(SubTask subTask, EntityManager entityManagerMSI) throws IOException {
        List<Long> sliceOfReporterIonIds = (List<Long>) subTask.getSubList(m_masterQuantReporterIonIds);
        return fetchReporterIonDataFromIds(entityManagerMSI, sliceOfReporterIonIds);
    }

    /**
     * subTask to load reporter Ion data
     *
     * @return
     */
    private boolean fetchReporterIonForQuanDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        boolean isSuccess= false;
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            if (slice.getSubTaskId() == SUB_TASK_REPORTER_ION) {
                isSuccess = fetchReporterIonData(slice, entityManagerMSI);
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
            isSuccess=  false;
        } finally {
            entityManagerMSI.close();
        }

        return isSuccess;
    }

    private boolean fetchReporterIonDataFromIds(EntityManager entityManagerMSI, List<Long> listMasterReporterIonIds) throws IOException {

        int nbM = m_masterQuantReporterIonList.size();

        long start  = System.currentTimeMillis();
        String repAndPepIons = "SELECT mqri, pi, mqpi, p, pm "
                            + "FROM fr.proline.core.orm.msi.MasterQuantReporterIon mqri, fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi, "
                            + "fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideMatch pm "
                            + "WHERE mqri.masterQuantPeptideIon = mqpi AND pi.id = mqpi.peptideInstance.id AND  "
                            + "mqpi.peptideId=p.id AND mqpi.bestPeptideMatchId = pm.id AND "
                            + "mqri.id IN (:listId) ";

        Query repAndPepIonsQuery = entityManagerMSI.createQuery(repAndPepIons);
        repAndPepIonsQuery.setParameter("listId", listMasterReporterIonIds);
        List<Object[]> qResultList = (List<Object[]>) repAndPepIonsQuery.getResultList();

        Map<MasterQuantReporterIon, Long>  objectTreeIdByRepIon = new HashMap<>();
        HashMap<Long, MasterQuantReporterIon> mqRepIonById = new HashMap<>();
        HashMap<Long, DPeptideInstance> peptideInstanceMap = new HashMap<>();
        HashMap<Long, Peptide> peptideMap = new HashMap<>();

        for (Object[] resCur : qResultList) {
            MasterQuantReporterIon mRepIOn = (MasterQuantReporterIon) resCur[0];
            PeptideInstance pi = (PeptideInstance) resCur[1];
            MasterQuantPeptideIon mqpi = (MasterQuantPeptideIon) resCur[2];
            Peptide p = (Peptide) resCur[3];
            PeptideMatch pm = (PeptideMatch) resCur[4];
            DMasterQuantPeptideIon dMqPepIon = new DMasterQuantPeptideIon(pi, mqpi, p, pm);

            DPeptideInstance dpi = dMqPepIon.getPeptideInstance();
            Long keyPi = dpi.getId();
            if (peptideInstanceMap.containsKey(keyPi)) {
                // replace peptide instance in mQuantPeptideIon, to limit number of PeptideInstance instances
                dpi = peptideInstanceMap.get(keyPi);
                dMqPepIon.setPeptideInstance(dpi);
            } else {
                peptideInstanceMap.put(keyPi, dpi);
            }

            Peptide pepInstPep = dpi.getPeptide();
            pepInstPep.getTransientData().setPeptideReadablePtmStringLoaded();
            peptideMap.put(p.getId(), pepInstPep);

            // update the list
            int index = -1;
            for (int k = 0; k < nbM; k++) {
                if (m_masterQuantReporterIonList.get(k).getId() == mRepIOn.getId()) {
                    index = k;
                    break;
                }
            }
            m_masterQuantReporterIonList.set(index, mRepIOn);
            mRepIOn.getTransientData().setDMasterQuantPeptideIon(dMqPepIon);
            objectTreeIdByRepIon.put(mRepIOn, mRepIOn.getMasterQuantComponent().getObjectTreeId());
            mqRepIonById.put(mRepIOn.getId(), mRepIOn);
        }

        DatabasePTMsTask.fillReadablePTMDataForPeptides(entityManagerMSI, m_dataset.getResultSetId(), peptideMap, null);
        DatabasePTMsTask.fillPeptidePTMForPeptides(entityManagerMSI, peptideMap, null);

        fetchReporterIonAbundances(objectTreeIdByRepIon, entityManagerMSI);

        //fetch peptide matches
        String pepMQuery = "SELECT mqRepIon.id, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, pm.msQuery.spectrum.firstTime " +
                " FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.MasterQuantReporterIon mqRepIon " +
                "WHERE pm.msQuery.id = mqRepIon.msQueryId " +
                "AND pm.resultSet.id = mqRepIon.resultSummary.resultSet.id " +
                "AND pm.peptideId = mqRepIon.masterQuantPeptideIon.peptideId " +
                "AND mqRepIon.id IN (:listId) ";
        Query query = entityManagerMSI.createQuery(pepMQuery);
        query.setParameter("listId", mqRepIonById.keySet().stream().toList());
        List<Object[]> resultList = (List<Object[]>) query.getResultList();

        for (Object[] resCur : resultList) {
            Long mqRepId = (Long) resCur[0];
            Long pmId = (Long) resCur[1];
            Integer pmRank = (Integer) resCur[2];
            Integer pmCharge = (Integer) resCur[3];
            Float pmDeltaMoz = (Float) resCur[4];
            Double pmExperimentalMoz = (Double) resCur[5];
            Integer pmMissedCleavage = (Integer) resCur[6];
            Float pmScore = (Float) resCur[7];
            Long pmResultSetId = (Long) resCur[8];
            Integer pmCdPrettyRank = (Integer) resCur[9];
            Integer pmSdPrettyRank = (Integer) resCur[10];
            String pmSerializedProp = (String) resCur[11];
            Float retTime = (Float) resCur[12];
            DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank, pmSerializedProp);
            pm.setPeptide(mqRepIonById.get(mqRepId).getTransientData().getDMasterQuantPeptideIon().getPeptideInstance().getPeptide());
            pm.setRetentionTime(retTime);
            mqRepIonById.get(mqRepId).getTransientData().setPeptideMatch(pm);
        }

        long end = System.currentTimeMillis();
        m_logger.info("Reporter Ions data fetched in = " + (end - start) + " ms. ");

        return true;
    }



    /**
     * load reporter Ion data for a given list of MasterQuantReporterIon
     *
     * @param entityManagerMSI
     * @param listMasterReporterIons
     * @return
     */
    private boolean fetchReporterIonData(EntityManager entityManagerMSI, List<MasterQuantReporterIon> listMasterReporterIons) {

        Map<MasterQuantReporterIon, Long>  objectTreeIdByRepIon = new HashMap<>();
        HashMap<Long, MasterQuantReporterIon> mqRepIonById = new HashMap<>();
        for (MasterQuantReporterIon mQuantRepIon : listMasterReporterIons) {
            objectTreeIdByRepIon.put(mQuantRepIon, mQuantRepIon.getMasterQuantComponent().getObjectTreeId());
            mqRepIonById.put(mQuantRepIon.getId(), mQuantRepIon);
        } // end for

        fetchReporterIonAbundances(objectTreeIdByRepIon, entityManagerMSI);

        //fetch peptide matches
        String pepMQuery = "SELECT mqRepIon.id, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, pm.msQuery.spectrum.firstTime " +
                " FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.MasterQuantReporterIon mqRepIon " +
                "WHERE pm.msQuery.id = mqRepIon.msQueryId " +
                "AND pm.resultSet.id = mqRepIon.resultSummary.resultSet.id " +
                "AND pm.peptideId = mqRepIon.masterQuantPeptideIon.peptideId " +
                "AND mqRepIon.id IN (:listId) ";

        Query query = entityManagerMSI.createQuery(pepMQuery);
        query.setParameter("listId", mqRepIonById.keySet().stream().toList());
        List<Object[]> resultList =(List<Object[]> ) query.getResultList();
        for (Object[] resCur : resultList) {
            Long mqRepId = (Long) resCur[0];
            Long pmId = (Long) resCur[1];
            Integer pmRank = (Integer) resCur[2];
            Integer pmCharge = (Integer) resCur[3];
            Float pmDeltaMoz = (Float) resCur[4];
            Double pmExperimentalMoz = (Double) resCur[5];
            Integer pmMissedCleavage = (Integer) resCur[6];
            Float pmScore = (Float) resCur[7];
            Long pmResultSetId = (Long) resCur[8];
            Integer pmCdPrettyRank = (Integer) resCur[9];
            Integer pmSdPrettyRank = (Integer) resCur[10];
            String pmSerializedProp = (String) resCur[11];
            Float retTime = (Float) resCur[12];
            DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank, pmSerializedProp);
            pm.setPeptide(mqRepIonById.get(mqRepId).getTransientData().getDMasterQuantPeptideIon().getPeptideInstance().getPeptide());
            pm.setRetentionTime(retTime);
            mqRepIonById.get(mqRepId).getTransientData().setPeptideMatch(pm);
        }
        return true;
    }

    private void fetchReporterIonAbundances(Map<MasterQuantReporterIon, Long> objectTreeIdByRepIonId, EntityManager entityManagerMSI) {
        //object tree
        Map<Long, ObjectTree> listOtById = new HashMap<>();
        if (!objectTreeIdByRepIonId.isEmpty()) {
            String otQuery = "SELECT ot FROM fr.proline.core.orm.msi.ObjectTree ot WHERE id IN (:listId) ";
            TypedQuery<ObjectTree> queryObjectTree = entityManagerMSI.createQuery(otQuery, ObjectTree.class);
            queryObjectTree.setParameter("listId", objectTreeIdByRepIonId.values());
            List<ObjectTree> listOt = queryObjectTree.getResultList();
            listOtById = listOt.stream().collect(Collectors.toMap(ObjectTree::getId, Function.identity()));
        }

        for (MasterQuantReporterIon repIon : objectTreeIdByRepIonId.keySet()) {

            Long objTreeId = objectTreeIdByRepIonId.get(repIon);
            ObjectTree objTree = listOtById.get(objTreeId);

            String quantRepIonData = objTree.getClobData();

            if (quantRepIonData != null && !quantRepIonData.isEmpty())
                repIon.parseAnSetQuantReporterIonFromProperties(quantRepIonData);
            else
                repIon.getTransientData().setQuantReporterIonByQchIds(null);
        }
    }

    private void fetchPeptideIonAbundances(List<Long> listObjectTreeId, EntityManager entityManagerMSI, List<Integer> indexes) {
        //object tree
        List<ObjectTree> listOt = new ArrayList<>();
        if (!listObjectTreeId.isEmpty()) {
            String otQuery = "SELECT ot FROM fr.proline.core.orm.msi.ObjectTree ot WHERE id IN (:listId) ";
            TypedQuery<ObjectTree> queryObjectTree = entityManagerMSI.createQuery(otQuery, ObjectTree.class);
            queryObjectTree.setParameter("listId", listObjectTreeId);
            listOt = queryObjectTree.getResultList();
        }
        for (Integer index : indexes) {
            DMasterQuantPeptideIon mQuantPeptideIon = m_masterQuantPeptideIonList.get(index);

            String quantPeptideIonData = ""; //ObjectTree.clobData
            ObjectTree oti = null;
            // search objectTree
            for (ObjectTree objectTree : listOt) {
                if (objectTree.getId() == mQuantPeptideIon.getMasterQuantComponent().getObjectTreeId()) {
                    oti = objectTree;
                    break;
                }
            }
            //ObjectTree oti = entityManagerMSI.find(ObjectTree.class, mQuantPeptideIon.getMasterQuantComponent().getObjectTreeId()); // get the objectTree from id.
            if (oti != null) {
                quantPeptideIonData = oti.getClobData();
            }

            Map<Long, DQuantPeptideIon> quantPeptideIonByQchIds = null;
            if (quantPeptideIonData != null && !quantPeptideIonData.isEmpty()) {
                quantPeptideIonByQchIds = mQuantPeptideIon.parseQuantPeptideIonFromProperties(quantPeptideIonData);
            }
            mQuantPeptideIon.setQuantPeptideIonByQchIds(quantPeptideIonByQchIds);
            m_masterQuantPeptideIonList.set(index, mQuantPeptideIon);
        }
    }

    /**
     * load peptide matches for a specific Peptide for each quant channel
     *
     * @return
     */
    private boolean fetchDataPSMForPeptideMainTask() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            m_peptideMatchIds = new ArrayList<>();
            m_peptideMatchMap = new HashMap<>();
            m_peptideMatchSequenceMatchArrayMap = new HashMap<>();

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    List<DQuantitationChannel> listQuantChannels = masterQuantitationChannel.getQuantitationChannels();
                    for (DQuantitationChannel quantChannel : listQuantChannels) {
                        Long qcId = quantChannel.getId();
                        Long identRsmId = quantChannel.getIdentResultSummaryId();
                        Long peptideId = m_masterQuantPeptideForPSM.getPeptideInstance().getPeptideId();
                        String qPepInst = "SELECT pi.id "
                                + "FROM fr.proline.core.orm.msi.PeptideInstance pi "
                                + "WHERE pi.resultSummary.id =:rsmId AND "
                                + "pi.peptide.id =:peptideId ";
                        Query queryPepInst = entityManagerMSI.createQuery(qPepInst);
                        queryPepInst.setParameter("rsmId", identRsmId);
                        queryPepInst.setParameter("peptideId", peptideId);
                        Long peptideInstanceIdRSM = (Long) queryPepInst.getSingleResult();

                        String query = "SELECT  new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties) "
                                + "FROM  fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm , fr.proline.core.orm.msi.Peptide p "
                                + "WHERE pipm.resultSummary.id=:rsmId AND "
                                + "pipm.id.peptideMatchId=pm.id AND "
                                + "pm.peptideId=p.id AND "
                                + "pipm.id.peptideInstanceId=:peptideInstanceId "
                                + "ORDER BY pm.msQuery.initialId ASC, p.sequence ASC ";
                        TypedQuery<DPeptideMatch> queryPM = entityManagerMSI.createQuery(query, DPeptideMatch.class);
                        queryPM.setParameter("peptideInstanceId", peptideInstanceIdRSM);
                        queryPM.setParameter("rsmId", identRsmId);
                        List<DPeptideMatch> rList = queryPM.getResultList();
                        m_peptideMatchList.addAll(rList);
                        List<Long> listPeptideMatchIds = new ArrayList<>();
                        int nb = rList.size();
                        for (DPeptideMatch psm : rList) {
                            Long pmId = psm.getId();
                            listPeptideMatchIds.add(psm.getId());
                            m_peptideMatchIds.add(pmId);
                            m_peptideMatchMap.put(pmId, psm);

                            ArrayList<DPeptideMatch> sequenceMatchArray = m_peptideMatchSequenceMatchArrayMap.computeIfAbsent(pmId, k -> new ArrayList<>());
                            sequenceMatchArray.add(psm);
                        }
                        m_psmIdPerQC.put(qcId, listPeptideMatchIds);
                    }
                } // end of the for masterQuantitationChannel
                int nb = m_peptideMatchList.size();
                if (nb > 0) {
                    /**
                     * Peptide for each PeptideMatch
                     *
                     */
                    // slice the task and get the first one
                    SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE, m_peptideMatchList.size(), SLICE_SIZE);

                    // execute the first slice now
                    fetchPeptide(entityManagerMSI, subTask);

                    /**
                     * MS_Query for each PeptideMatch
                     *
                     */
                    // slice the task and get the first one
                    subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, m_peptideMatchList.size(), SLICE_SIZE);

                    // execute the first slice now
                    fetchMsQuery(entityManagerMSI, subTask);

                    /**
                     * ProteinSet String list for each PeptideMatch
                     *
                     */
                    // slice the task and get the first one
                    subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEINSET_NAME_LIST, m_peptideMatchList.size(), SLICE_SIZE);

                    // execute the first slice now
                    fetchProteinSetName(entityManagerMSI, subTask);
                }
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }

    private void fetchPeptide(EntityManager entityManagerMSI, SubTask subTask) {

        List<Long> sliceOfPeptideMatchIds =(List<Long>) subTask.getSubList(m_peptideMatchIds);

        HashMap<Long, Peptide> peptideMap = new HashMap<>();

        Query peptideQuery = entityManagerMSI.createQuery("SELECT pm.id, p FROM PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pm.id IN (:listId) AND pm.peptideId=p.id");
        peptideQuery.setParameter("listId", sliceOfPeptideMatchIds);

        List<Object[]> peptides =( List<Object[]> ) peptideQuery.getResultList();
        for (Object[] res : peptides) {
            Long peptideMatchId = (Long) res[0];
            Peptide peptide = (Peptide) res[1];
            peptide.getTransientData().setPeptideReadablePtmStringLoaded();
            m_peptideMatchMap.get(peptideMatchId).setPeptide(peptide);
            peptideMap.put(peptide.getId(), peptide);
        }

        // Retrieve PeptideReadablePtmString
        String query = "SELECT p.id, ptmString "
                + "FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString, fr.proline.core.orm.msi.PeptideMatch pm "
                + "WHERE  "
                + "pm.id IN (:listId) AND pm.peptideId=p.id AND "
                + "ptmString.peptide=p AND "
                + "ptmString.resultSet.id=pm.resultSet.id ";
        Query ptmStingQuery = entityManagerMSI.createQuery(query);
        ptmStingQuery.setParameter("listId", sliceOfPeptideMatchIds);

        List<Object[]> ptmStrings = (List<Object[]> ) ptmStingQuery.getResultList();
        for (Object[] res : ptmStrings) {
            Long peptideId = (Long) res[0];
            PeptideReadablePtmString ptmString = (PeptideReadablePtmString) res[1];
            Peptide peptide = peptideMap.get(peptideId);
            peptide.getTransientData().setPeptideReadablePtmString(ptmString);
        }

    }

    private void fetchMsQuery(EntityManager entityManagerMSI, SubTask subTask) {

        List<Long> sliceOfPeptideMatchIds = (List<Long>) subTask.getSubList(m_peptideMatchIds);

        String query = "SELECT pm.id, msq.id, msq.initialId, s.precursorIntensity, s.firstTime "
                + "FROM PeptideMatch pm,MsQuery msq, Spectrum s "
                + "WHERE pm.id IN (:listId) AND "
                + "pm.msQuery=msq AND "
                + "msq.spectrum=s";
        Query msQueryQuery = entityManagerMSI.createQuery(query);
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);

        List<Object[]> msQueries =(List<Object[]>) msQueryQuery.getResultList();
        for (Object[] o : msQueries) {
            int i = 0;
            long pmId = (Long) o[i++];
            long msqId = (Long) o[i++];
            int msqInitialId = (Integer) o[i++];
            Float precursorIntensity = (Float) o[i++];
            Float retentionTime = (Float) o[i];
            DMsQuery q = new DMsQuery(pmId, msqId, msqInitialId, precursorIntensity);

            DPeptideMatch peptideMatch = m_peptideMatchMap.get(q.getPeptideMatchId());
            peptideMatch.setMsQuery(q);
            peptideMatch.setRetentionTime(retentionTime);

        }
    }

    private void fetchProteinSetName(EntityManager entityManagerMSI, SubTask subTask) {

        List<Long> sliceOfPeptideMatchIds = (List<Long>) subTask.getSubList(m_peptideMatchIds);
        fetchProteinSetName(entityManagerMSI, sliceOfPeptideMatchIds, m_peptideMatchMap);
    }

    public static void fetchProteinSetName(EntityManager entityManagerMSI, List<Long> sliceOfPeptideMatchIds, HashMap<Long, DPeptideMatch> peptideMatchMap) {

        String query = "SELECT typpm.accession, pepm.id "
                + "FROM fr.proline.core.orm.msi.PeptideMatch pepm, fr.proline.core.orm.msi.PeptideInstance pepi, "
                + "fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pi_pm, fr.proline.core.orm.msi.ProteinSet prots, "
                + "fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem ps_pi, fr.proline.core.orm.msi.PeptideSet peps, "
                + "fr.proline.core.orm.msi.ProteinMatch typpm "
                + "WHERE pepm.id IN (:listId) AND "
                + "pi_pm.peptideMatch=pepm AND "
                //+ "pi_pm.resultSummary.id=:rsmId AND "
                + "pi_pm.peptideInstance=pepi AND "
                + "ps_pi.peptideInstance=pepi AND "
                + "ps_pi.peptideSet=peps AND "
                + "peps.proteinSet=prots AND "
                + "prots.representativeProteinMatchId = typpm.id AND "
                + "prots.isValidated=true "
                + "ORDER BY pepm.id ASC, typpm.accession ASC";
        Query proteinSetQuery = entityManagerMSI.createQuery(query);

        proteinSetQuery.setParameter("listId", sliceOfPeptideMatchIds);

        ArrayList<String> proteinSetNameArray = new ArrayList<>();
        long prevPeptideMatchId = -1;

        List<Object[]> msQueries = (List<Object[]>) proteinSetQuery.getResultList();
        for (Object[] resCur : msQueries) {
            String proteinName = (String) resCur[0];
            Long peptideMatchId = (Long) resCur[1];

            if (peptideMatchId != prevPeptideMatchId) {
                if (prevPeptideMatchId != -1) {
                    DPeptideMatch prevPeptideMatch = peptideMatchMap.get(prevPeptideMatchId);
                    String[] proteinSetNames = proteinSetNameArray.toArray(new String[0]);
                    prevPeptideMatch.setProteinSetStringArray(proteinSetNames);
                    proteinSetNameArray.clear();
                    proteinSetNameArray.add(proteinName);
                } else {
                    proteinSetNameArray.add(proteinName);
                }
            } else {
                proteinSetNameArray.add(proteinName);
            }

            prevPeptideMatchId = peptideMatchId;
        }
        if (prevPeptideMatchId != -1) {
            DPeptideMatch prevPeptideMatch = peptideMatchMap.get(prevPeptideMatchId);
            String[] proteinSetNames = proteinSetNameArray.toArray(new String[0]);
            prevPeptideMatch.setProteinSetStringArray(proteinSetNames);
        }

        for (Long peptideMatchId : sliceOfPeptideMatchIds) {
            DPeptideMatch peptideMatch = peptideMatchMap.get(peptideMatchId);
            if (peptideMatch.getProteinSetStringArray() == null) {
                String[] proteinSetNames = new String[0];
                peptideMatch.setProteinSetStringArray(proteinSetNames);
            }
        }
    }

    private String getMasterQuantProteinSetName(DMasterQuantProteinSet masterQuantProteinSet) {

        String name = null;

        if (masterQuantProteinSet.getProteinSet() != null) {
            DProteinMatch proteinMatch = masterQuantProteinSet.getProteinSet().getTypicalProteinMatch();
            if (proteinMatch != null) {
                name = proteinMatch.getAccession();
            }
        }

        if (name == null) {
            name = String.valueOf(masterQuantProteinSet.getId());
        }
        return name;
    }
}
