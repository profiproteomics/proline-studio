/* 
 * Copyright (C) 2019 VD225637
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

import fr.proline.logparser.gui.ColorPalette;
import fr.proline.logparser.model.Utility;
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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog to chose a server log file
 *
 * @author KX257079
 */
public class ServerLogFileNameDialog extends DefaultDialog {

    protected static final Logger m_logger = LoggerFactory.getLogger(ServerLogFileNameDialog.class);
    private static final String LOG_REMOTE_PATH = "./logs/";
    private static final File LOG_LOCAL_PATH = new File(Utility.WORKING_DATA_DIRECTORY + File.separator + "cortexlogs");
    private static final String LOG_DEBUG_FILE_NAME = "proline_cortex_debug";
    private static final String LOG_TODAY_DEBUG_FILE_NAME = "proline_cortex_debug.txt";
    private static final String LOG_FILE_NAME = "proline_cortex_log";
    private static final String LOG_FILE_SUFFIX = ".txt";
    private JComboBox m_dateChooser;
    private JTextField m_fileNameTxtField;
    private JCheckBox m_debugFileCheckBox;
    private JRadioButton m_singleFileButton;
    private JRadioButton m_batchFileButton;
    private int m_dateAdjust;
    boolean m_isTodayDebug = false;
    /**
     * localFile
     */
    ArrayList<File> m_fileList;
    File m_localPath;

    public ServerLogFileNameDialog() {
        super(WindowManager.getDefault().getMainWindow(), Dialog.ModalityType.APPLICATION_MODAL);
        m_dateAdjust = 0;
        JPanel m_internalPanel = initInternalPanel();
        m_fileList = new ArrayList();
        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getServerConnectionManager();
        String host = serverConnectionManager.getServerURL();
        Utility.init();//create directory entry point
        if (!LOG_LOCAL_PATH.isDirectory()) {
            LOG_LOCAL_PATH.mkdir();
        }
        m_localPath = new File(LOG_LOCAL_PATH + File.separator + host);
        if (!m_localPath.isDirectory()) {
            m_localPath.mkdir();
        }
        String dialog_title = "Server Parsed Tasks";
        super.setTitle(dialog_title);
        String help_text = "<html>Specify which server tasks log to view : <br> select the date and  log mode (debug or info)</html>";
        super.setHelpHeaderText(help_text);
        super.setInternalComponent(m_internalPanel);
        super.setButtonVisible(DefaultDialog.BUTTON_HELP, false);//use only cancel, ok button
        super.setStatusVisible(false);
        super.setResizable(true);
    }

