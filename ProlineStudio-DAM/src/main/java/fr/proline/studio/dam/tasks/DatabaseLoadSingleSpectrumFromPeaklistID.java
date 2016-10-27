package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
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
public class DatabaseLoadSingleSpectrumFromPeaklistID extends AbstractDatabaseTask {

    private long m_peaklistID = -1;
    private long m_projectID = -1;
    private DSpectrum m_spectrum = null;

    public DatabaseLoadSingleSpectrumFromPeaklistID(AbstractDatabaseCallback callback, long peaklistID, long projectID) {
        super(callback, new TaskInfo("Load Spectrum for Peaklist ID " + peaklistID, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_peaklistID = peaklistID;
        m_projectID = projectID;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectID).createEntityManager();

        try {
            
            entityManagerMSI.getTransaction().begin();

            TypedQuery<DSpectrum> spectrumQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DSpectrum(sp.id, sp.firstScan, sp.firstTime, sp.lastTime, sp.intensityList, sp.mozList, sp.precursorCharge, sp.precursorIntensity, sp.precursorMoz, sp.title) FROM fr.proline.core.orm.msi.Spectrum sp WHERE sp.peaklistId = " + m_peaklistID, DSpectrum.class);
            spectrumQuery.setMaxResults(1);
            
            List<DSpectrum> spectrumList = spectrumQuery.getResultList();

            if (!spectrumList.isEmpty()) {
                m_spectrum = spectrumList.get(0);
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

    public DSpectrum getSpectrum() {
        return m_spectrum;
    }
}
