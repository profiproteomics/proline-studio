package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.repository.Database;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load Spectrum from a MsQuery
 *
 * @author JM235353
 */
public class DatabaseLoadSpectrumsTask extends AbstractDatabaseTask {

    private PeptideMatch peptideMatch = null;

    public DatabaseLoadSpectrumsTask(AbstractDatabaseCallback callback, PeptideMatch peptideMatch) {
        super(callback);
        this.peptideMatch = peptideMatch;
    }

    @Override
    public boolean needToFetch() {
        return ((! peptideMatch.getTransientData().getIsMsQuerySet()) ||
                (! peptideMatch.getMsQuery().getTransientIsSpectrumSet()));
    }

    @Override
    public boolean fetchData() {

        
        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(Database.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();

            // Load MsQuery if needed
            if (! peptideMatch.getTransientData().getIsMsQuerySet()) {

               TypedQuery<MsQuery> msQueryQuery = entityManagerMSI.createQuery("SELECT ms FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.MsQuery ms WHERE pm.msQuery=ms AND pm.id=:peptideMatchId", MsQuery.class);
           
               msQueryQuery.setParameter("peptideMatchId", peptideMatch.getId());

                MsQuery msq = msQueryQuery.getSingleResult();
                peptideMatch.setMsQuery(msq);
                peptideMatch.getTransientData().setIsMsQuerySet(true);
                
            }
            

            // Load Spectrum
            MsQuery msQuery = peptideMatch.getMsQuery();
            
            TypedQuery<Spectrum> spectrumQuery = entityManagerMSI.createQuery("SELECT sp FROM fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp WHERE ms.spectrum=sp AND ms.id=:MsQueryId", Spectrum.class);
           
            spectrumQuery.setParameter("MsQueryId", msQuery.getId());

            Spectrum s = spectrumQuery.getSingleResult();
            msQuery.setSpectrum(s);
            msQuery.setTransientIsSpectrumSet(true);
            
            
            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

 
        
        return true;
    }
}
