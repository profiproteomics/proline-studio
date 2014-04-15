package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Task to start the validation service
 * @author jm235353
 * 
 * REPLACED By SpectralCountTask
 */
public class ComputeSCTask extends AbstractServiceTask {

    private Dataset m_refDataset = null;    
    List<Long> m_resultSummaryIds = null;
    
    String[] m_spCountJSONResult;
    
    public ComputeSCTask(AbstractServiceCallback callback, Dataset refDataset, List<Long> resultSummaryIds,  String[] spectralCountResultList) {
        super(callback, false /*asynchronous*/, new TaskInfo("Compute Spectral Count based on "+refDataset.getName(), true, TASK_LIST_INFO));
        m_refDataset = refDataset;
        m_resultSummaryIds = resultSummaryIds;
        m_spCountJSONResult = spectralCountResultList;
    }
    
     
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(m_id);
            request.setMethod("run_job");
            
            
            Map<String, Object> params = new HashMap<>();
	    params.put("project_id", m_refDataset.getProject().getId());  
            params.put("ref_result_summary_id", m_refDataset.getResultSummaryId() );
            params.put("compute_result_summary_ids", m_resultSummaryIds );                         
            request.setParameters(params);
            m_loggerProline.debug("Will postRequest with params  project_id "+m_refDataset.getProject().getId()+" ; ref_result_summary_id "+m_refDataset.getResultSummaryId()+" ; compute_result_summary_ids "+m_resultSummaryIds);
            HttpResponse response = postRequest("dps.msq/compute_relative_sc/"+request.getMethod()+getIdString(), request);

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {
                String message = (String) errorMap.get("message");

                if (message != null) {
                    m_taskError = new TaskError(message);
                }
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    if (m_taskError == null) {
                        m_taskError = new TaskError(data);
                    } else {
                        m_taskError.setErrorText(data);
                    }
                }
                
                if (m_taskError != null) {
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed : "+m_taskError.toString());
                }
                
                return false;
            }
            
            
            BigDecimal jobId = (BigDecimal) jsonResult.get("result");
            if (jobId != null) {
                m_id = jobId.intValue();
            } else {
                m_loggerProline.error(getClass().getSimpleName() + " failed : id not defined");
            }



        } catch (Exception e) {
            m_taskError = new TaskError(e.getMessage());
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            return false;
        }

        return true;
    }
    
      @Override
    public ServiceState getServiceState() {
        
        try {
                
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(m_idIncrement++);
            request.setMethod("get_job_status");
            
            Map<String, Object> params = new HashMap<>();
	    params.put("job_id", m_id);

            request.setParameters(params);

            HttpResponse response = postRequest("dps.msq/compute_relative_sc/"+request.getMethod()+getIdString(), request);

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {

                String message = (String) errorMap.get("message");

                if (message != null) {
                    m_taskError = new TaskError(message);
                }
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    if (m_taskError == null) {
                        m_taskError = new TaskError(data);
                    } else {
                        m_taskError.setErrorText(data);
                    }
                }
                
                if (m_taskError != null) {
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed : "+m_taskError.toString());
                }
                
                return ServiceState.STATE_FAILED; // should not happen !
            }
            
            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");
        
            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");  
                
                if (success == null) {
                    
                    String message = (String) resultMap.get("message");
                    if ((message!=null) && message.startsWith("Running")) {
                        getTaskInfo().setRunning(false);
                    }
                    
                    return ServiceState.STATE_WAITING;
                }
                
                if (success) {

                    BigDecimal duration = (BigDecimal) resultMap.get("duration");
                    if (duration != null) {
                        getTaskInfo().setDuration(duration.longValue());
                    }
                    
                    // retrieve resultSummary id
                    String resultSummariesSC = (String) resultMap.get("result");
                    if (resultSummariesSC == null || resultSummariesSC.isEmpty()) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No Spectral Count returned.");
                        return ServiceState.STATE_FAILED;
                    }

                    m_spCountJSONResult[0] = resultSummariesSC;

                    return ServiceState.STATE_DONE;
                } else {
                    m_taskError = new TaskError((String) resultMap.get("message"));
                    
                    
                    if (m_taskError != null) {
                        m_loggerWebcore.error(getClass().getSimpleName() + " failed : " + m_taskError.toString());
                    }
                
                    
                    return ServiceState.STATE_FAILED;
                }
                
            }

            

        } catch (Exception e) {
            m_taskError = new TaskError(e.getMessage());
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            return ServiceState.STATE_FAILED; // should not happen !
        }
               
        return ServiceState.STATE_WAITING;
        
    }


