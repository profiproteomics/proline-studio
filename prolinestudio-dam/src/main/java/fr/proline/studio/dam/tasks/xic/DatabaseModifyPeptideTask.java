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
package fr.proline.studio.dam.tasks.xic;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.proline.core.orm.msi.MasterQuantComponent;
import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.msi.dto.DQuantProteinSet;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.core.orm.util.JsonSerializer;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import static fr.proline.studio.dam.tasks.AbstractDatabaseTask.TASK_LIST_INFO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseModifyPeptideTask extends AbstractDatabaseTask {

    private int m_action;

    private ArrayList<DMasterQuantPeptide> m_masterQuantPeptideList;
    private ArrayList<DMasterQuantProteinSet> m_masterQuantProteinSetModified;
    private long m_projectId;

    //private ArrayList<Long> m_proteinSetIds;
    private final static int MODIFY_MASTER_QUANT_PEPTIDE = 0;
    private final static int REMOVE_PEPTIDEMODIFIED_ON_PROTEIN = 1;

    public DatabaseModifyPeptideTask(AbstractDatabaseCallback callback) {
        super(callback, null);

    }

    public void initDisablePeptide(long projectId, ArrayList<DMasterQuantPeptide> masterQuantPeptideList, ArrayList<DMasterQuantProteinSet> masterQuantProteinSetModified) {
        setTaskInfo(new TaskInfo("Disable Peptide", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));

        m_action = MODIFY_MASTER_QUANT_PEPTIDE;
        m_projectId = projectId;
        m_masterQuantPeptideList = masterQuantPeptideList;
        m_masterQuantProteinSetModified = masterQuantProteinSetModified;
        setPriority(Priority.TOP);
    }

    public void initRemovePeptideModifiedOnProtein(long projectId, ArrayList<DMasterQuantProteinSet> masterQuantProteinSetModified) {
        setTaskInfo(new TaskInfo("Proteins Refined", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));

        m_action = REMOVE_PEPTIDEMODIFIED_ON_PROTEIN;
        m_projectId = projectId;
        m_masterQuantProteinSetModified = masterQuantProteinSetModified;
        setPriority(Priority.TOP);
    }

    @Override
    public boolean needToFetch() {
        switch (m_action) {
            case MODIFY_MASTER_QUANT_PEPTIDE:
                return true;
            case REMOVE_PEPTIDEMODIFIED_ON_PROTEIN:
                return true;
        }
        return true; // should not happen
    }

    @Override
    public boolean fetchData() {
        switch (m_action) {
            case MODIFY_MASTER_QUANT_PEPTIDE:
                return modifyMasterQuantPeptide();
            case REMOVE_PEPTIDEMODIFIED_ON_PROTEIN:
                return removePeptideModifiedOnProtein();
        }
        return false;  // should not happen
    }

    public boolean removePeptideModifiedOnProtein() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            int nb = m_masterQuantProteinSetModified.size();
            ArrayList<Long> masterQuantProteinSetModifiedIDs = new ArrayList(nb);
            HashMap<Long, DMasterQuantProteinSet> map = new HashMap<>();
            for (int i = 0; i < nb; i++) {
                DMasterQuantProteinSet dmasterQuantProteinSet = m_masterQuantProteinSetModified.get(i);
                Long id = dmasterQuantProteinSet.getId();
                masterQuantProteinSetModifiedIDs.add(dmasterQuantProteinSet.getId());
                map.put(id, dmasterQuantProteinSet);

            }

            String queryMasterQuantProteinSet = "SELECT q"
                    + " FROM MasterQuantComponent q "
                    + " WHERE q.id IN (:mqproteinSetIds) ";
            TypedQuery<MasterQuantComponent> masterQuantProteinSetsQuery = entityManagerMSI.createQuery(queryMasterQuantProteinSet, MasterQuantComponent.class);
            masterQuantProteinSetsQuery.setParameter("mqproteinSetIds", masterQuantProteinSetModifiedIDs);

            List<MasterQuantComponent> masterQuantProteinSets = masterQuantProteinSetsQuery.getResultList();
            Iterator<MasterQuantComponent> it = masterQuantProteinSets.iterator();
            while (it.hasNext()) {
                MasterQuantComponent masterQuantProteinSet = it.next();
                Map<String, Object> pmqSerializedMap = masterQuantProteinSet.getSerializedPropertiesAsMap();
                if (pmqSerializedMap == null) {
                    // nothing to do
                    // should not happen
                } else {
                    pmqSerializedMap.put(DMasterQuantProteinSet.MASTER_QUANT_PROTEINSET_WITH_PEPTIDE_MODIFIED, Boolean.FALSE);
                    if (pmqSerializedMap.isEmpty()) {
                        masterQuantProteinSet.setSerializedProperties(null);
                    } else {
                        masterQuantProteinSet.setSerializedPropertiesAsMap(pmqSerializedMap);
                    }
                    map.get(masterQuantProteinSet.getId()).setSerializedProperties(masterQuantProteinSet.getSerializedProperties());
                    entityManagerMSI.merge(masterQuantProteinSet);
                }
            }

            /*String queryDMasterQuantProteinSet = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantProteinSet"
                    + "(q.id,  q.selectionLevel, q.objectTreeId,  q.serializedProperties,  p.resultSummary.id,  p.id) "
                    + " FROM MasterQuantComponent q,  ProteinSet p "
                    + " WHERE q.id IN (:mproteinSetId) ";
            TypedQuery<DMasterQuantProteinSet> masterQuantDProteinSetsQuery = entityManagerMSI.createQuery(queryDMasterQuantProteinSet, DMasterQuantProteinSet.class);
            masterQuantDProteinSetsQuery.setParameter("mproteinSetIds", m_proteinSetIds);
            List<DMasterQuantProteinSet> dMasterQuantProteinSets = masterQuantDProteinSetsQuery.getResultList();
            m_masterQuantProteinSetModified.addAll(dMasterQuantProteinSets);*/
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

    public boolean modifyMasterQuantPeptide() {

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            int nbPeptides = m_masterQuantPeptideList.size();
            ArrayList<Long> peptideInstanceIdList = new ArrayList<>(nbPeptides);
            for (int i = 0; i < nbPeptides; i++) {
                DMasterQuantPeptide masterQuantPeptide = m_masterQuantPeptideList.get(i);
                // Change the selection level of the peptide
                MasterQuantComponent peptideMasterQuantComponent = entityManagerMSI.find(MasterQuantComponent.class, masterQuantPeptide.getId());
                peptideMasterQuantComponent.setSelectionLevel(masterQuantPeptide.getSelectionLevel());
                entityManagerMSI.merge(peptideMasterQuantComponent);

                // Load Protein Sets which contain this peptide and modify them
                DPeptideInstance peptideInstance = masterQuantPeptide.getPeptideInstance();
                long id = peptideInstance.getId();
                peptideInstanceIdList.add(id);
            }

            // Load Protein Sets which contain this peptide and modify them
            TypedQuery<ProteinSet> proteinSetsQuery = entityManagerMSI.createQuery("SELECT prots FROM fr.proline.core.orm.msi.ProteinSet prots, fr.proline.core.orm.msi.PeptideSet peps, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem peps_to_pepi WHERE peps.proteinSet=prots AND peps.id=peps_to_pepi.id.peptideSetId AND peps_to_pepi.id.peptideInstanceId IN (:peptideInstanceId) AND prots.isValidated=true ORDER BY peps.score DESC", ProteinSet.class);
            proteinSetsQuery.setParameter("peptideInstanceId", peptideInstanceIdList);

            List<ProteinSet> proteinSets = proteinSetsQuery.getResultList();
            Iterator<ProteinSet> it = proteinSets.iterator();
            while (it.hasNext()) {
                ProteinSet proteinSet = it.next();
                /*Map<String, Object> serializedMap = proteinSet.getSerializedPropertiesAsMap();
                if (serializedMap == null) {
                    serializedMap = new HashMap();
                }
                serializedMap.put("MQComponent_SLevelChanged", Boolean.TRUE);
                proteinSet.setSerializedPropertiesAsMap(serializedMap); 
                entityManagerMSI.merge(proteinSet);*/

                String queryDMasterQuantProteinSet = "SELECT new fr.proline.core.orm.msi.dto.DMasterQuantProteinSet"
                        + "(q.id,  q.selectionLevel, q.objectTreeId,  q.serializedProperties,  p.resultSummary.id,  p.id) "
                        + " FROM MasterQuantComponent q,  ProteinSet p "
                        + " WHERE p.id = :proteinSetId AND   q.id = p.masterQuantComponentId "
                        + " ORDER BY q.id ASC ";
                TypedQuery<DMasterQuantProteinSet> masterQuantProteinSetsQuery = entityManagerMSI.createQuery(queryDMasterQuantProteinSet, DMasterQuantProteinSet.class);
                masterQuantProteinSetsQuery.setParameter("proteinSetId", proteinSet.getId());
                DMasterQuantProteinSet masterQuantProteinSet = masterQuantProteinSetsQuery.getSingleResult();

                m_masterQuantProteinSetModified.add(masterQuantProteinSet);

                MasterQuantComponent proteinSetMasterQuantComponent = entityManagerMSI.find(MasterQuantComponent.class, proteinSet.getMasterQuantComponentId());
                if (proteinSetMasterQuantComponent != null) {

                    Map<String, Object> pmqSerializedMap = proteinSetMasterQuantComponent.getSerializedPropertiesAsMap();
                    if (pmqSerializedMap == null) {
                        pmqSerializedMap = new HashMap();
                    }
                    pmqSerializedMap.put(DMasterQuantProteinSet.MASTER_QUANT_PROTEINSET_WITH_PEPTIDE_MODIFIED, Boolean.TRUE);
                    proteinSetMasterQuantComponent.setSerializedPropertiesAsMap(pmqSerializedMap);
                    entityManagerMSI.merge(proteinSetMasterQuantComponent);
                    masterQuantProteinSet.setSerializedProperties(proteinSetMasterQuantComponent.getSerializedProperties()); // update corresponding DMasterQuantProteinSet

                    ObjectTree proteinSetMasterobjectTree = entityManagerMSI.find(ObjectTree.class, proteinSetMasterQuantComponent.getObjectTreeId());
                    String quantProtSetdata = proteinSetMasterobjectTree.getClobData();

                    Map<Long, DQuantProteinSet> quantProteinSetByQchIds = null;
                    if (quantProtSetdata != null && !quantProtSetdata.isEmpty()) {
                        quantProteinSetByQchIds = masterQuantProteinSet.parseQuantProteinSetFromProperties(quantProtSetdata);
                    }

                    // retrieve list of peptides
                    Long resultSummaryId = proteinSetMasterQuantComponent.getResultSummary().getId();
                    String queryPep = "SELECT pi.id "
                            + "FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem pspi, "
                            + "fr.proline.core.orm.msi.PeptideSet pepSet "
                            + "WHERE pi.resultSummary.id=:rsmId AND pi.id = pspi.peptideInstance.id AND "
                            + "pspi.peptideSet.id = pepSet.id AND pepSet.proteinSet.id=:proteinSetId "
                            + "ORDER BY pi.id ASC";
                    Query peptidesQuery = entityManagerMSI.createQuery(queryPep);
                    peptidesQuery.setParameter("rsmId", resultSummaryId);
                    peptidesQuery.setParameter("proteinSetId", proteinSet.getId());
                    List<Long> peptideInstanceIds = (List<Long>) peptidesQuery.getResultList();

                    HashMap<Long, Peptide> peptideMap = new HashMap<>();

                    List<DPeptideInstance> peptideInstanceList = new ArrayList();
                    String querySelect = "SELECT  pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, p "
                            + "FROM fr.proline.core.orm.msi.PeptideInstance pi,  "
                            + "fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p  "
                            + "WHERE pi.id IN (:listId) AND  "
                            + "pi.bestPeptideMatchId=pm.id AND "
                            + "pm.peptideId=p.id  "
                            + "ORDER BY pm.score DESC";

                    Query query = entityManagerMSI.createQuery(querySelect);
                    query.setParameter("listId", peptideInstanceIds);
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
                        String pmSerializedProp = (String) resCur[11];
                        DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank, pmSerializedProp);

                        Peptide p = (Peptide) resCur[12];
                        p.getTransientData().setPeptideReadablePtmStringLoaded();
                        peptideMap.put(p.getId(), p);

                        dpi.setBestPeptideMatch(pm);

                        pm.setPeptide(p);

                        peptideInstanceList.add(dpi);
                    }

                    int nbPI = peptideInstanceList.size();

                    String queryDMasterQuantPeptide = "SELECT q.id, q.selectionLevel, q.objectTreeId,  q.serializedProperties,  pi.resultSummary.id, pi.id "
                            + "FROM MasterQuantComponent q, PeptideInstance pi "
                            + " WHERE pi.id IN (:listPeptideInstanceId) AND  q.id = pi.masterQuantComponentId "
                            + " ORDER BY q.id ASC ";
                    Query mqPeptideQ = entityManagerMSI.createQuery(queryDMasterQuantPeptide);
                    mqPeptideQ.setParameter("listPeptideInstanceId", peptideInstanceIds);
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

                    List<ObjectTree> listOt = new ArrayList();
                    if (listObjectTreeId.size() > 0) {
                        String otQuery = "SELECT ot FROM fr.proline.core.orm.msi.ObjectTree ot WHERE id IN (:listId) ";
                        TypedQuery<ObjectTree> queryObjectTree = entityManagerMSI.createQuery(otQuery, ObjectTree.class);
                        queryObjectTree.setParameter("listId", listObjectTreeId);
                        listOt = queryObjectTree.getResultList();
                    }

                    int nbMP = listMasterQP.size();
                    List<DMasterQuantPeptide> masterQuantPeptideList = new ArrayList<>();
                    for (int i = 0; i < nbMP; i++) {
                        masterQuantPeptideList.add(null);
                    }

                    for (DMasterQuantPeptide masterQuantPeptide : listMasterQP) {
                        int index = -1;
                        for (int k = 0; k < nbMP; k++) {
                            if (listMasterQP.get(k).getPeptideInstanceId() == masterQuantPeptide.getPeptideInstanceId()) {
                                index = k;
                                break;
                            }
                        }
                        // list of quantPeptide
                        String quantPeptideData = ""; //ObjectTree.clobData
                        ObjectTree ot = null;
                        // search objectTree
                        for (ObjectTree objectTreeCur : listOt) {
                            if (objectTreeCur.getId() == masterQuantPeptide.getObjectTreeId()) {
                                ot = objectTreeCur;
                                break;
                            }
                        }
                        if (ot != null) {
                            quantPeptideData = ot.getClobData();
                        }
                        Map<Long, DQuantPeptide> quantPeptideByQchIds = null;
                        if (quantPeptideData != null && !quantPeptideData.isEmpty()) {
                            quantPeptideByQchIds = masterQuantPeptide.parseQuantPeptideFromProperties(quantPeptideData);
                        }

                        masterQuantPeptide.setQuantPeptideByQchIds(quantPeptideByQchIds);
                        // update the list 
                        masterQuantPeptideList.set(index, masterQuantPeptide);
                    }

                    // calculate the raw abundance
                    for (Map.Entry<Long, DQuantProteinSet> entrySet : quantProteinSetByQchIds.entrySet()) {
                        Long qcId = entrySet.getKey();
                        DQuantProteinSet quantProteinSet = quantProteinSetByQchIds.get(qcId);

                        float rawAbundance = 0;
                        for (DMasterQuantPeptide masterQuantPeptide : listMasterQP) {
                            if (masterQuantPeptide.getSelectionLevel() != 2) {
                                continue;
                            }
                            Map<Long, DQuantPeptide> quantPeptideByQchIds = masterQuantPeptide.getQuantPeptideByQchIds();
                            DQuantPeptide quantPeptideCur = quantPeptideByQchIds.get(qcId);
                            if (quantPeptideCur != null) {
                                rawAbundance += quantPeptideCur.getRawAbundance();
                            }
                        }

                        quantProteinSet.setRawAbundance(new Float(rawAbundance));

                    }

                    // save the raw abundances for Master Protein Set
                    ObjectMapper mapper = JsonSerializer.getMapper();
                    mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
                    LinkedList<DQuantProteinSet> values = new LinkedList<>(quantProteinSetByQchIds.values());
                    String clobData = JsonSerializer.getMapper().writeValueAsString(values);
                    proteinSetMasterobjectTree.setClobData(clobData);
                    entityManagerMSI.merge(proteinSetMasterobjectTree);

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

        return true;
    }

}
