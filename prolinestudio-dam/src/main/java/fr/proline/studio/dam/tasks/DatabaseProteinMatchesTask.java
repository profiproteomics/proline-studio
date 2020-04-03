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


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.dam.memory.TransientMemoryClientInterface;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;


/**
 * Load Protein Matches for a Rset or for a PeptideMatch
 * @author JM235353
 */
public class DatabaseProteinMatchesTask extends AbstractDatabaseTask {
    
    private long m_projectId = -1;
    private DPeptideMatch m_peptideMatch = null;
    private ResultSet m_rset = null;

    private final int m_action;

    private final static int LOAD_PROTEINS_FROM_PEPTIDE_MATCH  = 0;
    private final static int LOAD_ALL_PROTEINS_OF_RSET = 1;
    
    
    public DatabaseProteinMatchesTask(AbstractDatabaseCallback callback, long projectId, DPeptideMatch peptideMatch) {
        super(callback, new TaskInfo("Load Proteins for a Peptide Match "+getPeptideName(peptideMatch), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));        
        m_projectId = projectId;
        m_peptideMatch = peptideMatch;     
        m_action = LOAD_PROTEINS_FROM_PEPTIDE_MATCH;
    }
    
    public DatabaseProteinMatchesTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset) {
        super(callback, new TaskInfo("Load Proteins of Search Result "+rset.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));        
        m_projectId = projectId;
        m_rset = rset;        
        m_action = LOAD_ALL_PROTEINS_OF_RSET;
    }

    private static String getPeptideName(DPeptideMatch peptideMatch) {
        
        String name;
        
        Peptide peptide = peptideMatch.getPeptide();
        if (peptide != null) {
            name = peptide.getSequence();
        } else {
            name = String.valueOf(peptideMatch.getId());
        }
        
        return name;
    }
    
    
    @Override
    // must be implemented for all AbstractDatabaseSlicerTask 
    public void abortTask() {
        super.abortTask();
        switch (m_action) {
            case LOAD_ALL_PROTEINS_OF_RSET:
                m_rset.getTransientData(TransientMemoryCacheManager.getSingleton()).setProteinMatches(null);
                break;
            case LOAD_PROTEINS_FROM_PEPTIDE_MATCH:
                m_peptideMatch.setProteinMatches(null);
                break;
        }
    }
    
    @Override
    public boolean needToFetch() {
        
         switch (m_action) {
            case LOAD_ALL_PROTEINS_OF_RSET:
                return (m_rset.getTransientData(TransientMemoryCacheManager.getSingleton()).getProteinMatches() == null);
            case LOAD_PROTEINS_FROM_PEPTIDE_MATCH:
                return (m_peptideMatch.getProteinMatches() == null);
        }
        return false; // should not happen
        
        
            
    }
    
    @Override
    public boolean fetchData() {
        switch (m_action) {
            case LOAD_ALL_PROTEINS_OF_RSET:
                if (needToFetch()) {
                    return fechDataForRset();
                }
            case LOAD_PROTEINS_FROM_PEPTIDE_MATCH:
                if (needToFetch()) {
                    return fetchDataForPeptideMatch();
                }
        }
        return true; // should not happen
    }
   
    private boolean fechDataForRset() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            // Load Proteins for PeptideMatch
            TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(protm.id, protm.accession, protm.score, protm.peptideCount, protm.resultSet.id, protm.description, protm.serializedProperties)  FROM ProteinMatch protm WHERE protm.resultSet.id=:resultSetId ORDER BY protm.score DESC", DProteinMatch.class);
            proteinMatchQuery.setParameter("resultSetId", m_rset.getId());
            List<DProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();

            
            int nbProteins = proteinMatchList.size();
            DProteinMatch[] proteins = proteinMatchList.toArray(new DProteinMatch[nbProteins]);
            m_rset.getTransientData(TransientMemoryCacheManager.getSingleton()).setProteinMatches(proteins);

            // Biosequence for each Protein Match
            DatabaseBioSequenceTask.fetchData(proteinMatchList, m_projectId);            
            
            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
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
    
    private boolean fetchDataForPeptideMatch() {

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            
            entityManagerMSI.getTransaction().begin();

            // Load Proteins for PeptideMatch
            TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(protm.id, protm.accession, protm.score, protm.peptideCount, protm.resultSet.id, protm.description, protm.serializedProperties) FROM ProteinMatch protm, SequenceMatch sm, PeptideMatch pepm, fr.proline.core.orm.msi.Peptide p WHERE pepm.id=:peptideMatchId AND  pepm.peptideId=p.id AND p.id=sm.id.peptideId AND sm.resultSetId=pepm.resultSet.id AND sm.id.proteinMatchId=protm.id ORDER BY protm.score DESC", DProteinMatch.class);
            proteinMatchQuery.setParameter("peptideMatchId", m_peptideMatch.getId());
            List<DProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();

            int nbProteins = proteinMatchList.size();
            DProteinMatch[] proteins = proteinMatchList.toArray(new DProteinMatch[nbProteins]);            
            m_peptideMatch.setProteinMatches(proteins);

             // Biosequence for each Protein Match
            DatabaseBioSequenceTask.fetchData(proteinMatchList, m_projectId);            
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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