//public static class WSCResultData {
//    
//    final String rootPropName  = "\"SpectralCountResult\"";
//    final String rsmIDPropName  = "\"rsm_id\"";
//    final String protSCsListPropName = "\"proteins_spectral_counts\"";
//    final String protACPropName ="\"protein_accession\"";
//    final String bscPropName  = "\"bsc\"";
//    final String sscPropName  = "\"ssc\"";
//    final String wscPropName  = "\"wsc\"";
//            
//    private Map<Long, Map<String, SpectralCountsStruct>> scsByProtByRSMId;
//    private Dataset m_refDS;
//    private List<Dataset> m_datasetRSMs;
//            
//    public WSCResultData(Dataset refDataset, List<Dataset> datasets,  String spectralCountResult) {
//        m_refDS = refDataset;
//        m_datasetRSMs = datasets;
//        scsByProtByRSMId = new HashMap<>();
//        try {
//            initData(spectralCountResult);
//        }catch(Exception e){
//            throw new IllegalArgumentException(e.getMessage());
//        }
//    } 
//    
//    public ResultSummary getRSMReference(){
//        return m_refDS.getTransientData().getResultSummary();
//    }
//    
//    public Dataset getDataSetReference(){
//        return m_refDS;
//    }
//    
//    public List<Dataset> getComputedSCDatasets (){
//        return m_datasetRSMs;
//    }
//    
//    public Map<String, SpectralCountsStruct> getRsmSCResult(Long rsmId){
//        return scsByProtByRSMId.get(rsmId);
//    }
//    
//    
//    /**
//     * Parse SC Result to created formatted data 
//     * m_scResult is formatted as :
//     * "{"SpectralCountResult":{[
//     * {
//     * "rsm_id":Long,
//     * "proteins_spectral_counts":[
//     * { "protein_accession"=Acc,"bsc"=Float,"ssc"=Float,"wsc"=Float},
//     * {...}
//     * ]
//     * },
//     * {
//     * "rsm_id"...
//     * }
//     * ]}}"
//     *    
//     */
//    private void initData(String scResult) {
//        //first 27 char are constant
//        String parsingSC = scResult.substring(27);
//        
//        String[] rsmEntries = parsingSC.split("\\{"+rsmIDPropName);
//        for(String rsmEntry : rsmEntries){ //{"rsm_id":Long,"proteins_spectral_counts":[...
//            if(rsmEntry.isEmpty())
//                continue;
//            String rsmSCResult=rsmEntry.substring(rsmEntry.indexOf(":")+1);
//                    //ToDO : Verify rsmId belongs to m_datasetRSMs ?
//            Long rsmId = Long.parseLong(rsmSCResult.substring(0, rsmSCResult.indexOf(",")).trim()); 
//                    
//            Map<String, SpectralCountsStruct> rsmSCRst =  parseRsmSC(rsmSCResult.substring(rsmSCResult.indexOf(protSCsListPropName)));
//            scsByProtByRSMId.put(rsmId, rsmSCRst);
//        }
//    }
//    
//    /**
//     * Parse one RSM Sc entry
//     *
//     * 
//     * "proteins_spectral_counts":[
//     * { "protein_accession"=Acc,"bsc"=Float,"ssc"=Float,"wsc"=Float},
//     * {...}
//     * ]
//     *},
//     * 
//     * @return Map of spectralCounts for each Protein Matches
//     */
//     
//    private Map<String, SpectralCountsStruct> parseRsmSC(String rsmsSCResult){
//        m_loggerProline.debug(" parseRsmSC :   "+rsmsSCResult);
//                
//        //"proteins_spectral_counts":[{"protein_accession"=MyProt,"bsc"=123.6,"ssc"=45.6,"wsc"=55.5}, {"protein_accession"=OtherProt,"bsc"=17.2,"ssc"=2.6,"wsc"=1.5} ]
//        Map<String, SpectralCountsStruct> scByProtAcc = new HashMap<>();
//
//        //Remove "proteins_spectral_counts":[
//        String protEntries =rsmsSCResult.substring(rsmsSCResult.indexOf("[")+1);
//        protEntries = protEntries.substring(0,protEntries.indexOf("]"));
//        
//        String[] protAccEntries = protEntries.split("}"); //Each ProtAcc entry
//        int protIndex = 0;
//        for(String protAcc : protAccEntries){        
//            //For each protein ...            
//            String[] protAccPropertiesEntries = protAcc.split(","); //Get properties list : Acc / bsc / ssc / wsc 
//            String protAccStr= null;
//            Float bsc = null;
//            Float ssc= null;
//            Float wsc= null;
//            for(String protProperty  : protAccPropertiesEntries){ //Should create 2 entry : key -> value 
//               String[] propKeyValues  = protProperty.split("="); //split prop key / value 
//               if(propKeyValues[0].contains(protACPropName))
//                   protAccStr = propKeyValues[1];
//               if(propKeyValues[0].contains(bscPropName))
//                   bsc = Float.valueOf(propKeyValues[1]);
//               if(propKeyValues[0].contains(sscPropName))
//                   ssc = Float.valueOf(propKeyValues[1]);
//               if(propKeyValues[0].contains(wscPropName))
//                   wsc = Float.valueOf(propKeyValues[1]);                   
//            }
//            if(bsc==null ||ssc ==null ||wsc == null||protAccStr==null)
//                throw new IllegalArgumentException("Invalid Spectral Count result. Value missing : "+protAcc);
//            scByProtAcc.put(protAccStr, new SpectralCountsStruct(bsc, ssc, wsc));
//            protIndex++;
//        }
//      
//        return scByProtAcc;
//        
//    }
//
//           
//}
 
// public static class SpectralCountsStruct{
//     Float m_basicSC;
//     Float m_specificSC;
//     Float m_weightedSC;
//     
//     public SpectralCountsStruct(Float bsc, Float ssc, Float wsc){
//         this.m_basicSC = bsc;
//         this.m_specificSC = ssc;
//         this.m_weightedSC = wsc;
//     }
//     
//     public Float getBsc(){return m_basicSC;}
//     
//     public Float getSsc(){return m_specificSC;}
//     
//     public Float getWsc(){return m_weightedSC;}
//     
// }
}