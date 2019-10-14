/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dpm.data;

import static fr.proline.studio.dam.taskinfo.TaskInfo.*;
import java.util.Date;

/**
 *
 * @author VD225637
 */
public class JMSNotificationMessage {
    
    private String m_serviceName;
    private String m_serviceVersion;
    private String m_serviceSource;
    private String m_serviceDescr;
    private String m_serviceInfo;
    private Date m_eventTime;
    private String m_jmsMsgId;
    private String m_jsonRPCMsgId;
    private MessageStatus m_eventType;
    
    //Use Proline Node ?! 
    public enum MessageStatus {
        PENDING("Pending",PUBLIC_STATE_WAITING),
        STARTED("Start",PUBLIC_STATE_RUNNING),
        ABORTED("Abort", PUBLIC_STATE_ABORTED),
        SUCCES("Success",PUBLIC_STATE_FINISHED),
        FAILED("Fail",PUBLIC_STATE_FAILED);
        
        String m_value;
        Integer m_publicState;
        
        MessageStatus(String value, Integer publicState) {
            this.m_value = value;
            this.m_publicState = publicState;
        }
        
        public String getValue() {
            return m_value;
        }
        
        public Integer getPublicState(){
            return m_publicState;
        }
        
        public static MessageStatus parseString(String searchedVal){				
            MessageStatus[] allMessageStatus = MessageStatus.values();
				
            for(MessageStatus nextMessageStatus : allMessageStatus){
                if(nextMessageStatus.m_value.equals(searchedVal))
                        return nextMessageStatus;
            }
            return null;
        }
    };
    
    
    public JMSNotificationMessage(String serviceName, String serviceVersion, String serviceSource, String serviceDescription,
            String serviceInfo, Long  eventTimestamp, String jmsMsgId, String jsonRPCId, MessageStatus eventType ){
        m_serviceName = serviceName;
        m_serviceVersion = serviceVersion;
        m_serviceSource = serviceSource;
        m_serviceDescr = serviceDescription;
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
    
    public String getServiceDescription(){
        return m_serviceDescr;
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

    public MessageStatus getEventType() {
        return m_eventType;
    }
    
    public void setEventType(MessageStatus eventType){
        m_eventType = eventType;        
    }

    public String getServiceVersion() {
        return m_serviceVersion;
    }

    public String getServiceSource() {
        return m_serviceSource;
    }
    
    public Integer getPublicState(){
        return m_eventType.getPublicState();
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
