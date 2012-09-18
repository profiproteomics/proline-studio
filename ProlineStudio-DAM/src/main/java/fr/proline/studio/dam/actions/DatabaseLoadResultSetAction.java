/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.actions;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.dam.data.Data;
import fr.proline.core.orm.uds.IdentificationFraction;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.*;
import fr.proline.studio.dam.data.ResultSetData;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author JM235353
 */
public class DatabaseLoadResultSetAction extends DatabaseAction{
    
    private IdentificationFraction identificationFraction = null;
    private List<Data> list = null;
    
    public DatabaseLoadResultSetAction(DatabaseCallback callback, IdentificationFraction identificationFraction, List<Data> list) {
        super(callback);
        this.identificationFraction = identificationFraction;
        this.list = list;
        
    }

    public boolean callbackInAWT() {
        return false;
    }
    
    public boolean fetchData() {

        Integer id = identificationFraction.getResultSetId();
        if (id == null) {
            // should never happen
            logger.error("IdentificationFraction : no ResultSetId : "+identificationFraction.getId());
        }
        

        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();

            ResultSet rset = entityManagerMSI.find(ResultSet.class, id);
            if (rset == null) {
                // should not happen
                logger.error("IdentificationFraction : ResultSet not found with Id :" + id);
            } else {
                list.add(new ResultSetData(rset));
            }

            entityManagerMSI.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error("DatabaseLoadResultSetAction failed", e);
        }
        entityManagerMSI.close();


        return true;
    }
    
    
}
