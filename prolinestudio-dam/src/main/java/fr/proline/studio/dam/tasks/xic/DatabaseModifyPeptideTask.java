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

    /**
     * Mark protein set as "modified" by peptide selection level change
     * VDS 07/05/2020: remove raw_abundance recalculation
     * 
     * @return 
     */
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
