package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.DatabaseRunsTask;
import java.math.BigDecimal;
import java.util.*;

/**
 * XIC Quantitation Task
 * @author vd225637
 */
public class RunXICTask extends AbstractServiceTask {

    private Long[] m_xicQuantiResult = null;
    private HashMap<String, ArrayList<String>> m_samplesByGroup;
    private HashMap<String, ArrayList<String>> m_samplesAnalysisBySample;
    private HashMap<String, Long> m_rsmIdBySampleAnalysis;
    private String m_quantiDSName;
    private Long m_pId;
    private Map<String,Object> m_quantParams;
    private Map<String,Object> m_expDesignParams;
    private boolean useExistingJSON =false;
    
    public RunXICTask(AbstractServiceCallback callback, Long projectId,  String quantDSName,  Map<String,Object> quantParams, Map<String,Object> expDesignParams, Long[] retValue  ) {
        super(callback, false /** asynchronous */, new   TaskInfo("Run XIC Quantitation for "+quantDSName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH ));
        m_xicQuantiResult = retValue;     
        m_expDesignParams = expDesignParams;
        m_quantiDSName = quantDSName;
        m_pId= projectId;
        m_quantParams = quantParams;
        useExistingJSON= true;
    }
        
    public RunXICTask(AbstractServiceCallback callback, Long projectId,  String quantDSName,  Map<String,Object> quantParams, HashMap<String, ArrayList<String>> samplesByGroup, HashMap<String, ArrayList<String>> samplesAnalysisBySample,HashMap<String, Long> rsmIdBySampleAnalysis, Long[] retValue  ) {
        super(callback, false /** asynchronous */, new TaskInfo("Run XIC Quantitation for "+quantDSName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH ));
        m_xicQuantiResult = retValue;     
        m_samplesByGroup = samplesByGroup;
        m_samplesAnalysisBySample = samplesAnalysisBySample;
        m_rsmIdBySampleAnalysis = rsmIdBySampleAnalysis;
        m_quantiDSName = quantDSName;
        m_pId= projectId;
        m_quantParams = quantParams;
    }

    @Override
    public boolean askService() {
        //Create JSON for XIC Quanttitation service         
        try {
            
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();

            request.setId(m_id);
            request.setMethod("run_job");

            //Create JSON parameters Strings 

            Map<String, Object> params = new HashMap<>();
            
            //-- Global PARAMS
            params.put("name",m_quantiDSName);
            params.put("description", m_quantiDSName);
            params.put("project_id", m_pId);
            params.put("method_id", 1); //TODO Attention en dure !!! A lire la methode type = "label_free" & abundance_unit = "feature_intensity"

            if(useExistingJSON){
                params.put("experimental_design", m_expDesignParams);
            }else {
                            //Get Run Ids for specified RSMs
                Map<Long, Long> runIdByRsmId = new HashMap<>();
                for(Long rsmId : m_rsmIdBySampleAnalysis.values()){
                    ArrayList<Long> returnedRunId = new ArrayList<> ();
                    DatabaseRunsTask loadRunIdsTask = new DatabaseRunsTask(null);
                    loadRunIdsTask.initLoadRunIdForRsm(m_pId, rsmId, returnedRunId);
                    loadRunIdsTask.fetchData();
                    if(returnedRunId.size() >0)
                        runIdByRsmId.put(rsmId, returnedRunId.get(0));
                    else
                        runIdByRsmId.put(rsmId, -1l);
                }
                
                //-- experimental_design Params
                Map<String, Object> experimentalDesignParams = new HashMap<>();

                Map<Integer, String> splByNbr = new HashMap<>();
                Map<String, Integer> splNbrByName = new HashMap<>();            
                Iterator<String> groupsIt = m_samplesByGroup.keySet().iterator();
                int ratioNumeratorGrp = 1;
                int ratioDenominatorGrp =1;
                int grpNumber = 1;
                int splNumber = 1;
                int splAnalysisNumber = 1;
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

                if(grpNumber>2)
                    ratioDenominatorGrp = 2; //VD TODO :  Comment gerer les ratois ?

                Map<String, Object> ratioParams = new HashMap<>();
                ratioParams.put("number", 1);
                ratioParams.put("numerator_group_number", ratioNumeratorGrp);
                ratioParams.put("denominator_group_number",ratioDenominatorGrp);
                List ratioParamsList = new ArrayList();
                ratioParamsList.add(ratioParams);

                Map<String, Object> groupSetupParams = new HashMap<>();
                groupSetupParams.put("number", 1);
                groupSetupParams.put("name", m_quantiDSName);
                groupSetupParams.put("biological_groups", biologicalGroupList);
                groupSetupParams.put("ratio_definitions", ratioParamsList);

                ArrayList groupSetupParamsList = new ArrayList();
                groupSetupParamsList.add(groupSetupParams);
                experimentalDesignParams.put("group_setups",groupSetupParamsList );            

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
                        quantChannelParams.put("run_id", runIdByRsmId.get(m_rsmIdBySampleAnalysis.get(nextSplAnalysis)));
                        quantChanneList.add(quantChannelParams);
                    }

                } // End go through samples
                experimentalDesignParams.put("biological_samples", biologicalSampleList);

                List masterQuantChannelsList = new ArrayList();
                Map<String, Object> masterQuantChannelParams = new HashMap<>();
                masterQuantChannelParams.put("number", 1);
                masterQuantChannelParams.put("name", m_quantiDSName);
                masterQuantChannelParams.put("quant_channels", quantChanneList);
                masterQuantChannelsList.add(masterQuantChannelParams);
                experimentalDesignParams.put("master_quant_channels", masterQuantChannelsList);
                
                params.put("experimental_design", experimentalDesignParams);
            }
            

            
            //-- quanti Params
            /*"quantitation_config": {
		"extraction_params": {
			"moz_tol": "5",
			"moz_tol_unit": "PPM"
		},
		"clustering_params": {
			"moz_tol": "5",
			"moz_tol_unit": "PPM",
			"time_tol": "15",
			"time_computation": "MOST_INTENSE",
			"intensity_computation": "MOST_INTENSE"
		},
		"aln_method_name": "ITERATIVE",
		"aln_params": {
			"mass_interval": "20000",
			"max_iterations": "3",
			"smoothing_method_name": "TIME_WINDOW",
			"smoothing_params": {
				"window_size": "200",
				"window_overlap": "20",
				"min_window_landmarks": "50"
			},
			"ft_mapping_params": {
				"moz_tol": "5",
				"moz_tol_unit": "PPM",
				"time_tol": "600"
			}
		},
		"ft_filter": {
			"name": "INTENSITY",
			"operator": "GT",
			"value": "0"
		},
		"ft_mapping_params": {
			"moz_tol": "10",
			"moz_tol_unit": "PPM",
			"time_tol": "120"
		},
		"normalization_method": "MEDIAN_RATIO"
	}*/
           //Should already be structured like described
            params.put("quantitation_config", m_quantParams);
            
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
