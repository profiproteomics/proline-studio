/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.tasklog;

import fr.proline.logviewer.gui.Config;
import fr.proline.logviewer.gui.ControlInterface;
import fr.proline.logviewer.gui.LogReaderWorker;
import fr.proline.logviewer.gui.TaskConsolePane;
import fr.proline.logviewer.gui.ColorPalette;
import fr.proline.logviewer.gui.TaskLoaderWorker;
import fr.proline.logviewer.gui.TaskView;
import fr.proline.logviewer.model.LogLineReader;
import fr.proline.logviewer.model.LogTask;
import fr.proline.logviewer.model.TaskInJsonCtrl;
import fr.proline.logviewer.model.Utility;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author KX257079
 */
public class ServerLogControlPanel extends JPanel implements ControlInterface {

    protected static final Logger m_logger = LoggerFactory.getLogger(ServerLogControlPanel.class);
    private TaskConsolePane m_taskConsole;
    private ServerLogTaskListView m_taskQueueView;
    private TaskView m_taskView;
    private JInternalFrame m_taskFlowFrame;
    private JTextPane m_taskFlowTextPane;
    private File m_file;
    Utility.DATE_FORMAT m_dateFormat = Utility.DATE_FORMAT.SHORT;
    InfoToggleButton m_infoToggleButton;
    boolean m_isBigFile;

    public ServerLogControlPanel(File localFile) {
        super();
        m_file = localFile;
        m_taskQueueView = new ServerLogTaskListView(this);
        m_taskView = new TaskView();
        m_taskConsole = new TaskConsolePane();
        m_isBigFile = false;
        this.setBackground(Color.white);
        initComponent();
        this.setSize(1400, 800);
        logParse();
    }

    private void initComponent() {
        //upPane
        JPanel upPane = new JPanel(new BorderLayout());
        ColorPalette colorPalette = new ColorPalette(0, 0);
        colorPalette.setAlignmentX(RIGHT_ALIGNMENT);
        JPanel colorPane = new JPanel(new FlowLayout());
        colorPane.add(new JLabel("Number of task in it's color: "));
        colorPane.add(colorPalette);
        upPane.add(colorPane, BorderLayout.NORTH);
        upPane.add(m_taskQueueView, BorderLayout.CENTER);
        //bottonPane
        JScrollPane taskPane = new JScrollPane(m_taskView);
        JSplitPane contentBottomPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taskPane, m_taskConsole);
        contentBottomPanel.setDividerLocation(680);//about half 
        //mainPane
        JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainPanel.setTopComponent(upPane);
        mainPanel.setBottomComponent(contentBottomPanel);
        mainPanel.setDividerLocation(300);
        //this
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(initToolbar(), BorderLayout.WEST);
        //invisible TaskFlowPane
        m_taskFlowTextPane = new JTextPane();
        m_taskFlowFrame = new JInternalFrame("Log Task Flow", true, true);
        m_taskFlowFrame.add(new JScrollPane(m_taskFlowTextPane));
        m_taskFlowFrame.setSize(700, 750);
        m_taskFlowFrame.setVisible(false);

    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        FilterButton filterButton = new FilterButton(m_taskQueueView.getCompoundTableModel()) {
            @Override
            protected void filteringDone() {
            }
        };
        toolbar.add(filterButton);

        JButton showTaskFlowBt = new JButton(IconManager.getIcon(IconManager.IconType.DOCUMENT_LIST));

        showTaskFlowBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_taskFlowFrame.setVisible(true);
            }
        });
        toolbar.add(showTaskFlowBt);

        m_infoToggleButton = new InfoToggleButton(null, m_taskQueueView.getTable());
        toolbar.add(m_infoToggleButton);

        return toolbar;
    }

    private void logParse() {
        try {
            LogLineReader logReader;
            m_logger.info("File to analyse: " + m_file.getName() + ".");
            long fileLength = m_file.length();
            long bigFileSize = Config.getBigFileSize();

            if (fileLength > bigFileSize) {
                m_isBigFile = true;
            }
            String fileName = m_file.getName();
            logReader = new LogLineReader(m_file.getName(), m_dateFormat, m_isBigFile, false);

            LogReaderWorker readWorker = new LogReaderWorker(this, m_taskFlowTextPane, m_file, m_dateFormat, logReader);
            m_taskFlowFrame.setVisible(true);
            m_taskFlowFrame.requestFocus();
            readWorker.execute();
            repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex + "\n" + ex.getStackTrace()[0], "Exception", JOptionPane.ERROR_MESSAGE);
            StackTraceElement[] trace = ex.getStackTrace();
            String result = "";
            for (StackTraceElement el : trace) {
                result += el.toString() + "\n";
            }
            m_logger.error(ex + "\n" + result);
        }
    }

    public void valueChanged(LogTask selectedTask) {
        //long begin = System.currentTimeMillis();
        String order = "";
        if (selectedTask != null) {
            order = "" + selectedTask.getTaskOrder();
        }
        //System.out.println("task " + order + ": " + Utility.getMemory());

        m_taskView.setData(selectedTask);
        //System.out.println("task " + order + " view  show time " + (System.currentTimeMillis() - begin));
        //begin = System.currentTimeMillis();
        if (selectedTask == null) {
            m_taskConsole.setData("");

        } else {
            m_taskConsole.setData("In loading...");
            ArrayList<LogTask.LogLine> trace = selectedTask.getTrace();
            if (trace == null) {
                trace = TaskInJsonCtrl.getInstance().getCurrentTask(selectedTask.getTaskOrder()).getTrace();
            }
            TaskLoaderWorker taskLoader = new TaskLoaderWorker(trace, this);
            taskLoader.execute();
        }
    }

    public synchronized void setData(ArrayList<LogTask> tasks, String fileName) {
        m_logger.debug("setData {} tasks for {}", tasks.size(), fileName);
        m_taskQueueView.setData(tasks, fileName);
        if (tasks == null || tasks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No task to show");
            m_taskView.setData(null);
            m_taskConsole.setData("");
        }
        super.requestFocus();
    }

    public void clear() {
        m_taskQueueView.setData(null, null);
        m_taskView.setData(null);
        m_taskConsole.setData("");
    }

    @Override
    public boolean isBigFile() {
        return m_isBigFile;
    }

    @Override
    public String getAnalysedTaskName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TaskConsolePane getConsole() {
        return m_taskConsole;
    }
}
