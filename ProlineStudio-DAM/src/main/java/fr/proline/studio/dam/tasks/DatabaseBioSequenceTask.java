package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.dto.DBioSequence;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.module.seq.BioSequenceProvider;
import fr.proline.module.seq.dto.BioSequenceWrapper;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Find biosequence for a list of ProteinMatch, check the biosequence according to the validated peptides when it is possible
 * @author JM235353
 */
public class DatabaseBioSequenceTask extends AbstractDatabaseTask {

    private List<DProteinMatch> m_proteinMatchList = null;
    private Long m_rsmId = null;
    
    public DatabaseBioSequenceTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }
    
    public void initLoadBioSequences(List<DProteinMatch> proteinMatchList) {
        setTaskInfo(new TaskInfo("Load Biosequence for " + proteinMatchList.get(0).getAccession() + "...", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_proteinMatchList = proteinMatchList;
        m_rsmId = null;
        setPriority(Priority.NORMAL_1);
    }
    
    public void initLoadBioSequences(List<DProteinMatch> proteinMatchList, Long rsmId) {
        setTaskInfo(new TaskInfo("Load Biosequence for " + proteinMatchList.get(0).getAccession() + "...", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_proteinMatchList = proteinMatchList;
        m_rsmId = rsmId;
        setPriority(Priority.NORMAL_1);
    }
    
    public void initLoadBioSequences(DProteinMatch proteintMatch) {
        setTaskInfo(new TaskInfo("Load Biosequence for " + proteintMatch.getAccession() + "...", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_proteinMatchList = new ArrayList<>(1);
        m_proteinMatchList.add(proteintMatch);
        m_rsmId = null;
        setPriority(Priority.NORMAL_1);
    }

    public void initLoadBioSequences(DProteinMatch proteintMatch, Long rsmId) {
        setTaskInfo(new TaskInfo("Load Biosequence for " + proteintMatch.getAccession() + "...", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_proteinMatchList = new ArrayList<>(1);
        m_proteinMatchList.add(proteintMatch);
        m_rsmId = rsmId;
        setPriority(Priority.NORMAL_1);
    }

    @Override
    public boolean fetchData() {
        return fetchData(m_proteinMatchList, m_rsmId);
    }
    public static boolean fetchData(List<DProteinMatch> proteinMatchList, Long rsmId) {

        int nbProteinMatches = proteinMatchList.size();
        
        if (! DatabaseDataManager.getDatabaseDataManager().isSeqDatabaseExists()) {
            // Seq Database does not exists : no data is available
            for (int i = 0; i < nbProteinMatches; i++) {
                DProteinMatch proteinMatch = proteinMatchList.get(i);
                proteinMatch.setDBioSequence(null);
            }
            return true;
        }
        
        
        ArrayList<String> values = new ArrayList<>(nbProteinMatches);
        for (int i = 0; i < nbProteinMatches; i++) {
            String accession = proteinMatchList.get(i).getAccession();
            values.add(accession);
        }

        Map<String, BioSequenceProvider.SEDbIdentifierRelated> result = BioSequenceProvider.findSEDbIdentRelatedData(values);

        for (int i = 0; i < nbProteinMatches; i++) {
            DProteinMatch proteinMatch = proteinMatchList.get(i);

            BioSequenceProvider.SEDbIdentifierRelated relatedObjects = result.get(proteinMatch.getAccession());
            if(relatedObjects == null || relatedObjects.getBioSequenceWrappers() == null || relatedObjects.getBioSequenceWrappers().isEmpty()){
                proteinMatch.setDBioSequence(null);
                continue;
            }
            
            List<BioSequenceWrapper> bioSequenceWrapperList = relatedObjects.getBioSequenceWrappers();

            PeptideSet peptideSet = null;
            if (rsmId != null) {
                peptideSet = proteinMatch.getPeptideSet(rsmId);
            }


            DPeptideInstance[] peptideInstances = (peptideSet == null) ? null : peptideSet.getTransientDPeptideInstances();
            if (peptideInstances == null) {
                // we can not check with peptides, we return the first biosequence
                BioSequenceWrapper biosequenceWrapperSelected = null;
                if (bioSequenceWrapperList.size() > 0) {
                    biosequenceWrapperSelected = bioSequenceWrapperList.get(0);
                }
                if (biosequenceWrapperSelected == null) {
                    proteinMatch.setDBioSequence(null);
                } else {
                    proteinMatch.setDBioSequence(new DBioSequence(biosequenceWrapperSelected.getSequence(), biosequenceWrapperSelected.getMass(), biosequenceWrapperSelected.getPI()));
                }
            } else {

                // we check the biosequence according to the peptides found for the ProteinMatch

                BioSequenceWrapper biosequenceWrapperSelected = null;
                int nb = bioSequenceWrapperList.size();
                for (int j = 0; j < nb; j++) {
                    biosequenceWrapperSelected = bioSequenceWrapperList.get(j);

                    String sequence = biosequenceWrapperSelected.getSequence();

                    // check for different peptides that the sequences math to the biosequence
                    int nbPeptides = peptideInstances.length;
                    for (int k = 0; k < nbPeptides; k++) {

                        DPeptideMatch peptideMatch = peptideInstances[k].getBestPeptideMatch();
                        String peptideSequence = peptideMatch.getPeptide().getSequence();
                        int start = peptideMatch.getSequenceMatch().getId().getStart();
                        int stop = peptideMatch.getSequenceMatch().getId().getStop();
                        if (stop > sequence.length()) {
                            biosequenceWrapperSelected = null;
                            break;
                        }

                        String subSequence = sequence.substring(start - 1, stop);
                        if (subSequence.compareTo(peptideSequence) != 0) {
                            biosequenceWrapperSelected = null;
                            break;
                        }
                    }
                    if (biosequenceWrapperSelected != null) {
                        break;
                    }
                }

                if (biosequenceWrapperSelected == null) {
                    proteinMatch.setDBioSequence(null);
                } else {
                    proteinMatch.setDBioSequence(new DBioSequence(biosequenceWrapperSelected.getSequence(), biosequenceWrapperSelected.getMass(), biosequenceWrapperSelected.getPI()));
                }
            }



        }

        return true;

    }

    @Override
    public boolean needToFetch() {
        return true;
    }
}
