package fr.proline.studio.rsmexplorer.gui.tasklog;

import fr.proline.logparser.gui.ColorPalette;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.DownloadFileTask;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author KX257079
 */
public class ServerLogFileNameDialog extends DefaultDialog {

    protected static final Logger m_logger = LoggerFactory.getLogger(ServerLogFileNameDialog.class);
    private static String LOG_REMOTE_PATH = "./logs/";
    private static String LOG_LOCAL_PATH = "cortexlogs/";
    private static String LOG_DEBUG_FILE_NAME = "proline_cortex_debug";
    private static String LOG_TODAY_DEBUG_FILE_NAME = "proline_cortex_debug.txt";
    private static String LOG_FILE_NAME = "proline_cortex_log";
    private static String LOG_FILE_SUFFIX = ".txt";
    private JPanel m_internalPanel;
    private JComboBox m_dateChooser;
    private JTextField m_fileNameTxtField;
    private JCheckBox m_debugFileCheckBox;
    private int m_dateAdjust;
    /**
     * localFile
     */
    ArrayList<File> m_fileList;
    File m_localPath;

    public ServerLogFileNameDialog() {
        super(WindowManager.getDefault().getMainWindow(), Dialog.ModalityType.APPLICATION_MODAL);
        m_dateAdjust = 0;
        m_internalPanel = createInternalPanel();
        m_fileList = new ArrayList();
        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getServerConnectionManager();
        String host = serverConnectionManager.getServerURL();
        m_localPath = new File(LOG_LOCAL_PATH);
        if (!m_localPath.isDirectory()) {
            m_localPath.mkdir();
        }
        m_localPath = new File(LOG_LOCAL_PATH + host + "/");
        if (!m_localPath.isDirectory()) {
            m_localPath.mkdir();
        }
        String dialog_title = "Server Log File Parser";
        super.setTitle(dialog_title);
        String help_text = "<html>Take a Log File on the server & view tasks. <br> You shold choice the date & type of log file</html>";
        super.setHelpHeaderText(help_text);
        super.setInternalComponent(m_internalPanel);
        super.setButtonVisible(DefaultDialog.BUTTON_HELP, false);//use only cancel, ok button
    }

    private JDialog createLogParserDialog(ArrayList<File> fileList) {

        JDialog logViewDialog = new JDialog(WindowManager.getDefault().getMainWindow(), "Parse Tasks On The Server", Dialog.ModalityType.APPLICATION_MODAL);
        logViewDialog.getContentPane().setLayout(new BorderLayout());
        //task flow
        JInternalFrame taskFlowFrame;
        JTextPane taskFlowTextPane;
        taskFlowTextPane = new JTextPane();
        taskFlowFrame = new JInternalFrame("Log Task Flow", true, true);
        taskFlowFrame.setSize(700, 650);
        taskFlowFrame.setVisible(false);
        logViewDialog.getLayeredPane().add(taskFlowFrame, JLayeredPane.PALETTE_LAYER);
        //color palette
        ColorPalette colorPalette = new ColorPalette();
        colorPalette.setLocation(800, 2);
        colorPalette.setVisible(true);
        logViewDialog.getLayeredPane().add(colorPalette, JLayeredPane.PALETTE_LAYER + 1);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setSize(400, 50);
        progressBar.setVisible(false);
        progressBar.setLocation(200, 200);
        logViewDialog.getLayeredPane().add(progressBar, JLayeredPane.PALETTE_LAYER + 2);
        ServerLogControlPanel logPanel;
        //main logPanel
        logPanel = new ServerLogControlPanel(fileList, taskFlowFrame, progressBar);
        logViewDialog.getContentPane().add(logPanel, BorderLayout.CENTER);
        logViewDialog.pack();
        logViewDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        logViewDialog.setVisible(true);

        return logViewDialog;
    }

