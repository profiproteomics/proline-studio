package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.uds.Identification;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.repository.IdentificationRepository;
import fr.proline.repository.Database;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.IdentificationData;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * Load Identifications of a Project
 *
 * @author JM235353
 */
public class DatabaseLoadIdentificationTask extends AbstractDatabaseTask {

    private Project project = null;
    private List<AbstractData> list = null;

    public DatabaseLoadIdentificationTask(AbstractDatabaseCallback callback, Project project, List<AbstractData> list) {
        super(callback);
        this.project = project;
        this.list = list;

    }

    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    @Override
    public boolean fetchData() {

        Integer projectId = project.getId();

        EntityManager entityManagerUDS = ProlineDBManagement.getProlineDBManagement().getEntityManager(Database.UDS, true);
        try {
            entityManagerUDS.getTransaction().begin();

            // load identifications
            IdentificationRepository identificationRepository = new IdentificationRepository(entityManagerUDS);
            List<Identification> identifications = identificationRepository.findIdentificationsByProject(projectId);

            Iterator<Identification> it = identifications.iterator();
            while (it.hasNext()) {
                Identification identification = it.next();
                list.add(new IdentificationData(identification));
            }

            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
}
