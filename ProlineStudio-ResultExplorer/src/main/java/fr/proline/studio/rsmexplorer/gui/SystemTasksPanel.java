/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.dpm.data.JMSNotificationMessage;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author VD225637
 */
public class SystemTasksPanel extends JPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private ArrayList<JMSNotificationMessage> receivedMsgs = new ArrayList<>();
    private SystemMessageTable m_messageTable;

    public SystemTasksPanel() {
        setLayout(new GridBagLayout());
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

        ConnectButton refreshButton = new ConnectButton();
        toolbar.add(refreshButton);
        
        ClearButton clearButton = new ClearButton();
        toolbar.add(clearButton);

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
//
//    private String constructString() {
//        StringBuilder sb = new StringBuilder();
//        for (JMSNotificationMessage jmsMsg : receivedMsgs) {
//            sb.append("Service : ").append(jmsMsg.getServiceName()).append("\n");
//            sb.append("Event : ").append(jmsMsg.getEventType()).append("\n");
//            sb.append("Date : ").append(jmsMsg.getEventDate().toString()).append("\n");
//            sb.append("JsonRPC ID : ").append(jmsMsg.getJsonRPCMsgId()).append("\n");
//            sb.append("Message ID : ").append(jmsMsg.getRequestMsgId()).append("\n");
//
//            sb.append("\n\n");
//
//        }
//        return sb.toString();
//    }

    public void clearData() {
        receivedMsgs.clear();
       ((SystemMessageTableModel) m_messageTable.getModel()).setData(null);
    }

    private void startCollectData() {
        final JMSNotificationMessage[] sysInfoResult = new JMSNotificationMessage[1];
        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                receivedMsgs.add(sysInfoResult[0]);
                ((SystemMessageTableModel) m_messageTable.getModel()).addData(sysInfoResult[0]);
            }
        };

        ServiceNotificationListener listener = JMSConnectionManager.getJMSConnectionManager().getNotificationListener();
        listener.setServiceNotifierCallback(callback, sysInfoResult);
    }
    
    private void stopCollectData() {

        ServiceNotificationListener listener = JMSConnectionManager.getJMSConnectionManager().getNotificationListener();
        listener.setServiceNotifierCallback(null, null);
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

    public class ClearButton extends JButton implements ActionListener {

        public ClearButton() {

            setIcon(IconManager.getIcon(IconManager.IconType.ERASER));
            setToolTipText("Clear system tasks list");            
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            clearData();
        }
    }

    public class ConnectButton extends JButton implements ActionListener {

        boolean isRunning = false;
        
        public ConnectButton() {

            setIcon(IconManager.getIcon(IconManager.IconType.EXECUTE));
            setToolTipText("Connect to System tasks logs");

            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(! isRunning){
                startCollectData();
                isRunning = true;
                setIcon(IconManager.getIcon(IconManager.IconType.STOP));
            } else {
                stopCollectData();
                isRunning = false;
                setIcon(IconManager.getIcon(IconManager.IconType.EXECUTE));                
            }
        }
    }

    class SystemMessageTable extends DecoratedTable {
        
        public SystemMessageTable(){
            super();
            setDefaultRenderer(Date.class, new TableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {                        
                        SimpleDateFormat sf = new SimpleDateFormat("h:mm a - d MMM yyyy ");
                        return new JLabel(sf.format((Date)value));
                    }
                }
            );
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
        public static final int COLTYPE_MESSAGE_REQ_ID = 4;

        private final String[] m_columnNames = {"JsonRPC Id", "Service Name", "Event", "Date", "Message ID"};
        private final String[] m_columnTooltips = {"Identifier of JSON RPC message", "Proline Service Name", "Type of event on service", "Date of the event on the service", "Message ID"};

        private JMSNotificationMessage[] m_data = null;

        public void setData(JMSNotificationMessage[] data ){
            m_data = data;
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
                    return msg.getServiceName();
                case COLTYPE_MESSAGE_EVENT_TYPE:
                    return msg.getEventType();
                case COLTYPE_MESSAGE_EVENT_DATE:
                    return msg.getEventDate();
                case COLTYPE_MESSAGE_REQ_ID:
                    return msg.getRequestMsgId();
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
                case COLTYPE_MESSAGE_REQ_ID:
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
