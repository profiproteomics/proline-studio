package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Used for Test : To be removed later JPM.TODO
 * (create 1000 Protein Groups and Proteins)
 *
 * @author JM235353
 */
public class CreateDatabaseTestTask extends AbstractDatabaseTask {

    public CreateDatabaseTestTask(AbstractDatabaseCallback callback) {
        super(callback);


    }

    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    @Override
    public boolean fetchData() {


        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();

            ResultSet rset = entityManagerMSI.find(ResultSet.class, 13);

            TypedQuery<ResultSummary> rsmQuery = entityManagerMSI.createQuery("SELECT rs FROM fr.proline.core.orm.msi.ResultSummary rs WHERE rs.resultSet.id=:resultSetId", ResultSummary.class);   //find(ResultSummary.class, id);
            rsmQuery.setParameter("resultSetId", new Integer(13));
            List<ResultSummary> rsmList = rsmQuery.getResultList();

            Iterator<ResultSummary> it = rsmList.iterator();
            ResultSummary rsm = it.next();

            Random r = new Random();

            int NB_OBJECTS = 1000;


            BioSequence[] bioSeq = new BioSequence[NB_OBJECTS];
            for (int i = 0; i < bioSeq.length; i++) {
                bioSeq[i] = new BioSequence();
                bioSeq[i].setAlphabet("");
                bioSeq[i].setCrc64("");
                bioSeq[i].setLength(new Integer(1));
                bioSeq[i].setMass((float) r.nextDouble() % 100);
                bioSeq[i].setPi(0);
                bioSeq[i].setSequence("bioSeq" + i);
                bioSeq[i].setSerializedProperties("");
            }

            // flush
            entityManagerMSI.flush();



            ProteinMatch[] pMatch = new ProteinMatch[NB_OBJECTS];
            for (int i = 0; i < pMatch.length; i++) {
                pMatch[i] = new ProteinMatch();

                pMatch[i].setAccession("ProteinD" + i);
                pMatch[i].setCoverage(0);
                pMatch[i].setDescription("Desc ProteinD " + i);
                pMatch[i].setIsDecoy(Boolean.FALSE);
                pMatch[i].setPeptideCount(1);
                pMatch[i].setPeptideMatchCount(1);
                pMatch[i].setScore(((float) r.nextDouble()) * 80);
                pMatch[i].setResultSet(rset);
                pMatch[i].setIsLastBioSequence(Boolean.FALSE);
                pMatch[i].setScoringId(new Integer(1));
                pMatch[i].setBioSequenceId(bioSeq[i].getId());
            }

            // Persist Protein Match
            for (int i = 0; i < pMatch.length; i++) {
                entityManagerMSI.persist(pMatch[i]);
            }

            // flush
            entityManagerMSI.flush();

            ProteinSet[] pSet = new ProteinSet[NB_OBJECTS];
            for (int i = 0; i < pSet.length; i++) {
                pSet[i] = new ProteinSet();
                pSet[i].setIsValidated(Boolean.TRUE);
                pSet[i].setScore(pMatch[i].getScore());
                pSet[i].setSelectionLevel(0);
                pSet[i].setProteinMatchId(pMatch[i].getId());
                pSet[i].setResultSummary(rsm);
                pSet[i].setScoringId(new Integer(1));
            }


            // Persist ProteinSet
            for (int i = 0; i < pMatch.length; i++) {
                entityManagerMSI.persist(pSet[i]);
            }

            // flush
            entityManagerMSI.flush();

            ProteinSetProteinMatchItemPK[] psTopmPK = new ProteinSetProteinMatchItemPK[NB_OBJECTS];
            for (int i = 0; i < psTopmPK.length; i++) {
                psTopmPK[i] = new ProteinSetProteinMatchItemPK();

                psTopmPK[i].setProteinMatchId(pMatch[i].getId());
                psTopmPK[i].setProteinSetId(pSet[i].getId());

            }

            ProteinSetProteinMatchItem[] psTopm = new ProteinSetProteinMatchItem[NB_OBJECTS];
            for (int i = 0; i < psTopm.length; i++) {
                psTopm[i] = new ProteinSetProteinMatchItem();

                psTopm[i].setId(psTopmPK[i]);
                psTopm[i].setProteinMatch(pMatch[i]);
                psTopm[i].setProteinSet(pSet[i]);
                psTopm[i].setResultSummary(rsm);

            }

            // ProteinSetProteinMatchItem
            for (int i = 0; i < psTopm.length; i++) {
                entityManagerMSI.persist(psTopm[i]);
            }

            // flush
            entityManagerMSI.flush();

            // Retrieve a PeptideSet
            PeptideSet peptideSet = entityManagerMSI.find(PeptideSet.class, 1);


            PeptideSetProteinMatchMapPK[] psetTopmPK = new PeptideSetProteinMatchMapPK[NB_OBJECTS];
            for (int i = 0; i < psetTopmPK.length; i++) {
                psetTopmPK[i] = new PeptideSetProteinMatchMapPK();

                psetTopmPK[i].setProteinMatchId(pMatch[i].getId());
                psetTopmPK[i].setPeptideSetId(peptideSet.getId());

            }

            PeptideSetProteinMatchMap[] pssetTopm = new PeptideSetProteinMatchMap[NB_OBJECTS];
            for (int i = 0; i < pssetTopm.length; i++) {
                pssetTopm[i] = new PeptideSetProteinMatchMap();

                pssetTopm[i].setId(psetTopmPK[i]);
                pssetTopm[i].setProteinMatch(pMatch[i]);
                pssetTopm[i].setPeptideSet(peptideSet);
                pssetTopm[i].setResultSummary(rsm);

            }

            // PeptideSetProteinMatchMap
            for (int i = 0; i < psTopm.length; i++) {
                entityManagerMSI.persist(pssetTopm[i]);
            }

            // flush
            entityManagerMSI.flush();


            entityManagerMSI.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error("CreateDatabaseTestTask failed", e);
            entityManagerMSI.getTransaction().rollback();
        }

        entityManagerMSI.close();

        return true;
    }
}
