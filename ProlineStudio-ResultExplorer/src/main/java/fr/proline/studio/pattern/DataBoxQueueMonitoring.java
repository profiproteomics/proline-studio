/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.studio.dpm.task.util.QueueMonitor;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

/**
 *
 * @author VD225637
 */
public class DataBoxQueueMonitoring extends AbstractDataBox {

    private JTextArea m_txtArea;
    private AbstractDataBox m_dataBox;
    private QueueMonitor m_queueMonitor = null;
    public DataBoxQueueMonitoring() {
        super(DataboxType.DataBoxSystemTasks);
        m_typeName = "Queue Monotoring";
    }

    @Override
    public void createPanel() {

        m_panel = new QueueMonotorPanel();

    }

    @Override
    public void dataChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    class QueueMonotorPanel extends JPanel implements DataBoxPanelInterface {

        public QueueMonotorPanel() {

            setLayout(new BorderLayout());

            JPanel internalPanel = new JPanel();
            internalPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            // create objects
            JScrollPane scrollPane = new JScrollPane();
            m_txtArea = new JTextArea();
            m_txtArea.setRows(20);
            m_txtArea.setColumns(50);
            m_txtArea.setText("Click on Refresh button to get list of all pending messages.\n");
            scrollPane.setViewportView(m_txtArea);

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.gridwidth = 3;
            internalPanel.add(scrollPane, c);

            add(internalPanel, BorderLayout.CENTER);

            JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
            toolbar.setFloatable(false);

            RefreshButton refreshButton = new RefreshButton();
            toolbar.add(refreshButton);
            ClearButton clearButton = new ClearButton();
            toolbar.add(clearButton);
            add(toolbar, BorderLayout.WEST);
        }

        @Override
        public void addSingleValue(Object v) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

        private void updateMessagesList() {
            if(m_queueMonitor == null)
                m_queueMonitor = new QueueMonitor();
            String newMessage = m_queueMonitor.browse();
            StringBuilder sb = new StringBuilder();
            sb.append(m_txtArea.getText()).append("\n\n");
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
            sb.append(" List of message at ").append(sdf.format(new Date())).append("\n");
            sb.append(newMessage);
            m_txtArea.setText(sb.toString());
        }
        
        private void clearMessagesList(){
            m_txtArea.setText("Click on Refresh button to get list of all pending messages.\n");
        }

        public class RefreshButton extends JButton implements ActionListener {

            public RefreshButton() {

                setIcon(IconManager.getIcon(IconManager.IconType.REFRESH));
                setToolTipText("Refresh pending messages list.");

                addActionListener(this);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                updateMessagesList();
            }
        }
        
        public class ClearButton extends JButton implements ActionListener {

            public ClearButton() {

                setIcon(IconManager.getIcon(IconManager.IconType.ERASER));
                setToolTipText("Clear list of pending messages.");

                addActionListener(this);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                clearMessagesList();
            }
        }
    }

}
