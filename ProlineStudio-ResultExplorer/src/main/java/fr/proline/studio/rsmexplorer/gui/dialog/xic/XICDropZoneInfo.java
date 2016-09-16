/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author AK249877
 */
public class XICDropZoneInfo extends JPanel {

    private JList list;
    private DefaultListModel model;
    private JTextArea log;

    public XICDropZoneInfo() {
        
        this.setLayout(new GridLayout(2, 1));

        log = new JTextArea();
        log.setEditable(false);
        JScrollPane logPane = new JScrollPane(log);
        logPane.setBorder(BorderFactory.createTitledBorder("Log"));

        model = new DefaultListModel();
        list = new JList(model);
        JScrollPane listPane = new JScrollPane(list);
        listPane.setBorder(BorderFactory.createTitledBorder("Not associated files"));

        this.add(logPane);
        this.add(listPane);

    }

    public void updateLog(ArrayList<File> samples) {     
        log.setText(null);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("List was updated with ").append(samples.size()).append(" new files.\n\n");
        for (int i = 0; i < samples.size(); i++) {
            stringBuilder.append(samples.get(i).toString());
            stringBuilder.append("\n");
        }
        log.setText(stringBuilder.toString());        
    }
    
    public void updateList(ArrayList<String> samples){
        model.removeAllElements();
        for(int i=0; i<samples.size(); i++){
            model.addElement(samples.get(i));
        }
    }
    

}
