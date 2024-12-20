/* 
 * Copyright (C) 2019
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
package fr.proline.studio.rsmexplorer.gui.tasklog;

import fr.proline.studio.WindowManager;
import fr.proline.studio.dpm.data.JMSNotificationMessage;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.PurgeConsumer;
import fr.proline.studio.dpm.task.util.ConnectionListener;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.dpm.task.util.ServiceNotificationListener;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.FilterTableModelInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.dialog.GetSystemInfoButtonAction;
import fr.proline.studio.utils.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 *
 * @author VD225637
 * 
 * TODO: check if subclasses table and/or tableModel code could be merged here
 */
public abstract class AbstractTasksPanel extends HourglassPanel implements DataBoxPanelInterface, ConnectionListener {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    protected final static ImageIcon[] PUBLIC_STATE_ICONS = { IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16), IconManager.getIcon(IconManager.IconType.ARROW_RIGHT_SMALL), IconManager.getIcon(IconManager.IconType.CROSS_BLUE_SMALL16), IconManager.getIcon(IconManager.IconType.TICK_SMALL), IconManager.getIcon(IconManager.IconType.CROSS_SMALL16)};

    private AbstractDataBox m_dataBox;
    private boolean m_isConnected;
        
    private ServiceNotificationListener m_serviceListener = null;
    private AbstractJMSCallback m_notifierCallback = null; //Callback to be called by ServiceNotificationListener
    private AbstractJMSCallback m_purgerCallback = null; //Callback to be called by purger

    public AbstractTasksPanel(){
        this.m_isConnected = false;
        initListener(); //calling it in constructor is incorrect: it calls override methods
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.weightx = 1;
        c.weighty = 1;
        add(createToolbarPanel(), c);
    }

    protected  JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel();

        toolbarPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        toolbarPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        toolbarPanel.add(toolbar, BorderLayout.WEST);

        return toolbarPanel;

    }

    protected JButton getEraseButton(){
        JButton b = new JButton();
        b.setIcon(IconManager.getIcon(IconManager.IconType.ERASER));
        b.setToolTipText("Clear Tasks list");
        return b;
    }

    protected abstract JPanel createInternalPanel();
    protected abstract void clearActionPerformed();

    protected abstract FilterTableModelInterface getTaskTableModel();
    protected JToolBar initToolbar(){
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        FilterButton filterButton = new FilterButton( getTaskTableModel()) {

            @Override
            protected void filteringDone() {
            }

        };

        toolbar.add(filterButton);

        JButton clearBtn = getEraseButton();
        clearBtn.addActionListener(e -> clearActionPerformed());
        toolbar.add(clearBtn);

        GetSystemInfoButtonAction systemInfoButton = new GetSystemInfoButtonAction();
        toolbar.add(systemInfoButton);


        return toolbar;
    }
    protected boolean isMonitoringConnected(){
        return m_isConnected;
    }


    /**
     * Listener of JMS Connection state change : to connect/disconnect from topic
     */
    public final void initListener(){
        JMSConnectionManager.getJMSConnectionManager().addConnectionListener(this);
        int currentState =  JMSConnectionManager.getJMSConnectionManager().getConnectionState();
        connectionStateChanged(currentState);
    }
    
    protected boolean checkJMSVariables(){        
        if(m_serviceListener == null) {
            m_serviceListener =  JMSConnectionManager.getJMSConnectionManager().getNotificationListener();
             if(m_serviceListener == null) {
                JOptionPane.showMessageDialog( WindowManager.getDefault().getMainWindow(), "Unable to get Notification Listener (JMS Connection problem ?!). Try Later", "Server Tasks Logs error",JOptionPane.ERROR_MESSAGE);
                return false;
             }
        }
        return true;
    }
    
    abstract protected AbstractJMSCallback getServiceNotificationCallback(JMSNotificationMessage[] sysInfoResult);
    
    abstract protected AbstractJMSCallback getPurgeConsumerCallback(JMSNotificationMessage[] purgerResult);
    
    abstract void startOtherDataCollecting();
    
    abstract void stopOtherDataCollecting();
    
    @Override
    public void connectionStateChanged(int newStatus) {
            switch(newStatus) {
            case CONNECTION_DONE: 
                if(!m_isConnected){
                    m_serviceListener= null;
                    if (checkJMSVariables()){
                        final JMSNotificationMessage[] sysInfoResult = new JMSNotificationMessage[1];
                        m_notifierCallback = getServiceNotificationCallback(sysInfoResult);
                        m_serviceListener.addServiceNotifierCallback(m_notifierCallback, sysInfoResult);

                        final JMSNotificationMessage[] purgerResult = new JMSNotificationMessage[1];
                        m_purgerCallback = getPurgeConsumerCallback(purgerResult);
                        if(m_purgerCallback != null){
                            PurgeConsumer.getPurgeConsumer().addCallback(m_purgerCallback, purgerResult);
                        }
                        startOtherDataCollecting();
                    }
                    m_isConnected = true;
                }
                break;
            case CONNECTION_FAILED :
            case NOT_CONNECTED:
                if(m_isConnected && m_serviceListener != null){
                    if(m_notifierCallback != null)
                        m_serviceListener.removeCallback(m_notifierCallback);
                    m_serviceListener = null;
                    m_notifierCallback = null;
                    if(m_purgerCallback != null)
                        PurgeConsumer.getPurgeConsumer().removeCallback(m_purgerCallback);
                    m_purgerCallback = null;
                    stopOtherDataCollecting();
                }
                m_isConnected = false;
                break;                        
        }        
    }
    
            
    // ---- Implement DataBoxPanelInterface methods            
    @Override
    public void addSingleValue(Object v) {
//        throw new UnsupportedOperationException("Not available for System Tasks Panel.");
    }
    
    @Override 
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    
    
}
