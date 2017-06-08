package fr.proline.studio.dam.tasks.xic;

import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DCluster;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.msi.dto.DQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DQuantProteinSet;
import fr.proline.core.orm.msi.dto.MasterQuantProteinSetProperties;
import fr.proline.core.orm.msi.dto.MasterQuantProteinSetProperties.MasterQuantProteinSetProfile;
import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.BiologicalSplSplAnalysisMap;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.GroupSetup;
import fr.proline.core.orm.uds.MasterQuantitationChannel;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.QuantitationLabel;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseSlicerTask;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.SubTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
    public static final int SUB_TASK_COUNT_QC = 0; 

    public static final int SUB_TASK_PROTEIN_SET = 1;
    public static final int SUB_TASK_COUNT_PROTEIN = 2; 
    

    public static final int SUB_TASK_COUNT_PEPTIDE = 3; 
    public static final int SUB_TASK_PEPTIDE_INSTANCE = 4;

    public static final int SUB_TASK_COUNT_PEPTIDE_ION = 5;
    public static final int SUB_TASK_PEPTIDE_ION = 6;

    
    public static final int SUB_TASK_PEPTIDE = 7;
    public static final int SUB_TASK_MSQUERY = 8;
    public static final int SUB_TASK_PROTEINSET_NAME_LIST = 9;
    public static final int SUB_TASK_COUNT_PSM = 10;
    
    public static final int SUB_TASK_PTM_PEPTIDE_INSTANCE = 11;
    
    public static final int SUB_TASK_NB = 12; // <<----- get in sync
    
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
    private List<DMasterQuantPeptideIon> m_masterQuantPeptideIonList;
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
   
    public DatabaseLoadXicMasterQuantTask(AbstractDatabaseCallback callback) {
        super(callback);

    }

    public void initLoadProteinSets(long projectId, DDataset dataset, List<DMasterQuantProteinSet> masterQuantProteinSetList) {
        init(SUB_TASK_NB, new TaskInfo("Load Protein Sets of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantProteinSetList = masterQuantProteinSetList;
        action = LOAD_PROTEIN_SET_FOR_XIC;
    }

    public void initLoadQuantChannels(long projectId, DDataset dataset) {
        init(SUB_TASK_NB, new TaskInfo("Load Quant Channels of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        action = LOAD_QUANT_CHANNELS_FOR_XIC;
    }

    public void initLoadPeptides(long projectId, DDataset dataset, List<DMasterQuantPeptide> masterQuantPeptideList, boolean xic) {
        init(SUB_TASK_NB, new TaskInfo("Load Peptides of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));

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
        init(SUB_TASK_NB, new TaskInfo("Load Peptides of proteinSet " + proteinSetName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideList = masterQuantPeptideList;
        m_dMasterQuantProteinSet = masterQuantProteinSet;
        m_xic = xic;
        action = LOAD_PEPTIDE_FOR_PROTEIN_SET;
    }
    
    public void initLoadPeptides(long projectId, DDataset dataset, Long[] peptideInstanceIdArray, List<DMasterQuantPeptide> masterQuantPeptideList, boolean xic) {

        init(SUB_TASK_NB, new TaskInfo("Load Peptides from peptide Instances ", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideList = masterQuantPeptideList;
        m_peptideInstanceIdArray = peptideInstanceIdArray;
        m_xic = xic;
        action = LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES;
    }

//      public void initLoadPeptidesWithIds(long projectId, DDataset dataset, List<Long> listPeptideInstanceIds, DMasterQuantProteinSet masterQuantProteinSet, List<DMasterQuantPeptide> masterQuantPeptideList) {
//          //EntityManager entityManagerMSI, List<Long> listPeptideInstanceIds, boolean xic
//        String proteinSetName = "";
//        if (masterQuantProteinSet != null) {
//            proteinSetName = getMasterQuantProteinSetName(masterQuantProteinSet);
//        }
//        
//        init(SUB_TASK_NB, new TaskInfo("Load Peptides of protein " + proteinSetName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
//        m_projectId = projectId;
//        m_dataset = dataset;
//        m_masterQuantPeptideList = masterQuantPeptideList;
//        m_peptideInstanceIds = listPeptideInstanceIds;
//        m_dMasterQuantProteinSet = masterQuantProteinSet;
//        m_xic = true;
//        action = LOAD_PEPTIDE_WITH_ID;
//    }
      
    public void initLoadPeptideIons(long projectId, DDataset dataset, DMasterQuantPeptide masterQuantPeptide, List<DMasterQuantPeptideIon> masterQuantPeptideIonList) {
        String peptideName = "";
        if (masterQuantPeptide != null && masterQuantPeptide.getPeptideInstance() != null && masterQuantPeptide.getPeptideInstance().getBestPeptideMatch() != null && masterQuantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide() != null) {
            peptideName = masterQuantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide().getSequence();
        }
        init(SUB_TASK_NB, new TaskInfo("Load Peptides Ions of peptide " + peptideName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptide = masterQuantPeptide;
        m_masterQuantPeptideIonList = masterQuantPeptideIonList;
        action = LOAD_PEPTIDE_ION_FOR_PEPTIDE;
    }

    public void initLoadPeptideIons(long projectId, DDataset dataset, List<DMasterQuantPeptideIon> masterQuantPeptideIonList) {
        init(SUB_TASK_NB, new TaskInfo("Load Peptides Ions of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideIonList = masterQuantPeptideIonList;
        action = LOAD_PEPTIDE_ION_FOR_XIC;
    }

    public void initLoadPSMForPeptide(long projectId, DDataset dataset, DMasterQuantPeptide masterQuantPeptide, List<DPeptideMatch> listPeptideMatch, Map<Long, List<Long>> psmPerQC) {
        String peptideName = "";
        if (masterQuantPeptide != null && masterQuantPeptide.getPeptideInstance() != null && masterQuantPeptide.getPeptideInstance().getBestPeptideMatch() != null && masterQuantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide() != null) {
            peptideName = masterQuantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide().getSequence();
        }
        init(SUB_TASK_NB, new TaskInfo("Load Protein Match of peptide " + peptideName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideForPSM = masterQuantPeptide;
        m_psmIdPerQC = psmPerQC;
        m_peptideMatchList = listPeptideMatch;
        action = LOAD_PSM_FOR_PEPTIDE;
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
            case LOAD_PEPTIDE_FOR_XIC:
                m_masterQuantPeptideList = null;
                break;
            case LOAD_PEPTIDE_FOR_PROTEIN_SET:
            case LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES:
                m_masterQuantPeptideList = null;
                break;
            case LOAD_PEPTIDE_ION_FOR_PEPTIDE:
                m_masterQuantPeptideIonList = null;
                break;
            case LOAD_PEPTIDE_ION_FOR_XIC:
                m_masterQuantPeptideIonList = null;
                break;
            case LOAD_PSM_FOR_PEPTIDE:
                m_peptideMatchList = null;
                break;
        }
    }

    @Override
    public boolean fetchData() {
        if (action == LOAD_QUANT_CHANNELS_FOR_XIC) {
            if (needToFetch()) {
                return fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
            }
        } else if (action == LOAD_PROTEIN_SET_FOR_XIC) {
            if (needToFetch()) {
                // first data are fetched
                if (m_dataset.getMasterQuantitationChannels() == null || m_dataset.getMasterQuantitationChannels().isEmpty()) {
                    fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
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
                    fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
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
                return fetchDataPeptideForProteinSetMainTask(m_xic);
            }

        } else if (action == LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES) {
            if (needToFetch()) {
                // first data are fetched
                //fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
                return fetchDataPeptideMainTask(m_xic, m_peptideInstanceIdArray);
            } else {
                // fetch data of SubTasks
                return fetchPeptideInstanceDataSubTask(m_xic);
            }
        } 
        
        
        else if (action == LOAD_PEPTIDE_ION_FOR_XIC) {
            if (needToFetch()) {
                // first data are fetched
                if (m_dataset.getMasterQuantitationChannels() == null || m_dataset.getMasterQuantitationChannels().isEmpty()) {
                    fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
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
                return fetchDataPeptideIonForPeptideMainTask();
            }

        } else if (action == LOAD_PSM_FOR_PEPTIDE) {
            if (needToFetch()) {
                // first data are fetched
                //fetchDataQuantChannels(m_projectId, m_dataset, m_taskError);
                return fetchDataPSMForPeptideMainTask();
            } else {
                return fetchDataSubTask();
            }

        } 
        return true; // should not happen
    }

    private boolean fetchDataSubTask() {
        SubTask subTask = m_subTaskManager.getNextSubTask();
        if (subTask == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
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
                return (m_masterQuantPeptideList == null || m_masterQuantPeptideList.isEmpty());
            case LOAD_PEPTIDE_FOR_PROTEIN_SET:
            case LOAD_PEPTIDE_FROM_PEPTIDES_INSTANCES:
                return (m_masterQuantPeptideList == null || m_masterQuantPeptideList.isEmpty());
            case LOAD_PEPTIDE_ION_FOR_XIC:
                return (m_masterQuantPeptideIonList == null || m_masterQuantPeptideIonList.isEmpty());
            case LOAD_PEPTIDE_ION_FOR_PEPTIDE:
                return (m_masterQuantPeptideIonList == null || m_masterQuantPeptideIonList.isEmpty());
            case LOAD_PSM_FOR_PEPTIDE:
                return (m_peptideMatchList == null || m_peptideMatchList.isEmpty());            
        }
        return false; // should not happen 
    }

    /**
     * load list quantitation channels
     *
     * @param projectId
     * @param dataset
     * @param taskError
     * @return
     */
    public static boolean fetchDataQuantChannels(Long projectId, DDataset dataset, TaskError taskError) {
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).createEntityManager();
        EntityManager entityManagerLCMS = DataStoreConnectorFactory.getInstance().getLcMsDbConnector(projectId).createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            entityManagerMSI.getTransaction().begin();
            entityManagerLCMS.getTransaction().begin();
            // load DDataset
            // force initialization of lazy data (data will be needed for the display of properties)
            Dataset datasetDB = entityManagerUDS.find(Dataset.class, dataset.getId());
            QuantitationMethod quantMethodDB = datasetDB.getMethod();
            List<MasterQuantitationChannel> listMasterQuantitationChannels = datasetDB.getMasterQuantitationChannels();

            // fill the current object with the db object
            dataset.setQuantitationMethod(quantMethodDB);
            // load labels lazydata
            Set<QuantitationLabel> labels = quantMethodDB.getLabels();
            labels.size();
            dataset.setDescription(datasetDB.getDescription());
            List<DMasterQuantitationChannel> masterQuantitationChannels = null;

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                masterQuantitationChannels = new ArrayList<>();
                for (MasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();
                    // load the list of quantitation channel linked to this masterQuantitationChannel
                    List<QuantitationChannel> listQuantitationChannels = masterQuantitationChannel.getQuantitationChannels();
                    List<DQuantitationChannel> listDQuantChannels = new ArrayList();
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
                            rsId = (Long)res[2];
                        } catch (NoResultException | NonUniqueResultException e) {
                            
                        }
                        // take the dataset name as qch name
                        String queryQCName = "SELECT ds.name "
                                    + "FROM fr.proline.core.orm.uds.Dataset ds, fr.proline.core.orm.uds.QuantitationChannel qc "
                                    + "WHERE ds.resultSummaryId = qc.identResultSummaryId AND "
                                    + "qc.id=:qChId AND ds.project.id=:projectId ";
                        TypedQuery<String> queryQCNameQ = entityManagerUDS.createQuery(queryQCName, String.class);
                        queryQCNameQ.setParameter("qChId", qc.getId());
                        queryQCNameQ.setParameter("projectId", projectId);
                        try {
                            String name = queryQCNameQ.getSingleResult();
                            resultFileName = name;
                        } catch (NoResultException | NonUniqueResultException e2) {

                        }
                        dqc.setResultFileName(resultFileName);
                        dqc.setRawFilePath(rawPath);
                        // search for run_identification rawFileName (mzdb fileName) in UDS

                        String mzdbFile = "";
                        try{
                            //mzdbFile = (String) queryMzdb.getSingleResult();
                             mzdbFile = qc.getRun().getRawFile().getMzDbFileName();
                        }catch( Exception e) {
                            m_logger.error("Error while retrieving mzdb file "+e);
                        }
                        dqc.setMzdbFileName(mzdbFile);
                        // search for raw map in LCMS database
                        String queryLcms = "SELECT pmrm.rawMap.id "
                                + "FROM fr.proline.core.orm.lcms.Map  m, ProcessedMap pm, ProcessedMapRawMapMapping pmrm  "
                                + "WHERE m.id =:processedMapId "
                                + "AND m.id = pm.id "
                                + "AND pm.id = pmrm.id.processedMapId ";
                        TypedQuery<Long> queryRawMapLcms = entityManagerLCMS.createQuery(queryLcms, Long.class);
                        queryRawMapLcms.setParameter("processedMapId", qc.getLcmsMapId());
                        try {
                            Long rawMapId = queryRawMapLcms.getSingleResult();
                            dqc.setLcmsRawMapId(rawMapId);
                        } catch (NoResultException | NonUniqueResultException e) {

                        }
                        if (rsId != null){
                            ResultSet rsetFound = entityManagerMSI.find(ResultSet.class, rsId);
                            dqc.setIdentRs(rsetFound);
                        }else{
                            dqc.setIdentRs(null);
                        }
                        // search if a dataset with rsmId, rsId exists
                        String queryIdentDsS = "SELECT ds.id FROM Dataset ds WHERE ds.resultSetId=:rsId AND ds.resultSummaryId=:rsmId ";
                        TypedQuery<Long> queryIdentDs = entityManagerUDS.createQuery(queryIdentDsS, Long.class);
                        queryIdentDs.setParameter("rsId",rsId );
                        queryIdentDs.setParameter("rsmId", qc.getIdentResultSummaryId());
                        try {
                            Long identDsId  = queryIdentDs.getSingleResult();
                            dqc.setIdentDatasetId(identDsId);
                        } catch (NoResultException | NonUniqueResultException e) {
                            dqc.setIdentDatasetId((long)-1);
                        }
                        
                        listDQuantChannels.add(dqc);
                    }
                    DMasterQuantitationChannel dMaster = new DMasterQuantitationChannel(masterQuantitationChannel.getId(), masterQuantitationChannel.getName(),
                            resultSummaryId, listDQuantChannels,
                            masterQuantitationChannel.getDataset(), masterQuantitationChannel.getSerializedProperties());
                    // load the dataset for which the id is stored in the serialized properties
                    Map<String, Object> propertiesMap = dMaster.getSerializedPropertiesAsMap();
                    DDataset identDataset = null;
                    if (propertiesMap != null) {
                        Object o = propertiesMap.get("ident_dataset_id");
                        if (o != null) {
                            Long identDatasetId = Long.parseLong(o.toString());
                            Dataset identDatasetDB = entityManagerUDS.find(Dataset.class, identDatasetId);
                            if (identDatasetDB != null) {
                                identDataset = new DDataset(identDatasetDB.getId(), identDatasetDB.getProject(), identDatasetDB.getName(), identDatasetDB.getType(),
                                        identDatasetDB.getChildrenCount(), identDatasetDB.getResultSetId(), identDatasetDB.getResultSummaryId(), identDatasetDB.getNumber());
                            }
                        }
                    }
                    dMaster.setIdentDataset(identDataset);
                    // load the list of quantitation channel linked to this masterQuantitationChannel
                    if (listQuantitationChannels != null && !listQuantitationChannels.isEmpty()) {
                        int id2 = 0;
                        for (QuantitationChannel quantitationChannel : listQuantitationChannels) {
                            // load biologicalSample
                            BiologicalSample biologicalSample = quantitationChannel.getBiologicalSample();
                            dMaster.getQuantitationChannels().get(id2).setBiologicalSample(biologicalSample);
                            id2++;
                        }
                    }
                    // add into the list
                    masterQuantitationChannels.add(dMaster);
                } // end of the for

                // Set the ResultSummary of the Dataset as the ResultSummary of the first MasterQuantitationChannel
                MasterQuantitationChannel masterQuantitationChannel = listMasterQuantitationChannels.get(0);
                Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();
                ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, resultSummaryId);
                dataset.setResultSummaryId(resultSummaryId);
                dataset.setResultSummary(rsm);
                dataset.setResultSet(rsm.getResultSet());
                dataset.setResultSetId(rsm.getResultSet().getId());

            }
            dataset.setMasterQuantitationChannels(masterQuantitationChannels);
            // load groupSetup
            Set<GroupSetup> groupSetupSet = datasetDB.getGroupSetups();
            if (groupSetupSet != null && !groupSetupSet.isEmpty()){
                GroupSetup groupSetup = groupSetupSet.iterator().next();
                List<BiologicalGroup> listBiolGroup = groupSetup.getBiologicalGroups();
                for (BiologicalGroup biolGroup : listBiolGroup) {
                    List<BiologicalSample> listBiologicalSamples = biolGroup.getBiologicalSamples();
                    for (BiologicalSample sample : listBiologicalSamples) {
                        List<BiologicalSplSplAnalysisMap> splAnalysisMap = sample.getBiologicalSplSplAnalysisMap();
                        for(BiologicalSplSplAnalysisMap splAnalysis:splAnalysisMap){
                            splAnalysis.getSampleAnalysis().getDataset();
                        }
                    }
                }
                dataset.setGroupSetup(groupSetup);
            }
            // sort qCh by BiologicalGroup/BiologicalSample
            if (!dataset.getMasterQuantitationChannels().isEmpty()){
                List<DQuantitationChannel> listQch = dataset.getMasterQuantitationChannels().get(0).getQuantitationChannels();
                if (dataset.getGroupSetup() != null && !dataset.getGroupSetup().getBiologicalGroups().isEmpty()){
                    List<DQuantitationChannel> sortedQch = new ArrayList();
                    List<BiologicalGroup> listBiolGroup = dataset.getGroupSetup().getBiologicalGroups();
                    for (BiologicalGroup biolGroup : listBiolGroup) {
                        List<BiologicalSample> listBiologicalSamples = biolGroup.getBiologicalSamples();
                        for (BiologicalSample sample : listBiologicalSamples) {
                            List<QuantitationChannel> listQchSample = sample.getQuantitationChannels(); // are sorted by number
                            for (QuantitationChannel qchS : listQchSample) {
                                for(DQuantitationChannel dqch :listQch ){
                                    if (dqch.getId() == qchS.getId()){
                                        dqch.setBiologicalGroupId(biolGroup.getId());
                                        //dqch.setBiologicalGroupName(biolGroup.getName());  //JPM.TODO
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
            // load ObjectTree corresponding to the QUANT_PROCESSING_CONFIG
            Map<String, Long> objectTreeIdByName = datasetDB.getObjectTreeIdByName();
            if (objectTreeIdByName != null && objectTreeIdByName.get("quantitation.label_free_config") != null){
                Long objectId = objectTreeIdByName.get("quantitation.label_free_config");
                String queryObject = "SELECT clobData FROM fr.proline.core.orm.uds.ObjectTree WHERE id=:objectId ";
                Query qObject = entityManagerUDS.createQuery(queryObject);
                qObject.setParameter("objectId", objectId);
                try{
                    String clobData = (String)qObject.getSingleResult();
                    fr.proline.core.orm.uds.ObjectTree objectTree = new fr.proline.core.orm.uds.ObjectTree();
                    objectTree.setId(objectId);
                    objectTree.setClobData(clobData);
                    dataset.setQuantProcessingConfig(objectTree);
                }catch(NoResultException | NonUniqueResultException e){
                            
                }
            }
            // load ObjectTree corresponding to the POST_QUANT_PROCESSING_CONFIG
            if (objectTreeIdByName != null && objectTreeIdByName.get("quantitation.post_quant_processing_config") != null){
                Long objectId = objectTreeIdByName.get("quantitation.post_quant_processing_config");
                String queryObject = "SELECT clobData FROM fr.proline.core.orm.uds.ObjectTree WHERE id=:objectId ";
                Query qObject = entityManagerUDS.createQuery(queryObject);
                qObject.setParameter("objectId", objectId);
                try{
                    String clobData = (String)qObject.getSingleResult();
                    fr.proline.core.orm.uds.ObjectTree objectTree = new fr.proline.core.orm.uds.ObjectTree();
                    objectTree.setId(objectId);
                    objectTree.setClobData(clobData);
                    dataset.setPostQuantProcessingConfig(objectTree);
                }catch(NoResultException | NonUniqueResultException e){
                            
                }
            }

            entityManagerMSI.getTransaction().commit();
            entityManagerUDS.getTransaction().commit();
            entityManagerLCMS.getTransaction().commit();
        } catch (Exception e) {
            //logger.error(getClass().getSimpleName() + " failed", e);
            taskError = new TaskError(e);
            try {
                entityManagerMSI.getTransaction().rollback();
                entityManagerUDS.getTransaction().rollback();
                entityManagerLCMS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(DatabaseLoadXicMasterQuantTask.class.getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerLCMS.close();
            entityManagerMSI.close();
            entityManagerUDS.close();
        }
        return true;
    }

    /**
     * Fetch first data to display proteinSet (all Master Protein Sets,
     * quantProteinSet
     *
     * @return
     */
    private boolean fetchDataProteinMainTask() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();
            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary
                    if (resultSummaryId != null) {
                        // retrieve the proteinSet list with isValidated = true
                        QuantitationMethod quantitationMethod = m_dataset.getQuantitationMethod();
                        boolean isSC = quantitationMethod != null && (quantitationMethod.getAbundanceUnit().compareTo("spectral_counts") == 0);
                        Query proteinSetsQuery = entityManagerMSI.createQuery("SELECT ps.id  FROM PeptideSet pepset JOIN pepset.proteinSet as ps "
                                                + " WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true ORDER BY pepset.score DESC");
                        if (isSC){
                           proteinSetsQuery = entityManagerMSI.createQuery("SELECT ps.id  FROM PeptideSet pepset JOIN pepset.proteinSet as ps "
                                                + " WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true AND ps.masterQuantComponentId is not null ORDER BY pepset.score DESC");
                        }
                        proteinSetsQuery.setParameter("rsmId", resultSummaryId);
                        List<Long> proteinSets = proteinSetsQuery.getResultList();
                        if (proteinSets != null && !proteinSets.isEmpty()) {
                            // for each proteinSet, retrieve the DMasterQuantProteinSet
                            int nbProteinSet = proteinSets.size();
                            m_proteinSetIds = new ArrayList<>(nbProteinSet);
                            m_resultSetIds = new ArrayList<>(nbProteinSet);
                            for (int i = 0; i < nbProteinSet; i++) {
                                Long id = Long.valueOf(proteinSets.get(i));
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
                            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEIN_SET, m_proteinSetIds.size(), SLICE_SIZE);

                            // execute the first slice now
                            fetchProteinSetData(subTask, entityManagerMSI);

                        } // end proteinSets != null
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
     * load all quant peptides for a xic
     *
     * @return
     */
    private boolean fetchDataPeptideMainTask(boolean xic, Long[] peptideInstanceIdArray) {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary
                    if (resultSummaryId != null) {
                        // load peptideInstance
                        String queryPep = "SELECT pi.id "
                                + "FROM fr.proline.core.orm.msi.PeptideInstance pi "
                                + "WHERE pi.resultSummary.id=:rsmId ";
                        
                        if (peptideInstanceIdArray != null) {
                            queryPep += "AND pi.id IN (:pepIds) ";
                        }
                        queryPep += "ORDER BY pi.id ASC";
                        Query peptidesQuery = entityManagerMSI.createQuery(queryPep);
                        peptidesQuery.setParameter("rsmId", resultSummaryId);
                        if (peptideInstanceIdArray != null) {
                            ArrayList<Long> idList = new ArrayList<>(Arrays.asList(peptideInstanceIdArray));
                            peptidesQuery.setParameter("pepIds", idList);
                        }
                        List<Long> listIds = (List<Long>) peptidesQuery.getResultList();
                        m_peptideInstanceIds = listIds;

                        List<DMasterQuantPeptide> listDMasterQuantPeptideFake = new ArrayList();
                        for (Long m_peptideInstanceId : m_peptideInstanceIds) {
                            DMasterQuantPeptide f = new DMasterQuantPeptide(-1, 0, -1, "", resultSummaryId);
                            f.setPeptideInstanceId(m_peptideInstanceId);
                            listDMasterQuantPeptideFake.add(f);
                        }
                        m_masterQuantPeptideList.addAll(listDMasterQuantPeptideFake);

                        // load MasterQuantPeptide without PeptideInstance
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
                        quantPeptideNonIdentQuery.setParameter("rsmId", resultSummaryId);
                        List<Long> listPepNonIdentIds = (List<Long>) quantPeptideNonIdentQuery.getResultList();
                        // in order to add these master quantPeptides to the peptideInstance to load, we add the -masterQuantPeptideId to load in the peptideInstanceId...
                        for (Long pepNonIdentId : listPepNonIdentIds) {
                            DMasterQuantPeptide f = new DMasterQuantPeptide(pepNonIdentId, 0, -1, "", resultSummaryId);
                            f.setPeptideInstanceId(-pepNonIdentId);
                            m_peptideInstanceIds.add(-pepNonIdentId);
                            m_masterQuantPeptideList.add(f);
                        }
                        // slice the task and get the first one
                        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE_INSTANCE, m_peptideInstanceIds.size(), SLICE_SIZE);

                        // execute the first slice now
                        fetchPeptideInstanceData(subTask, entityManagerMSI, xic);

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
     * load all quant peptides ions for a xic
     *
     * @return
     */
    private boolean fetchDataPeptideIonMainTask() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary
                    if (resultSummaryId != null) {
                        // load masterQuantPeptideIon
                        String queryPepIon = "SELECT mqpi.id "
                                + "FROM MasterQuantPeptideIon mqpi "
                                + "WHERE mqpi.resultSummary.id=:rsmId "
                                + "ORDER BY mqpi.id ASC";
                        Query peptideIonsQuery = entityManagerMSI.createQuery(queryPepIon);
                        peptideIonsQuery.setParameter("rsmId", resultSummaryId);
                        List<Long> listIds = (List<Long>) peptideIonsQuery.getResultList();

                        m_masterQuantPeptideIonIds = listIds;
                        
                        for (Long m_masterQuantPeptideIonId : m_masterQuantPeptideIonIds) {
                            DMasterQuantPeptideIon mqpi = new DMasterQuantPeptideIon();
                            mqpi.setId(m_masterQuantPeptideIonId);
                            m_masterQuantPeptideIonList.add(mqpi);
                        }

                        // slice the task and get the first one
                        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE_ION, m_masterQuantPeptideIonIds.size(), SLICE_SIZE);

                        // execute the first slice now
                        if (subTask != null){
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
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
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
                        List<Long> listIds = (List<Long>) peptidesQuery.getResultList();
                        m_peptideInstanceIds = listIds;


                        List<DMasterQuantPeptide> listDMasterQuantPeptideFake = new ArrayList();
                        for (Long m_peptideInstanceId : m_peptideInstanceIds) {
                            DMasterQuantPeptide f = new DMasterQuantPeptide(-1, 0, -1, "", resultSummaryId);
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
    private boolean fetchPeptideInstanceData(EntityManager entityManagerMSI, List<Long> listPeptideInstanceIds, boolean xic) {
        
        HashMap<Long, Peptide> peptideMap = new HashMap<>();
        // map qcId rsmId
        Map<Long, Long> rsmIdVsQcId = new HashMap<>();
        List<Long> rsmIdList = new ArrayList();
        List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();
        for (DMasterQuantitationChannel masterQuantChannel : listMasterQuantitationChannels) {
            List<DQuantitationChannel> listQc = masterQuantChannel.getQuantitationChannels();
            for (DQuantitationChannel qc : listQc) {
                rsmIdVsQcId.put(qc.getIdentResultSummaryId(), qc.getId());
                rsmIdList.add(qc.getIdentResultSummaryId());
            }
        }
        // load dPeptideInstance and PeptideMatch
        List<DPeptideInstance> peptideInstanceList = new ArrayList();
        String querySelect;
        if (xic) {
            querySelect = "SELECT  pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, p, mqpi.elutionTime "
                + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                + "fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi "
                + "WHERE pi.id IN (:listId) AND "
                + "pi.bestPeptideMatchId=pm.id AND "
                + "pm.peptideId=p.id  AND  pi.id = mqpi.peptideInstance.id "
                + "ORDER BY pm.score DESC";
        } else { // Spectral Count
            querySelect = "SELECT  pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, p "
                + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                + "fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p "
                + "WHERE pi.id IN (:listId) AND "
                + "pi.bestPeptideMatchId=pm.id AND "
                + "pm.peptideId=p.id "
                + "ORDER BY pm.score DESC";
        }
        
        Query query = entityManagerMSI.createQuery(querySelect);
        query.setParameter("listId", listPeptideInstanceIds);
        List resultList = query.getResultList();
        
        HashMap<Long, DPeptideMatch> peptideMatchMap = new HashMap<>();
        Iterator<Object[]> itPeptidesQuery = resultList.iterator();
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
            PeptideInstance pi = (PeptideInstance) resCur[0];
            Float elutionTime = (xic) ? (Float) resCur[12] : pi.getElutionTime();
            DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), elutionTime);
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
                pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank);
                pm.setRetentionTime(elutionTime);
                peptideMatchMap.put(pmId, pm);
            }
 

            Peptide p = (Peptide) resCur[11];
            p.getTransientData().setPeptideReadablePtmStringLoaded();
            peptideMap.put(p.getId(), p);
            
            dpi.setBestPeptideMatch(pm);

            pm.setPeptide(p);

            peptideInstanceList.add(dpi);
        }
        
        DatabaseLoadPeptidesInstancesTask.fetchReadablePtmData(entityManagerMSI, m_dataset.getResultSetId(), peptideMap);
        
        try {
            DatabaseLoadPeptidesInstancesTask.fetchPtmDataFromPSdb(peptideMap);
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            return false;
        }

        



        
        int nbMP = m_masterQuantPeptideList.size();
        int nbPI = peptideInstanceList.size();
        //  load MasterQuantPeptide and list of QuantPeptide
        String queryDMasterQuantPeptide = "SELECT q.id, q.selectionLevel, q.objectTreeId,  q.serializedProperties,  pi.resultSummary.id, pi.id "
                + "FROM MasterQuantComponent q, PeptideInstance pi "
                + " WHERE pi.id IN (:listPeptideInstanceId) AND  q.id = pi.masterQuantComponentId "
                + " ORDER BY q.id ASC ";
        Query mqPeptideQ = entityManagerMSI.createQuery(queryDMasterQuantPeptide);
        mqPeptideQ.setParameter("listPeptideInstanceId", listPeptideInstanceIds);
        List<Object[]> resultListMQPeptide = mqPeptideQ.getResultList();
        List<Long> listObjectTreeId = new ArrayList();
        List<Long> listPeptideId = new ArrayList();
        List<DMasterQuantPeptide> listMasterQP = new ArrayList();
        for (Object[] resCur : resultListMQPeptide) {
            int i = 0;
            long masterQuantPeptideId = (long) resCur[i++];
            int selectionLevel = (int) resCur[i++];
            long objectTreeId = (long) resCur[i++];
            String serializedProp = (String) resCur[i++];
            long quantRSId = (long) resCur[i++];
            long peptideInstanceId = (long) resCur[i++];
            DMasterQuantPeptide masterQuantPeptide = new DMasterQuantPeptide(masterQuantPeptideId, selectionLevel, objectTreeId, serializedProp, quantRSId);
            //search index of peptideInstance
            DPeptideInstance pi = null;
            for (int k = 0; k < nbPI; k++) {
                if (peptideInstanceList.get(k).getId() == peptideInstanceId) {
                    pi = peptideInstanceList.get(k);
                    break;
                }
            }
            masterQuantPeptide.setPeptideInstance(pi);
            masterQuantPeptide.setPeptideInstanceId(peptideInstanceId);
            listObjectTreeId.add(objectTreeId);
            listPeptideId.add(pi.getPeptideId());
            listMasterQP.add(masterQuantPeptide);
        }
        // load PSM count for each peptide instance in the different quantChannels
        Map<Long, Map<Long, Integer>> identPSMPerQCIdPerPepId = new HashMap();
        if (!listPeptideId.isEmpty()){
            String queryCountPSM = "SELECT pi.peptideMatchCount, pi.resultSummary.id, pi.peptide.id "
                    + "FROM PeptideInstance pi "
                    + "WHERE pi.resultSummary.id in (:listRsm) AND pi.peptide.id in (:listPepIds) ";
            Query queryPSM = entityManagerMSI.createQuery(queryCountPSM);
            queryPSM.setParameter("listRsm", rsmIdList);
            queryPSM.setParameter("listPepIds", listPeptideId);
            List resultListPSM = queryPSM.getResultList();
            for (Iterator iterator = resultListPSM.iterator(); iterator.hasNext();) {
                Object[] nb = (Object[]) (iterator.next());
                int psm = ((Integer) nb[0]).intValue();
                Long rsmId = ((Long) nb[1]);
                Long qcId = rsmIdVsQcId.get(rsmId);
                Long peptideId = ((Long) nb[2]);
                Map<Long, Integer> identPSMPerQCId = identPSMPerQCIdPerPepId.get(peptideId);
                if (identPSMPerQCId == null) {
                    identPSMPerQCId = new HashMap();
                }
                identPSMPerQCId.put(qcId, psm);
                identPSMPerQCIdPerPepId.put(peptideId, identPSMPerQCId);
            }
        }
        List<ObjectTree> listOt = new ArrayList();
        if (listObjectTreeId.size() > 0) {
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
                    Integer psm  = identPSMPerQCId.get(qcId);
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

        // no master quantPeptide: build a fake masterQuantPeptide to display the peptideInstance
        List<DPeptideInstance> extendedPeptideInstanceList = null;
        for (int i = 0; i < nbMP; i++) {
            DMasterQuantPeptide masterQuantPeptide = m_masterQuantPeptideList.get(i);
            if (masterQuantPeptide.getId() == -1) {
                if (extendedPeptideInstanceList == null) {
                    extendedPeptideInstanceList = new ArrayList();
                    
                    String querySelect2 = "SELECT  pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, p "
                            + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                            + "fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p  "
                            + "WHERE pi.id IN (:listId) AND  "
                            + "pi.bestPeptideMatchId=pm.id AND "
                            + "pm.peptideId=p.id  "
                            + "ORDER BY pm.score DESC";

                          Query query2 = entityManagerMSI.createQuery(querySelect2);
                    query2.setParameter("listId", listPeptideInstanceIds);
                    List resultList2 = query2.getResultList();

                    //HashMap<Long, DPeptideMatch> peptideMatchMap = new HashMap<>();
                    itPeptidesQuery = resultList2.iterator();
                    while (itPeptidesQuery.hasNext()) {
                        Object[] resCur = itPeptidesQuery.next();
                        PeptideInstance pi = (PeptideInstance) resCur[0];
                        DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), null); //JPM.TODO
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
                            pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank);
                            
                            if (!xic) {
                                pm.setRetentionTime(pi.getElutionTime());
                            }
                            peptideMatchMap.put(pmId, pm);
                        }

                        Peptide p = (Peptide) resCur[11];
                        p.getTransientData().setPeptideReadablePtmStringLoaded();
                        peptideMap.put(p.getId(), p);

                        dpi.setBestPeptideMatch(pm);

                        pm.setPeptide(p);

                        extendedPeptideInstanceList.add(dpi);
                    }
                }


                
                //search index of peptideInstance
                DPeptideInstance peptideInstance = null;
                if (masterQuantPeptide.getPeptideInstanceId() > 0 && listPeptideInstanceIds.indexOf(masterQuantPeptide.getPeptideInstanceId()) != -1) {
                    nbPI = extendedPeptideInstanceList.size();
                    for (int k = 0; k < nbPI; k++) {
                        if (extendedPeptideInstanceList.get(k).getId() == masterQuantPeptide.getPeptideInstanceId()) {
                            peptideInstance = extendedPeptideInstanceList.get(k);
                            break;
                        }
                    }
                    DMasterQuantPeptide o = new DMasterQuantPeptide(-1, 0, -1, null, masterQuantPeptide.getQuantResultSummaryId());
                    o.setPeptideInstance(peptideInstance);
                    o.setPeptideInstanceId(masterQuantPeptide.getPeptideInstanceId());
                    o.setPeptideInstance(peptideInstance);
                    // load PSM count for each peptide instance in the different quantChannels
                    String queryCountPSM = "SELECT pi.peptideMatchCount, pi.resultSummary.id FROM PeptideInstance pi "
                            + "WHERE pi.resultSummary.id in (:listRsm) AND pi.peptide.id =:pepId ";
                    Query queryPSM = entityManagerMSI.createQuery(queryCountPSM);
                    queryPSM.setParameter("listRsm", rsmIdList);
                    queryPSM.setParameter("pepId", peptideInstance.getPeptideId());
                    Map<Long, DQuantPeptide> quantProteinSetByQchIds = new HashMap();
                    List resultListPSM = queryPSM.getResultList();
                    for (Iterator iterator = resultListPSM.iterator(); iterator.hasNext();) {
                        Object[] nb = (Object[]) (iterator.next());
                        int psm = ((Integer) nb[0]).intValue();
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
            }
        }

        // load masterQuantPeptide not linked to a peptideInstance
        queryDMasterQuantPeptide = "SELECT  new fr.proline.core.orm.msi.dto.DMasterQuantPeptide"
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
                        DPeptideInstance fakePeptideInstance = new DPeptideInstance(-1, -1, 0, Float.NaN);
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

                } catch (NoResultException | NonUniqueResultException e) {

                }
            }
        }

        ArrayList<Long> peptideMatchIds = new ArrayList<>(peptideMatchMap.size());
        peptideMatchIds.addAll(peptideMatchMap.keySet());
        fetchProteinSetName(entityManagerMSI, peptideMatchIds, peptideMatchMap);
        
        return true;
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
                            String groupSetupNumber = entry.getKey();
                            List<MasterQuantProteinSetProfile> listMasterQuantProteinSetProfile = entry.getValue();
                            if (listMasterQuantProteinSetProfile != null) {
                                int nbP = listMasterQuantProteinSetProfile.size();
                                for (int i = 0; i < nbP; i++) {
                                    MasterQuantProteinSetProfile profile = listMasterQuantProteinSetProfile.get(i);
                                    if (profile.getMqPeptideIds() != null && profile.getMqPeptideIds().contains(masterQuantPeptideId)) {
                                        DCluster cluster = new DCluster(i + 1, profile.getAbundances(), profile.getRatios());
                                        return cluster;
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
        return fetchProteinSetData(entityManagerMSI, sliceOfProteinSetIds);
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

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
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
    private boolean fetchProteinSetData(EntityManager entityManagerMSI, List<Long> proteinSetIds) {
        int nbMQP = m_masterQuantProteinSetList.size();

        String queryDMasterQuantProteinSet = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantProteinSet"
                + "(q.id,  q.selectionLevel, q.objectTreeId,  q.serializedProperties,  p.resultSummary.id,  p.id) "
                + " FROM MasterQuantComponent q,  ProteinSet p "
                + " WHERE p.id IN (:listId) AND   q.id = p.masterQuantComponentId "
                + " ORDER BY q.id ASC ";
        TypedQuery<DMasterQuantProteinSet> masterQuantProteinSetsQuery = entityManagerMSI.createQuery(queryDMasterQuantProteinSet, DMasterQuantProteinSet.class);
        masterQuantProteinSetsQuery.setParameter("listId", proteinSetIds);
        //masterQuantProteinSetsQuery.setParameter("rsmId", resultSummaryId); // NO NEED, and even if this constaint is added, the query become slow
        List<DMasterQuantProteinSet> listResult = masterQuantProteinSetsQuery.getResultList();

        
        TypedQuery<DProteinSet> proteinSetQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinSet(ps.id, ps.representativeProteinMatchId, ps.resultSummary.id, ps.serializedProperties) FROM ProteinSet ps WHERE ps.id=:psId ", DProteinSet.class);
        String queryProteinMatch = "SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession,  pm.score, pm.peptideCount, pm.resultSet.id, pm.description, pm.serializedProperties) "
                + "FROM ProteinMatch pm "
                + "WHERE pm.id=:pmId";
        TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery(queryProteinMatch, DProteinMatch.class);

        String queryCountNbPep = "SELECT count(pi.id) , pepSet.peptideCount "
                + "FROM fr.proline.core.orm.msi.PeptideSet pepSet JOIN pepSet.peptideSetPeptideInstanceItems pspi JOIN pspi.peptideInstance pi "
                + "WHERE pepSet.resultSummaryId=:rsmId AND pepSet.proteinSet.id=:proteinSetId AND "
                + "pi.masterQuantComponentId IS NOT NULL "
                + "GROUP BY pepSet.peptideCount ";
        String queryCountNbPepCount = "SELECT  pepSet.peptideCount "
                + "FROM fr.proline.core.orm.msi.PeptideSet pepSet "
                + "WHERE pepSet.resultSummaryId=:rsmId AND pepSet.proteinSet.id=:proteinSetId  ";
        Query queryCountPep = entityManagerMSI.createQuery(queryCountNbPep);
        Query queryCountPepCount = entityManagerMSI.createQuery(queryCountNbPepCount);
        
        String queryPepNumber = "SELECT ps.peptideCount, pspmm.proteinMatch.id "
                    + "FROM PeptideSetProteinMatchMap pspmm, PeptideSet ps " +
                "WHERE  pspmm.resultSummary.id=:rsmId  AND ps.id = pspmm.peptideSet.id ";
        Query queryPepNumberQ = entityManagerMSI.createQuery(queryPepNumber);
        String queryStatus = "SELECT ps.id, pspmi.proteinMatch.id, pspmi.isInSubset, ps.representativeProteinMatchId, ps.isValidated "
                + "FROM ProteinSetProteinMatchItem pspmi, ProteinSet ps " +
                " WHERE ps.id = pspmi.proteinSet.id " +
                " AND pspmi.resultSummary.id=:rsmId ";
        Query queryStatusQ = entityManagerMSI.createQuery(queryStatus);
        List<DQuantitationChannel> listQC = new ArrayList();
        Map<Long, Map<Long, String>> protMatchStatusByIdByQcId = new HashMap();
        Map<Long, Map<Long, Integer>> protMatchPepNumberByIdByQcId = new HashMap();
        if (m_dataset != null && m_dataset.getMasterQuantitationChannels() != null && !m_dataset.getMasterQuantitationChannels().isEmpty()){
            listQC = m_dataset.getMasterQuantitationChannels().get(0).getQuantitationChannels();
            for (DQuantitationChannel qch : listQC) {
                Long identQCRsmId = qch.getIdentResultSummaryId();
                Map<Long, String> statusByProtMatchId = new HashMap();
                Map<Long, Integer> pepNumberByProtMatchId = new HashMap();
                queryStatusQ.setParameter("rsmId", identQCRsmId);
                queryPepNumberQ.setParameter("rsmId", identQCRsmId);
                List rStatus = queryStatusQ.getResultList();
                List rPepNumber = queryPepNumberQ.getResultList();
                for (Object resSt : rStatus) {
                    Object[] res = (Object[]) resSt;
                    Long proteinMatchId = (Long)res[1];
                    Boolean isInSubset = (Boolean)res[2];
                    Long typProteinMatchId = (Long)res[3];
                    Boolean isProtSetValidated = (Boolean)res[4];
                    String  protMatchStatus;
                    if (isInSubset) {
                        protMatchStatus = "Subset";
                    } else if (typProteinMatchId.equals(proteinMatchId)) { //is the typical 
                        protMatchStatus = "Typical";
                    } else{
                        protMatchStatus = "Sameset";
                    }
                    if(!isProtSetValidated){
                        protMatchStatus = "Invalid "+protMatchStatus;
                    }
                    statusByProtMatchId.put( proteinMatchId , protMatchStatus);
                }
                for (Object resPn : rPepNumber) {
                    Object[] res = (Object[]) resPn;
                    Long proteinMatchId = (Long)res[1];
                    Integer pepNumber = (Integer)res[0];
                    pepNumberByProtMatchId.put( proteinMatchId , pepNumber);
                }
                protMatchStatusByIdByQcId.put(qch.getId(), statusByProtMatchId);
                protMatchPepNumberByIdByQcId.put(qch.getId(), pepNumberByProtMatchId);
            }
        }
       
        for (DMasterQuantProteinSet masterQuantProteinSet : listResult) {
            proteinSetQuery.setParameter("psId", masterQuantProteinSet.getProteinSetId());
            DProteinSet dProteinSet = proteinSetQuery.getSingleResult();
            // typical protein match id
            proteinMatchQuery.setParameter("pmId", dProteinSet.getProteinMatchId());
            try {
                DProteinMatch typicalProteinMatch = proteinMatchQuery.getSingleResult();
                dProteinSet.setTypicalProteinMatch(typicalProteinMatch);
            } catch (NoResultException | NonUniqueResultException e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
            }
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
            // nb PeptideInstance and nbPeptide quantified
            // nb PeptideInstance and nbPeptide quantified

            queryCountPep.setParameter("rsmId", masterQuantProteinSet.getQuantResultSummaryId());
            queryCountPep.setParameter("proteinSetId", masterQuantProteinSet.getProteinSetId());
            int nbPep = 0;
            int nbPepQuant = 0;
            List resultList = queryCountPep.getResultList();
            if (!resultList.isEmpty()) {
                Object[] nb = (Object[]) resultList.get(0);
                nbPep = ((Integer) nb[1]).intValue();
                nbPepQuant = ((Long) nb[0]).intValue();
            }
            if (nbPepQuant == 0) {
                queryCountPepCount.setParameter("rsmId", masterQuantProteinSet.getQuantResultSummaryId());
                queryCountPepCount.setParameter("proteinSetId", masterQuantProteinSet.getProteinSetId());
                List rl = queryCountPepCount.getResultList();
                if (!rl.isEmpty()) {
                    nbPep = (Integer) rl.get(0);
                }
            }
            masterQuantProteinSet.setNbPeptides(nbPep);
            masterQuantProteinSet.setNbQuantifiedPeptides(nbPepQuant);
            
            // load status and peptideNumber by QcId
            
            Map<Long, String> quantStatusByQchIds = new HashMap();
            Map<Long, Integer> quantPeptideNumberByQchIds = new HashMap();
            for (DQuantitationChannel qch : listQC) {
                String status = "Undefined";
                Integer pepNumber = -1; //0;
                quantStatusByQchIds.put(qch.getId(), status);
                quantPeptideNumberByQchIds.put(qch.getId(), pepNumber);
                if (masterQuantProteinSet.getQuantProteinSetByQchIds().containsKey(qch.getId())){
                    DQuantProteinSet protSetQch = masterQuantProteinSet.getQuantProteinSetByQchIds().get(qch.getId());
                    Long pmId = protSetQch.getProteinMatchId();// proteinMatchId in this qch
                    if (protMatchStatusByIdByQcId.containsKey(qch.getId())){
                        Map<Long, String> protMatchStatusByIdPepMatch = protMatchStatusByIdByQcId.get(qch.getId());
                        if (protMatchStatusByIdPepMatch.containsKey(pmId)){
                            status = protMatchStatusByIdPepMatch.get(pmId);
                        }
                    }
                    if (protMatchPepNumberByIdByQcId.containsKey(qch.getId())){
                        Map<Long, Integer> protMatchPepNumberByIdPepMatch = protMatchPepNumberByIdByQcId.get(qch.getId());
                        if (protMatchPepNumberByIdPepMatch.containsKey(pmId)){
                            pepNumber = protMatchPepNumberByIdPepMatch.get(pmId);
                        }
                    }
                }
                quantStatusByQchIds.put(qch.getId(), status);
                quantPeptideNumberByQchIds.put(qch.getId(), pepNumber);
            }
            masterQuantProteinSet.setQuantStatusByQchIds(quantStatusByQchIds);
            masterQuantProteinSet.setQuantPeptideNumberByQchIds(quantPeptideNumberByQchIds);
            
            // update in the list
            int index = -1;
            for (int k = 0; k < nbMQP; k++) {
                if (m_masterQuantProteinSetList.get(k).getProteinSetId() == dProteinSet.getId()) {
                    index = k;
                    break;
                }
            }
            m_masterQuantProteinSetList.set(index, masterQuantProteinSet);
        }
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

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            switch (slice.getSubTaskId()) {
                case SUB_TASK_PEPTIDE_INSTANCE:
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
    private boolean fetchPeptideInstanceData(SubTask subTask, EntityManager entityManagerMSI, boolean xic) {
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

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
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
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
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
                        List<Long> listIds = (List<Long>) peptidesIonQuery.getResultList();
                        m_masterQuantPeptideIonIds = listIds;

                        for (Long m_masterQuantPeptideIonId : m_masterQuantPeptideIonIds) {
                            DMasterQuantPeptideIon mqpi = new DMasterQuantPeptideIon();
                            mqpi.setId(m_masterQuantPeptideIonId);
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
        String queryMasterPeptideIons = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon(pi, mqpi, p) "
                + "FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi, "
                + "fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideMatch pm  "
                + "WHERE mqpi.id IN (:listId) AND pi.id = mqpi.peptideInstance.id AND  "
                + "pi.peptide.id=p.id AND pm.id= pi.bestPeptideMatchId  "
                + "ORDER BY pm.score DESC";

        TypedQuery<DMasterQuantPeptideIon> queryMasterIons = entityManagerMSI.createQuery(queryMasterPeptideIons, DMasterQuantPeptideIon.class);
        queryMasterIons.setParameter("listId", listMasterPeptideIonsIds);
        List<DMasterQuantPeptideIon> resultListIons = new ArrayList();
        if (listMasterPeptideIonsIds != null && !listMasterPeptideIonsIds.isEmpty()) {
            resultListIons = queryMasterIons.getResultList();
        }
        List<Integer> indexes = new ArrayList();
        List<Long> listObjectTreeId = new ArrayList();

        ArrayList<Long> peptideInstanceIds = new ArrayList<Long>(resultListIons.size());
        HashMap<Long, DPeptideInstance> peptideInstanceMap = new HashMap<>();
        for (DMasterQuantPeptideIon mQuantPeptideIon : resultListIons) {
            int i = 0;
            DPeptideInstance pi = mQuantPeptideIon.getPeptideInstance();
            peptideInstanceIds.add(pi.getId());
            Long keyPi = Long.valueOf( pi.getId());
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
            m_masterQuantPeptideIonList.set(index, mQuantPeptideIon);
            indexes.add(index);
        } // end for

        
        
        /////////////////////////////////
        HashMap<Long, Peptide> peptideMap = new HashMap<>();

        // load dPeptideInstance and PeptideMatch
        List<DPeptideInstance> peptideInstanceList = new ArrayList();
        String querySelect = "SELECT  pi.id, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, p "
                + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                + "fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p  "
                + "WHERE pi.id IN (:listId) AND  "
                + "pi.bestPeptideMatchId=pm.id AND "
                + "pm.peptideId=p.id  "
                + "ORDER BY pm.score DESC";

        
        HashMap<Long, DPeptideMatch> peptideMatchMap = new HashMap<>();
        
        Query query = entityManagerMSI.createQuery(querySelect);
        query.setParameter("listId", peptideInstanceIds);
        List resultList = query.getResultList();
        Iterator<Object[]> itPeptidesQuery = resultList.iterator();
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
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
                pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank);
                peptideMatchMap.put(pmId, pm);
            }
            
            Peptide p = (Peptide) resCur[11];
            p.getTransientData().setPeptideReadablePtmStringLoaded();
            peptideMap.put(p.getId(), p);
            
            dpi.setBestPeptideMatch(pm);

            pm.setPeptide(p);

            peptideInstanceList.add(dpi);
        }
        
        DatabaseLoadPeptidesInstancesTask.fetchReadablePtmData(entityManagerMSI, m_dataset.getResultSetId(), peptideMap);
        
        ArrayList<Long> peptideMatchIds = new ArrayList<>(peptideMatchMap.size());
        peptideMatchIds.addAll(peptideMatchMap.keySet());
        fetchProteinSetName(entityManagerMSI, peptideMatchIds, peptideMatchMap);
        
        //////////////////////////////////

        
        //object tree
        List<ObjectTree> listOt = new ArrayList();
        if (listObjectTreeId.size() > 0) {
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

        // peptideIons without peptide
        indexes = new ArrayList();
        listObjectTreeId = new ArrayList();
        String queryPeptideIonWithoutPeptide = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon(mqpi) "
                + "FROM  fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi "
                + "WHERE mqpi.id IN (:listId) AND mqpi.peptideInstance is NULL  "
                + "ORDER BY mqpi.id ASC";
        TypedQuery<DMasterQuantPeptideIon> queryMasterIons2 = entityManagerMSI.createQuery(queryPeptideIonWithoutPeptide, DMasterQuantPeptideIon.class);
        queryMasterIons2.setParameter("listId", listMasterPeptideIonsIds);
        List<DMasterQuantPeptideIon> resultListIons2 = new ArrayList();
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

        //object tree
        listOt = new ArrayList();
        if (listObjectTreeId.size() > 0) {
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
        return true;
    }

    /**
     * load protein match for a masterQuantPeptide for each quant channel
     *
     * @return
     */
    private boolean fetchDataPSMForPeptideMainTask() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            m_peptideMatchIds = new ArrayList();
            m_peptideMatchMap = new HashMap();
            m_peptideMatchSequenceMatchArrayMap = new HashMap();
                        
            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    List<DQuantitationChannel> listQuantChannels = masterQuantitationChannel.getQuantitationChannels();
                    for (DQuantitationChannel quantChannel : listQuantChannels) {
                        Long qcId = quantChannel.getId();
                        Long identRsmId = quantChannel.getIdentResultSummaryId();
                        Long peptideId = m_masterQuantPeptideForPSM.getPeptideInstance().getPeptideId();
                        Long peptideInstanceIdRSM = null;
                        String qPepInst = "SELECT pi.id "
                                + "FROM fr.proline.core.orm.msi.PeptideInstance pi "
                                + "WHERE pi.resultSummary.id =:rsmId AND "
                                + "pi.peptide.id =:peptideId ";
                        Query queryPepInst = entityManagerMSI.createQuery(qPepInst);
                        queryPepInst.setParameter("rsmId", identRsmId);
                        queryPepInst.setParameter("peptideId", peptideId);
                        List resultList = queryPepInst.getResultList();
                        for (Iterator iterator = resultList.iterator(); iterator.hasNext();) {
                            Object o = iterator.next();
                            peptideInstanceIdRSM = (Long) o;
                        }

                        String query = "SELECT  new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank) "
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
                        List<Long> listPeptideMatchIds = new ArrayList();
                        int nb = rList.size();
                        for (DPeptideMatch psm : rList) {
                            Long pmId = psm.getId();
                            listPeptideMatchIds.add(psm.getId());
                            m_peptideMatchIds.add(pmId);
                            m_peptideMatchMap.put(pmId, psm);

                            ArrayList<DPeptideMatch> sequenceMatchArray = m_peptideMatchSequenceMatchArrayMap.get(pmId);
                            if (sequenceMatchArray == null) {
                                sequenceMatchArray = new ArrayList<>();
                                m_peptideMatchSequenceMatchArrayMap.put(pmId, sequenceMatchArray);
                            }
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

        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        HashMap<Long, Peptide> peptideMap = new HashMap<>();

        Query peptideQuery = entityManagerMSI.createQuery("SELECT pm.id, p FROM PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pm.id IN (:listId) AND pm.peptideId=p.id");
        peptideQuery.setParameter("listId", sliceOfPeptideMatchIds);

        List<Object[]> peptides = peptideQuery.getResultList();
        Iterator<Object[]> it = peptides.iterator();
        while (it.hasNext()) {
            Object[] res = it.next();
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

        List<Object[]> ptmStrings = ptmStingQuery.getResultList();
        Iterator<Object[]> it2 = ptmStrings.iterator();
        while (it2.hasNext()) {
            Object[] res = it2.next();
            Long peptideId = (Long) res[0];
            PeptideReadablePtmString ptmString = (PeptideReadablePtmString) res[1];
            Peptide peptide = peptideMap.get(peptideId);
            peptide.getTransientData().setPeptideReadablePtmString(ptmString);
        }

    }

    private void fetchMsQuery(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        String query = "SELECT pm.id, msq.id, msq.initialId, s.precursorIntensity, s.firstTime "
                + "FROM PeptideMatch pm,MsQuery msq, Spectrum s "
                + "WHERE pm.id IN (:listId) AND "
                + "pm.msQuery=msq AND "
                + "msq.spectrum=s";
        Query msQueryQuery = entityManagerMSI.createQuery(query);
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);

        List<Object[]> msQueries = msQueryQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] o = it.next();
            int i=0;
            long pmId = (Long)o[i++];
            long msqId = (Long)o[i++];
            int msqInitialId = (Integer)o[i++];
            Float precursorIntensity = (Float)o[i++];
            Float retentionTime = (Float)o[i++];
            DMsQuery q = new DMsQuery(pmId, msqId, msqInitialId, precursorIntensity);

            DPeptideMatch peptideMatch = m_peptideMatchMap.get(q.getPeptideMatchId());
            peptideMatch.setMsQuery(q);
            peptideMatch.setRetentionTime(retentionTime);

        }
    }

    private void fetchProteinSetName(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);
        fetchProteinSetName(entityManagerMSI, sliceOfPeptideMatchIds, m_peptideMatchMap);
    }
    public static void fetchProteinSetName(EntityManager entityManagerMSI,  List sliceOfPeptideMatchIds, HashMap<Long, DPeptideMatch> peptideMatchMap) {

        

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

        ArrayList<String> proteinSetNameArray = new ArrayList();
        long prevPeptideMatchId = -1;

        List<Object[]> msQueries = proteinSetQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            String proteinName = (String) resCur[0];
            Long peptideMatchId = (Long) resCur[1];

            if (peptideMatchId != prevPeptideMatchId) {
                if (prevPeptideMatchId != -1) {
                    DPeptideMatch prevPeptideMatch = peptideMatchMap.get(prevPeptideMatchId);
                    String[] proteinSetNames = proteinSetNameArray.toArray(new String[proteinSetNameArray.size()]);
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
            String[] proteinSetNames = proteinSetNameArray.toArray(new String[proteinSetNameArray.size()]);
            prevPeptideMatch.setProteinSetStringArray(proteinSetNames);
        }

        Iterator itIds = sliceOfPeptideMatchIds.iterator();
        while (itIds.hasNext()) {
            Long peptideMatchId = (Long) itIds.next();
            DPeptideMatch peptideMatch = peptideMatchMap.get(peptideMatchId);
            if (peptideMatch.getProteinSetStringArray() == null) {
                String[] proteinSetNames = new String[0];
                peptideMatch.setProteinSetStringArray(proteinSetNames);
            }
        }
    }
    
    private String getMasterQuantProteinSetName(DMasterQuantProteinSet masterQuantProteinSet) {

        String name = null;
        
        if( masterQuantProteinSet.getProteinSet() != null) {
            DProteinMatch proteinMatch = masterQuantProteinSet.getProteinSet().getTypicalProteinMatch();
            if (proteinMatch != null) {
                name = proteinMatch.getAccession();
            } 
        }
        
        if(name == null)
            name = String.valueOf(masterQuantProteinSet.getId());
        return name;
    }
}
