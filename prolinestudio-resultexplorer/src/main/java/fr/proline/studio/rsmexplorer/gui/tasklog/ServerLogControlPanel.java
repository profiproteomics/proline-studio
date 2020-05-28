/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.tasklog;

import fr.proline.logparser.gui.LogControlPanel;
import fr.proline.logparser.gui.LogReaderWorker;
import fr.proline.logparser.gui.TaskConsolePane;
import fr.proline.logparser.gui.TaskView;
import fr.proline.logparser.model.LogLineReader;
import fr.proline.logparser.model.Utility;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author KX257079
 */
public class ServerLogControlPanel extends LogControlPanel {

    protected static final Logger m_logger = LoggerFactory.getLogger(ServerLogControlPanel.class);

    private JInternalFrame m_taskFlowFrame;
    private JTextPane m_taskFlowTextPane;
    private ArrayList<File> m_fileList;
    Utility.DATE_FORMAT m_dateFormat = Utility.DATE_FORMAT.SHORT;
    InfoToggleButton m_infoToggleButton;
    private int BIG_FILE_SIZE = 4 * 1024 * 1024;//4 Mega
    LogLineReader m_logReader;
    LogReaderWorker m_readWorker;
    JProgressBar m_progressBar;

    public ServerLogControlPanel(ArrayList<File> localFileList, JInternalFrame taskFlowFrame, JProgressBar progressBar) {
        super();
        initParameters();
        m_fileList = localFileList;
        m_taskQueueView = new ServerLogTaskListView(this);
        m_taskView = new TaskView();
        m_taskConsole = new TaskConsolePane();
        this.setBackground(Color.white);
        m_progressBar = progressBar;
        m_taskFlowFrame = taskFlowFrame;
        m_taskFlowTextPane = new JTextPane();
        m_taskFlowFrame.getContentPane().add(new JScrollPane(m_taskFlowTextPane));
        initComponent();
        this.setSize(1400, 800);
        parseFile();
    }

    private void initComponent() {
        //upPane
        JPanel upPane = new JPanel(new BorderLayout());

        upPane.add((JScrollPane) m_taskQueueView, BorderLayout.CENTER);
        //bottonPane
        JScrollPane taskPane = new JScrollPane(m_taskView);
        JSplitPane contentBottomPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taskPane, m_taskConsole);
        contentBottomPanel.setDividerLocation(700);//about half 
        //mainPane
        JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainPanel.setTopComponent(upPane);
        mainPanel.setBottomComponent(contentBottomPanel);
        mainPanel.setDividerLocation(280);
        //this
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(initToolbar(), BorderLayout.WEST);
        //invisible TaskFlowPane
        m_taskFlowFrame.setLocation(700, 300);

    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        //FilerButton
        FilterButton filterButton = new FilterButton(((ServerLogTaskListView) m_taskQueueView).getCompoundTableModel()) {
            @Override
            protected void filteringDone() {
            }
        };
        toolbar.add(filterButton);
        //Show Task flow button
        JButton showTaskFlowBt = new JButton(IconManager.getIcon(IconManager.IconType.DOCUMENT_LIST));
        showTaskFlowBt.setToolTipText("Show task flow");
        showTaskFlowBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_taskFlowFrame.setVisible(true);
            }
        });
        toolbar.add(showTaskFlowBt);

        ServerLogTaskListView taskListPane = (ServerLogTaskListView) m_taskQueueView;
        //Export Button
        ExportButton exportButton = new ExportButton(taskListPane.getCompoundTableModel(), "Tasks", taskListPane.getTable());
        toolbar.add(exportButton);
        //
        m_infoToggleButton = new InfoToggleButton(null, taskListPane.getTable());
        toolbar.add(m_infoToggleButton);

        return toolbar;
    }

    static String KEY_LOG_DATE_FORMAT = "Server_log_parse_date_format";

    public void saveParameters() {
        Preferences preferences = NbPreferences.root();
        preferences.put(KEY_LOG_DATE_FORMAT, m_dateFormat.name());
    }

    public void initParameters() {
        Preferences preferences = NbPreferences.root();
        String format = preferences.get(KEY_LOG_DATE_FORMAT, "SHORT");
        if (format.equals(Utility.DATE_FORMAT.SHORT.name())) {
            m_dateFormat = Utility.DATE_FORMAT.SHORT;
        } else {
            m_dateFormat = Utility.DATE_FORMAT.NORMAL;
        }
    }

    /**
     * Main Method
     */
    private void parseFile() {
        try {
            LogLineReader logReader;
            String fileName = m_fileList.get(0).getName();
            m_logger.info("File to analyse: " + fileName + ".");

            m_logReader = new LogLineReader(fileName, m_dateFormat, isBigFile(), false);

            m_readWorker = new LogReaderWorker(this, m_taskFlowTextPane, m_fileList, m_dateFormat, m_logReader);
            m_taskFlowFrame.setVisible(true);
            m_readWorker.execute();
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

    @Override
    public boolean isBigFile() {
        long size = 0;
        for (File file : m_fileList) {
            size += file.length();
        }
        return size > BIG_FILE_SIZE;
    }

    static int MAX_LINE_TO_SHOW = 5000;

    @Override
    public int getMaxLine2Show() {
        return MAX_LINE_TO_SHOW;
    }

    @Override
    public void redo() {
        if (m_dateFormat.equals(Utility.DATE_FORMAT.NORMAL)) {
            m_dateFormat = Utility.DATE_FORMAT.SHORT;
        } else {
            m_dateFormat = Utility.DATE_FORMAT.NORMAL;
        }
        saveParameters();
        parseFile();
    }

    @Override
    public void setProgressBarVisible(boolean visible) {
        m_progressBar.setVisible(visible);
        m_taskFlowFrame.setVisible(visible);
    }

    @Override
    public void setProgress(int percent) {
        m_progressBar.setValue(percent);
    }
}
