/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.data;

import static fr.proline.studio.dam.taskinfo.TaskInfo.*;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author VD225637
 */
public class JMSNotificationMessage {
    
    private String m_serviceName;
    private String m_serviceVersion;
    private String m_serviceSource;
    private String m_serviceInfo;
    private Date m_eventTime;
    private String m_jmsMsgId;
    private String m_jsonRPCMsgId;
    private String m_eventType;
    
    //Use Proline Node ?! 
    public static final String PENDING_MESSAGE_STATUS = "Pending";
    public static final String START_MESSAGE_STATUS = "Start";
    public static final String SUCCES_MESSAGE_STATUS = "Success";
    public static final String FAILED_MESSAGE_STATUS = "Fail";
    
    static final HashMap<String,Integer> publicStateByEventType = new HashMap<>();
    static{ 
        publicStateByEventType.put(PENDING_MESSAGE_STATUS, PUBLIC_STATE_WAITING);
        publicStateByEventType.put(START_MESSAGE_STATUS, PUBLIC_STATE_RUNNING);
        publicStateByEventType.put(SUCCES_MESSAGE_STATUS, PUBLIC_STATE_FINISHED);
        publicStateByEventType.put(FAILED_MESSAGE_STATUS, PUBLIC_STATE_FAILED);        
    };
    
    public JMSNotificationMessage(String serviceName, String serviceVersion, String serviceSource,
            String serviceInfo, Long  eventTimestamp, String jmsMsgId, String jsonRPCId, String eventType ){
        m_serviceName = serviceName;
        m_serviceVersion = serviceVersion;
        m_serviceSource = serviceSource;
        m_jmsMsgId = jmsMsgId;
        m_jsonRPCMsgId = jsonRPCId;
        m_serviceInfo = serviceInfo;
        m_eventType = eventType;
        m_eventTime = new Date(eventTimestamp);
        
    }
    
    public String getServiceInfo() {
        return m_serviceInfo;
    }
    
    public String getServiceName() {
        return m_serviceName;
    }

    public Date getEventDate() {
        return m_eventTime;
    }

    public String getServerUniqueMsgId() {
        return m_jmsMsgId;
    }

    public String getJsonRPCMsgId() {
        return m_jsonRPCMsgId;
    }

    public String getEventType() {
        return m_eventType;
    }

    public String getServiceVersion() {
        return m_serviceVersion;
    }

    public String getServiceSource() {
        return m_serviceSource;
    }
    
    public Integer getPublicState(){
        return publicStateByEventType.getOrDefault(m_eventType, PUBLIC_STATE_ABORTED);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("Message ").append(getJsonRPCMsgId()).append(" / ").append(getServerUniqueMsgId()).append(" : {");
        sb.append(getServiceName());
        if(m_serviceVersion != null)
            sb.append(" [Version ").append(m_serviceVersion).append("]");
        
        if(m_serviceSource != null)
            sb.append(" From ").append(m_serviceSource);
        
        return sb.toString();
    }
}
