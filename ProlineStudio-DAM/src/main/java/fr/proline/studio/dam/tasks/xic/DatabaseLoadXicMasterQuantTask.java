package fr.proline.studio.dam.tasks.xic;

import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.msi.dto.DQuantProteinSet;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.MasterQuantitationChannel;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseSlicerTask;
import fr.proline.studio.dam.tasks.DatabaseProteinsFromProteinSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private static final int SLICE_SIZE = 500;

    // different possible subtasks
    public static final int SUB_TASK_COUNT_QC = 0; // <<----- get in sync  
    public static final int SUB_TASK_COUNT_PROTEIN = 1; // <<----- get in sync  
    public static final int SUB_TASK_PROTEIN_SET = 0;
    public static final int SUB_TASK_COUNT_PEPTIDE = 1; // <<----- get in sync  
    public static final int SUB_TASK_PEPTIDE_INSTANCE = 0;

    // data kept for sub tasks
    private List<Long> m_proteinSetIds = null;
    private HashMap<Long, DProteinSet> m_proteinSetMap = null;
    
    private List<Long> m_peptideInstanceIds = null;

    private Long m_projectId;
    private DDataset m_dataset;
    private List<DMasterQuantProteinSet> m_masterQuantProteinSetList;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;
    private DProteinSet m_dProteinSet;

    private int action;
    private static final int LOAD_PROTEIN_SET_FOR_XIC = 0;
    private static final int LOAD_PEPTIDE_FOR_XIC = 1;
    private static final int LOAD_QUANT_CHANNELS_FOR_XIC = 2;
    private static final int LOAD_PEPTIDE_FOR_PROTEIN_SET = 3;

    public DatabaseLoadXicMasterQuantTask(AbstractDatabaseCallback callback) {
        super(callback);

    }

    public void initLoadProteinSets(long projectId, DDataset dataset, List<DMasterQuantProteinSet> masterQuantProteinSetList) {
        init(SUB_TASK_COUNT_PROTEIN, new TaskInfo("Load Protein Sets of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantProteinSetList = masterQuantProteinSetList;
        action = LOAD_PROTEIN_SET_FOR_XIC;
    }

    public void initLoadQuantChannels(long projectId, DDataset dataset) {
        init(SUB_TASK_COUNT_QC, new TaskInfo("Load Quant Channels of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        action = LOAD_QUANT_CHANNELS_FOR_XIC;
    }

    public void initLoadPeptides(long projectId, DDataset dataset, List<DMasterQuantPeptide> masterQuantPeptideList) {
        init(SUB_TASK_COUNT_PEPTIDE, new TaskInfo("Load Peptides of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideList = masterQuantPeptideList;
        action = LOAD_PEPTIDE_FOR_XIC;
    }
    
    public void initLoadPeptides(long projectId, DDataset dataset, DProteinSet proteinSet ,  List<DMasterQuantPeptide> masterQuantPeptideList ) {
        init(SUB_TASK_COUNT_PEPTIDE, new TaskInfo("Load Peptides of proteinSet " + DatabaseProteinsFromProteinSetTask.getProteinSetName(proteinSet), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideList = masterQuantPeptideList;
        m_dProteinSet = proteinSet ;
        action = LOAD_PEPTIDE_FOR_PROTEIN_SET;
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
                m_masterQuantPeptideList = null;
                break;
        }
    }

    @Override
    public boolean fetchData() {
        if (action == LOAD_QUANT_CHANNELS_FOR_XIC) {
            if (needToFetch()) {
                return fetchDataQuantChannels();
            }
        } else if (action == LOAD_PROTEIN_SET_FOR_XIC) {
            if (needToFetch()) {
                // first data are fetched
                fetchDataQuantChannels();
                return fetchDataProteinMainTask();
            } else {
                // fetch data of SubTasks
                return fetchProteinSetDataSubTask();
            }

        } else if (action == LOAD_PEPTIDE_FOR_XIC) {
            if (needToFetch()) {
                // first data are fetched
                fetchDataQuantChannels();
                return fetchDataPeptideMainTask();
            } else {
                // fetch data of SubTasks
                return fetchPeptideInstanceDataSubTask();
            }

        }else if (action == LOAD_PEPTIDE_FOR_PROTEIN_SET) {
            if (needToFetch()) {
                // first data are fetched
                fetchDataQuantChannels();
                return fetchDataPeptideForProteinSetMainTask();
            }

        }
        return true; // should not happen
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
                return (m_masterQuantPeptideList == null || m_masterQuantPeptideList.isEmpty());
        }
        return false; // should not happen 
    }

    /**
     * load list quantitation channels
     *
     * @return
     */
    private boolean fetchDataQuantChannels() {
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            entityManagerMSI.getTransaction().begin();
            // load DDataset
            // force initialization of lazy data (data will be needed for the display of properties)
            Dataset datasetDB = entityManagerUDS.find(Dataset.class, m_dataset.getId());
            QuantitationMethod quantMethodDB = datasetDB.getMethod();
            List<MasterQuantitationChannel> listMasterQuantitationChannels = datasetDB.getMasterQuantitationChannels();

            // fill the current object with the db object
            m_dataset.setQuantitationMethod(quantMethodDB);
            m_dataset.setDescription(datasetDB.getDescription());
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
                        // search resultFileName
                        String resultFileName = "";
                        String queryMsi = "SELECT msi.resultFileName FROM MsiSearch msi, ResultSet rs, ResultSummary rsm "
                                + " WHERE rsm.id=:rsmId AND rsm.resultSet.id = rs.id AND rs.msiSearch.id = msi.id ";
                        Query qMsi = entityManagerMSI.createQuery(queryMsi);
                        qMsi.setParameter("rsmId", qc.getIdentResultSummaryId());
                        try {
                            resultFileName = (String) qMsi.getSingleResult();
                            if (resultFileName != null && resultFileName.contains(".")) {
                                resultFileName = resultFileName.substring(0, resultFileName.indexOf('.'));
                            }
                        } catch (NoResultException | NonUniqueResultException e) {

                        }
                        dqc.setResultFileName(resultFileName);
                        listDQuantChannels.add(dqc);
                    }
                    DMasterQuantitationChannel dMaster = new DMasterQuantitationChannel(masterQuantitationChannel.getId(), masterQuantitationChannel.getName(),
                            resultSummaryId, listDQuantChannels,
                            masterQuantitationChannel.getDataset(), masterQuantitationChannel.getSerializedProperties());

                    // add into the list
                    masterQuantitationChannels.add(dMaster);
                } // end of the for

                // Set the ResultSummary of the Dataset as the ResultSummary of the first MasterQuantitationChannel
                MasterQuantitationChannel masterQuantitationChannel = listMasterQuantitationChannels.get(0);
                Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();
                ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, resultSummaryId);
                m_dataset.setResultSummaryId(resultSummaryId);
                m_dataset.setResultSummary(rsm);

            }
            m_dataset.setMasterQuantitationChannels(masterQuantitationChannels);

            entityManagerMSI.getTransaction().commit();
            entityManagerUDS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
            entityManagerUDS.close();
        }
        return true;
    }

    /**
     * Fetch first data.to display proteinSet (all Master Protein Sets, quantProteinSet
     *
     * @return
     */
    private boolean fetchDataProteinMainTask() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            List<DMasterQuantitationChannel> listMasterQuantitationChannels = m_dataset.getMasterQuantitationChannels();

            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                for (DMasterQuantitationChannel masterQuantitationChannel : listMasterQuantitationChannels) {
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId();

                    //resultSummary
                    if (resultSummaryId != null) {
                        // retrieve the proteinSet list with isValidated = true
                        TypedQuery<DProteinSet> proteinSetsQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinSet(ps.id, ps.typicalProteinMatchId ,ps.resultSummary.id) FROM PeptideSet pepset JOIN pepset.proteinSet as ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true ORDER BY pepset.score DESC", DProteinSet.class);
                        proteinSetsQuery.setParameter("rsmId", resultSummaryId);
                        List<DProteinSet> proteinSets = proteinSetsQuery.getResultList();
                        if (proteinSets != null && !proteinSets.isEmpty()) {
                            // for each proteinSet, retrieve the DMasterQuantProteinSet
                            int nbProteinSet = proteinSets.size();
                            m_proteinSetIds = new ArrayList<>(nbProteinSet);
                            m_proteinSetMap = new HashMap<>(nbProteinSet);
                            for (int i = 0; i < nbProteinSet; i++) {
                                DProteinSet ps = proteinSets.get(i);
                                Long id = Long.valueOf(ps.getId());
                                m_proteinSetIds.add(id);
                                m_proteinSetMap.put(id, ps);
                            }
                            
                            fetchProteinSetData(entityManagerMSI, m_proteinSetIds);
                            /*
                            // slice the task and get the first one
                            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEIN_SET, m_proteinSetIds.size(), SLICE_SIZE);

                            // execute the first slice now
                            fetchProteinSetData(subTask, entityManagerMSI);
                            */
                        } // end proteinSets != null
                    }// end resultSummaryId null

                } // end of the for

            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
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
    private boolean fetchDataPeptideMainTask() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
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
                                + "WHERE pi.resultSummary.id=:rsmId "
                                + "ORDER BY pi.id ASC";
                        Query peptidesQuery = entityManagerMSI.createQuery(queryPep);
                        peptidesQuery.setParameter("rsmId", resultSummaryId);
                        List<Long> listIds = (List<Long>) peptidesQuery.getResultList();
                        m_peptideInstanceIds = listIds ;
                        
                        fetchPeptideInstanceData(entityManagerMSI, m_peptideInstanceIds);
                        /*
                        // slice the task and get the first one
                        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE_INSTANCE, m_peptideInstanceIds.size(), SLICE_SIZE);

                        // execute the first slice now
                        fetchPeptideInstanceData(subTask, entityManagerMSI);
                        */
                    }// end resultSummaryId null

                } // end of the for

            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
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
    private boolean fetchDataPeptideForProteinSetMainTask() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
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
                                + "FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem pspi, "
                                + "fr.proline.core.orm.msi.PeptideSet pepSet "
                                + "WHERE pi.resultSummary.id=:rsmId AND pi.id = pspi.peptideInstance.id AND "
                                + "pspi.peptideSet.id = pepSet.id AND pepSet.proteinSet.id=:proteinSetId " 
                                + "ORDER BY pi.id ASC";
                        Query peptidesQuery = entityManagerMSI.createQuery(queryPep);
                        peptidesQuery.setParameter("rsmId", resultSummaryId);
                        peptidesQuery.setParameter("proteinSetId", m_dProteinSet.getId());
                        List<Long> listIds = (List<Long>) peptidesQuery.getResultList();
                        m_peptideInstanceIds = listIds ;
                        
                        fetchPeptideInstanceData(entityManagerMSI, m_peptideInstanceIds);
                        
                    }// end resultSummaryId null

                } // end of the for

            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
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
    private boolean fetchPeptideInstanceData(EntityManager entityManagerMSI, List<Long> listPeptideInstanceIds) {
        // load dPeptideInstance and PeptideMatch
        List<DPeptideInstance> peptideInstanceList = new ArrayList();
        String querySelect = "SELECT  pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, p "
                + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                + "fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p  "
                + "WHERE pi.id IN (:listId) AND  "
                + "pi.bestPeptideMatchId=pm.id AND "
                + "pm.peptideId=p.id AND pi.peptideId = p.id  "
                + "ORDER BY pm.score DESC";

        Query query = entityManagerMSI.createQuery(querySelect);
        query.setParameter("listId", listPeptideInstanceIds);
        List resultList = query.getResultList();
        Iterator<Object[]> itPeptidesQuery = resultList.iterator();
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
            PeptideInstance pi = (PeptideInstance) resCur[0];
            DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptideId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
            dpi.setResultSummary(pi.getResultSummary());

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
            DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank);

            Peptide p = (Peptide) resCur[11];

            dpi.setBestPeptideMatch(pm);

            pm.setPeptide(p);

            peptideInstanceList.add(dpi);
        }

        // for each PeptideInstance, load MasterQuantPeptide and list of QuantPeptide
        List<DMasterQuantPeptide> listDMasterQuantPeptide = new ArrayList();
        for (DPeptideInstance peptideInstance : peptideInstanceList) {
            String queryDMasterQuantPeptide = "SELECT  new fr.proline.core.orm.msi.dto.DMasterQuantPeptide "
                    + "(q.id, q.selectionLevel, q.objectTreeId,  q.serializedProperties,  pi.resultSummary.id) "
                    + "FROM MasterQuantComponent q, PeptideInstance pi "
                    + " WHERE pi.id=:peptideInstanceId AND  pi.resultSummary.id=:rsmId AND pi.resultSummary.id = q.resultSummary.id AND  q.id = pi.masterQuantComponentId "
                    + " ORDER BY q.id ASC ";
            TypedQuery<DMasterQuantPeptide> masterQuantPeptideQuery = entityManagerMSI.createQuery(queryDMasterQuantPeptide, DMasterQuantPeptide.class);
            masterQuantPeptideQuery.setParameter("peptideInstanceId", peptideInstance.getId());
            masterQuantPeptideQuery.setParameter("rsmId", peptideInstance.getResultSummary().getId());
            DMasterQuantPeptide masterQuantPeptide = null;
            try {
                masterQuantPeptide = masterQuantPeptideQuery.getSingleResult();
                if (masterQuantPeptide != null) {
                    masterQuantPeptide.setPeptideInstance(peptideInstance);
                    // list of quantPeptide
                    String quantPeptideData = ""; //ObjectTree.clobData
                    ObjectTree ot = entityManagerMSI.find(ObjectTree.class, masterQuantPeptide.getObjectTreeId()); // get the objectTree from id.
                    if (ot != null) {
                        quantPeptideData = ot.getClobData();
                    }

                    Map<Long, DQuantPeptide> quantProteinSetByQchIds = masterQuantPeptide.parseQuantPeptideFromProperties(quantPeptideData);
                    masterQuantPeptide.setQuantPeptideByQchIds(quantProteinSetByQchIds);

                    // list of MasterQuantPeptideIons
                    String queryMasterPeptideIons = "";
                }

                listDMasterQuantPeptide.add(masterQuantPeptide);

            } catch (NoResultException | NonUniqueResultException e) {
                //m_logger.error(getClass().getSimpleName()+" failed", e);
            }
        }
        m_masterQuantPeptideList.addAll(listDMasterQuantPeptide);
        return true;
    }

    /***
     * load ProteinSet data for a given subTask
     * @param subTask
     * @param entityManagerMSI
     * @return 
     */
    private boolean fetchProteinSetData(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfProteinSetIds = subTask.getSubList(m_proteinSetIds);
        return fetchProteinSetData(entityManagerMSI, sliceOfProteinSetIds) ;
    }
    
    /**
     * subTask to load proteinSet
     * @return 
     */
    private boolean fetchProteinSetDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
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
            entityManagerMSI.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }

    /**
     * load proteinSet data for a given proteinSet Ids list
     * @param entityManagerMSI
     * @param proteinSetIds
     * @return 
     */
    private boolean fetchProteinSetData(EntityManager entityManagerMSI, List<Long> proteinSetIds) {
        // resultSummaryId is the same for all proteinSet
        Long resultSummaryId = m_proteinSetMap.entrySet().iterator().next().getValue().getResultSummaryId();

        String queryDMasterQuantProteinSet = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantProteinSet"
                + "(q.id,  q.selectionLevel, q.objectTreeId,  q.serializedProperties,  r.id,  p.id) "
                + " FROM MasterQuantComponent q, ResultSummary r, ProteinSet p "
                + " WHERE p.id IN (:listId) AND p.resultSummary.id = r.id AND r.id = q.resultSummary.id AND r.id=:rsmId AND p.masterQuantComponentId = q.id "
                + " ORDER BY q.id ASC ";
        TypedQuery<DMasterQuantProteinSet> masterQuantProteinSetsQuery = entityManagerMSI.createQuery(queryDMasterQuantProteinSet, DMasterQuantProteinSet.class);
        masterQuantProteinSetsQuery.setParameter("listId", proteinSetIds);
        masterQuantProteinSetsQuery.setParameter("rsmId", resultSummaryId);
        List<DMasterQuantProteinSet> listResult = masterQuantProteinSetsQuery.getResultList();

        for (DMasterQuantProteinSet masterQuantProteinSet : listResult) {
            DProteinSet dProteinSet = m_proteinSetMap.get(masterQuantProteinSet.getProteinSetId());
            // typical protein match id
            String queryProteinMatch = "SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession,  pm.score, pm.peptideCount, pm.resultSet.id, pm.description) "
                    + "FROM ProteinMatch pm "
                    + "WHERE pm.id=:pmId";
            TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery(queryProteinMatch, DProteinMatch.class);
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
            if (quantProtSetdata != null) {
                quantProteinSetByQchIds= masterQuantProteinSet.parseQuantProteinSetFromProperties(quantProtSetdata);
            }
            masterQuantProteinSet.setQuantProteinSetByQchIds(quantProteinSetByQchIds);

            masterQuantProteinSet.setProteinSet(dProteinSet);
            // add to the list
            m_masterQuantProteinSetList.add(masterQuantProteinSet);
        }
        return true;
    }
    
    /**
     * subTask to load peptide
     * @return 
     */
    private boolean fetchPeptideInstanceDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            switch (slice.getSubTaskId()) {
                case SUB_TASK_PEPTIDE_INSTANCE:
                    fetchPeptideInstanceData(slice, entityManagerMSI);
                    break;
            }
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }

    /***
     * load Peptide data for a given subTask
     * @param subTask
     * @param entityManagerMSI
     * @return 
     */
    private boolean fetchPeptideInstanceData(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfPeptideIds = subTask.getSubList(m_peptideInstanceIds);
        return fetchPeptideInstanceData(entityManagerMSI, sliceOfPeptideIds) ;
    }
}
