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
package fr.proline.studio.dpm;

import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dpm.task.jms.AbstractJMSTask;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
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
        initSession();
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

                // init session if needed
                initSession();
                
                // fetch data
                task.askJMS();

            }


        } catch (InterruptedException | JMSException t) {
            LoggerFactory.getLogger("ProlineStudio.DPM").debug("Unexpected exception in main loop of AccessServiceThread", t);
            m_instance = null; // reset thread
        }
    }
    
    
    private void initSession() {
        if (m_connection == null) {
            try {
                // Get JMS Connection
                m_connection = JMSConnectionManager.getJMSConnectionManager().getJMSConnection();
                m_connection.start(); // Explicitely start connection to begin Consumer reception 
                m_session = m_connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            } catch (JMSException je) {
                LoggerFactory.getLogger("ProlineStudio.DPM").error("Unexpected exception when initializing JMS Connection", je);
            } catch (Exception e) {
                LoggerFactory.getLogger("ProlineStudio.DPM").error("Unexpected exception when initializing JMS Connection", e);
            }
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
    
    public void cleanup() {
        if (m_session != null) {
            synchronized (this) {
                try {
                    m_session.close();

                    m_taskList.clear();
                } catch (Exception e) {

                } finally {
                    m_session = null;
                    m_connection = null;
                }
            }
        }
        
    }
}
