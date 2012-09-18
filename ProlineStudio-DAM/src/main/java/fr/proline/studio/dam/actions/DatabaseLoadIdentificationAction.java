/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.actions;

import fr.proline.core.orm.uds.Identification;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.repository.IdentificationRepository;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.data.Data;
import fr.proline.studio.dam.data.IdentificationData;
import fr.proline.studio.dam.DatabaseAction;
import fr.proline.studio.dam.DatabaseCallback;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author JM235353
 */
public class DatabaseLoadIdentificationAction extends DatabaseAction {
    
    private Project project = null;
    private List<Data> list = null;
    
    public DatabaseLoadIdentificationAction(DatabaseCallback callback, Project project, List<Data> list) {
        super(callback);
        this.project = project;
        this.list = list;
        
    }

    public boolean callbackInAWT() {
        return false;
    }
    
    public boolean fetchData() {
        
        Integer projectId = project.getId();
        
        EntityManager entityManagerUDS = ProlineDBManagement.getProlineDBManagement().getEntityManager(ProlineRepository.Databases.UDS, true);
        try {
            entityManagerUDS.getTransaction().begin();

            IdentificationRepository identificationRepository = new IdentificationRepository(entityManagerUDS);
            List<Identification> identifications = identificationRepository.findIdentificationsByProject(projectId);
            
            Iterator<Identification> it = identifications.iterator();
            while (it.hasNext()) {
                Identification identification = it.next();
                list.add(new IdentificationData(identification));
            }
            
            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error("DatabaseLoadAction failed", e);
            entityManagerUDS.getTransaction().rollback();
        }

        entityManagerUDS.close();
        
        return true;
    }
    
    
}
