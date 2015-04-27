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
 * @author JM235353
 */
public class SpectralCountTask extends AbstractServiceTask {

    private DDataset m_refDataset = null;
    private List<DDataset> m_rsmDataset = null;
    private List<DDataset> m_rsmWeightDataset = null;
    private Long[] m_quantiDatasetId = null;
    private String[] m_spCountJSONResult = null;
    private String m_dsName = null;
    private String m_dsDescr = null;

    public SpectralCountTask(AbstractServiceCallback callback, DDataset refDataset, List<DDataset> rsmDataset,List<DDataset> rsmWeightDataset, String dsName, String dsDescr, Long[] quantiDatasetId, String[] spectralCountResultList) {
        super(callback, false /*
                 * asynchronous
                 */, new TaskInfo("Spectral Count on " + refDataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_refDataset = refDataset;
        m_rsmDataset = rsmDataset;
        m_rsmWeightDataset = rsmWeightDataset;
        m_quantiDatasetId = quantiDatasetId;
        m_spCountJSONResult = spectralCountResultList;
        m_dsName = dsName; 
        m_dsDescr = dsDescr;
        if(m_dsName == null  || m_dsName.isEmpty()) {
            m_dsName = m_refDataset.getName() + " Spectral Count";
        }
        
        if(m_dsDescr == null  || m_dsDescr.isEmpty()) {
            m_dsDescr = m_dsName;
        }
       setWsVersion("0.2");
    }

    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();

            request.setId(m_id);
            request.setMethod("run_job");


            Map<String, Object> params = new HashMap<>();
            params.put("name",m_dsName);
            params.put("description", m_dsDescr);
            params.put("project_id", m_refDataset.getProject().getId());
            params.put("ref_rsm_id", m_refDataset.getResultSummaryId());
            params.put("ref_ds_id", m_refDataset.getId());
            List<Long> weightRefRSMIds = new ArrayList<>();
            for(DDataset ddset : m_rsmWeightDataset){
                weightRefRSMIds.add(ddset.getResultSummaryId());
            }
            params.put("peptide_ref_rsm_ids", weightRefRSMIds);

            // experimental_design
            Map<String, Object> experimentalDesignParams = new HashMap<>();


            List sampleNumbers = new ArrayList();
            List biologicalSampleList = new ArrayList();
            List quantChanneList = new ArrayList();
            int number = 1;
            Iterator<DDataset> itDataset = m_rsmDataset.iterator();
            while (itDataset.hasNext()) {
                DDataset d = itDataset.next();
                String name = d.getName();

                Map<String, Object> biologicalSampleParams = new HashMap<>();
                biologicalSampleParams.put("number", Integer.valueOf(number));
                biologicalSampleParams.put("name", name);

                biologicalSampleList.add(biologicalSampleParams);

                Map<String, Object> quantChannelParams = new HashMap<>();
                quantChannelParams.put("number", Integer.valueOf(number));
                quantChannelParams.put("sample_number", Integer.valueOf(number));
                quantChannelParams.put("ident_result_summary_id", d.getResultSummaryId());

                quantChanneList.add(quantChannelParams);

                sampleNumbers.add(Integer.valueOf(number));

                number++;
            }
            experimentalDesignParams.put("biological_samples", biologicalSampleList);

            List biologicalGroupList = new ArrayList();
            Map<String, Object> biologicalGroupParams = new HashMap<>();
            biologicalGroupParams.put("number", Integer.valueOf(0));
            biologicalGroupParams.put("name", m_refDataset.getName());
            biologicalGroupParams.put("sample_numbers", sampleNumbers);
            biologicalGroupList.add(biologicalGroupParams);
            experimentalDesignParams.put("biological_groups", biologicalGroupList);


            List masterQuantChannelsList = new ArrayList();
            Map<String, Object> masterQuantChannelParams = new HashMap<>();
            masterQuantChannelParams.put("number", 0);
            masterQuantChannelParams.put("name", m_refDataset.getName() + " Spectral Count");
            masterQuantChannelParams.put("quant_channels", quantChanneList);
            masterQuantChannelsList.add(masterQuantChannelParams);
            experimentalDesignParams.put("master_quant_channels", masterQuantChannelsList);

            params.put("experimental_design", experimentalDesignParams);


            request.setParameters(params);
            //m_loggerProline.debug("Will postRequest with params  project_id "+m_refDataset.getProject().getId()+" ; ref_result_summary_id "+m_refDataset.getResultSummaryId()+" ; compute_result_summary_ids "+m_resultSummaryIds);
            HttpResponse response = postRequest("dps.msq/quantifysc/" + request.getMethod() + getIdString()+getWSVersionString(), request);

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

            HttpResponse response = postRequest("dps.msq/quantifysc/" + request.getMethod() + getIdString(), request);

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

                return ServiceState.STATE_FAILED; // should not happen !
            }

            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");

            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");
                // key not used : "duration", "progression" JPM.TODO

                if (success == null) {

                    String message = (String) resultMap.get("message");
                    if ((message != null) && message.startsWith("Running")) {
                        getTaskInfo().setRunning(false);
                    }

                    return ServiceState.STATE_WAITING;
                }

                if (success) {

                    BigDecimal duration = (BigDecimal) resultMap.get("duration");
                    if (duration != null) {
                        getTaskInfo().setDuration(duration.longValue());
                    }

                    Map returnedValues = (Map) resultMap.get("result");
                    if ((returnedValues == null) || (returnedValues.isEmpty())) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
                        return ServiceState.STATE_FAILED;
                    }

//                    ArrayMap returnedValuesMap = (ArrayMap) returnedValues.get(0);

                    // retrieve Quanti Dataset ID
                    BigDecimal quantiDatasetIdBD = (BigDecimal) returnedValues.get("quant_dataset_id");
                    if (quantiDatasetIdBD == null) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No returned Quanti Dataset Id");
                        return ServiceState.STATE_FAILED;
                    }
                    m_quantiDatasetId[0] = new Long(quantiDatasetIdBD.longValue());

                    //retrieve SC Values as JSON String 
                    String scValues = (String) returnedValues.get("spectral_count_result");
                    if (scValues == null) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No Spectral Count returned.");
                        return ServiceState.STATE_FAILED;
                    }
                    m_spCountJSONResult[0] = scValues;

                    return ServiceState.STATE_DONE;
                } else {
                    String errorMessage = (String) resultMap.get("message");
                    if (errorMessage == null) {
                        errorMessage = "";
                    } else {
                        m_taskError = new TaskError(errorMessage);
                    }
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed " + errorMessage);
                    return ServiceState.STATE_FAILED;
                }

            }



        } catch (Exception e) {
            m_taskError = new TaskError(e);
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            return ServiceState.STATE_FAILED; // should not happen !
        }

        return ServiceState.STATE_WAITING;

    }

}
