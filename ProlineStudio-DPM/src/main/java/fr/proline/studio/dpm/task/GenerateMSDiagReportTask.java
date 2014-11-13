package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;




 

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo; 

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

/**

 *
 * @author AW
 */
public class GenerateMSDiagReportTask extends AbstractServiceTask {
 

    private Long m_projectId;
    private Long m_resultSetId;
	public ArrayList<String> m_resultMessages ;
    
   // public ExportRSMTask(AbstractServiceCallback callback, DDataset dataset, boolean exportAllPSMs, String[] filePathInfo) {
   //     super(callback, false /** asynchronous */, new TaskInfo("Export Identification Summary " + dataset.getName(), true, TASK_LIST_INFO));
   
    //public GenerateMSDiagReportTask(AbstractServiceCallback callback, String datasetName, Long projectId, Long resultSetId, Long resultSummaryId) {
    public GenerateMSDiagReportTask(AbstractServiceCallback callback,  Long projectId, Long resultSetId, ArrayList<String> resultMessages) {
        super(callback, false /** asynchronous */,  new TaskInfo( ((resultSetId != null) ? "Generate MSDiag Report for resultSet id "+ resultSetId : "Generate MSDiag Report"), true, TASK_LIST_INFO,TaskInfo.INFO_IMPORTANCE_HIGH));

     //   m_dataset = dataset;
      //  m_exportAllPSMs =  exportAllPSMs;
      //  m_filePathResult = filePathInfo;

        m_projectId = projectId;
        m_resultSetId = resultSetId;
        m_resultMessages = resultMessages;
        //JOptionPane.showMessageDialog(null," l 50" + resultMessages.get(resultMessages.size()-1), "GenerateMSDiagReportTask",1);
    	
        //m_resultMessages = new ArrayList<String> (2);
        //m_resultMessages.add( "Success constructor from GenerateMSDiagReportTask"); //resultMessage;
        //m_resultMessages.add( "Success first element not yet defined in method"); //resultMessage;

        //m_resultSummaryId = resultSummaryId;
       }
    
    
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(m_id);
            request.setMethod("run_job");


            Map<String, Object> params = new HashMap<>();
            params.put("project_id", m_projectId);
            params.put("result_set_id", m_resultSetId);
            request.setParameters(params);


            m_loggerWebcore.debug(getClass().getSimpleName() + "AW debug: Message: dps.msi/generate_msdiag_report/"+request.getMethod()+getIdString() + "request:\n"+ request);
            
            HttpResponse response = postRequest("dps.msi/generate_msdiag_report/"+request.getMethod()+getIdString(), request);
            
            GenericJson jsonResult = response.parseAs(GenericJson.class);

            //JOptionPane.showMessageDialog(null,"l 82: response from post request:\n" + response, "GenerateMSDiagReportTask",1);

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
            	

            
            m_loggerWebcore.debug("return from jsonPostRequest:" + response);
            BigDecimal jobId = (BigDecimal) jsonResult.get("result");
            if (jobId != null) {
                m_id = jobId.intValue();
            } else {
                m_loggerProline.error(getClass().getSimpleName() + " failed : id not defined");
            }


        } catch (Exception e) {
            m_taskError = new TaskError(e);
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

            m_loggerWebcore.debug((getClass().getSimpleName() + " POSTING MESSAGE to service..."));
            HttpResponse response = postRequest("dps.msi/generate_msdiag_report/"+request.getMethod()+getIdString(), request);

            
            m_loggerWebcore.debug((getClass().getSimpleName() + " response back from service:\n" + response));
            
            m_resultMessages.add("Element 0 from GenerateMSDiagReportTask"); //response.toString();
            
            GenericJson jsonResult = response.parseAs(GenericJson.class);
            
           // JOptionPane.showMessageDialog(null," l 157\n" + jsonResult, "GenerateMSDiagReportTask",1);

            
            m_resultMessages.add(jsonResult.toPrettyString());
            m_loggerWebcore.debug((getClass().getSimpleName() + " json message back from service:\n" + jsonResult.toPrettyString()));
           
            
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
                Boolean success = (Boolean) resultMap.get("success");  //JPM.TODO : get ResultSummary created
                
                if (success == null) {
                    
                    String message = (String) resultMap.get("message");
                    if ((message!=null) && message.startsWith("Running")) {
                        getTaskInfo().setRunning(false);
                    }
                    
                    return ServiceState.STATE_WAITING;
                }
                
                if (success) {
                	// get the data serialized object from MSDiag

                	BigDecimal duration = (BigDecimal) resultMap.get("duration");
                    if (duration != null) {
                        getTaskInfo().setDuration(duration.longValue());
                    }
                    
                   String returnedValue = (String) resultMap.get("result");

                    
                    String receivedStringValue = (String) returnedValue; //.get("messageString");
                     m_loggerProline.debug(getClass().getSimpleName() + " messageString:" + receivedStringValue);
              
                    
                    if (receivedStringValue == null) {
                        m_loggerProline.debug(getClass().getSimpleName() + " failed : No returned String value");
                        return ServiceState.STATE_FAILED;
                    }
            //        JOptionPane.showMessageDialog(null," l 222" + receivedStringValue, "GenerateMSDiagReportTask",1);

                   m_resultMessages.add( receivedStringValue); // SEND MESSAGE BACK ***********************
                   return ServiceState.STATE_DONE;
                } else {
                    String errorMessage = (String) resultMap.get("message");
                    
                    
                    if (errorMessage != null) {
                        m_taskError = new TaskError(errorMessage);
                        m_loggerWebcore.error(getClass().getSimpleName() + " failed : " + errorMessage);
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

