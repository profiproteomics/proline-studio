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

import fr.profi.util.StringUtils;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dpm.data.JMSNotificationMessage;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.dpm.task.util.JMSMessageUtil;
import fr.proline.studio.rsmexplorer.gui.dialog.GetSystemInfoButtonAction;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.utils.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 *
 * @author VD225637
 *
 */
public class SystemTasksPanel extends AbstractTasksPanel {

    private SystemMessageTable m_messageTable;

    //QueueBrowser attributes
    private QueueBrowser m_qBrowser = null;
    private static final int UPDATE_DELAY = 1000;
    private Timer m_updateTimer = null;
    private static int m_connectionErrCount = 0;
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private JButton m_reconnectButton;
    private JButton m_logParserButton;
    private ServerLogFileNameDialog m_logParserDialog;

    public SystemTasksPanel() {
        super();
        m_loggerProline.warn(" STARTED system tasks !!");
        setLayout(new GridBagLayout());
        m_logParserDialog = new ServerLogFileNameDialog();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.weightx = 1;
        c.weighty = 1;
        add(createToolbarPanel(), c);
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        JButton clearButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        clearButton.setToolTipText("Clear system tasks list");
        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearData();
            }
        });
        toolbar.add(clearButton);
        GetSystemInfoButtonAction systemInfoButton = new GetSystemInfoButtonAction();
        toolbar.add(systemInfoButton);

        m_reconnectButton = new JButton(IconManager.getIcon(IconManager.IconType.REFRESH));
        m_reconnectButton.setToolTipText("Reconnect to server tasks logs");
        m_reconnectButton.setEnabled(false);
        m_reconnectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isMonitoringConnected()) { //should be the case if button enabled !
                    m_connectionErrCount = 0; //reinit counter
                    reInitConnection();
                }
            }
        });
        toolbar.add(m_reconnectButton);
        m_logParserButton = new JButton(IconManager.getIcon(IconManager.IconType.DOCUMENT_LIST));
        m_logParserButton.setToolTipText("View server tasks log history");
        m_logParserButton.setEnabled(true);
        m_logParserButton.addActionListener(createLogParserButtonAction());
        toolbar.add(m_logParserButton);
        return toolbar;
    }

    private ActionListener createLogParserButtonAction() {
        ActionListener action = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_logParserDialog.setLocationRelativeTo(m_logParserButton);
                m_logParserDialog.setVisible(true);

            }

        };
        return action;
    }

    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel();

        toolbarPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        toolbarPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        toolbarPanel.add(toolbar, BorderLayout.WEST);

        return toolbarPanel;

    }

    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_messageTable = new SystemMessageTable();
        m_messageTable.setModel(new SystemMessageTableModel());

        TableColumnModel columnModel = m_messageTable.getColumnModel();
        columnModel.getColumn(SystemMessageTableModel.COLTYPE_MESSAGE_EVENT_DATE).setCellRenderer(new DateAndTimeRenderer());
        columnModel.getColumn(SystemMessageTableModel.COLTYPE_MESSAGE_EVENT_TYPE).setCellRenderer(new EventTypeRenderer());

        // create objects
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(m_messageTable);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(scrollPane, c);

        return internalPanel;

    }

    public void clearData() {
        ((SystemMessageTableModel) m_messageTable.getModel()).resetData();
    }

    @Override
    protected boolean checkJMSVariables() {
        if (m_qBrowser == null) {
            m_qBrowser = JMSConnectionManager.getJMSConnectionManager().getQueueBrowser();
            if (m_qBrowser == null) {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Unable to get JMS Queue Browser !! No server tasks monitoring will be done.", "Server Tasks Logs error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return super.checkJMSVariables(); // AbstractTasksPanel checks listener params
    }

    // Creates specific callback method to be called when service event notification occurs
    @Override
    protected AbstractJMSCallback getServiceNotificationCallback(JMSNotificationMessage[] sysInfoResult) {
        AbstractJMSCallback notifierCallback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {

                int selectedRow = m_messageTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedRow = m_messageTable.convertRowIndexToModel(selectedRow);
                }

                ((SystemMessageTableModel) m_messageTable.getModel()).addMessage(sysInfoResult[0]);

                if (selectedRow >= 0) {
                    int modelIndex = m_messageTable.convertRowIndexToView(selectedRow);
                    m_messageTable.setRowSelectionInterval(modelIndex, modelIndex);
                }
            }
        };
        return notifierCallback;
    }

    // Creates specific callback method to be called when purge consumer has been executed
    @Override
    protected AbstractJMSCallback getPurgeConsumerCallback(JMSNotificationMessage[] purgerResult) {
        AbstractJMSCallback purgerCallback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                int selectedRow = m_messageTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedRow = m_messageTable.convertRowIndexToModel(selectedRow);
                }

                ((SystemMessageTableModel) m_messageTable.getModel()).addMessage(purgerResult[0]);

                if (selectedRow >= 0) {
                    int modelIndex = m_messageTable.convertRowIndexToView(selectedRow);
                    m_messageTable.setRowSelectionInterval(modelIndex, modelIndex);
                }

            }
        };
        return purgerCallback;
    }


    /*
     * Called when notification is enabled to allow other king of data collect.
     * Browse Proline JMS Queue
     */
    @Override
    protected void startOtherDataCollecting() {
        if (m_updateTimer == null) {
            ActionListener taskPerformer = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    browsePendingMessages();
                }
            };
            m_updateTimer = new Timer(UPDATE_DELAY, taskPerformer);

        }

        m_updateTimer.start();
    }

    private void browsePendingMessages() {
        try {

            Enumeration<Message> messageEnum = m_qBrowser.getEnumeration();
            List<JMSNotificationMessage> pendingMsg = new ArrayList<>();

            while (messageEnum.hasMoreElements()) {
                Message msg = messageEnum.nextElement();
                JMSNotificationMessage notifMsg = JMSMessageUtil.buildJMSNotificationMessage(msg, JMSNotificationMessage.MessageStatus.PENDING);
                if (notifMsg != null) {
                    pendingMsg.add(notifMsg);
                    //m_logger.debug(notifMsg.toString());
                } else {
                    m_logger.debug("Invalid message in Queue ! " + msg);
                }
            }

            int selectedRow = m_messageTable.getSelectedRow();
            if (selectedRow >= 0) {
                selectedRow = m_messageTable.convertRowIndexToModel(selectedRow);
            }
            ((SystemMessageTableModel) m_messageTable.getModel()).addMessages(pendingMsg);
            if (selectedRow >= 0) {
                int modelIndex = m_messageTable.convertRowIndexToView(selectedRow);
                m_messageTable.setRowSelectionInterval(modelIndex, modelIndex);
            }

        } catch (Exception jmsE) {

            jmsE.printStackTrace();

            m_connectionErrCount++;
            if (m_connectionErrCount < 2) {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Unable to browse pending messages (JMS Connection problem ?! : " + jmsE.getMessage() + ")", "Server Tasks Logs error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Unable to browse pending messages. Stop browsing server tasks ", "Server Tasks Logs error", JOptionPane.ERROR_MESSAGE);
                stopOtherDataCollecting();
                m_reconnectButton.setEnabled(true);
//                m_qBrowser = null;
//                //TODO: add method in JMSConnectionManager to disconnect/reconnect without reset of variables
//                String jmsHost = JMSConnectionManager.getJMSConnectionManager().m_jmsServerHost;
//                JMSConnectionManager.getJMSConnectionManager().closeConnection();
//                JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(jmsHost);
//                if (!checkJMSVariables()) { // still can't connect
//                    stopOtherDataCollecting();
//                }
//                reInitConnection();
                m_connectionErrCount = 0;
            }
        }
    }

    private void reInitConnection() {
        m_qBrowser = null;
        //TODO: add method in JMSConnectionManager to disconnect/reconnect without reset of variables
        String jmsHost = JMSConnectionManager.getJMSConnectionManager().m_jmsServerHost;
        JMSConnectionManager.getJMSConnectionManager().closeConnection();
        JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(jmsHost);
        if (!checkJMSVariables()) { // still can't connect
            stopOtherDataCollecting();
            m_reconnectButton.setEnabled(true);
        } else {
            m_reconnectButton.setEnabled(false);
        }
    }

    /**
     * Called when notification is stopped (JMS connection is closed) to stop
     * other king of data collect. Stop browsing Proline JMS Queue
     */
    @Override
    protected void stopOtherDataCollecting() {
        m_qBrowser = null;
        m_updateTimer.stop();
    }

    class SystemMessageTable extends DecoratedTable {

        public SystemMessageTable() {
            super();
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        }

        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        @Override
        public void prepostPopupMenu() {
            //nothing to do
        }

    }

    class SystemMessageTableModel extends DecoratedTableModel {

        public static final int COLTYPE_MESSAGE_EVENT_TYPE = 0;
        public static final int COLTYPE_MESSAGE_JSON_RPC_ID = 1;
        public static final int COLTYPE_MESSAGE_SERVICE_NAME = 2;
        public static final int COLTYPE_MESSAGE_EVENT_DATE = 3;
        public static final int COLTYPE_MESSAGE_SERVICE_SOURCE = 4;
        public static final int COLTYPE_MESSAGE_REQ_ID = 5;
        public static final int COLTYPE_MESSAGE_COMPLEMENTARY_DATA = 6;

        private final String[] m_columnNames = {"", "Id", "Service", "Date", "Source", "Message ID", "Supplementary Data"};
        private final String[] m_columnTooltips = {"Current Service State (START, FAIL, SUCCESS, PENDING)", "Identifier of JSON RPC message", "Proline Service Description", "Date of the event on the service", "The source (user/computer) of the service call", "Message Server Unique ID", "All complementary data we can get: service name/version ..."};

        private ArrayList<JMSNotificationMessage> m_notificationMsgs = new ArrayList<>();
        private HashMap<String, Integer> indexByMsgId = new HashMap<>();

        public void resetData() {
            m_notificationMsgs.clear();
            indexByMsgId.clear();
            fireTableDataChanged();
        }

        public JMSNotificationMessage getMessage(int row) {
            if (m_notificationMsgs.size() <= row) {
                return null;
            }
            return m_notificationMsgs.get(row);
        }

        public void addMessage(JMSNotificationMessage msg) {
            addSingleMessage(msg);
            sortMessageById();
            fireTableDataChanged();
        }

        synchronized private void addSingleMessage(JMSNotificationMessage msg) {
            String msgId = msg.getServerUniqueMsgId();
            if (indexByMsgId.containsKey(msgId)) {
                int index = indexByMsgId.get(msgId);
                JMSNotificationMessage prevMsg = m_notificationMsgs.get(index);
                if ((!msg.getJsonRPCMsgId().equals(prevMsg.getJsonRPCMsgId())) && msgId.equals(msg.getJsonRPCMsgId())) {
                    msg = new JMSNotificationMessage(msg.getServiceName(), msg.getServiceVersion(), msg.getServiceSource(), msg.getServiceDescription(), msg.getServiceInfo(), msg.getEventDate().getTime(), msgId, prevMsg.getJsonRPCMsgId(), msg.getEventType());
                }
                m_notificationMsgs.set(index, msg);
            } else {
                m_notificationMsgs.add(msg);
                indexByMsgId.put(msgId, m_notificationMsgs.size() - 1);
            }
        }

        public void addMessages(List<JMSNotificationMessage> msgs) {
            for (JMSNotificationMessage msg : msgs) {
                addSingleMessage(msg);
            }
            sortMessageById();
            fireTableDataChanged();
        }

        private synchronized void sortMessageById() {
            Collections.sort(m_notificationMsgs, new SortById());
            indexByMsgId.clear();
            for (int i = 0; i < m_notificationMsgs.size(); i++) {
                indexByMsgId.put(m_notificationMsgs.get(i).getServerUniqueMsgId(), i);
            }

        }

        /**
         * Used for sorting in descending order
         */
        class SortById implements Comparator<JMSNotificationMessage> {

            @Override
            public int compare(JMSNotificationMessage a, JMSNotificationMessage b) {
                try {
                    Integer bIntId = Integer.valueOf(b.getJsonRPCMsgId());
                    Integer aIntId = Integer.valueOf(a.getJsonRPCMsgId());
                    return bIntId - aIntId;
                } catch (NumberFormatException nfe) {
                    return a.getJsonRPCMsgId().compareTo(b.getJsonRPCMsgId());
                }
            }
        }

        @Override
        public int getColumnCount() {
            return m_columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return m_columnNames[col];
        }

        @Override
        public int getRowCount() {
            if (m_notificationMsgs == null) {
                return 0;
            }

            return m_notificationMsgs.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            JMSNotificationMessage msg = m_notificationMsgs.get(rowIndex);
            switch (columnIndex) {
                case COLTYPE_MESSAGE_EVENT_TYPE:
                    return msg;
                case COLTYPE_MESSAGE_JSON_RPC_ID:
                    try {
                    return Integer.valueOf(msg.getJsonRPCMsgId());
                } catch (NumberFormatException nfe) {
                    return -1;
                }
                case COLTYPE_MESSAGE_SERVICE_NAME:
                    if (StringUtils.isNotEmpty(msg.getServiceDescription())) {
                        return msg.getServiceDescription();
                    } else {
                        StringBuilder sb = new StringBuilder(msg.getServiceName());
                        if (StringUtils.isNotEmpty(msg.getServiceVersion())) {
                            sb.append(" [").append(msg.getServiceVersion()).append("]");
                        }
                        return sb.toString();
                    }
                case COLTYPE_MESSAGE_EVENT_DATE:
                    return msg.getEventDate();
                case COLTYPE_MESSAGE_SERVICE_SOURCE:
                    return msg.getServiceSource();
                case COLTYPE_MESSAGE_REQ_ID:
                    return msg.getServerUniqueMsgId();
                case COLTYPE_MESSAGE_COMPLEMENTARY_DATA:
                    StringBuilder sb = new StringBuilder();
                    if (StringUtils.isNotEmpty(msg.getServiceDescription())) {
                        sb.append(msg.getServiceName());
                        if (StringUtils.isNotEmpty(msg.getServiceVersion())) {
                            sb.append(" [").append(msg.getServiceVersion()).append("]");
                        }
                    }
                    if (StringUtils.isNotEmpty(msg.getServiceInfo())) {
                        sb.append(" ").append(msg.getServiceInfo());
                    } else if (sb.length() < 0) {
                        sb.append("-");
                    }

                    return sb.toString();
            }
            return null; // should never happen
        }

        @Override
        public String getToolTipForHeader(int col) {
            return m_columnTooltips[col];
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

        @Override
        public Class getColumnClass(int col) {
            switch (col) {
                case COLTYPE_MESSAGE_JSON_RPC_ID:
                    return Integer.class;
                case COLTYPE_MESSAGE_SERVICE_NAME:
                case COLTYPE_MESSAGE_SERVICE_SOURCE:
                case COLTYPE_MESSAGE_REQ_ID:
                case COLTYPE_MESSAGE_COMPLEMENTARY_DATA:
                    return String.class;
                case COLTYPE_MESSAGE_EVENT_DATE:
                    return Date.class;
                case COLTYPE_MESSAGE_EVENT_TYPE:
                    return JMSNotificationMessage.class;
            }
            return null; // should not happen
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            return new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(getColumnClass(col)));
        }
    }

    public class DateAndTimeRenderer extends DefaultTableCellRenderer {

        public DateAndTimeRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            SimpleDateFormat sf = new SimpleDateFormat("h:mm:ss a - d MMM yyyy ");
            setText(sf.format((Date) value));

            return this;
        }
    }

    public class EventTypeRenderer extends DefaultTableCellRenderer {

        public EventTypeRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

            JMSNotificationMessage msg = (JMSNotificationMessage) value;

            setIcon(PUBLIC_STATE_ICONS[msg.getPublicState()]);
            setText("");
            return this;
        }
    }

}
