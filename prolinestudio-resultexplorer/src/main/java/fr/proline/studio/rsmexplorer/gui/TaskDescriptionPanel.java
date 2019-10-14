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
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Panel to display information about a specific task
 * @author JM235353
 */
public class TaskDescriptionPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    
    private JTextField m_taskTextfield;
    private JTextField m_askTimeTextfield;
    private JTextField m_startTimeTextfield;
    private JTextField m_deltaStartTimeTextfield;
    private JTextField m_endTimeTextfield;
    private JTextField m_deltaEndTimeTextfield;
    private JTextArea m_errorTextArea;
    private JButton m_requestButton; 
    
    private String m_requestContent;
    private String m_requestURL;
    
    public TaskDescriptionPanel() {
        initComponents();
    }
    
    private void initComponents() {

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(""));
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // --- create objects
        JLabel taskLabel = new JLabel("Task:");
        m_taskTextfield = new JTextField();
        m_taskTextfield.setEditable(false);
        m_taskTextfield.setBackground(Color.white);
        
        JPanel timePanel = createTimePanel();
        
        JPanel errorPanel = createErrorPanel();
        
        m_requestButton = new JButton("...");
        m_requestButton.setMargin(new Insets(0, 5, 0, 5));
        m_requestButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRequestDetailsDialog();
            }
        });
        m_requestButton.setEnabled(false);
        
        // --- add objects
        c.gridx = 0;
        c.gridy = 0;
        add(taskLabel, c);
        
        c.weightx = 1;
        c.gridx++;
        add(m_taskTextfield, c);

        c.weightx = 0;
        c.gridx++;
        add(m_requestButton, c);
        
        c.weightx = 1;
        c.gridx = 0 ;
        c.gridy++;
        c.gridwidth = 3;
        add(timePanel, c);
        
        c.gridy++;
        c.weighty = 1;
        add(errorPanel, c);

    }
    
    public void showRequestDetailsDialog() {
        
        JPanel requestPanel = new JPanel(new GridBagLayout());
      
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JTextField requestURLTextfield = new JTextField();
        requestURLTextfield.setText(m_requestURL);
        
        JTextArea requestContentTextarea = new JTextArea();
        requestContentTextarea.setText(m_requestContent);
        requestContentTextarea.setEditable(false);
        requestContentTextarea.setLineWrap(true);

        // --- add objects
        c.gridx = 0;
        c.gridy = 0;
        requestPanel.add(new JLabel("URL:"), c);
        c.gridx++;
        c.weightx = 1;
        requestPanel.add(requestURLTextfield, c);
        
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        requestPanel.add(new JLabel("Content:"), c);
        c.gridx++;
        c.weightx = 1;
        c.weighty = 1;
        requestPanel.add(new JScrollPane(requestContentTextarea), c);
  
         NotifyDescriptor nd = new NotifyDescriptor(
                requestPanel, // instance of your panel
                "Request details", // title of the dialog
                NotifyDescriptor.PLAIN_MESSAGE, // it is Yes/No dialog ...
                NotifyDescriptor.PLAIN_MESSAGE, // ... of a question type => a question mark icon
                null,
                null
        );
         
         DialogDisplayer.getDefault().notify(nd);
         
    }
    
    public JPanel createTimePanel() {
        JPanel timePanel = new JPanel(new GridBagLayout());
        timePanel.setBorder(BorderFactory.createTitledBorder(" Timestamp "));

        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // --- create objects
        JLabel askTimeLabel = new JLabel("Ask Time:");
        m_askTimeTextfield = new JTextField();
        m_askTimeTextfield.setEditable(false);
        m_askTimeTextfield.setBackground(Color.white);
        
        JLabel startTimeLabel = new JLabel("Start Time:");
        m_startTimeTextfield = new JTextField();
        m_startTimeTextfield.setEditable(false);
        m_startTimeTextfield.setBackground(Color.white);
        JLabel startDelayLabel = new JLabel("Start Delay:");
        m_deltaStartTimeTextfield = new JTextField();
        m_deltaStartTimeTextfield.setEditable(false);
        m_deltaStartTimeTextfield.setBackground(Color.white);
        JLabel durationLabel = new JLabel("Duration:");
        
        JLabel endTimeLabel = new JLabel("End Time:");
        m_endTimeTextfield = new JTextField();
        m_endTimeTextfield.setEditable(false);
        m_endTimeTextfield.setBackground(Color.white);
        m_deltaEndTimeTextfield = new JTextField();
        m_deltaEndTimeTextfield.setEditable(false);
        m_deltaEndTimeTextfield.setBackground(Color.white);
        
        
        // --- add objects
        c.gridx = 0;
        c.gridy = 0;
        timePanel.add(askTimeLabel, c);
        
        c.gridx++;
        c.weightx = 1;
        timePanel.add(m_askTimeTextfield, c);
        
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        timePanel.add(startTimeLabel, c);
        
        c.gridx++;
        c.weightx = 1;
        timePanel.add(m_startTimeTextfield, c);
        
        c.gridx++;
        c.weightx = 0;
        timePanel.add(startDelayLabel, c);
        
        c.gridx++;
        c.weightx = 0.3;
        timePanel.add(m_deltaStartTimeTextfield, c);
     
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        timePanel.add(endTimeLabel, c);
        
        c.gridx++;
        c.weightx = 1;
        timePanel.add(m_endTimeTextfield, c);
        
        c.gridx++;
        c.weightx = 0;
        timePanel.add(durationLabel, c);
        
        c.gridx++;
        c.weightx = 0.3;
        timePanel.add(m_deltaEndTimeTextfield, c);
        
        return timePanel;
    }
    
    
    public JPanel createErrorPanel() {
        JPanel errorPanel = new JPanel(new GridBagLayout());
        errorPanel.setBorder(BorderFactory.createTitledBorder(" Error Message "));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // --- create objects
        m_errorTextArea = new JTextArea();
        m_errorTextArea.setEditable(false);
        JScrollPane scrollpane = new JScrollPane(m_errorTextArea);
 
        // --- add objects
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        errorPanel.add(scrollpane, c);

        return errorPanel;
    }
    
    public void setTaskInfo(TaskInfo taskInfo) {
        
        if (taskInfo == null) {
            reinit();
            return;
        }
        
        m_taskTextfield.setText(taskInfo.getTaskDescription());
        
        TaskError taskError = taskInfo.getTaskError();
        String errorString;
        if (taskError != null) {
             errorString = taskError.toString();
        } else if (taskInfo.isAborted()) {
            errorString = "Task stopped by the User.";
        } else {
            errorString = "";
        }

        m_errorTextArea.setText(errorString);
        
        m_askTimeTextfield.setText(formatTime(taskInfo.getAskTimestamp()));
        
        m_startTimeTextfield.setText(formatTime(taskInfo.getStartTimestamp()));
        m_deltaStartTimeTextfield.setText(formatDeltaTime(taskInfo.getDelay()));
        
        m_endTimeTextfield.setText(formatTime(taskInfo.getEndTimestamp()));
        m_deltaEndTimeTextfield.setText(formatDeltaTime(taskInfo.getDuration()));
        
        m_requestContent = taskInfo.getRequestContent();
        m_requestURL = taskInfo.getRequestURL();
        
        m_requestButton.setEnabled((m_requestURL != null) && (!m_requestURL.isEmpty()));

    }
    
    private void reinit() {
        m_taskTextfield.setText("");
        m_errorTextArea.setText("");
        m_askTimeTextfield.setText("");
        m_startTimeTextfield.setText("");
        m_deltaStartTimeTextfield.setText("");
        m_endTimeTextfield.setText("");
        m_deltaEndTimeTextfield.setText("");
        m_requestContent = null;
        m_requestURL = null;
        m_requestButton.setEnabled(false);
    }
    
    
    private String formatTime(long timestamp) {
        if (timestamp == -1) {
            return "N/A";
        }
        return m_timestampFormat.format(new Date(timestamp));
    }
    private static final SimpleDateFormat m_timestampFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    
    private String formatDeltaTime(long delta) {
        if (delta == -1) {
            return "";
        }

        double deltaSeconds = ((double) delta)/1000.0;
        
        
        return "+"+m_decimalFormat.format(deltaSeconds)+"s";
    }

    private static final DecimalFormat m_decimalFormat = new DecimalFormat("####0.00");
    
    
    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public void addSingleValue(Object v) {
        // should not be used
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
    
}
