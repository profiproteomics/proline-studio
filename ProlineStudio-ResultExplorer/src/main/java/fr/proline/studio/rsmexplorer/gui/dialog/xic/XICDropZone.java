/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.DropZoneInterface;
import fr.proline.studio.gui.TreeFileChooserTransferHandler;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.SelectRawFilesPanel.FlatDesignTableModel;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.MiscellaneousUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.Border;

/**
 *
 * @author AK249877
 */
public class XICDropZone extends JPanel implements DropZoneInterface {

    private Hashtable<String, File> m_samplesTable;
    private JLabel message;
    private JTable m_table;
    private FlatDesignTableModel m_model;
    private String[] suffix = {"raw", ".mzdb"};

    private JList m_list;
    private DefaultListModel m_listModel;
    private JTextArea m_textArea;

    private JPanel m_dropArea;

    private Border dashedBorder = BorderFactory.createDashedBorder(null);
    private Border defaultBorder = BorderFactory.createEmptyBorder();
    
    private Timer m_timer;
    
    private TreeFileChooserTransferHandler m_transferHandler;

    public XICDropZone(TreeFileChooserTransferHandler transferHandler) {
        m_transferHandler = transferHandler;
        m_samplesTable = new Hashtable<String, File>();
        
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Automatic Raw File Association"),BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        this.setToolTipText("Drag your .mzdb files & folders in the drop zone");
        this.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,2, 10, 10));
        panel.add(this.initDetails());
        panel.add(this.initDropArea());
        
        this.add(panel);

        m_transferHandler.addComponent(m_dropArea);
        
        this.setTransferHandler(m_transferHandler);

    }

    public void setTable(JTable table) {
        m_table = table;
        m_model = (FlatDesignTableModel) m_table.getModel();
    }

    private JPanel initDropArea() {
        m_dropArea = new JPanel();
        m_dropArea.setLayout(new BorderLayout());

        message = new JLabel("", JLabel.CENTER);
        message.setFont(new Font(message.getFont().getFontName(), Font.PLAIN, 12));
        message.setForeground(Color.RED);
        message.setOpaque(false);

        m_dropArea.add(new JLabel("<html><center><font size='6' color='green'>Drop Zone</font><br><font size='4' color='black'>Drop your .mzdb files & folders here</font></center></html>", IconManager.getIcon(IconManager.IconType.DOCUMENT_LARGE), JLabel.CENTER), BorderLayout.CENTER);
        m_dropArea.add(message, BorderLayout.SOUTH);

        return m_dropArea;
    }

    private JPanel initDetails() {

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(2, 1));

        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BorderLayout());
        m_textArea = new JTextArea();
        m_textArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(m_textArea);
        logPanel.setBorder(BorderFactory.createTitledBorder("Log"));
        logPanel.add(logScrollPane);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        m_listModel = new DefaultListModel();
        m_list = new JList(m_listModel);
        JScrollPane listScrollPane = new JScrollPane(m_list);
        listPanel.setBorder(BorderFactory.createTitledBorder("Not associated files"));
        listPanel.add(listScrollPane);

        detailsPanel.add(logPanel);
        detailsPanel.add(listPanel);

        return detailsPanel;
    }

    private void updateTable() {

        Hashtable<String, Integer> shortages = m_model.getModelShortages();

        Enumeration<String> enumKey = shortages.keys();

        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            if (m_samplesTable.containsKey(key)) {
                ArrayList<File> fileList = new ArrayList<File>();
                fileList.add(m_samplesTable.get(key));
                int index = shortages.get(key);
                m_model.setFiles(fileList, index);
            }
        }

        if (m_model.getModelShortages().size() > 0) {
            message.setText("You have " + m_model.getModelShortages().size() + " samples without the corresponding .mzdb file!");
        } else {
            message.setText("");
        }

    }

    private void updateLog(ArrayList<File> newSamples) {
        m_textArea.setText(null);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("List was updated with ").append(newSamples.size()).append(" new files.\n\n");

        for (int i = 0; i < newSamples.size(); i++) {
            stringBuilder.append(newSamples.get(i).toString());
            stringBuilder.append("\n");
        }

        m_textArea.setText(stringBuilder.toString());
    }

    private void updateList() {
        m_listModel.removeAllElements();

        Hashtable<String, Integer> shortages = m_model.getModelShortages();

        Enumeration<String> enumKey = m_samplesTable.keys();

        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            if (!shortages.containsKey(key)) {
                m_listModel.addElement(key);
            }
        }

    }

    public void removeAllSamples() {
        if (m_samplesTable != null) {
            m_samplesTable.clear();
        }
    }

    public Hashtable<String, File> getAllSamples() {
        if (m_samplesTable != null) {
            return m_samplesTable;
        } else {
            return null;
        }
    }

    @Override
    public void addSample(Object sample) {
        if (sample instanceof File) {
            if (!m_samplesTable.containsKey(MiscellaneousUtils.getFileName(((File) sample).toString().toLowerCase(), suffix))) {
                m_samplesTable.put(MiscellaneousUtils.getFileName(((File) sample).toString().toLowerCase(), suffix), (File) sample);
            }
        }
    }

    @Override
    public void removeSample(Object key) {
        if (m_samplesTable != null) {
            m_samplesTable.remove(key);
        }
    }

    @Override
    public void addSamples(Object o) {
        if (o instanceof ArrayList) {
            ArrayList<File> sampleList = (ArrayList<File>) o;
            this.updateLog(sampleList);
            for (int i = 0; i < sampleList.size(); i++) {
                this.addSample(sampleList.get(i));
            }
            this.updateTable();
            this.updateList();
        }
    }
}
