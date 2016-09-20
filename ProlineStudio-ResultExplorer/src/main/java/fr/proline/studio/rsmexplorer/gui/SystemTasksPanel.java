/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.profi.util.StringUtils;
import fr.proline.studio.dpm.data.JMSNotificationMessage;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.PurgeConsumer;
import fr.proline.studio.dpm.task.util.ConnectionListener;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.dpm.task.util.JMSMessageUtil;
import fr.proline.studio.dpm.task.util.ServiceNotificationListener;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.dialog.GetSystemInfoButtonAction;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 * TODO : Merge some code with TasksPanel !!
 */
public class SystemTasksPanel extends JPanel implements DataBoxPanelInterface, ConnectionListener {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private final static ImageIcon[] PUBLIC_STATE_ICONS = { IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16), IconManager.getIcon(IconManager.IconType.ARROW_RIGHT_SMALL), IconManager.getIcon(IconManager.IconType.CROSS_BLUE_SMALL16), IconManager.getIcon(IconManager.IconType.TICK_SMALL), IconManager.getIcon(IconManager.IconType.CROSS_SMALL16)};

    private AbstractDataBox m_dataBox;
    private SystemMessageTable m_messageTable;
//    private JButton m_refreshButton;
//    private JButton m_clearButton;
    private boolean m_isRunning;
    
    private ServiceNotificationListener m_serviceListener = null;
    private AbstractJMSCallback m_notifierCallback = null; //Callback to be called by ServiceNotificationListener
    
    private AbstractJMSCallback m_purgerCallback = null; //Callback to be called by purger
    
    //Timer to monitor QueueBrowser
    private QueueBrowser m_qBrowser = null;
    private static final int UPDATE_DELAY = 1000;
    private Timer m_updateTimer = null;
    
    public SystemTasksPanel() {

        setLayout(new GridBagLayout());
        this.m_isRunning = false;
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.weightx = 1;
        c.weighty = 1;
        add(createToolbarPanel(), c);                              
    }

    /**
     * Listener of JMS Connection state change : to connect/disconnect from topic
     */
    public void initListener(){
        JMSConnectionManager.getJMSConnectionManager().addConnectionListener(this);
        int currentState =  JMSConnectionManager.getJMSConnectionManager().getConnectionState();
        connectionStateChanged(currentState);
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
//        m_refreshButton = new JButton(IconManager.getIcon(IconManager.IconType.EXECUTE));         
//        m_refreshButton.setToolTipText("Connect to System tasks logs");
//        m_refreshButton.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (!m_isRunning) {
//                    startCollectData();
//                } else {
//                    stopCollectData();
//                }
//            }
//        });       
//        toolbar.add(m_refreshButton);
        
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
        return toolbar;
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

    private boolean checkJMSVariables(){
        if(m_qBrowser == null){
            m_qBrowser = JMSConnectionManager.getJMSConnectionManager().getQueueBrowser();
            if(m_qBrowser == null){
                JOptionPane.showMessageDialog( WindowManager.getDefault().getMainWindow(), "Unable to get JMS Queue Browser. Try Later", "Server Tasks Logs error",JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        if(m_serviceListener == null) {
            m_serviceListener =  JMSConnectionManager.getJMSConnectionManager().getNotificationListener();
             if(m_serviceListener == null) {
                JOptionPane.showMessageDialog( WindowManager.getDefault().getMainWindow(), "Unable to get Notification Listener (JMS Connection problem ?!). Try Later", "Server Tasks Logs error",JOptionPane.ERROR_MESSAGE);
                return false;                 
             }
        }
        return true;
    }
    
 
    /**
     * Start being notified from message topic & PurgeConsumer 
     */
    private void startCollectData() {
      
        if(!m_isRunning && checkJMSVariables()) {
            //Register for Service Notification
            final JMSNotificationMessage[] sysInfoResult = new JMSNotificationMessage[1];
            m_notifierCallback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    
                    int selectedRow = m_messageTable.getSelectedRow();
                    if(selectedRow >= 0)
                        selectedRow = m_messageTable.convertRowIndexToModel(selectedRow);
                                
                    ((SystemMessageTableModel) m_messageTable.getModel()).addMessage(sysInfoResult[0]);
                    
                    if(selectedRow>=0){
                        int modelIndex = m_messageTable.convertRowIndexToView(selectedRow);
                        m_messageTable.setRowSelectionInterval(modelIndex, modelIndex);
                    }
                }
            };
            m_serviceListener.addServiceNotifierCallback(m_notifierCallback, sysInfoResult);
            
            //Register for Purger Notification
            final JMSNotificationMessage[] purgerResult = new JMSNotificationMessage[1];
            m_purgerCallback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                                       int selectedRow = m_messageTable.getSelectedRow();
                    if(selectedRow >= 0)
                        selectedRow = m_messageTable.convertRowIndexToModel(selectedRow);
                                
                    ((SystemMessageTableModel) m_messageTable.getModel()).addMessage(purgerResult[0]);
                    
                    if(selectedRow>=0){
                        int modelIndex = m_messageTable.convertRowIndexToView(selectedRow);
                        m_messageTable.setRowSelectionInterval(modelIndex, modelIndex);
                    }
                    
                }
            };
            PurgeConsumer.getPurgeConsumer().addCallback(m_purgerCallback, purgerResult);
            
            startBrowsePendingMsg(); // Start browsing JMS Proline Queue
            m_isRunning = true;
//            m_refreshButton.setIcon(IconManager.getIcon(IconManager.IconType.STOP));
//            m_refreshButton.setToolTipText("Unconnect System tasks logs");
        }
    }
    
    private void stopBrowsingPendingMsg(){
        m_updateTimer.stop();
    }
    
    private void startBrowsePendingMsg(){
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
                if(notifMsg != null){
                    pendingMsg.add(notifMsg);
                    m_logger.debug(notifMsg.toString());
                } else 
                    m_logger.debug("Invalid message in Queue ! "+msg);                
	    }
            
            int selectedRow = m_messageTable.getSelectedRow();
            if(selectedRow >= 0)
                selectedRow = m_messageTable.convertRowIndexToModel(selectedRow);
            ((SystemMessageTableModel) m_messageTable.getModel()).addMessages(pendingMsg);      
            if(selectedRow>=0){
                int modelIndex = m_messageTable.convertRowIndexToView(selectedRow);
                m_messageTable.setRowSelectionInterval(modelIndex, modelIndex);
            }
                    
            
        } catch (Exception jmsE){
           JOptionPane.showMessageDialog( WindowManager.getDefault().getMainWindow(), "Unable to browse pending messages (JMS Connection problem ?! : "+jmsE.getMessage()+")", "Server Tasks Logs error",JOptionPane.ERROR_MESSAGE);
        }           
    }
    
    /**
     * Stop being notified for message/tasks management
     */
    private void stopCollectData() {   
        if(m_isRunning){
            m_serviceListener.removeCallback(m_notifierCallback);
            PurgeConsumer.getPurgeConsumer().removeCallback(m_purgerCallback);
            m_purgerCallback = null;
            m_notifierCallback = null;
            stopBrowsingPendingMsg();
            m_isRunning = false;
    //        m_refreshButton.setIcon(IconManager.getIcon(IconManager.IconType.EXECUTE));                
    //        m_refreshButton.setToolTipText("Connect to System tasks logs");
        }
    }

    @Override
    public void addSingleValue(Object v) {
        throw new UnsupportedOperationException("Not available for System Tasks Panel.");
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

    @Override
    public void setLoading(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLoading(int id, boolean calculating) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLoaded(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectionStateChanged(int newStatus) {
            switch(newStatus) {
            case CONNECTION_DONE: 
                if(!m_isRunning){
                    m_qBrowser = null;
                    m_serviceListener= null;
                    startCollectData();
                }
                break;
            case CONNECTION_FAILED :
            case NOT_CONNECTED:
                if(m_isRunning)
                    stopCollectData();
                break;                        
        }
    }
    

    class SystemMessageTable extends DecoratedTable {
        
        public SystemMessageTable(){
            super();
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        }

        @Override
        public TablePopupMenu initPopupMenu() {
//            TablePopupMenu popupMenu = new TablePopupMenu();
//            popupMenu.addAction(new StopTaskAction());

//            return popupMenu;
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

        private final String[] m_columnNames = {"","Id", "Service",  "Date", "Source", "Message ID", "Supplementary Data"};
        private final String[] m_columnTooltips = {"Current Service State (START, FAIL, SUCCESS, PENDING)","Identifier of JSON RPC message", "Proline Service Description",  "Date of the event on the service", "The source (user/computer) of the service call","Message Server Unique ID","All complementary data we can get: service name/version ..."};

        private ArrayList<JMSNotificationMessage> m_notificationMsgs = new ArrayList<>();
        private HashMap<String,Integer> indexByMsgId = new HashMap<>();

        public void resetData( ){
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
 
        public void addMessage(JMSNotificationMessage msg ){          
            int sr = m_messageTable.getSelectedRow();
            addSingleMessage(msg);   
            fireTableDataChanged();
        }
        
       synchronized private void addSingleMessage(JMSNotificationMessage msg){
            String msgId  = msg.getServerUniqueMsgId();
            if(indexByMsgId.containsKey(msgId)){
                int index= indexByMsgId.get(msgId);
                m_notificationMsgs.set(index, msg);
            } else {
                m_notificationMsgs.add(msg);
                indexByMsgId.put(msgId, m_notificationMsgs.size()-1);
            }
        }
        
        public void addMessages(List<JMSNotificationMessage> msgs ){
            for(JMSNotificationMessage msg : msgs){
                addSingleMessage(msg); 
            }            
            fireTableDataChanged();
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
                    return msg.getJsonRPCMsgId();  
                case COLTYPE_MESSAGE_SERVICE_NAME:
                    if(StringUtils.isNotEmpty(msg.getServiceDescription())){
                        return msg.getServiceDescription();
                    } else {
                        StringBuilder sb = new StringBuilder(msg.getServiceName());
                        if(StringUtils.isNotEmpty( msg.getServiceVersion()))
                            sb.append(" [").append(msg.getServiceVersion()).append("]");
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
                    if(StringUtils.isNotEmpty(msg.getServiceDescription())){
                        sb.append(msg.getServiceName());                            
                        if(StringUtils.isNotEmpty( msg.getServiceVersion()))
                            sb.append(" [").append(msg.getServiceVersion()).append("]");
                    } 
                    if(StringUtils.isNotEmpty(msg.getServiceInfo()))
                        sb.append(" ").append(msg.getServiceInfo());
                    else if(sb.length()<0)
                        sb.append("-");
                    
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
            setText(sf.format((Date)value));

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
    
    
//    private class StopTaskAction extends AbstractTableAction {
//
//        public StopTaskAction() {
//            super("Remove System Task");
//        }
//
//        @Override
//        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
//            SystemMessageTableModel tableModel = (SystemMessageTableModel) m_messageTable.getModel();
//            int nbSelectedRset = selectedRows.length;
//            if(nbSelectedRset != 1 ){
//                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),"Only one message can be aborted at the same time","Stop System Task Error",JOptionPane.ERROR_MESSAGE);
//                return;
//            }                
//            
//            int rowInModel = m_messageTable.convertRowIndexToModel(selectedRows[0]);
//            JMSNotificationMessage notifMsg  = tableModel.getMessage(rowInModel);
//            if(!notifMsg.getEventType().equals(JMSNotificationMessage.MessageStatus.PENDING )){
//                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),"Only Pending message can be aborted","Stop System Task Error",JOptionPane.ERROR_MESSAGE);
//                return;
//            }                  
//            
//            final JMSNotificationMessage[] sysInfoResult = new JMSNotificationMessage[1];
//            AbstractJMSCallback notifierCallback = new AbstractJMSCallback() {
//
//                @Override
//                public boolean mustBeCalledInAWT() {
//                    return true;
//                }
//
//                @Override
//                public void run(boolean success) {
//                    ((SystemMessageTableModel) m_messageTable.getModel()).addMessage(sysInfoResult[0]);
//                    browsePendingMessages();
//                }
//            };
//            PurgeConsumer purger = new PurgeConsumer(notifierCallback, notifMsg.getServerUniqueMsgId(), sysInfoResult);
//            purger.clearMessage();
//        }
//
//        @Override
//        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
//            SystemMessageTableModel tableModel = (SystemMessageTableModel) m_messageTable.getModel();
//            
//            boolean enable = true;
//            if(selectedRows.length != 1 )
//                enable = false;
//            else {
//                int rowInModel = m_messageTable.convertRowIndexToModel(selectedRows[0]);
//                JMSNotificationMessage notifMsg  = tableModel.getMessage(rowInModel);
//                if(!notifMsg.getEventType().equals(JMSNotificationMessage.MessageStatus.PENDING))
//                enable = false;
//            }
//            
//            setEnabled(enable);            
//        }
//    }
}
