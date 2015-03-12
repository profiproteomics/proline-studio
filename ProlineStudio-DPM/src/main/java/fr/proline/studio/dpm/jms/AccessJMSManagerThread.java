package fr.proline.studio.dpm.jms;

import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dpm.task.jms.AbstractJMSTask;
import fr.proline.studio.dpm.task.util.JMSConstants;
import java.util.LinkedList;
import javax.jms.*;
import org.slf4j.LoggerFactory;


/**
 * Manager used to add Tasks to the JMS Queue
 * @author JM235353
 */
public class AccessJMSManagerThread extends Thread {
    
    private static AccessJMSManagerThread m_instance;
    private Connection m_connection;
    private Session m_session;
    private LinkedList<AbstractJMSTask> m_taskList = new LinkedList<>();
    
    public static AccessJMSManagerThread getAccessJMSManagerThread() {
        if (m_instance == null) {
            m_instance = new AccessJMSManagerThread();
            m_instance.start();
        }
        return m_instance;
    }
    
    private AccessJMSManagerThread() {
        super("AccessJMSManagerThread"); // useful for debugging
        try {
            // Get JMS Connection
            m_connection = JMSConstants.getJMSConnection();
            m_connection.start(); // Explicitely start connection to begin Consumer reception 
            m_session = m_connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        } catch (JMSException e) {
            LoggerFactory.getLogger("ProlineStudio.DPM").error("Unexpected exception when initializing JMS Connection", e);
        }
    }
    
    public Session getSession(){
        return m_session;
    }

    /**
     * Main loop of the thread
     */
    @Override
    public void run() {
        try {
            while (true) {
                AbstractJMSTask task = null;
                synchronized (this) {

                    while (true) {

                        // look for a task to be done
                        if (!m_taskList.isEmpty()) {
                            task = m_taskList.poll();
                            break;
                        }
                        wait();
                    }
                    notifyAll();
                }

                //Thread.sleep(500);
                //System.out.println("Action : "+action.getClass().toString()+" "+System.currentTimeMillis());

                // fetch data
                task.askJMS(JMSConstants.getJMSConnection());

            }


        } catch (InterruptedException | JMSException t) {
            LoggerFactory.getLogger("ProlineStudio.DPM").debug("Unexpected exception in main loop of AccessServiceThread", t);
            m_instance = null; // reset thread
        }
    }
    
    
    /**
     * Add a task to be done
     *
     * @param task
     */
    public final void addTask(AbstractJMSTask task) {

        TaskInfoManager.getTaskInfoManager().add(task.getTaskInfo());
        // task is queued
        synchronized (this) {
            m_taskList.add(task);
            notifyAll();
        }
    }
}