    /**
     * to view log tasks
     *
     * @param fileList
     * @return
     */
    private JDialog createLogParserDialog(ArrayList<File> fileList) {

        JDialog logViewDialog = new JDialog(WindowManager.getDefault().getMainWindow(), "Server Parsed Tasks", Dialog.ModalityType.APPLICATION_MODAL);
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

    private JPanel initInternalPanel() {
        //FileName
        m_fileNameTxtField = new JTextField(LOG_TODAY_DEBUG_FILE_NAME);
        //Date Combobox
        int maxDuration = 7;
        String[] dateChoice = new String[maxDuration];
        for (int i = 0; i < maxDuration; i++) {
            dateChoice[i] = this.getDateInFileName(i);
        }
        m_dateChooser = new JComboBox(dateChoice);
        m_dateChooser.addActionListener(createDateChoiceActionListener());
        //CheckBox for debug log File vs normal log File
        m_debugFileCheckBox = new JCheckBox("Debug mode");
        m_debugFileCheckBox.setSelected(true);
        m_debugFileCheckBox.addItemListener(createIsDebugFileItemListener());
        m_singleFileButton = new JRadioButton("Single File");
        m_batchFileButton = new JRadioButton("Batch File");
        ButtonGroup debugFileGroup = new ButtonGroup();
        debugFileGroup.add(m_singleFileButton);
        debugFileGroup.add(m_batchFileButton);
        m_batchFileButton.setSelected(true);
        //layout
        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        pane.add(new JLabel("File:"), c);
        c.gridx++;
        c.fill = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        pane.add(m_fileNameTxtField, c);
        c.gridy++;
        c.gridx = 0;
        pane.add(new JPanel(), c);//vertical space between filename & date chooser

        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        pane.add(new JLabel("Log date:"), c);
        c.gridx++;
        c.weightx = 1;
        c.gridwidth = 2;
        pane.add(m_dateChooser, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.insets = new Insets(0, 0, 0, 0);
        pane.add(m_debugFileCheckBox, c);

        c.gridx = 1;
        c.gridy++;
        c.gridwidth = 1;
        pane.add(m_singleFileButton, c);
        c.gridx++;
        pane.add(m_batchFileButton, c);

        Dimension d = pane.getPreferredSize();
        pane.setBorder(new EmptyBorder(8, 16, 8, 16));
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
            m_singleFileButton.setVisible(true);
            m_batchFileButton.setVisible(true);
        } else {
            fileName = getLogFileName(m_dateAdjust);
            m_singleFileButton.setVisible(false);
            m_batchFileButton.setVisible(false);
        }
        m_fileNameTxtField.setText(fileName);
    }

    /**
     * download file / use local file downloaded
     *
     * @return
     */
    @Override
    protected boolean okCalled() {
        m_fileList = new ArrayList();
        m_isTodayDebug = false;
        String filetxt = m_fileNameTxtField.getText();
        String fileName = (new File(filetxt)).getName();//delete all path for security reason
        LogFileName debugFileP = this.getPrefix(fileName);
        boolean isDebugFile = (debugFileP != null);
        boolean isBatchFile = false;
        if (isDebugFile) {
            isBatchFile = m_batchFileButton.isSelected();
            if (fileName.equals(LOG_TODAY_DEBUG_FILE_NAME)) {//today
                m_isTodayDebug = true;
            }
            //retriveFile(debugFileP, isDebugFile, 0);
            loadLocalFile(debugFileP.name, isDebugFile, debugFileP.index, isBatchFile);
        } else {
            //only one file
            //retriveFile(fileName, isDebugFile, -1);
            loadLocalFile(fileName, isDebugFile, -1, isBatchFile);
        }

        return true;
    }

    /**
     *
     * @param fileName if it is a proline_cortex_log file, has suffix *.txt, if
     * it is a proline_cortex_debug_ file, has not suffix *.txt
     * @param isDebugFile
     * @param index
     * @param isBatchFile
     */
    private void loadLocalFile(String fileName, boolean isDebugFile, int index, boolean isBatchFile) {
        String localFilePath = m_localPath + File.separator + fileName;
        if (isDebugFile) {
            localFilePath = m_localPath + File.separator + fileName + "." + index + LOG_FILE_SUFFIX;
        }
        m_logger.debug("loadLocalFile path ={}", localFilePath);
        File localFile = new File(localFilePath);
        if (localFile.isFile()) {//alreaday downloaded
            try {
                //get File create date
                BasicFileAttributes attr = Files.readAttributes(localFile.toPath(), BasicFileAttributes.class);
                Long createTime = attr.creationTime().toMillis();
                LocalDate fileCreateDate = Instant.ofEpochMilli(createTime).atZone(ZoneId.systemDefault()).toLocalDate();
                //Date in file name + 1 day
                LocalDate logNextDate = getDateInFileName(fileName).plusDays(1);//for one day plus
                //file created date = retrived date, if the file is retrived 1 day before, then, all log file of this day are retrived.
                if (fileCreateDate.isAfter(logNextDate)) {//if local file is created at least 1 day later than logDate
                    m_logger.debug("local File later retrived ok ={}", localFile.getName());
                    m_fileList.add(localFile);

                    if (isDebugFile && isBatchFile) { //retrive other local file
                        index += 1;
                        addLocalFile(fileName, index);
                    }
                    createLogParserDialog(m_fileList);//we have at least 1 file
                    return;
                } else {//not all file retrived, if isBatchFile, suppose number of all retrived file is n, so just the file n is perhaps not updated
                    if (isBatchFile) {
                        int debugIndex = addLocalFile(fileName, index);
                        m_logger.debug("####less than 1 day, debug not today, index to retrieve {}", debugIndex);
                        //debugIndex do not exist, debugIndex-1 must a updated file, we don't need to reload it
                        retriveFile(fileName, isDebugFile, debugIndex, true);

                    } else {
                        m_logger.debug("####less than 1 day, index to retrieve {}", index);
                        retriveFile(fileName, isDebugFile, index, false);//file retrveied the same day, so we need redo to have the recent logs
                    }
                }
            } catch (Exception anyE) {
                m_logger.debug("Exception when loadLocalFile(){}  ", anyE.getMessage());
                retriveFile(fileName, isDebugFile, index, isBatchFile);
            }
        } else {
            retriveFile(fileName, isDebugFile, index, isBatchFile); //file not found

        }

    }

    /**
     * add index continued files
     *
     * @param fileName
     * @param index, next index of the file to retrive
     * @return first index that the file with this index don't exist
     */
    private int addLocalFile(String fileName, int index) {
        String localFilePath;
        while (true) {
            localFilePath = m_localPath + File.separator + fileName + "." + index + LOG_FILE_SUFFIX;//must be debug+batch mode
            m_logger.debug("loadLocalFile path ={}", localFilePath);
            File localFile = new File(localFilePath);
            if (localFile.isFile()) {//alreaday downloaded
                m_logger.debug("local File later retrived ok ={}", localFile.getName());
                m_fileList.add(localFile);//retrive local file
                index++;
            } else {
                break;
            }
        }
        return index;
    }

    /**
     * We give the remote File path & local path to register files here, when
     * isDebugFile(isIndexUseful == true), filePath is only debugfilePrefix as
     * ./logs/proline_cortex_debug_.yyyy-MM-dd
     *
     * @param fileName, File for normal log file, as *
     * ./logs/proline_cortex_log.yyyy-MM-dd
     * @param isIndexUseful
     * @param index
     */
    private void retriveFile(String fileName, boolean isIndexUseful, int index, boolean isBatchFile) {
        m_logger.debug("retrive file: {}, isDebugFile={}, index ={}", fileName, isIndexUseful, index);
        if (m_isTodayDebug && !isBatchFile && index == 0) {//this means, failed to retrive today log file proline_cortex_debug_.date.index.txt,the file pred the r
            retriveFile(LOG_TODAY_DEBUG_FILE_NAME, false, -1, false);//the last single log file to retrive, !isIndexUseful used to create special remote name
            return;
        }
        String remoteFilePath = LOG_REMOTE_PATH + fileName;//format remote file with remote path
        String localFilePath = m_localPath + File.separator + fileName;//format local file with local path
        if (isIndexUseful) {
            remoteFilePath = LOG_REMOTE_PATH + fileName + "." + index + LOG_FILE_SUFFIX;//add debug file index
            localFilePath = m_localPath + File.separator + fileName + "." + index + LOG_FILE_SUFFIX;
        }
        File localFile = new File(localFilePath);
        AbstractJMSCallback callback;
        callback = new AbstractJMSCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                String notExistMsg = "The file does not exist, perhaps there are no log for the selected day.";
                if (!isIndexUseful) {//1. cortex_log file, 2. default cortex_debug file
                    if (success) {
                        m_logger.debug("Retrieving file \"" + localFile.getName() + "\" from server succes.");
                        m_fileList.add(localFile);
                        createLogParserDialog(m_fileList);
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Retrieving  File \"" + localFile.getName() + "\"has failed.\n"
                                + notExistMsg);
                    }
                } else {//debug log file
                    if (success) {
                        m_logger.debug("Retrieving file \"" + localFile.getName() + "\" from server succes.");

                        m_fileList.add(localFile);
                        if (isBatchFile) {
                            int next = index + 1;
                            retriveFile(fileName, isIndexUseful, next, isBatchFile);
                        } else {
                            createLogParserDialog(m_fileList);
                        }
                    } else {
                        if (!isBatchFile) {

                            JOptionPane.showMessageDialog(rootPane, "Retrive File \"" + localFile.getName() + "\" failed.\n"
                                    + notExistMsg);

                        } else {
                            if (index == 0 && !m_isTodayDebug) {//first file do not exist => this day, we have not debug log file
                                JOptionPane.showMessageDialog(rootPane, "Retrive File \"" + localFile.getName() + "\" failed.\n"
                                        + notExistMsg);
                            } else {
                                //LOG_TODAY_DEBUG_FILE_NAME = "proline_cortex_debug.txt" is the most recent debug log file "today"
                                //when m_isTodayDebug=true, we have tried to retrive proline_cortex_debug_.date.index.txt, when this faild, we can retried the proline_cortex_debug.txt
                                if (m_isTodayDebug) {//this means, failed to retrive today log file proline_cortex_debug_.date.index.txt,the file pred the r
                                    retriveFile(LOG_TODAY_DEBUG_FILE_NAME, !isIndexUseful, index, !isBatchFile);//the last single log file to retrive, !isDebugFile used to create special remote name
                                } else {
                                    //JOptionPane.showMessageDialog(rootPane, "Retrive end " + filePath + " stop at " + index);
                                    m_logger.debug("retrive multi file end");
                                    createLogParserDialog(m_fileList);
                                }
                            }
                        }
                    }
                }
            }

        };
        DownloadFileTask task = null;
        /**
         * if remote file don't existe, there will be an "Error handling JMS
         * Message" Exception, in order to avoid to show
         * StudioExceptions.notify: false.
         */
        task = new DownloadFileTask(callback, remoteFilePath, localFile, false);

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
     * @param text, often = LOG_DEBUG_FILE_NAME+"_."+date+"."+LOG_FILE_SUFFIX;
     * @return file name with path, without .\d+.txt
     */
    private LogFileName getPrefix(String text) {
        LogFileName result = new LogFileName();
        if (text.contains(LOG_DEBUG_FILE_NAME)) {
            if (text.contains(LOG_TODAY_DEBUG_FILE_NAME)) {//today without index
                result.name = LOG_DEBUG_FILE_NAME + "_." + getDateInFileName(0);
                result.index = 0;
            } else {
                final String regex = "(proline_cortex_debug_.\\d\\d\\d\\d-\\d\\d-\\d\\d).(\\d+).txt";
                final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                final Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    result.name = matcher.group(1);
                    result.index = Integer.parseInt(matcher.group(2));
                } else {
                    result = null;
                }
            }
            return result;
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

    private LocalDate getDateInFileName(String fileName) {
        final String regex = "(2\\d\\d\\d)-([0-1]\\d)-([0-3]\\d)";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            LocalDate date = LocalDate.parse(matcher.group(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return date;
        }
        return null;//impossible
    }

    class LogFileName {

        /**
         * without suffix .txt
         */
        String name; //format like proline_cortex_debug_.date. 
        int index;

        public LogFileName() {
            this.name = LOG_DEBUG_FILE_NAME;
            this.index = -1;
        }

    }
}
