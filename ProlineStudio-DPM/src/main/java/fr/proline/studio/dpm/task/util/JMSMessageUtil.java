/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.util;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.profi.util.StringUtils;
import fr.proline.studio.dpm.data.JMSNotificationMessage;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class JMSMessageUtil {
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");
    private static final String TAB = "    ";
    private static final String DATE_FORMAT = "%td/%<tm/%<tY %<tH:%<tM:%<tS.%<tL";
    private static final int MESSAGE_BUFFER_SIZE = 2048;
    

    
    
    public static JMSNotificationMessage buildJMSNotificationMessage(final Message message, JMSNotificationMessage.MessageStatus status) {
        
        try {
            String sName = message.getStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY);
            String sVersion  = message.getStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY);
            String sSource  = message.getStringProperty(JMSConnectionManager.PROLINE_SERVICE_SOURCE_KEY);
            String sDescription  = message.getStringProperty(JMSConnectionManager.PROLINE_SERVICE_DESCR_KEY);
            String sMoreInfo = null;
            if(TextMessage.class.isInstance(message))
                sMoreInfo = ((TextMessage) message).getText();
            Long sTimestamp = message.getJMSTimestamp();
            String jmsId = message.getJMSMessageID();
            
            String jsonId = "";
            if(TextMessage.class.isInstance(message)){
                try {
                    JSONRPC2Request jsonMsg = JSONRPC2Request.parse(((TextMessage)message).getText());                            
                    jsonId= jsonMsg.getID().toString();
                } catch (JSONRPC2ParseException ex) {
                     jsonId = jmsId;
                }                
            }
            
            JMSNotificationMessage notifMsg = new  JMSNotificationMessage(sName,sVersion, sSource, sDescription, sMoreInfo, sTimestamp, jmsId,jsonId, status);
            return notifMsg;
        }catch(JMSException jmsE ){
            return null;
        }
    }
    
    /**
     * Formats some Header filds, Properties and Body of the given JMS Message to print usefull debug info.
     */
    public static String formatMessage(final Message message) {

	if (message == null) {
	    throw new IllegalArgumentException("Message is null");
	}

	final StringBuilder buff = new StringBuilder(MESSAGE_BUFFER_SIZE);

	try {
	    buff.append(message.getClass().getName()).append("  ").append(message.getJMSMessageID());
	    buff.append(StringUtils.LINE_SEPARATOR);

	    buff.append(TAB).append("JMSCorrelationID ");
	    append(buff, message.getJMSCorrelationID());
	    buff.append(StringUtils.LINE_SEPARATOR);

	    buff.append(TAB).append("JMSTimestamp ")
		    .append(String.format(DATE_FORMAT, message.getJMSTimestamp()));
	    buff.append(StringUtils.LINE_SEPARATOR);

	    buff.append(TAB).append("JMSDestination ");
	    append(buff, message.getJMSDestination());
	    buff.append(StringUtils.LINE_SEPARATOR);

	    buff.append(TAB).append("JMSReplyTo ");
	    append(buff, message.getJMSReplyTo());
	    buff.append(StringUtils.LINE_SEPARATOR);

	    final Enumeration<String> nameEnum = message.getPropertyNames();

	    while (nameEnum.hasMoreElements()) {
		final String propertyName = nameEnum.nextElement();
		buff.append(TAB).append('[').append(propertyName).append("] : ");

		final String propertyValue = message.getStringProperty(propertyName);

		if (propertyValue == null) {
		    buff.append("NULL");
		} else {
		    buff.append('[').append(propertyValue).append(']');
		}

		buff.append(StringUtils.LINE_SEPARATOR);
	    }

	    if (message instanceof TextMessage) {
		buff.append(TAB).append(((TextMessage) message).getText());
	    }

	    buff.append(StringUtils.LINE_SEPARATOR);
	} catch (Exception ex) {
	    m_loggerProline.error("Error retrieving JMS Message header or content", ex);
	}

	return buff.toString();
    }
    
    
        
    private static void append(final StringBuilder sb, final Object obj) {
	assert (sb != null) : "append() sb is null";

	if (obj == null) {
	    sb.append("NULL");
	} else {
	    sb.append(obj);
	}

    } 
    
    public static void traceJSONResponse(final String jsonString) throws JSONRPC2ParseException {
	final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);

	if (jsonMessage instanceof JSONRPC2Notification) {
	    final JSONRPC2Notification jsonNotification = (JSONRPC2Notification) jsonMessage;

	    m_loggerProline.debug("JSON Notification method: " + jsonNotification.getMethod());

	    final Map<String, Object> namedParams = jsonNotification.getNamedParams();

	    if ((namedParams != null) && !namedParams.isEmpty()) {
		final StringBuilder buff = new StringBuilder("Params: ");

		boolean first = true;

		final Set<Map.Entry<String, Object>> entries = namedParams.entrySet();

		for (final Map.Entry<String, Object> entry : entries) {

		    if (first) {
			first = false;
		    } else {
			buff.append(" | ");
		    }

		    buff.append(entry.getKey());
		    buff.append(" : ").append(entry.getValue());
		}

		m_loggerProline.debug(buff.toString());
	    }

	} else if (jsonMessage instanceof JSONRPC2Response) {
	    final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;

	    m_loggerProline.debug("JSON Response Id: " + jsonResponse.getID());

	    final JSONRPC2Error jsonError = jsonResponse.getError();

	    if (jsonError != null) {
		m_loggerProline.error("JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
		m_loggerProline.error("JSON Throwable", jsonError);
	    }

	    final Object result = jsonResponse.getResult();

	    if (result == null) {
		m_loggerProline.debug("No result");
	    } else {
		m_loggerProline.debug("Result :\n" + result);
	    }

	}

    }
}
