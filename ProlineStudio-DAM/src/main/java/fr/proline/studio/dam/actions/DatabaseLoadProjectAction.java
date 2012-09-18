/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.actions;

import fr.proline.studio.dam.data.ProjectData;
import fr.proline.studio.dam.data.Data;
import fr.proline.core.orm.uds.Identification;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.repository.IdentificationRepository;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.*;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author JM235353
 */
public class DatabaseLoadProjectAction extends DatabaseAction {
    
    private Integer projectId = null;
    private List<Data> list = null;

    
    public DatabaseLoadProjectAction(DatabaseCallback callback, Integer projectId, List<Data> list) {
        super(callback);
        this.projectId = projectId;
        this.list = list;
        
    }

    public boolean callbackInAWT() {
        return false;
    }
    
    public boolean fetchData() {

        
        EntityManager entityManagerUDS = ProlineDBManagement.getProlineDBManagement().getEntityManager(ProlineRepository.Databases.UDS, true);
        try {
            entityManagerUDS.getTransaction().begin();

            Project project = entityManagerUDS.find(Project.class, projectId);
            list.add(new ProjectData(project));
            
            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error("DatabaseLoadAction failed", e);
            entityManagerUDS.getTransaction().rollback();
        }

        entityManagerUDS.close();
        
        return true;
    }
    
    
}
