/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.math.BigDecimal;
import java.util.*;

/**
 *
 * @author vd225637
 */
public class RunXICTask extends AbstractServiceTask {

    private Long[] m_xicQuantiResult = null;
    private HashMap<String, ArrayList<String>> m_samplesByGroup;
    private HashMap<String, ArrayList<String>> m_samplesAnalysisBySample;
    private HashMap<String, Long> m_rsmIdBySampleAnalysis;
    private DDataset m_quantiDS;
            
    public RunXICTask(AbstractServiceCallback callback,  DDataset quantiDS, HashMap<String, ArrayList<String>> samplesByGroup,HashMap<String, ArrayList<String>> samplesAnalysisBySample,HashMap<String, Long> rsmIdBySampleAnalysis, Long[] retValue  ) {
        super(callback, false /** asynchronous */, new TaskInfo("Run XIC Quantitation for ", true, TASK_LIST_INFO ));
        m_xicQuantiResult = retValue;     
        m_samplesByGroup = samplesByGroup;
        m_samplesAnalysisBySample = samplesAnalysisBySample;
        m_rsmIdBySampleAnalysis = rsmIdBySampleAnalysis;
        m_quantiDS= quantiDS;
    }

    @Override
    public boolean askService() {
        //Create JSON for XIC Quanttitation service         
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();

            request.setId(m_id);
            request.setMethod("run_job");


            Map<String, Object> params = new HashMap<>();
            params.put("name",m_quantiDS.getName());
            params.put("description", m_quantiDS.getName());
            params.put("project_id", m_quantiDS.getProject().getId());
            params.put("method_id", 1); //TODO Attention en dure !!! A lire la methode type = "label_free" & abundance_unit = "feature_intensity"

            // experimental_design
            Map<String, Object> experimentalDesignParams = new HashMap<>();
            
            Map<Integer, String> splByNbr = new HashMap<>();
            Map<String, Integer> splNbrByName = new HashMap<>();            
            Iterator<String> groupsIt = m_samplesByGroup.keySet().iterator();
            int ratioNumeratorGrp = 0;
            int ratioDenominatorGrp = 0;
            int grpNumber = 0;
            int splNumber = 0;
            int splAnalysisNumber = 0;
            List biologicalGroupList = new ArrayList();
            
            while(groupsIt.hasNext()){
                
                String grpName = groupsIt.next();
                List<String> grpSamples= m_samplesByGroup.get(grpName);
                List<Integer> splNumbers = new ArrayList(grpSamples.size());
                for(int i=0; i<grpSamples.size(); i++){
                    String splName = grpSamples.get(i);
 
                    splByNbr.put(splNumber, splName);
                    splNbrByName.put(splName, splNumber);
                    splNumbers.add(splNumber++);
                }
                
                Map<String, Object> biologicalGroupParams = new HashMap<>();
                biologicalGroupParams.put("number", grpNumber++);
                biologicalGroupParams.put("name", grpName);
                biologicalGroupParams.put("sample_numbers", splNumbers);
                biologicalGroupList.add(biologicalGroupParams);

            }// End go through groups
            
            if(grpNumber>1)
                ratioDenominatorGrp = 1; //VD TODO :  Comment gerer les ratois ?
            
            Map<String, Object> ratioParams = new HashMap<>();
            ratioParams.put("number", Integer.valueOf(0));
            ratioParams.put("numerator_group_number", ratioNumeratorGrp);
            ratioParams.put("denominator_group_number",ratioDenominatorGrp);
            
            Map<String, Object> groupSetupParams = new HashMap<>();
            groupSetupParams.put("number", Integer.valueOf(0));
            groupSetupParams.put("name", m_quantiDS.getName());
            groupSetupParams.put("biological_groups", biologicalGroupList);
            groupSetupParams.put("ratio_definitions", ratioParams);

            experimentalDesignParams.put("group_setups", groupSetupParams);            
            
            List biologicalSampleList = new ArrayList();
            List quantChanneList = new ArrayList();
            
            Iterator<String> samplesIt =  m_samplesAnalysisBySample.keySet().iterator();
            while(samplesIt.hasNext()){
                String nextSpl = samplesIt.next();
                Integer splNbr = splNbrByName.get(nextSpl);
                
                Map<String, Object> biologicalSampleParams = new HashMap<>();
                biologicalSampleParams.put("number", splNbr);
                biologicalSampleParams.put("name", nextSpl);

                biologicalSampleList.add(biologicalSampleParams);
                    
                List<String> splAnalysis = m_samplesAnalysisBySample.get(nextSpl);
                for(int i =0; i<splAnalysis.size(); i++){
                    String nextSplAnalysis = splAnalysis.get(i);
                    Map<String, Object> quantChannelParams = new HashMap<>();
                    quantChannelParams.put("number", splAnalysisNumber++);
                    quantChannelParams.put("sample_number", splNbr);
                    quantChannelParams.put("ident_result_summary_id", m_rsmIdBySampleAnalysis.get(nextSplAnalysis));
                    quantChanneList.add(quantChannelParams);
                }

            } // End go through samples
            experimentalDesignParams.put("biological_samples", biologicalSampleList);

            List masterQuantChannelsList = new ArrayList();
            Map<String, Object> masterQuantChannelParams = new HashMap<>();
            masterQuantChannelParams.put("number", 0);
            masterQuantChannelParams.put("name", m_quantiDS.getName());
            masterQuantChannelParams.put("quant_channels", quantChanneList);
            masterQuantChannelsList.add(masterQuantChannelParams);
            experimentalDesignParams.put("master_quant_channels", masterQuantChannelsList);

            params.put("experimental_design", experimentalDesignParams);


            request.setParameters(params);
            //m_loggerProline.debug("Will postRequest with params  project_id "+m_refDataset.getProject().getId()+" ; ref_result_summary_id "+m_refDataset.getResultSummaryId()+" ; compute_result_summary_ids "+m_resultSummaryIds);
            HttpResponse response = postRequest("dps.msq/quantify/" + request.getMethod() + getIdString(), request);

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
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed : " + m_taskError.toString());
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

            HttpResponse response = postRequest("dps.msq/quantify/"+request.getMethod()+getIdString(), request);

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
                    BigDecimal resultSummaryIdBD = (BigDecimal) resultMap.get("result");
                    if (resultSummaryIdBD == null) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No returned Quantitation dataset Id");
                        return ServiceState.STATE_FAILED;
                    }

                    m_xicQuantiResult[0] = new Long(resultSummaryIdBD.longValue());

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
    
}
