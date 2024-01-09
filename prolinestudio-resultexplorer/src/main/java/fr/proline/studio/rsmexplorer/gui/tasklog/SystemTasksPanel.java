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
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.*;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import fr.proline.studio.utils.IconManager;

import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
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

    private JButton m_reconnectButton;
    private JButton m_logParserButton;
    private final ServerLogFileNameDialog m_logParserDialog;

    public SystemTasksPanel() {
        super();
        m_logger.warn(" STARTED system tasks !!");

        m_logParserDialog = new ServerLogFileNameDialog();

    }

    @Override
    protected  FilterTableModelInterface getTaskTableModel(){
        return  (CompoundTableModel) m_messageTable.getModel();
    }


    @Override
    protected  JToolBar initToolbar() {
        JToolBar toolbar = super.initToolbar();

        m_reconnectButton = new JButton(IconManager.getIcon(IconManager.IconType.REFRESH));
        m_reconnectButton.setToolTipText("Reconnect to server tasks logs");
        m_reconnectButton.setEnabled(false);
        m_reconnectButton.addActionListener(e -> {
            if (!isMonitoringConnected()) { //should be the case if button enabled !
                m_connectionErrCount = 0; //reinit counter
                reInitConnection();
            }
        });
        toolbar.add(m_reconnectButton);

        m_logParserButton = new JButton(IconManager.getIcon(IconManager.IconType.DOCUMENT_LIST));
        m_logParserButton.setToolTipText("View server tasks log history");
        m_logParserButton.setEnabled(true);
        m_logParserButton.addActionListener(e -> {
            m_logParserDialog.setLocationRelativeTo(m_logParserButton);
            m_logParserDialog.setVisible(true);
        });
        toolbar.add(m_logParserButton);
        return toolbar;
    }



    @Override
    protected JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_messageTable = new SystemMessageTable();
        m_messageTable.setModel(new CompoundTableModel(new SystemMessageTableModel(), true));

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

    @Override
    protected void clearActionPerformed() {
        m_messageTable.getInnerModel().resetData();
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
      return new AbstractJMSCallback() {

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

                m_messageTable.getInnerModel().addMessage(sysInfoResult[0]);

                if (selectedRow >= 0) {
                    int modelIndex = m_messageTable.convertRowIndexToView(selectedRow);
                    m_messageTable.setRowSelectionInterval(modelIndex, modelIndex);
                }
            }
        };
    }

    // Creates specific callback method to be called when purge consumer has been executed
    @Override
    protected AbstractJMSCallback getPurgeConsumerCallback(JMSNotificationMessage[] purgerResult) {
      return new AbstractJMSCallback() {

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

                m_messageTable.getInnerModel().addMessage(purgerResult[0]);

                if (selectedRow >= 0) {
                    int modelIndex = m_messageTable.convertRowIndexToView(selectedRow);
                    m_messageTable.setRowSelectionInterval(modelIndex, modelIndex);
                }

            }
        };
    }


    /*
     * Called when notification is enabled to allow other king of data collect.
     * Browse Proline JMS Queue
     */
    @Override
    protected void startOtherDataCollecting() {
        if (m_updateTimer == null) {
            ActionListener taskPerformer = evt -> browsePendingMessages();
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
            m_messageTable.getInnerModel().addMessages(pendingMsg);
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

    static class SystemMessageTable extends DecoratedTable {

        SystemMessageTableModel innerModel;
        @Override
        public void setModel(TableModel dataModel) {
            super.setModel(dataModel);
            if(dataModel instanceof CompoundTableModel){
                innerModel =(SystemMessageTableModel) ((CompoundTableModel)dataModel).getBaseModel();
            }
        }

        public SystemMessageTableModel getInnerModel(){
            return innerModel;
        }

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

    static class SystemMessageTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

        public static final int COLTYPE_MESSAGE_EVENT_TYPE = 0;
        public static final int COLTYPE_MESSAGE_JSON_RPC_ID = 1;
        public static final int COLTYPE_MESSAGE_SERVICE_NAME = 2;
        public static final int COLTYPE_MESSAGE_EVENT_DATE = 3;
        public static final int COLTYPE_MESSAGE_SERVICE_SOURCE = 4;
        public static final int COLTYPE_MESSAGE_REQ_ID = 5;
        public static final int COLTYPE_MESSAGE_COMPLEMENTARY_DATA = 6;

        private final String[] m_columnNames = {"", "Id", "Service", "Date", "Source", "Message ID", "Supplementary Data"};
        private final String[] m_columnTooltips = {"Current Service State (START, FAIL, SUCCESS, PENDING)", "Identifier of JSON RPC message", "Proline Service Description", "Date of the event on the service", "The source (user/computer) of the service call", "Message Server Unique ID", "All complementary data we can get: service name/version ..."};

        private final ArrayList<JMSNotificationMessage> m_notificationMsgs = new ArrayList<>();
        private final HashMap<String, Integer> indexByMsgId = new HashMap<>();

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
            m_notificationMsgs.sort(new SortById());
            indexByMsgId.clear();
            for (int i = 0; i < m_notificationMsgs.size(); i++) {
                indexByMsgId.put(m_notificationMsgs.get(i).getServerUniqueMsgId(), i);
            }

        }

        @Override
        public String getExportRowCell(int row, int col) {
            return null;
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return null;
        }

        @Override
        public String getExportColumnName(int col) {
            return null;
        }

        @Override
        public ArrayList<ExtraDataType> getExtraDataTypes() {
            return null;
        }

        @Override
        public Object getValue(Class c) {
            return null;
        }

        @Override
        public Object getRowValue(Class c, int row) {
            return null;
        }

        @Override
        public Object getColValue(Class c, int col) {
            return null;
        }

        @Override
        public GlobalTableModelInterface getFrozzenModel() {
            return null;
        }

        @Override
        public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
            filtersMap.put(COLTYPE_MESSAGE_JSON_RPC_ID, new IntegerFilter(getColumnName(COLTYPE_MESSAGE_JSON_RPC_ID),null, COLTYPE_MESSAGE_JSON_RPC_ID));
            filtersMap.put(COLTYPE_MESSAGE_SERVICE_NAME, new StringDiffFilter(getColumnName(COLTYPE_MESSAGE_SERVICE_NAME),null, COLTYPE_MESSAGE_SERVICE_NAME));
            filtersMap.put(COLTYPE_MESSAGE_EVENT_DATE, new DateFilter(getColumnName(COLTYPE_MESSAGE_EVENT_DATE),null, COLTYPE_MESSAGE_EVENT_DATE));
            filtersMap.put(COLTYPE_MESSAGE_SERVICE_SOURCE, new StringDiffFilter(getColumnName(COLTYPE_MESSAGE_SERVICE_SOURCE),null, COLTYPE_MESSAGE_SERVICE_SOURCE));
            filtersMap.put(COLTYPE_MESSAGE_REQ_ID, new StringDiffFilter(getColumnName(COLTYPE_MESSAGE_REQ_ID),null, COLTYPE_MESSAGE_REQ_ID));
            filtersMap.put(COLTYPE_MESSAGE_COMPLEMENTARY_DATA, new StringDiffFilter(getColumnName(COLTYPE_MESSAGE_COMPLEMENTARY_DATA),null, COLTYPE_MESSAGE_COMPLEMENTARY_DATA));

        }

        @Override
        public PlotType getBestPlotType() {
            return null;
        }

        @Override
        public int[] getBestColIndex(PlotType plotType) {
            return new int[0];
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public int getLoadingPercentage() {
            return 100;
        }

        @Override
        public Long getTaskId() {
            return null;
        }

        @Override
        public LazyData getLazyData(int row, int col) {
            return null;
        }

        @Override
        public void givePriorityTo(Long taskId, int row, int col) {

        }

        @Override
        public void sortingChanged(int col) {

        }

        @Override
        public int getSubTaskId(int col) {
            return 0;
        }

        /**
         * Used for sorting in descending order
         */
        static class SortById implements Comparator<JMSNotificationMessage> {

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
        public String getDataColumnIdentifier(int columnIndex) {
            return null;
        }

        @Override
        public Class getDataColumnClass(int columnIndex) {
            return null;
        }

        @Override
        public Object getDataValueAt(int rowIndex, int columnIndex) {
            return null;
        }

        @Override
        public int[] getKeysColumn() {
            return new int[0];
        }

        @Override
        public int getInfoColumn() {
            return 0;
        }

        @Override
        public void setName(String name) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Map<String, Object> getExternalData() {
            return null;
        }

        @Override
        public PlotInformation getPlotInformation() {
            return null;
        }

        @Override
        public String getColumnName(int col) {
            return m_columnNames[col];
        }

        @Override
        public int getRowCount() {

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
            return new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(getColumnClass(col)), JLabel.LEFT);
        }
    }

    public static class DateAndTimeRenderer extends DefaultTableCellRenderer {

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

    public static class EventTypeRenderer extends DefaultTableCellRenderer {

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
