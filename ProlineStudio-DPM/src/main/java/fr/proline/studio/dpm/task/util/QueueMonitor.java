    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.util;

import static fr.proline.studio.dpm.task.util.JMSConnectionManager.SERVICE_REQUEST_QUEUE_NAME;
import java.util.Enumeration;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class QueueMonitor {
    QueueBrowser m_browser = null;
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");
    
    public QueueMonitor(){
//        JMSContext context = JMSConnectionManager.getJMSConnectionManager().getJMSContext();
//        try {
//            m_browser  = context.createBrowser(JMSConnectionManager.getJMSConnectionManager().getServiceQueue());
//        } catch (Exception ex) {
//           throw new InstantiationError(" Unable to get Queue Browser: "+ex.getMessage());
//        }
    }
    
    public String browse( ){
//        try {
//            StringBuilder sb = new StringBuilder();
//            final Enumeration<Message> messageEnum = m_browser.getEnumeration();
//        
//	    int nMessages = 0;
//
//	    while (messageEnum.hasMoreElements()) {
//		final Message msg = messageEnum.nextElement();
//		++nMessages;
//                String formattedMsg = JMSMessageUtil.formatMessage(msg);
//		m_loggerProline.debug(formattedMsg);
//                sb.append(formattedMsg);
//	    }
//
//	    if (nMessages == 0) {
//                String emptyMsg = "Queue "+SERVICE_REQUEST_QUEUE_NAME+" is empty !";
//		sb.append(emptyMsg);
//                m_loggerProline.debug(emptyMsg);
//	    } else {
//		m_loggerProline.debug("Total messages count: " + nMessages);
//	    }
//            return sb.toString();
//        }catch (JMSException jmsE){
//            return null;
//        }        
        return "";
    }
    
}
