/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.spectrum;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideFragmentationTable;
import java.util.Map;
import javax.persistence.EntityManager;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class TmpLoadData {
    
    
    public ObjectTree updateFragmentationTable(DPeptideMatch pepMatch, long projectId) {
        ObjectTree ot = null;

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            PeptideMatch pmORM = entityManagerMSI.find(PeptideMatch.class, pepMatch.getId());
            Map<String, Long> aw_Map = pmORM.getObjectTreeIdByName();

            if (aw_Map.size() > 1) {
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").warn("PeptideMatch {} has more than one object_tree ", pmORM.getId());
            }

            Long objectTreeId = aw_Map.get("peptide_match.spectrum_match");
            if (objectTreeId != null) {
                ot = entityManagerMSI.find(ObjectTree.class, objectTreeId); // get the objectTree from id.
            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            entityManagerMSI.getTransaction().rollback();
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("RsetPeptideSpectrumAnnotations", e);
        } finally {
            entityManagerMSI.close();
        }
        return ot;

    }

    
}