    private JPanel createInternalPanel() {
        //FileName
        m_fileNameTxtField = new JTextField(LOG_REMOTE_PATH + getDebugLogFileName(0, 0));
        //Date Combobox
        int maxDuration = 7;
        String[] dateChoice = new String[maxDuration];
        for (int i = 0; i < maxDuration; i++) {
            dateChoice[i] = this.getDateInFileName(i);
        }
        m_dateChooser = new JComboBox(dateChoice);
        m_dateChooser.addActionListener(createDateChoiceActionListener());
        //CheckBox for debug log File vs normal log File
        m_debugFileCheckBox = new JCheckBox("Debug File");
        m_debugFileCheckBox.setSelected(true);
        m_debugFileCheckBox.addItemListener(createIsDebugFileItemListener());
        //layout
        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new java.awt.Insets(0, 1, 1, 0);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.2;
        c.gridwidth = GridBagConstraints.RELATIVE;
        pane.add(new JLabel("File:"), c);
        c.gridx++;
        c.fill = 1;
        c.weightx = 0.8;
        c.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(m_fileNameTxtField, c);
        c.gridy++;
        c.gridx = 0;
        pane.add(new JPanel(), c);//vertical space between filename & date chooser
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0.2;
        c.gridwidth = GridBagConstraints.RELATIVE;
        pane.add(new JLabel("Date"), c);
        c.gridx++;
        c.weightx = 0.8;
        c.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(m_dateChooser, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(m_debugFileCheckBox, c);
        Dimension d = pane.getPreferredSize();
        pane.setBounds(0, 0, (int) d.getWidth(), (int) d.getHeight());
        return pane;
    }

    private ActionListener createDateChoiceActionListener() {
        ActionListener listener = (ActionEvent e) -> {
            m_dateAdjust = m_dateChooser.getSelectedIndex();
            setFileNameTxtField();
        };
        return listener;
    }

    private ItemListener createIsDebugFileItemListener() {
        ItemListener listener = (ItemEvent e) -> {
            setFileNameTxtField();
        };
        return listener;
    }

    private void setFileNameTxtField() {
        String fileName;
        if (m_debugFileCheckBox.isSelected()) {
            fileName = getDebugLogFileName(m_dateAdjust, 0);
        } else {
            fileName = getLogFileName(m_dateAdjust);
        }
        m_fileNameTxtField.setText(LOG_REMOTE_PATH + fileName);
    }

    @Override
    protected boolean okCalled() {
        //retriveConfig();
        String remoteFilePath = m_fileNameTxtField.getText();
        String debugFileP = this.getPrefix(remoteFilePath);
        boolean isDebugFile = (debugFileP != null);

        if (isDebugFile) {
            if (remoteFilePath.equals(LOG_REMOTE_PATH + LOG_TODAY_DEBUG_FILE_NAME)) {//today
                m_isTodayDebug = true;
            }
            retriveFile(debugFileP, isDebugFile, 0);
        } else {
            //only one file
            retriveFile(remoteFilePath, isDebugFile, 0);
        }

        return true;
    }
    boolean m_isTodayDebug = false;

    /**
     * when isDebugFile, filePath is only debugfilePrefix as
     * ./logs/proline_cortex_debug_.yyyy-MM-dd
     *
     * @param filePath, for normal log file, as
     * ./logs/proline_cortex_log.yyyy-MM-dd
     * @param isDebugFile
     * @param index
     */
    private void retriveFile(String filePath, boolean isDebugFile, int index) {
        String remoteFilePath = filePath;
        if (isDebugFile) {
            remoteFilePath = filePath + "." + index + LOG_FILE_SUFFIX;
        }
        int begin = remoteFilePath.lastIndexOf("/") + 1;
        String fileName = remoteFilePath.substring(begin);
        File localFile = new File(m_localPath +"/"+ fileName);
        AbstractJMSCallback callback;
        callback = new AbstractJMSCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (!isDebugFile) {
                    if (success) {
                        m_logger.debug("Retrive file \"" + localFile.getName() + "\" from server succes.");
                        m_fileList.add(localFile);
                        createLogParserDialog(m_fileList);
                    } else {

                        JOptionPane.showMessageDialog(rootPane, "Retrive File \"" + localFile.getName() + "\" failed.\n"
                                + " The file name/path error or it does not exist a so file(by example a off day)");
                    }
                } else {
                    if (success) {
                        m_logger.debug("Retrive file \"" + localFile.getName() + "\" from server succes.");

                        m_fileList.add(localFile);
                        int next = index + 1;
                        retriveFile(filePath, isDebugFile, next);
                    } else {
                        if (index == 0 && ! m_isTodayDebug) {//first file do not exist => this day, we have not debug log file
                            JOptionPane.showMessageDialog(rootPane, "Retrive File \"" + localFile.getName() + "\" failed.\n"
                                    + " The file name/path error or it does not exist a so file(by example a off day)");
                        } else {
                            if (m_isTodayDebug) {
                                retriveFile(LOG_REMOTE_PATH + LOG_TODAY_DEBUG_FILE_NAME, !isDebugFile, index);//the last log file to retrive, 
                            } else {
                                JOptionPane.showMessageDialog(rootPane, "Retrive end " + filePath + " stop at " + index);
                                m_logger.debug("retrive multi file end");
                                createLogParserDialog(m_fileList);
                            }
                        }
                    }
                }
            }

        };

        DownloadFileTask task = new DownloadFileTask(callback, remoteFilePath, localFile);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
    }

    private String getLogFileName(int ajuste) {

        return LOG_FILE_NAME + "." + getDateInFileName(ajuste) + LOG_FILE_SUFFIX;
    }

    private String getDebugLogFileName(int dateAjuste, int cutIndex) {
        if (dateAjuste <= 0) {
            return LOG_TODAY_DEBUG_FILE_NAME;
        } else {
            return LOG_DEBUG_FILE_NAME + "_." + getDateInFileName(dateAjuste) + "." + cutIndex + LOG_FILE_SUFFIX;
        }
    }

    /**
     *
     * @param debugFileName, often = LOG_REMOTE_PATH +
     * LOG_DEBUG_FILE_NAME+"_."+date+"."+LOG_FILE_SUFFIX;
     * @return file name with path, without .\d+.txt
     */
    private String getPrefix(String debugFileName) {
        if (debugFileName.contains(LOG_REMOTE_PATH + LOG_DEBUG_FILE_NAME)) {
            if (m_debugFileCheckBox.isSelected()) {
                if (debugFileName.contains(LOG_REMOTE_PATH + LOG_TODAY_DEBUG_FILE_NAME)) {//today
                    return LOG_REMOTE_PATH + LOG_DEBUG_FILE_NAME + "_." + getDateInFileName(0);
                } else {
                    String regex = ".\\d+" + LOG_FILE_SUFFIX;
                    String fileNamePrefix = debugFileName.replaceFirst(regex, "");
                    return fileNamePrefix;
                }
            }
        }
        return null;
    }

    private String getDateInFileName(int ajuste) {
        Calendar cal = Calendar.getInstance();
        if (ajuste > 0) {
            cal.add(Calendar.DATE, ajuste * -1);
        }
        return String.format("%tY-%tm-%td", cal, cal, cal);
    }

}
