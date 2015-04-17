package fr.proline.studio.dpm.data;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multiple RSMs Weighted SpectralCount result Data. 
 * 
 * @author VD225637
 */
public class SpectralCountResultData {
    
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");

    
    /*
     * Tags used to parse spectral count result from JSON string
     */
        final String rootPropName = "\"spectral_count_result\"";
        final String rsmIDPropName = "\"rsm_id\"";
        final String protSCsListPropName = "\"proteins_spectral_counts\"";
        final String protACPropName = "\"protein_accession\"";
        final String bscPropName = "\"bsc\"";
        final String sscPropName = "\"ssc\"";
        final String wscPropName = "\"wsc\"";
        final String protMatchIdPropName = "\"prot_match_id\"";
        final String protSetIdPropName  = "\"prot_set_id\"";
        final String protMatchStatusPropName ="\"prot_status\"";
        final String pepNbrPropName ="\"pep_nbr\"";
        
         private Project m_project = null;
         
        /**
         * Maps RSMids to SpectralCount values per ProteinNames
         */
        private Map<Long, Map<String, SpectralCountsStruct>> scsByProtByRSMId;
        
        /* reference Dataset used for SpectralCount */
        
        private DDataset m_refDataset;
        
        /*Ordered list of Compared RSM's id */
        private ArrayList<Long> m_rsmIds= null;
        
        /*Ordered list of Compared Dataset's  names */
        private List<String> m_dsNames = null;

