/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.profi.util.StringUtils;
import fr.proline.studio.dpm.data.JMSNotificationMessage;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.dpm.task.util.JMSMessageUtil;
import fr.proline.studio.dpm.task.util.ServiceNotificationListener;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableCellRenderer;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class SystemTasksPanel extends JPanel implements DataBoxPanelInterface {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
   
    private AbstractDataBox m_dataBox;
    private SystemMessageTable m_messageTable;
    private JButton m_refreshButton;
    private JButton m_clearButton;
    private boolean m_isRunning;
    private QueueBrowser m_qBrowser = null;
    private ServiceNotificationListener m_serviceListener = null;
            
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

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_refreshButton = new JButton(IconManager.getIcon(IconManager.IconType.EXECUTE));         
        m_refreshButton.setToolTipText("Connect to System tasks logs");
        m_refreshButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!m_isRunning) {
                    startCollectData();
                } else {
                    stopCollectData();
                }
            }
        });       
        toolbar.add(m_refreshButton);
        
        m_clearButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        m_clearButton.setToolTipText("Clear system tasks list");                      
        m_clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearData();
            }
        });       
        toolbar.add(m_clearButton);

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
       ((SystemMessageTableModel) m_messageTable.getModel()).setData(null);
    }

    private boolean checkJMSVariables(){
        if(m_qBrowser == null){
            m_qBrowser = JMSConnectionManager.getJMSConnectionManager().getQueueBrowser();
            if(m_qBrowser == null){
                JOptionPane.showMessageDialog( WindowManager.getDefault().getMainWindow(), "Unable to get JMS Queue Browser", "Server Tasks Logs error",JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        if(m_serviceListener == null) {
            m_serviceListener =  JMSConnectionManager.getJMSConnectionManager().getNotificationListener();
             if(m_serviceListener == null) {
                JOptionPane.showMessageDialog( WindowManager.getDefault().getMainWindow(), "Unable to get Notification Listener (JMS Connection problem ?!)", "Server Tasks Logs error",JOptionPane.ERROR_MESSAGE);
                return false;                 
             }
        }
        return true;
    }
    
    private void startCollectData() {
      
        if(checkJMSVariables()) {
            final JMSNotificationMessage[] sysInfoResult = new JMSNotificationMessage[1];
            AbstractJMSCallback callback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    ((SystemMessageTableModel) m_messageTable.getModel()).addData(sysInfoResult[0]);
                    browsePendingMessages();
                }
            };

            m_serviceListener.setServiceNotifierCallback(callback, sysInfoResult);
            m_isRunning = true;
            m_refreshButton.setIcon(IconManager.getIcon(IconManager.IconType.STOP));
            m_refreshButton.setToolTipText("Unconnect System tasks logs");
        }
    }
    
    private void browsePendingMessages() {
        try {

            Enumeration<Message> messageEnum = m_qBrowser.getEnumeration();
            List<JMSNotificationMessage> pendingMsg = new ArrayList<>();

	    while (messageEnum.hasMoreElements()) {
		Message msg = messageEnum.nextElement();
                JMSNotificationMessage notifMsg = JMSMessageUtil.buildJMSNotificationMessage(msg);
                if(notifMsg != null){
                    pendingMsg.add(notifMsg);
                    m_logger.debug(notifMsg.toString());
                } else 
                    m_logger.debug("Invalid message in Queue ! "+msg);                
	    }

            ((SystemMessageTableModel) m_messageTable.getModel()).setPendingData(pendingMsg);                
            
        } catch (Exception jmsE){
           JOptionPane.showMessageDialog( WindowManager.getDefault().getMainWindow(), "Unable to browse pending messages (JMS Connection problem ?! : "+jmsE.getMessage()+")", "Server Tasks Logs error",JOptionPane.ERROR_MESSAGE);
        }           
    }
    
    
    private void stopCollectData() {        
        m_serviceListener.setServiceNotifierCallback(null, null);
        m_isRunning = false;
        m_refreshButton.setIcon(IconManager.getIcon(IconManager.IconType.EXECUTE));                
        m_refreshButton.setToolTipText("Connect to System tasks logs");
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


    class SystemMessageTable extends DecoratedTable {
        
        public SystemMessageTable(){
            super();
            setDefaultRenderer(Date.class, new TableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {    
                        SimpleDateFormat sf = new SimpleDateFormat("h:mm:ss a - d MMM yyyy ");
                        return new JLabel(sf.format((Date)value));
                    }
            });
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

        public static final int COLTYPE_MESSAGE_JSON_RPC_ID = 0;
        public static final int COLTYPE_MESSAGE_SERVICE_NAME = 1;        
        public static final int COLTYPE_MESSAGE_EVENT_TYPE = 2;
        public static final int COLTYPE_MESSAGE_EVENT_DATE = 3;
        public static final int COLTYPE_MESSAGE_SERVICE_SOURCE = 4;
        public static final int COLTYPE_MESSAGE_REQ_ID = 5;
        public static final int COLTYPE_MESSAGE_COMPLEMENTARY_DATA = 6;

        private final String[] m_columnNames = {"JsonRPC Id", "Service Name [Version]", "State", "Date", "Source", "Message ID", "Supplementary Data"};
        private final String[] m_columnTooltips = {"Identifier of JSON RPC message", "Proline Service Name and Version", "Current Service State (START, FAIL, SUCCESS)", "Date of the event on the service", "The source (user/computer) of the service call","Message ID","All complementary data we can get"};

        private JMSNotificationMessage[] m_data = null;

        public void setData(JMSNotificationMessage[] data ){
            m_data = data;
            fireTableDataChanged();
        }
        
        public void setPendingData(List<JMSNotificationMessage> data ){
            List <JMSNotificationMessage> tmpMessages = new ArrayList();
            for(JMSNotificationMessage nextMsg : m_data){
                if(!nextMsg.getEventType().equals(JMSMessageUtil.PENDING_MESSAGE_STATUS)){
                    tmpMessages.add(nextMsg);
                }
            }
            tmpMessages.addAll(data);
            m_data = tmpMessages.toArray(new JMSNotificationMessage[tmpMessages.size()]);
            fireTableDataChanged();            
        }
        
        public void addData(JMSNotificationMessage msg ){
            if(m_data != null)
                m_data = Arrays.copyOf(m_data, m_data.length+1);
            else
                m_data = new JMSNotificationMessage[1];
            m_data[m_data.length-1] = msg;
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
            if (m_data == null) {
                return 0;
            }

            return m_data.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            JMSNotificationMessage msg = m_data[rowIndex];
            switch (columnIndex) {
                case COLTYPE_MESSAGE_JSON_RPC_ID:
                    return msg.getJsonRPCMsgId();  
                case COLTYPE_MESSAGE_SERVICE_NAME:
                    StringBuilder sb = new StringBuilder(msg.getServiceName());
                    if(StringUtils.isNotEmpty( msg.getServiceVersion()))
                        sb.append(" [").append(msg.getServiceVersion()).append("]");
                    return sb.toString();
                case COLTYPE_MESSAGE_EVENT_TYPE:
                    return msg.getEventType();
                case COLTYPE_MESSAGE_EVENT_DATE:
                    return msg.getEventDate();
                case COLTYPE_MESSAGE_SERVICE_SOURCE:
                    return msg.getServiceSource();
                case COLTYPE_MESSAGE_REQ_ID:
                    return msg.getRequestMsgId();
                case COLTYPE_MESSAGE_COMPLEMENTARY_DATA:
                    return (msg.getServiceInfo() != null) ? msg.getServiceInfo() : "-" ;
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
                case COLTYPE_MESSAGE_EVENT_TYPE:
                case COLTYPE_MESSAGE_SERVICE_SOURCE:
                case COLTYPE_MESSAGE_REQ_ID:
                case COLTYPE_MESSAGE_COMPLEMENTARY_DATA:
                    return String.class;
                case COLTYPE_MESSAGE_EVENT_DATE:
                    return Date.class;

            }
            return null; // should not happen
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            return new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(getColumnClass(col)));
        }

    }
}
