package fr.proline.studio.dam.tasks.xic;

import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
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
    private static final int SLICE_SIZE = 1000;
    
    public static final int SUB_TASK_COUNT = 0; // <<----- get in sync  //JPM.TODO
    
    private Long m_projectId;
    private DDataset m_dataset;
    private List<DMasterQuantProteinSet> m_masterQuantProteinSetList;
    
    private int action;
    private static final int LOAD_PROTEIN_SET_FOR_XIC = 0;
    
     public DatabaseLoadXicMasterQuantTask(AbstractDatabaseCallback callback) {
        super(callback);
       
    }
     
    public void initLoadProteinSets(long projectId, DDataset dataset, List<DMasterQuantProteinSet> masterQuantProteinSetList) {
        init(SUB_TASK_COUNT, new TaskInfo("Load Protein Sets of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        m_masterQuantProteinSetList = masterQuantProteinSetList ;
        action = LOAD_PROTEIN_SET_FOR_XIC; 
    }
    
    @Override
    // must be implemented for all AbstractDatabaseSlicerTask 
    public void abortTask() {
        super.abortTask();
        switch (action) {
            case LOAD_PROTEIN_SET_FOR_XIC:
                m_masterQuantProteinSetList = null;
                break;
            
        }
    }
     
    @Override
    public boolean fetchData() {
         if (action == LOAD_PROTEIN_SET_FOR_XIC) {
            if (needToFetch()) {
                // first data are fetched
                return fetchDataMainTaskForXIC();
            } /*else {
                // fetch data of SubTasks
                return fetchDataSubTaskFor();
            }*/
        }
         return true; // should not happen
    }

    @Override
    public boolean needToFetch() {
        switch (action) {
            case LOAD_PROTEIN_SET_FOR_XIC:
                return (m_masterQuantProteinSetList == null || m_masterQuantProteinSetList.isEmpty());
            
        }
        return false; // should not happen 
    }
    
    /**
     * Fetch first data. (all Master Protein Sets, quantProteinSet
     *
     * @return
     */
    private boolean fetchDataMainTaskForXIC() {
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
                    Long resultSummaryId = masterQuantitationChannel.getQuantResultSummaryId() ;
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
                        try{
                            resultFileName = (String)qMsi.getSingleResult();
                        }catch(NoResultException | NonUniqueResultException e){
                            
                        }
                        dqc.setResultFileName(resultFileName);
                        listDQuantChannels.add(dqc);
                    }
                    DMasterQuantitationChannel dMaster = new DMasterQuantitationChannel(masterQuantitationChannel.getId(), masterQuantitationChannel.getName(),
                            resultSummaryId, listDQuantChannels, 
                            masterQuantitationChannel.getDataset(), masterQuantitationChannel.getSerializedProperties());
                    //resultSummary
                    if (resultSummaryId != null) {
                        // retrieve the proteinSet list with isValidated = true
                        TypedQuery<DProteinSet> proteinSetsQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinSet(ps.id, ps.typicalProteinMatchId ,ps.resultSummary.id) FROM PeptideSet pepset JOIN pepset.proteinSet as ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true ORDER BY pepset.score DESC", DProteinSet.class);
                        proteinSetsQuery.setParameter("rsmId", resultSummaryId);
                        List<DProteinSet> proteinSets = proteinSetsQuery.getResultList();
                        if (proteinSets != null && !proteinSets.isEmpty()) {
                            // for each proteinSet, retrieve the DMasterQuantProteinSet
                            for (Iterator<DProteinSet> iterator = proteinSets.iterator(); iterator.hasNext();) {
                                DProteinSet dProteinSet = iterator.next();
                                long proteinSetId = dProteinSet.getId();
                                String queryDMasterQuantProteinSet = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantProteinSet"
                                        + "(q.id,  q.selectionLevel, q.objectTreeId,  q.serializedProperties,  r.id,  p.id) "
                                        + " FROM MasterQuantComponent q, ResultSummary r, ProteinSet p "
                                        + " WHERE p.id=:proteinSetId AND p.resultSummary.id = r.id AND r.id = q.resultSummary.id AND r.id=:rsmId AND p.masterQuantComponentId = q.id "
                                        + " ORDER BY q.id ASC ";
                                TypedQuery<DMasterQuantProteinSet> masterQuantProteinSetsQuery =  entityManagerMSI.createQuery(queryDMasterQuantProteinSet, DMasterQuantProteinSet.class);
                                masterQuantProteinSetsQuery.setParameter("proteinSetId", proteinSetId);
                                masterQuantProteinSetsQuery.setParameter("rsmId", resultSummaryId);
                                DMasterQuantProteinSet masterQuantProteinSet  = null;
                                try{ 
                                     masterQuantProteinSet  = masterQuantProteinSetsQuery.getSingleResult();
                                     
                                }catch (NoResultException | NonUniqueResultException e ){
                                    e.printStackTrace();
                                }
                               
                                // typical protein match id
                                String queryProteinMatch = "SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession,  pm.score, pm.peptideCount, pm.resultSet.id, pm.description) "
                                        + "FROM ProteinMatch pm "
                                        + "WHERE pm.id=:pmId";
                                TypedQuery<DProteinMatch> proteinMatchQuery =  entityManagerMSI.createQuery(queryProteinMatch, DProteinMatch.class);
                                proteinMatchQuery.setParameter("pmId", dProteinSet.getProteinMatchId());
                                try{
                                    DProteinMatch typicalProteinMatch = proteinMatchQuery.getSingleResult();
                                    dProteinSet.setTypicalProteinMatch(typicalProteinMatch);
                                }catch (NoResultException | NonUniqueResultException e ){
                                    e.printStackTrace();
                                }
                                
                                
                                // load QuantProteinSetList
                                if (masterQuantProteinSet != null) { 
                                    String quantProtSetdata = ""; //ObjectTree.clobData
                                    ObjectTree ot = entityManagerMSI.find(ObjectTree.class, masterQuantProteinSet.getObjectTreeId()); // get the objectTree from id.
                                    if (ot != null) {
                                        quantProtSetdata = ot.getClobData();
                                    }
                                
                                    Map<Long, DQuantProteinSet> quantProteinSetByQchIds = masterQuantProteinSet.parseQuantProteinSetFromProperties(quantProtSetdata);
                                    masterQuantProteinSet.setQuantProteinSetByQchIds(quantProteinSetByQchIds);
                                    
                                    masterQuantProteinSet.setProteinSet(dProteinSet);
                                    // add to the list
                                    m_masterQuantProteinSetList.add(masterQuantProteinSet);
                                     
                                }
                                
                            }
                        } // end proteinSets != null
                    }// end resultSummaryId null
                    
                    // add into the list
                    masterQuantitationChannels.add(dMaster);
                    //System.out.println("dMaster nb listQC "+dMaster.getQuantitationChannels().size());
                } // end of the for
            }
            m_dataset.setMasterQuantitationChannels(masterQuantitationChannels);
             
            entityManagerMSI.getTransaction().commit();
            entityManagerUDS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
            entityManagerUDS.close();
        }

        
        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }
    
    
}
