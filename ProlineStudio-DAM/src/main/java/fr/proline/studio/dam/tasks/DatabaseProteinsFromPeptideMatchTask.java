package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;


/**
 *
 * @author JM235353
 */
public class DatabaseProteinsFromPeptideMatchTask extends AbstractDatabaseTask {
    
    private long projectId = -1;
    private PeptideMatch peptideMatch = null;

    public DatabaseProteinsFromPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, PeptideMatch peptideMatch) {
        super(callback, Priority.NORMAL_3, new TaskInfo("Load Proteins for a Peptide Match "+getPeptideName(peptideMatch), TASK_LIST_INFO));
        this.projectId = projectId;
        this.peptideMatch = peptideMatch;        
    }

    private static String getPeptideName(PeptideMatch peptideMatch) {
        
        String name;
        
        Peptide peptide = peptideMatch.getTransientData().getPeptide();
        if (peptide != null) {
            name = peptide.getSequence();
        } else {
            name = String.valueOf(peptideMatch.getId());
        }
        
        return name;
    }
    
    @Override
    public boolean needToFetch() {
        return (peptideMatch.getTransientData().getProteinMatches() == null);
            
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        try {
            
            entityManagerMSI.getTransaction().begin();

            // Load Proteins for PeptideMatch
            TypedQuery<ProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT protm FROM ProteinMatch protm, SequenceMatch sm, PeptideMatch pepm, fr.proline.core.orm.msi.Peptide p WHERE pepm.id=:peptideMatchId AND  pepm.peptideId=p.id AND p.id=sm.id.peptideId AND sm.resultSetId=pepm.resultSet.id AND sm.id.proteinMatchId=protm.id ORDER BY protm.score DESC", ProteinMatch.class);
            proteinMatchQuery.setParameter("peptideMatchId", peptideMatch.getId());
            List<ProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();
            

            // temporary Map to link a bioSequenceId to a ProteinMatch
            HashMap<Long, ProteinMatch> biosequenceToProteinMap = new HashMap<>();
            HashMap<String, ProteinMatch> accessionToProteinMap = new HashMap<>();
            
            Iterator<ProteinMatch> it = proteinMatchList.iterator();
            while (it.hasNext()) {
                ProteinMatch proteinMatch = it.next();

                Long bioSequenceId = proteinMatch.getBioSequenceId();
                if (bioSequenceId != null) {
                    biosequenceToProteinMap.put(bioSequenceId, proteinMatch);
                } else {
                    accessionToProteinMap.put(proteinMatch.getAccession(), proteinMatch);
                }
 
            }
            
            // retrieve mass
            if (biosequenceToProteinMap.size()>0) {
                Set idSet = biosequenceToProteinMap.keySet();
                List<Integer> ids = new ArrayList<>(idSet.size());
                ids.addAll(idSet);


                Query massQuery = entityManagerMSI.createQuery("SELECT bs.id, bs FROM BioSequence bs WHERE bs.id IN (:listId)");
                massQuery.setParameter("listId", ids);
                
                List l = massQuery.getResultList();
                Iterator<Object[]> itMass=l.iterator();
                while (itMass.hasNext()) {
                    Object[] resCur = itMass.next();
                    Long bioSequenceId = (Long) resCur[0];
                    fr.proline.core.orm.msi.BioSequence bioSequence = (BioSequence) resCur[1];
                    ProteinMatch pm = biosequenceToProteinMap.get(bioSequenceId);
                    pm.getTransientData().setBioSequenceMSI(bioSequence);
                }
       
            }
            
            if (!accessionToProteinMap.isEmpty()) {

                Set<String> accessionSet = accessionToProteinMap.keySet();
                List<String> accessionList = new ArrayList<>(accessionSet.size());
                accessionList.addAll(accessionSet);

                // we are going to look to a Biosequence in PDI db through ProteinIdentifier
                EntityManager entityManagerPDI = DataStoreConnectorFactory.getInstance().getPdiDbConnector().getEntityManagerFactory().createEntityManager();
                try {
                    entityManagerPDI.getTransaction().begin();

                    Query bioseqQuery = entityManagerPDI.createQuery("SELECT pi.value, pi.bioSequence FROM ProteinIdentifier pi WHERE pi.value IN (:listAccession) ");
                    bioseqQuery.setParameter("listAccession", accessionList);

                    List l = bioseqQuery.getResultList();
                    Iterator<Object[]> itBioseq = l.iterator();
                    while (itBioseq.hasNext()) {
                        Object[] resCur = itBioseq.next();
                        String accession = (String) resCur[0];
                        fr.proline.core.orm.pdi.BioSequence bioSequence = (fr.proline.core.orm.pdi.BioSequence) resCur[1];
                        ProteinMatch pm = accessionToProteinMap.get(accession);
                        pm.getTransientData().setBioSequencePDI(bioSequence);
                    }

                    entityManagerPDI.getTransaction().commit();
                } finally {
                    entityManagerPDI.close();
                }
            }
            
            ProteinMatch[] proteins = proteinMatchList.toArray(new ProteinMatch[proteinMatchList.size()]);
            
            peptideMatch.getTransientData().setProteinMatches(proteins);
            
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        
        return true;
    }
    
    
}
