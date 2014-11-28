package fr.proline.studio.dam.tasks.xic;

import fr.proline.core.orm.msi.MasterQuantPeptideIon;
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
import fr.proline.core.orm.msi.dto.DQuantPeptideIon;
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
    private static final int SLICE_SIZE = 300;

    // different possible subtasks
    public static final int SUB_TASK_COUNT_QC = 0; // <<----- get in sync  
    
    public static final int SUB_TASK_COUNT_PROTEIN = 1; // <<----- get in sync  
    public static final int SUB_TASK_PROTEIN_SET = 0;
    
    public static final int SUB_TASK_COUNT_PEPTIDE = 1; // <<----- get in sync  
    public static final int SUB_TASK_PEPTIDE_INSTANCE = 0;
    
    public static final int SUB_TASK_COUNT_PEPTIDE_ION = 1;
    public static final int SUB_TASK_PEPTIDE_ION = 0;

    // data kept for sub tasks
    private List<Long> m_proteinSetIds = null;
    private List<Long> m_resultSetIds = null;
    
    private List<Long> m_peptideInstanceIds = null;
    
    private List<Long> m_masterQuantPeptideIonIds = null;

    private Long m_projectId;
    private DDataset m_dataset;
    private List<DMasterQuantProteinSet> m_masterQuantProteinSetList;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;
    private DProteinSet m_dProteinSet;
    private DMasterQuantPeptide m_masterQuantPeptide;
    private List<MasterQuantPeptideIon> m_masterQuantPeptideIonList;

    private int action;
    private static final int LOAD_PROTEIN_SET_FOR_XIC = 0;
    private static final int LOAD_PEPTIDE_FOR_XIC = 1;
    private static final int LOAD_QUANT_CHANNELS_FOR_XIC = 2;
    private static final int LOAD_PEPTIDE_FOR_PROTEIN_SET = 3;
    private static final int LOAD_PEPTIDE_ION_FOR_XIC = 4;
    private static final int LOAD_PEPTIDE_ION_FOR_PEPTIDE = 5;
    

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
        String proteinSetName = "";
        if (proteinSet != null) {
            proteinSetName = DatabaseProteinsFromProteinSetTask.getProteinSetName(proteinSet);
        }
        init(SUB_TASK_COUNT_PEPTIDE, new TaskInfo("Load Peptides of proteinSet " + proteinSetName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideList = masterQuantPeptideList;
        m_dProteinSet = proteinSet ;
        action = LOAD_PEPTIDE_FOR_PROTEIN_SET;
    }
    
    public void initLoadPeptideIons(long projectId, DDataset dataset,  DMasterQuantPeptide masterQuantPeptide ,  List<MasterQuantPeptideIon> masterQuantPeptideIonList ) {
        String peptideName = "";
        if (masterQuantPeptide != null && masterQuantPeptide.getPeptideInstance() != null && masterQuantPeptide.getPeptideInstance().getBestPeptideMatch() != null && masterQuantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide() != null  ) {
            peptideName =masterQuantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide().getSequence();
        }
        init(SUB_TASK_COUNT_PEPTIDE_ION, new TaskInfo("Load Peptides Ions of peptide " + peptideName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptide = masterQuantPeptide;
        m_masterQuantPeptideIonList  = masterQuantPeptideIonList;
        action = LOAD_PEPTIDE_ION_FOR_PEPTIDE;
    }
    
    public void initLoadPeptideIons(long projectId, DDataset dataset, List<MasterQuantPeptideIon> masterQuantPeptideIonList ) {
        init(SUB_TASK_COUNT_PEPTIDE_ION, new TaskInfo("Load Peptides Ions of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantPeptideIonList  = masterQuantPeptideIonList;
        action = LOAD_PEPTIDE_ION_FOR_XIC;
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
            case LOAD_PEPTIDE_ION_FOR_PEPTIDE:
                m_masterQuantPeptideIonList = null;
                break;
            case LOAD_PEPTIDE_ION_FOR_XIC:
                m_masterQuantPeptideIonList = null;
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

        }else if (action == LOAD_PEPTIDE_ION_FOR_XIC) {
            if (needToFetch()) {
                // first data are fetched
                fetchDataQuantChannels();
                return fetchDataPeptideIonMainTask();
            } else {
                // fetch data of SubTasks
                return fetchPeptideIonDataSubTask();
            }

        }else if (action == LOAD_PEPTIDE_ION_FOR_PEPTIDE) {
            if (needToFetch()) {
                // first data are fetched
                fetchDataQuantChannels();
                return fetchDataPeptideIonForPeptideMainTask();
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
            case LOAD_PEPTIDE_ION_FOR_XIC:
                return (m_masterQuantPeptideIonList == null || m_masterQuantPeptideIonList.isEmpty());
            case LOAD_PEPTIDE_ION_FOR_PEPTIDE:
                return (m_masterQuantPeptideIonList == null || m_masterQuantPeptideIonList.isEmpty());
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
                        // search resultFileName and raw path
                        String resultFileName = "";
                        String rawPath = "";
                        String queryMsi = "SELECT msi.resultFileName, pl.path "
                                + "FROM MsiSearch msi, Peaklist pl, ResultSet rs, ResultSummary rsm "
                                + " WHERE rsm.id=:rsmId AND rsm.resultSet.id = rs.id AND rs.msiSearch.id = msi.id "
                                + "AND msi.peaklist.id = pl.id ";
                        Query qMsi = entityManagerMSI.createQuery(queryMsi);
                        qMsi.setParameter("rsmId", qc.getIdentResultSummaryId());
                        try {
                            Object[] res = (Object[])qMsi.getSingleResult();
                            resultFileName = (String)res[0];
                            rawPath = (String)res[1];
                            if (resultFileName != null && resultFileName.contains(".")) {
                                resultFileName = resultFileName.substring(0, resultFileName.indexOf('.'));
                            }
                        } catch (NoResultException | NonUniqueResultException e) {

                        }
                        dqc.setResultFileName(resultFileName);
                        dqc.setRawFilePath(rawPath);
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
     * Fetch first data to display proteinSet (all Master Protein Sets, quantProteinSet
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
                        Query proteinSetsQuery = entityManagerMSI.createQuery("SELECT ps.id  FROM PeptideSet pepset JOIN pepset.proteinSet as ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true ORDER BY pepset.score DESC");
                        proteinSetsQuery.setParameter("rsmId", resultSummaryId);
                        List<Long> proteinSets = proteinSetsQuery.getResultList();
                        if (proteinSets != null && !proteinSets.isEmpty()) {
                            // for each proteinSet, retrieve the DMasterQuantProteinSet
                            int nbProteinSet = proteinSets.size();
                            m_proteinSetIds = new ArrayList<>(nbProteinSet);
                            m_resultSetIds =  new ArrayList<>(nbProteinSet);
                            for (int i = 0; i < nbProteinSet; i++) {
                                Long id = Long.valueOf(proteinSets.get(i));
                                m_proteinSetIds.add(id);
                                m_resultSetIds .add(resultSummaryId);
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
                        
                        //fetchPeptideInstanceData(entityManagerMSI, m_peptideInstanceIds);
                        
                        // slice the task and get the first one
                        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE_INSTANCE, m_peptideInstanceIds.size(), SLICE_SIZE);

                        // execute the first slice now
                        fetchPeptideInstanceData(subTask, entityManagerMSI);
                        
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
     * load all quant peptides ions for a xic
     *
     * @return
     */
    private boolean fetchDataPeptideIonMainTask() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
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
                        m_masterQuantPeptideIonIds = listIds ;
                        
                        for (Long m_masterQuantPeptideIonId : m_masterQuantPeptideIonIds) {
                            MasterQuantPeptideIon mqpi = new MasterQuantPeptideIon();
                            mqpi.setId(m_masterQuantPeptideIonId);
                            m_masterQuantPeptideIonList.add(mqpi) ;
                        }
                        
                        //fetchPeptideIonData(entityManagerMSI, m_masterQuantPeptideIonIds);
                        
                        // slice the task and get the first one
                        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE_ION, m_masterQuantPeptideIonIds.size(), SLICE_SIZE);

                        // execute the first slice now
                        fetchPeptideIonData(subTask, entityManagerMSI);
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
                        peptidesQuery.setParameter("proteinSetId", ( m_dProteinSet == null?-1:m_dProteinSet.getId()));
                        List<Long> listIds = (List<Long>) peptidesQuery.getResultList();
                        m_peptideInstanceIds = listIds ;
                        
                        List<DMasterQuantPeptide> listDMasterQuantPeptideFake = new ArrayList();
                        for (Long m_peptideInstanceId : m_peptideInstanceIds) {
                            DMasterQuantPeptide f = new DMasterQuantPeptide(-1, 0, -1, "", resultSummaryId);
                            f.setPeptideInstanceId(m_peptideInstanceId);
                            listDMasterQuantPeptideFake.add(f);
                        }
                        m_masterQuantPeptideList.addAll(listDMasterQuantPeptideFake);
                        
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
                + "pm.peptideId=p.id AND pi.peptide.id = p.id  "
                + "ORDER BY pm.score DESC";

        Query query = entityManagerMSI.createQuery(querySelect);
        query.setParameter("listId", listPeptideInstanceIds);
        List resultList = query.getResultList();
        Iterator<Object[]> itPeptidesQuery = resultList.iterator();
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
            PeptideInstance pi = (PeptideInstance) resCur[0];
            DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
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

        int nbMP = m_masterQuantPeptideList.size();
        // for each PeptideInstance, load MasterQuantPeptide and list of QuantPeptide
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
            int index = -1;
            for(int k=0; k<nbMP; k++) {
                if (m_masterQuantPeptideList.get(k).getPeptideInstanceId() == peptideInstance.getId()) {
                    index = k;
                    break;
                }
            }
            try {
                masterQuantPeptide = masterQuantPeptideQuery.getSingleResult();
                if (masterQuantPeptide != null) {
                    masterQuantPeptide.setPeptideInstance(peptideInstance);
                    masterQuantPeptide.setPeptideInstanceId(peptideInstance.getId());
                    // list of quantPeptide
                    String quantPeptideData = ""; //ObjectTree.clobData
                    ObjectTree ot = entityManagerMSI.find(ObjectTree.class, masterQuantPeptide.getObjectTreeId()); // get the objectTree from id.
                    if (ot != null) {
                        quantPeptideData = ot.getClobData();
                    }

                    Map<Long, DQuantPeptide> quantProteinSetByQchIds = null;
                    if (quantPeptideData != null && !quantPeptideData.isEmpty()) {
                        quantProteinSetByQchIds = masterQuantPeptide.parseQuantPeptideFromProperties(quantPeptideData);
                    }
                    masterQuantPeptide.setQuantPeptideByQchIds(quantProteinSetByQchIds);

                }

                // update the list 
                m_masterQuantPeptideList.set(index, masterQuantPeptide);

            } catch (NoResultException  e1) {
                // no master quantPeptide: build a fake masterQuantPeptide to display the peptideInstance
                masterQuantPeptide = new DMasterQuantPeptide(-1, 0, -1, null, peptideInstance.getResultSummary().getId());
                masterQuantPeptide.setPeptideInstance(peptideInstance);
                masterQuantPeptide.setPeptideInstanceId(peptideInstance.getId());
                // update the list 
                m_masterQuantPeptideList.set(index, masterQuantPeptide);
            }catch (NonUniqueResultException e2) {
                m_logger.error(getClass().getSimpleName()+" failed", e2);
            }
        }
        
        // load masterQuantPeptide not linked to a peptideInstance
        for (Long pepInstanceId : listPeptideInstanceIds) {
            if (pepInstanceId < 0) { // peptideInstanceId contains the -masterQuantPeptideId to load
                String queryDMasterQuantPeptide = "SELECT  new fr.proline.core.orm.msi.dto.DMasterQuantPeptide"
                    + "(q.id, q.selectionLevel, q.objectTreeId,  q.serializedProperties,  q.resultSummary.id) "
                    + "FROM MasterQuantComponent q "
                    + " WHERE q.id=:masterQuantPeptideId";
                TypedQuery<DMasterQuantPeptide> masterQuantPeptideQuery = entityManagerMSI.createQuery(queryDMasterQuantPeptide, DMasterQuantPeptide.class);
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

                        Map<Long, DQuantPeptide> quantProteinSetByQchIds = null;
                        if (quantPeptideData != null && !quantPeptideData.isEmpty()) {
                            quantProteinSetByQchIds = masterQuantPeptide.parseQuantPeptideFromProperties(quantPeptideData);
                        }
                        masterQuantPeptide.setQuantPeptideByQchIds(quantProteinSetByQchIds);

                    }
                    
                    int index = -1;
                    for(int k=0; k<nbMP; k++) {
                        if (m_masterQuantPeptideList.get(k).getId() == masterQuantPeptide.getId()) {
                            index = k;
                            break;
                        }
                    }
                    
                    // update the list 
                    m_masterQuantPeptideList.set(index, masterQuantPeptide);
                    
                }catch(NoResultException | NonUniqueResultException e) {
                    
                }
            }
        }
        
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
        int nbMQP = m_masterQuantProteinSetList.size();
        // resultSummaryId is the same for all proteinSet
        int indexRSM = -1;
        for (int k=0; k<nbMQP; k++) {
            if (m_masterQuantProteinSetList.get(k).getProteinSetId() == proteinSetIds.get(0)) {
                indexRSM = k;
                break;
             }
        }
        Long resultSummaryId = m_resultSetIds.get(indexRSM);

        String queryDMasterQuantProteinSet = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantProteinSet"
                + "(q.id,  q.selectionLevel, q.objectTreeId,  q.serializedProperties,  p.resultSummary.id,  p.id) "
                + " FROM MasterQuantComponent q,  ProteinSet p "
                + " WHERE p.id IN (:listId) AND   q.id = p.masterQuantComponentId "
                + " AND q.resultSummary.id=:rsmId AND p.resultSummary.id=:rsmId  "
                + " ORDER BY q.id ASC ";
        TypedQuery<DMasterQuantProteinSet> masterQuantProteinSetsQuery = entityManagerMSI.createQuery(queryDMasterQuantProteinSet, DMasterQuantProteinSet.class);
        masterQuantProteinSetsQuery.setParameter("listId", proteinSetIds);
        masterQuantProteinSetsQuery.setParameter("rsmId", resultSummaryId);
        List<DMasterQuantProteinSet> listResult = masterQuantProteinSetsQuery.getResultList();

        for (DMasterQuantProteinSet masterQuantProteinSet : listResult) {
            TypedQuery<DProteinSet> proteinSetQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinSet(ps.id, ps.typicalProteinMatchId, ps.resultSummary.id) FROM ProteinSet ps WHERE ps.id=:psId ", DProteinSet.class);
            proteinSetQuery.setParameter("psId", masterQuantProteinSet.getProteinSetId());
            DProteinSet dProteinSet = proteinSetQuery.getSingleResult();
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
            if (quantProtSetdata != null && !quantProtSetdata.isEmpty()) {
                quantProteinSetByQchIds= masterQuantProteinSet.parseQuantProteinSetFromProperties(quantProtSetdata);
            }
            masterQuantProteinSet.setQuantProteinSetByQchIds(quantProteinSetByQchIds);

            masterQuantProteinSet.setProteinSet(dProteinSet);
            // nb PeptideInstance and nbPeptide quantified
            String queryCountNbPep = "SELECT count(*) "
                    + "FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem pspi, "
                    + "fr.proline.core.orm.msi.PeptideSet pepSet "
                    + "WHERE pi.resultSummary.id=:rsmId AND pi.id = pspi.peptideInstance.id AND "
                    + "pspi.peptideSet.id = pepSet.id AND pepSet.proteinSet.id=:proteinSetId " ;
            Query queryCountPep = entityManagerMSI.createQuery(queryCountNbPep);
            queryCountPep.setParameter("rsmId", resultSummaryId);
            queryCountPep.setParameter("proteinSetId", masterQuantProteinSet.getProteinSetId());
            int nbPep = 0;
            try {
                nbPep = ((Long)queryCountPep.getSingleResult()).intValue();
            }catch(NoResultException | NonUniqueResultException e) {
                
            }
            masterQuantProteinSet.setNbPeptides(nbPep);
            String queryCountNbPepQuant = "SELECT count(*) "
                    + "FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem pspi, "
                    + "fr.proline.core.orm.msi.PeptideSet pepSet, MasterQuantComponent q "
                    + "WHERE pi.resultSummary.id=:rsmId AND pi.id = pspi.peptideInstance.id AND "
                    + "pspi.peptideSet.id = pepSet.id AND pepSet.proteinSet.id=:proteinSetId  "
                    + "AND q.resultSummary.id = pi.resultSummary.id AND  q.id = pi.masterQuantComponentId " ;
            Query queryCountPepQuant = entityManagerMSI.createQuery(queryCountNbPepQuant);
            queryCountPepQuant.setParameter("rsmId", resultSummaryId);
            queryCountPepQuant.setParameter("proteinSetId", masterQuantProteinSet.getProteinSetId());
            int nbPepQuant = 0;
            try {
                nbPepQuant = ((Long)queryCountPepQuant.getSingleResult()).intValue();
            }catch(NoResultException | NonUniqueResultException e) {
                
            }
            masterQuantProteinSet.setNbQuantifiedPeptides(nbPepQuant);
            // update in the list
            int index = -1;
            for (int k=0; k<nbMQP; k++) {
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
    
    /***
     * load PeptideIons data for a given subTask
     * @param subTask
     * @param entityManagerMSI
     * @return 
     */
    private boolean fetchPeptideIonData(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfPeptideIonIds = subTask.getSubList(m_masterQuantPeptideIonIds);
        return fetchPeptideIonData(entityManagerMSI, sliceOfPeptideIonIds) ;
    }
    
    /**
     * subTask to load peptideIon
     * @return 
     */
    private boolean fetchPeptideIonDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
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
            entityManagerMSI.getTransaction().rollback();
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
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
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
                        peptidesIonQuery.setParameter("masterQuantPeptideId",(m_masterQuantPeptide== null?-1: m_masterQuantPeptide.getId()));
                        List<Long> listIds = (List<Long>) peptidesIonQuery.getResultList();
                        m_masterQuantPeptideIonIds = listIds ;
                        
                        for (Long m_masterQuantPeptideIonId : m_masterQuantPeptideIonIds) {
                            MasterQuantPeptideIon mqpi = new MasterQuantPeptideIon();
                            mqpi.setId(m_masterQuantPeptideIonId);
                            m_masterQuantPeptideIonList.add(mqpi) ;
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
     * load peptideIon data for  a given list of Ids
     *
     * @param entityManagerMSI
     * @param listMasterPeptideIonsIds
     * @return
     */
    private boolean fetchPeptideIonData(EntityManager entityManagerMSI, List<Long> listMasterPeptideIonsIds) {
        // list of MasterQuantPeptideIons
        int nbM = m_masterQuantPeptideIonList.size();
        String queryMasterPeptideIons = "SELECT pi, mqpi, p "
                + "FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi, "
                + "fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideMatch pm  "
                + "WHERE mqpi.id IN (:listId) AND pi.id = mqpi.peptideInstance.id AND  "
                + "pi.peptide.id=p.id AND pm.id= pi.bestPeptideMatchId  "
                + "ORDER BY pm.score DESC";
        
        Query queryMasterIons = entityManagerMSI.createQuery(queryMasterPeptideIons);
        queryMasterIons.setParameter("listId", listMasterPeptideIonsIds);
        List<Object[]> resultListIons = new ArrayList();
        if (listMasterPeptideIonsIds != null && !listMasterPeptideIonsIds.isEmpty()) {
            resultListIons = queryMasterIons.getResultList();
        } 
        for (Object[] resCur : resultListIons) {
            int i = 0;
            PeptideInstance pi = (PeptideInstance) resCur[i++];
            MasterQuantPeptideIon mQuantPeptideIon = (MasterQuantPeptideIon) resCur[i++];
            Peptide p = (Peptide)resCur[i++];
            pi.setPeptide(p);
            
            mQuantPeptideIon.setPeptideInstance(pi);
            
            // load QuantPeptideIon
            String quantPeptideIonData = ""; //ObjectTree.clobData
            ObjectTree oti = entityManagerMSI.find(ObjectTree.class, mQuantPeptideIon.getMasterQuantComponent().getObjectTreeId()); // get the objectTree from id.
            if (oti != null) {
                quantPeptideIonData = oti.getClobData();
            }

            Map<Long, DQuantPeptideIon> quantPeptideIonByQchIds = null;
            if (quantPeptideIonData != null && !quantPeptideIonData.isEmpty()) {
                quantPeptideIonByQchIds = mQuantPeptideIon.parseQuantPeptideIonFromProperties(quantPeptideIonData);
            }
            mQuantPeptideIon.setQuantPeptideIonByQchIds(quantPeptideIonByQchIds);

            // update the list
            int index = -1;
            for(int k=0; k<nbM; k++) {
                if (m_masterQuantPeptideIonList.get(k).getId() == mQuantPeptideIon.getId()) {
                    index = k;
                    break;
                }
            }
            m_masterQuantPeptideIonList.set(index, mQuantPeptideIon);
        } // end for
        
        String queryPeptideIonWithoutPeptide = "SELECT mqpi "
                + "FROM  fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi " 
                + "WHERE mqpi.id IN (:listId) AND mqpi.peptideInstance is NULL  "
                + "ORDER BY mqpi.id ASC";
        TypedQuery<MasterQuantPeptideIon> queryMasterIons2 = entityManagerMSI.createQuery(queryPeptideIonWithoutPeptide, MasterQuantPeptideIon.class);
        queryMasterIons2.setParameter("listId", listMasterPeptideIonsIds);
        List<MasterQuantPeptideIon> resultListIons2 = new ArrayList();
        if (listMasterPeptideIonsIds != null && !listMasterPeptideIonsIds.isEmpty()) {
            resultListIons2 = queryMasterIons2.getResultList();
        } 
        for (MasterQuantPeptideIon mQuantPeptideIon : resultListIons2) {
            
            // load QuantPeptideIon
            String quantPeptideIonData = ""; //ObjectTree.clobData
            ObjectTree oti = entityManagerMSI.find(ObjectTree.class, mQuantPeptideIon.getMasterQuantComponent().getObjectTreeId()); // get the objectTree from id.
            if (oti != null) {
                quantPeptideIonData = oti.getClobData();
            }

            Map<Long, DQuantPeptideIon> quantPeptideIonByQchIds = null;
            if (quantPeptideIonData != null && !quantPeptideIonData.isEmpty()) {
                quantPeptideIonByQchIds = mQuantPeptideIon.parseQuantPeptideIonFromProperties(quantPeptideIonData);
            }
            mQuantPeptideIon.setQuantPeptideIonByQchIds(quantPeptideIonByQchIds);

            // update the list
            int index = -1;
            for(int k=0; k<nbM; k++) {
                if (m_masterQuantPeptideIonList.get(k).getId() == mQuantPeptideIon.getId()) {
                    index = k;
                    break;
                }
            }
            m_masterQuantPeptideIonList.set(index, mQuantPeptideIon);
        } // end for
        return true;
    }

}
