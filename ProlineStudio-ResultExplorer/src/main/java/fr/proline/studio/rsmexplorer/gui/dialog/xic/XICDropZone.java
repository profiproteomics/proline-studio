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
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 *
 * @author AK249877
 */
public class XICDropZone extends JPanel implements DropZoneInterface {

    private Hashtable<String, File> m_samplesTable;
    private FlatDesignTableModel m_model;
    private String[] suffix = {".raw", ".mzdb"};
    private XICDropZoneInfo m_info;
    private TreeFileChooserTransferHandler m_transferHandler;

    public XICDropZone(TreeFileChooserTransferHandler transferHandler) {
        m_transferHandler = transferHandler;
        m_samplesTable = new Hashtable<String, File>();
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.setToolTipText("Drag your .mzdb files & folders in the drop zone");
        this.setLayout(new BorderLayout());
        this.add(new JLabel("<html><center><font size='6' color='green'>Drop Zone</font><br><font size='4' color='black'>Drop your .mzdb files & folders here</font></center></html>", IconManager.getIcon(IconManager.IconType.DOCUMENT_LARGE), JLabel.CENTER), BorderLayout.CENTER);
        m_transferHandler.addComponent(this);
        this.setTransferHandler(m_transferHandler);
    }

    public void setTable(JTable table) {
        m_model = (FlatDesignTableModel) table.getModel();
    }

    public void setDropZoneInfo(XICDropZoneInfo info) {
        m_info = info;
    }

    private void updateTable() {
        if (m_model == null) {
            return;
        }

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
    }

    private ArrayList<String> getNotAssociatedFilenames() {
        ArrayList<String> filenames = new ArrayList<String>();
        Hashtable<String, Integer> shortages = m_model.getModelShortages();
        Enumeration<String> enumKey = m_samplesTable.keys();
        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            if (!shortages.containsKey(key)) {
                filenames.add(key);
            }
        }
        return filenames;
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
            for (int i = 0; i < sampleList.size(); i++) {
                this.addSample(sampleList.get(i));
            }

            if (m_model != null) {
                this.updateTable();
            }

            if (m_info != null) {
                m_info.updateLog(sampleList);
                m_info.updateList(this.getNotAssociatedFilenames());
            }
        }
    }

    @Override
    public void clearDropZone() {
        m_samplesTable.clear();
        m_info.clearDropInfo();
    }
}
