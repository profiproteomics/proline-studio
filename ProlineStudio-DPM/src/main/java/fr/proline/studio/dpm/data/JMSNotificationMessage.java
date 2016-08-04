/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.data;

import java.util.Date;

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
    private String m_reqJMSMsgId;
    private String m_jsonRPCMsgId;
    private String m_eventType;
    
       
    public JMSNotificationMessage(String serviceName, String serviceVersion, String serviceSource,
            String serviceInfo, Long  eventTimestamp, String reqJMSMsgId, String jsonRPCId, String eventType ){
        m_serviceName = serviceName;
        m_serviceVersion = serviceVersion;
        m_serviceSource = serviceSource;
        m_reqJMSMsgId = reqJMSMsgId;
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

    public String getRequestMsgId() {
        return m_reqJMSMsgId;
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
    

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("Message ").append(getJsonRPCMsgId()).append(" / ").append(getRequestMsgId()).append(" : {");
        sb.append(getServiceName());
        if(m_serviceVersion != null)
            sb.append(" [Version ").append(m_serviceVersion).append("]");
        
        if(m_serviceSource != null)
            sb.append(" From ").append(m_serviceSource);
        
        return sb.toString();
    }
}
