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

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load Properties for a Dataset
 * @author JM235353
 */
public class DatabaseRsummaryProperties extends AbstractDatabaseTask {
    
    private long m_projectId;
    private DDataset m_dataset = null;
    private ResultSummary m_rsm = null;
    
    public DatabaseRsummaryProperties(AbstractDatabaseCallback callback, long projectId, DDataset dataset) {
        super(callback, new TaskInfo("Load Properties for Identification Summary "+dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_dataset = dataset;
    }
    
    public DatabaseRsummaryProperties(AbstractDatabaseCallback callback, long projectId, ResultSummary rset, String name) {
        super(callback, new TaskInfo("Load Properties for Identification Summary "+name, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_rsm = rset;
    }

    
    @Override
    public boolean needToFetch() {
        if (m_rsm == null) {
            m_rsm = m_dataset.getResultSummary();
        }
        return (m_rsm.getTransientData().getNumberOfProteinSets() == null);
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();
            
            fetchData_count(entityManagerMSI, m_rsm);
            
            ResultSummary rsmDecoy = m_rsm.getDecoyResultSummary();
            if (rsmDecoy != null) {
                fetchData_count(entityManagerMSI, rsmDecoy);
            }
            
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
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

    private void fetchData_count(EntityManager entityManagerMSI, ResultSummary rsm) throws Exception {

        Long rsmId = rsm.getId();

        // Count Protein Sets
        TypedQuery<Long> countProteinSetsQuery = entityManagerMSI.createQuery("SELECT count(ps) FROM ProteinSet ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true", Long.class);
        countProteinSetsQuery.setParameter("rsmId", rsmId);
        Long proteinSetNumber = countProteinSetsQuery.getSingleResult();

        rsm.getTransientData().setNumberOfProteinSet(Integer.valueOf(proteinSetNumber.intValue()));
        
        // Count peptide instances 
        TypedQuery<Long> countPeptidesQuery = entityManagerMSI.createQuery("SELECT count(pi) FROM PeptideInstance pi WHERE pi.resultSummary.id=:rsmId AND pi.validatedProteinSetCount > 0", Long.class);
        countPeptidesQuery.setParameter("rsmId", rsmId);
        rsm.getTransientData().setNumberOfPeptides(countPeptidesQuery.getSingleResult().intValue());
        
        // Count peptide matches 
        TypedQuery<Long> countPeptideMatchesQuery = entityManagerMSI.createQuery("SELECT  sum(pi.peptideMatchCount) FROM PeptideInstance pi WHERE pi.resultSummary.id=:rsmId AND pi.validatedProteinSetCount > 0", Long.class);
        countPeptideMatchesQuery.setParameter("rsmId", rsmId);
        rsm.getTransientData().setNumberOfPeptideMatches(countPeptideMatchesQuery.getSingleResult().intValue());
        
    }
}
