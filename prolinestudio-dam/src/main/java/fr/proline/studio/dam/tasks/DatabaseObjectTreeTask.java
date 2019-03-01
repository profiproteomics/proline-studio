package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 *
 * @author JM235353
 */
public class DatabaseObjectTreeTask extends AbstractDatabaseTask {
    
    private DPeptideMatch m_peptideMatch = null;
    private long m_projectId;
    private ObjectTree[] m_objectTreeResult;
    
    public DatabaseObjectTreeTask(AbstractDatabaseCallback callback, long projectId, DPeptideMatch peptideMatch, ObjectTree[] objectTreeResult) {
        super(callback, new TaskInfo("Load Fragmentation Data for Peptide Match " + getPeptideName(peptideMatch), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_peptideMatch = peptideMatch;
        m_objectTreeResult = objectTreeResult;
    }
    


    @Override
    public boolean fetchData() {
        
        boolean result = true;
        ObjectTree ot = null;

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            PeptideMatch pmORM = entityManagerMSI.find(PeptideMatch.class, m_peptideMatch.getId());
            Map<String, Long> aw_Map = pmORM.getObjectTreeIdByName();

            if (aw_Map.size() > 1) {
                m_logger.warn("PeptideMatch {} has more than one object_tree ", pmORM.getId());
            }

            if (aw_Map.size() > 0){
                Long objectTreeId = aw_Map.get("peptide_match.spectrum_match");
                if (objectTreeId != null) {
                    ot = entityManagerMSI.find(ObjectTree.class, objectTreeId); // get the objectTree from id.
                } else {
                    result = false;
                }
            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            result = false;
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
        } finally {
            entityManagerMSI.close();
        }

        m_objectTreeResult[0] = ot;
        
        return result;
    }

    @Override
    public boolean needToFetch() {
        return true; // JPM.TODO : result could be saved in DPetideMatch not to be obliged to do the loading each time
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
}