        public SpectralCountResultData(DDataset refDataset, List<DDataset> datasets, String spectralCountResult, Project prj) {
            m_refDataset = refDataset;
            m_project = prj;
            m_rsmIds = new ArrayList<>();
            m_dsNames = new ArrayList<>();
            for(DDataset ddset : datasets){
                m_rsmIds.add(ddset.getResultSummaryId());
                m_dsNames.add(ddset.getName());
            }            
            scsByProtByRSMId = new HashMap<>();
            try {
                initSpectraCountData(spectralCountResult);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        
        public SpectralCountResultData(String spectralCountResult, Project prj) {
         
            m_project = prj;
//            loadData(refDatasetId);
            scsByProtByRSMId = new HashMap<>();
            try {
                initSpectraCountData(spectralCountResult);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            //GET INFO  FROM MAP 
        }

        public String getDSReferenceName() {
            if(m_refDataset != null)
                return m_refDataset.getName();
            return "Unknown";
        }

        public List<String> getComputedSCDatasetNames() {
            return m_dsNames;
        }
        
        public List<Long> getComputedSCRSMIds() {
            return m_rsmIds;
        }

        public Map<String, SpectralCountsStruct> getRsmSCResult(Long rsmId) {
            return scsByProtByRSMId.get(rsmId);
        }
        
        /**
         * Load data relative to experimental design : RSM and DS from wich SC was run
         * This should be done if SpectralCountResultData is not created from a 
         * compute SC action but a read back SC data action.
         * @param refDatasetId : ID of the dataset to load
         * @param callback : method to call back after data is loaded
         */
        public void loadData(Long refDatasetId, final AbstractDatabaseCallback callback){
            final ArrayList<DDataset> readDatasetList = new ArrayList<>();
            final ArrayList<String> readDatasetNames = new ArrayList<>();
             
            AbstractDatabaseCallback innerCallback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if(readDatasetList.size() >0)
                        m_refDataset = readDatasetList.get(0);
                    m_dsNames = readDatasetNames;
                    callback.run(success, taskId, subTask, finished);       

                }
            };
            
            DatabaseDataSetTask task = new DatabaseDataSetTask(innerCallback);
            task.initLoadDatasetAndRSMInfo(refDatasetId, m_rsmIds, readDatasetList, readDatasetNames, m_project);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }

        /** 
         * Parse SC Result to created formatted data m_scResult is formatted as
         * : "{"spectral_count_result":{[ { "rsm_id":Long,
         * "proteins_spectral_counts":[ {
         * "protein_accession"=Acc,"prot_match_id"=Long, "prot_set_id"=Long, "prot_status"=String, "bsc"=Float,"ssc"=Float,"wsc"=Float}, {...} ]
         * }, { "rsm_id"... } ]}}"
         *
         */
        private void initSpectraCountData(String scResult) {
            //first xx char are constant
            int firstRSMEntryIndex = scResult.indexOf("{" + rsmIDPropName);            
            String parsingSC = scResult.substring(firstRSMEntryIndex);
            boolean fillLists = m_rsmIds == null;
            if(fillLists){
                m_rsmIds = new ArrayList<>();                
            }
            String[] rsmEntries = parsingSC.split("\\{" + rsmIDPropName);
            for (String rsmEntry : rsmEntries) { //{"rsm_id":Long,"proteins_spectral_counts":[...
                if (rsmEntry.isEmpty()) {
                    continue;
                }
                String rsmSCResult = rsmEntry.substring(rsmEntry.indexOf(":") + 1);
                //ToDO : Verify rsmId belongs to m_datasetRSMs ?
                Long rsmId = Long.parseLong(rsmSCResult.substring(0, rsmSCResult.indexOf(",")).trim());
                if(fillLists){
                    m_rsmIds.add(rsmId);                 
                }
                Map<String, SpectralCountsStruct> rsmSCRst = parseRsmSC(rsmId, rsmSCResult.substring(rsmSCResult.indexOf(protSCsListPropName)));
                scsByProtByRSMId.put(rsmId, rsmSCRst);
            }
        }

        /**
         * Parse one RSM Sc entry
         *
         *
         * "proteins_spectral_counts":[ {
         * "protein_accession"=Acc,"prot_match_id"=Long, "prot_set_id"=Long, "prot_status"=String,"bsc"=Float,"ssc"=Float,"wsc"=Float}, {...} ]
         * },
         *
         * @return Map of spectralCounts for each Protein Matches
         */
        private Map<String, SpectralCountsStruct> parseRsmSC(Long rsmId, String rsmsSCResult) {

            //"proteins_spectral_counts":[{"protein_accession"=MyProt,"bsc"=123.6,"ssc"=45.6,"wsc"=55.5}, {"protein_accession"=OtherProt,"bsc"=17.2,"ssc"=2.6,"wsc"=1.5} ]
            Map<String, SpectralCountsStruct> scByProtAcc = new HashMap<>();

            //Remove "proteins_spectral_counts":[
            String protEntries = rsmsSCResult.substring(rsmsSCResult.indexOf("[") + 1);
            protEntries = protEntries.substring(0, protEntries.indexOf("]"));

            if (protEntries.isEmpty()) { // case proteins_spectral_counts":[]
                m_loggerProline.warn(" Spectral Count result, no accession values for RSM :   " + rsmId+" ("+rsmsSCResult+")");
                return scByProtAcc;
            }
            String[] protAccEntries = protEntries.split("}"); //Each ProtAcc entry
            for (String protAcc : protAccEntries) {
                //For each protein ...            
                String[] protAccPropertiesEntries = protAcc.split(","); //Get properties list : Acc / bsc / ssc / wsc 
                String protAccStr = null;
                Float bsc = null;
                Float ssc = null;
                Float wsc = null;
                String protMatchStatus = null;
                Integer peptideNumber = null;
                for (String protProperty : protAccPropertiesEntries) { //Should create 2 entry : key -> value 
                    String[] propKeyValues = protProperty.split("="); //split prop key / value 
                    if (propKeyValues[0].contains(protACPropName)) {
                        protAccStr = propKeyValues[1];
                    } else if (propKeyValues[0].contains(bscPropName)) {
                        bsc = Float.valueOf(propKeyValues[1]);
                    } else if (propKeyValues[0].contains(sscPropName)) {
                        ssc = Float.valueOf(propKeyValues[1]);
                    } else if (propKeyValues[0].contains(wscPropName)) {
                        wsc = Float.valueOf(propKeyValues[1]);
                    } else if (propKeyValues[0].contains(protMatchStatusPropName)) {
                        protMatchStatus = propKeyValues[1];
                    } else if (propKeyValues[0].contains(pepNbrPropName)) {
                        peptideNumber = Integer.valueOf(propKeyValues[1]);
                    }
                }
                if (bsc == null || ssc == null || wsc == null || protAccStr == null || peptideNumber==null ) {
                    throw new IllegalArgumentException("Invalid Spectral Count result. Value missing : " + protAcc);
                }
                scByProtAcc.put(protAccStr, new SpectralCountsStruct(bsc, ssc, wsc,protMatchStatus, peptideNumber));
            }

            return scByProtAcc;

        }
       
        
    public static class SpectralCountsStruct {

        private Float m_basicSC;
        private Float m_specificSC;
        private Float m_weightedSC;
        private String m_pmStatus;
        private Integer m_peptideNumber;

        public SpectralCountsStruct(Float bsc, Float ssc, Float wsc, String pmStatus, Integer peptideNumber) {
            this.m_basicSC = bsc;
            this.m_specificSC = ssc;
            this.m_weightedSC = wsc;
            this.m_pmStatus = pmStatus;
            this.m_peptideNumber = peptideNumber;
        }

        public Float getBsc() {
            return m_basicSC;
        }

        public Float getSsc() {
            return m_specificSC;
        }

        public Float getWsc() {
            return m_weightedSC;
        }
        
        public String getProtMatchStatus() {
            return m_pmStatus;
        }
        
        public Integer getPeptideNumber() {
            return m_peptideNumber;
        }
        
    }
}
