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
    private Date m_eventTime;
    private String m_reqJMSMsgId;
    private String m_jsonRPCMsgId;
    private String m_eventType;
    
    public JMSNotificationMessage(String serviceName, Long  eventTimestamp, String reqJMSMsgId, String jsonRPCId, String eventType ){
        m_serviceName = serviceName;
        m_reqJMSMsgId = reqJMSMsgId;
        m_jsonRPCMsgId = jsonRPCId;
        m_eventType = eventType;
        m_eventTime = new Date(eventTimestamp);
        
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
    
    
}
