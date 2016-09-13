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
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;

/**
 *
 * @author AK249877
 */
public class XICDropZone extends JPanel implements DropZoneInterface, DropTargetListener {

    private Hashtable<String, File> m_samplesTable;
    private JLabel message;
    private JTable m_table;
    private FlatDesignTableModel m_model;
    private String[] suffix = {"raw", ".mzdb"};
    private DropTarget m_dropTarget;

    private Border dashedBorder = BorderFactory.createDashedBorder(null);
    private Border defaultBorder = BorderFactory.createEmptyBorder();

    public XICDropZone() {
        m_samplesTable = new Hashtable<String, File>();

        this.setToolTipText("Drag your .mzdb files in the drop zone");

        this.setLayout(new BorderLayout());

        message = new JLabel("", JLabel.CENTER);
        message.setFont(new Font(message.getFont().getFontName(), Font.PLAIN, 12));
        message.setForeground(Color.RED);

        this.add(new JLabel("<html><center><font size='6' color='green'>Drop Zone</font><br><font size='4' color='black'>Drop your .mzdb files & folders here</font></center></html>", IconManager.getIcon(IconManager.IconType.DOCUMENT_LARGE), JLabel.CENTER), BorderLayout.CENTER);
        this.add(message, BorderLayout.SOUTH);

        m_dropTarget = new DropTarget(this, this);

        setTransferHandler(new TreeFileChooserTransferHandler());

    }

    public void setTable(JTable table) {
        m_table = table;
        m_model = (FlatDesignTableModel) m_table.getModel();
    }

    private void update() {

        Hashtable<String, Integer> shortages = m_model.getModelShortages();

        Enumeration<String> enumKey = shortages.keys();

        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            if (m_samplesTable.containsKey(key.toUpperCase())) {
                ArrayList<File> fileList = new ArrayList<File>();
                fileList.add(m_samplesTable.get(key.toUpperCase()));
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
            if (!m_samplesTable.containsKey(MiscellaneousUtils.getFileName(((File) sample).toString().toUpperCase(), suffix))) {
                m_samplesTable.put(MiscellaneousUtils.getFileName(((File) sample).toString().toUpperCase(), suffix), (File) sample);
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
            this.update();
        } else if (o instanceof Hashtable) {
            m_samplesTable = (Hashtable<String, File>) o;
            this.update();
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        this.setBackground(Color.WHITE);
        this.setBorder(dashedBorder);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        ;
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        this.setBackground(null);
        this.setBorder(defaultBorder);
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        ;
    }

}
