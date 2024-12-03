/* 
 * Copyright (C) 2019
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

import fr.proline.core.orm.msi.dto.DBioSequence;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Find biosequence for a list of ProteinMatch, check the biosequence according to the validated peptides when it is possible
 * @author JM235353
 */
public class DatabaseBioSequenceTask {

    public static boolean fetchData(List<DProteinMatch> proteinMatchList, Long projectId) {

        Map<Long, DProteinMatch> proteinMatchById = proteinMatchList.stream().collect(Collectors.toMap(pm -> pm.getId(), pm -> pm));
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(projectId).createEntityManager();
        try {

            Query query = entityManagerMSI.createQuery("SELECT pm.id, bs.sequence, bs.mass, bs.pi FROM fr.proline.core.orm.msi.ProteinMatch pm, fr.proline.core.orm.msi.BioSequence bs WHERE bs.id = pm.bioSequenceId AND pm.id IN (:proteinMatchIds)");
            query.setParameter("proteinMatchIds", proteinMatchById.keySet());
            Iterator<Object[]> itProteinGroupsQuery = query.getResultList().iterator();
            while (itProteinGroupsQuery.hasNext()) {
                Object[] resCur = itProteinGroupsQuery.next();
                Long proteinMatchId = (Long) resCur[0];
                String sequence = (String) resCur[1];
                Integer mass = (Integer) resCur[2];
                Float pI = (Float) resCur[3];
                proteinMatchById.get(proteinMatchId).setDBioSequence(new DBioSequence(sequence, mass, pI));
            }

        } catch (RuntimeException ignored) {

        } finally {
            entityManagerMSI.close();
        }
        return true;
    }

}
