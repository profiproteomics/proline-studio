package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load Spectrum from a MsQuery
 *
 * @author JM235353
 */
public class DatabaseLoadSpectrumsTask extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private DPeptideMatch m_peptideMatch = null;

    public DatabaseLoadSpectrumsTask(AbstractDatabaseCallback callback, long projectId, DPeptideMatch peptideMatch) {
        super(callback, new TaskInfo("Load Spectrum for Peptide Match "+getPeptideName(peptideMatch), TASK_LIST_INFO));
        m_projectId = projectId;
        m_peptideMatch = peptideMatch;
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
    public boolean needToFetch() {
        return ((! m_peptideMatch.isMsQuerySet()) ||
                (! m_peptideMatch.getMsQuery().isSpectrumSet()));
    }

    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            // Load DMsQuery if needed
            if (! m_peptideMatch.isMsQuerySet()) {

               TypedQuery<DMsQuery> msQueryQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DMsQuery(pm.id, msq.id, msq.initialId, s.precursorIntensity) FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.MsQuery msq, Spectrum s WHERE pm.msQuery=msq AND pm.id=:peptideMatchId AND msq.spectrum=s", DMsQuery.class);
               
               msQueryQuery.setParameter("peptideMatchId", m_peptideMatch.getId());

                DMsQuery msq = msQueryQuery.getSingleResult();
                m_peptideMatch.setMsQuery(msq);

            }
            

            // Load Spectrum
            DMsQuery msQuery = m_peptideMatch.getMsQuery();
            
            TypedQuery<Spectrum> spectrumQuery = entityManagerMSI.createQuery("SELECT sp FROM fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp WHERE ms.spectrum=sp AND ms.id=:MsQueryId", Spectrum.class);
           
            spectrumQuery.setParameter("MsQueryId", msQuery.getId());

  
            List<Spectrum> spectrums = spectrumQuery.getResultList();
            if (spectrums.isEmpty()) {
                msQuery.setSpectrum(null);
            } else {
                msQuery.setSpectrum(spectrums.get(0));
            }
 
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            return false;
        } finally {

            entityManagerMSI.close();

        }

 
        
        return true;
    }
}
