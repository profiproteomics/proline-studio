package fr.proline.studio.dpm.jms;

import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dpm.task.jms.AbstractJMSTask;
import fr.proline.studio.dpm.task.util.JMSConstants;
import javax.jms.*;
import org.slf4j.LoggerFactory;


/**
 * Manager used to add Tasks to the JMS Queue
 * @author JM235353
 */
public class AccessJMSManager extends Thread {
    
    private static AccessJMSManager m_instance;
    
    public static AccessJMSManager getAccessJMSManager() {
        if (m_instance == null) {
            m_instance = new AccessJMSManager();
        }
        return m_instance;
    }
    
    private AccessJMSManager() {
        try {
            // Get JMS Connection
            Connection connection = JMSConstants.getJMSConnection();
            connection.start(); // Explicitely start connection to begin Consumer reception
        } catch (JMSException e) {
            LoggerFactory.getLogger("ProlineStudio.DPM").error("Unexpected exception when initializing JMS Connection", e);
        }
    }

    /**
     * Add a task to be done
     *
     * @param task
     */
    public final void addTask(AbstractJMSTask task) {

        TaskInfoManager.getTaskInfoManager().add(task.getTaskInfo());
        try {
            task.askJMS(JMSConstants.getJMSConnection());
        } catch (JMSException e) {
            LoggerFactory.getLogger("ProlineStudio.DPM").error("Unexpected exception while getting JMS Connection", e);
        }
    }
}
