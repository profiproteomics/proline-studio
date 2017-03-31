/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.gui.DropZoneInterface;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserTransferHandler;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.SelectRawFilesPanel.FlatDesignTableModel;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.MiscellaneousUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 *
 * @author AK249877
 */
public class XICDropZone extends JPanel implements DropZoneInterface {

    private final HashMap<String, File> m_droppedFiles;
    private FlatDesignTableModel m_model;
    private final String[] suffix = {".raw", ".mzdb", ".wiff"};
    private XICDropZoneInfo m_info;
    private final TreeFileChooserTransferHandler m_transferHandler;
    private final HashMap<String, AssociationWrapper> m_associations;

    public XICDropZone(TreeFileChooserTransferHandler transferHandler) {
        m_transferHandler = transferHandler;
        
        m_droppedFiles = new HashMap<String, File>();
        m_associations = new HashMap<String, AssociationWrapper>();
        
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setToolTipText("Drag your .mzdb files & folders in the drop zone");
        setLayout(new BorderLayout());
        add(new JLabel("<html><center><font size='6' color='green'>Drop Zone</font><br><font size='4' color='black'>Drop your .mzdb files & folders here</font></center></html>", IconManager.getIcon(IconManager.IconType.DOCUMENT_LARGE), JLabel.CENTER), BorderLayout.CENTER);
        m_transferHandler.addComponent(this);
        this.setTransferHandler(m_transferHandler);
    }

    public void setTable(JTable table) {
        m_model = (FlatDesignTableModel) table.getModel();
    }

    public void setDropZoneInfo(XICDropZoneInfo info) {
        m_info = info;
    }

    public void updateTable() {
        if (m_model == null) {
            return;
        }
        
        m_model.calculateMissingValues();
        
        HashMap<Integer, HashSet<String>> missingValues = m_model.getMissingValues();
        Iterator<Integer> keyIterator = missingValues.keySet().iterator();
        while (keyIterator.hasNext()) {

            Integer key = keyIterator.next();

            HashSet<String> currentHashSet = missingValues.get(key);

            for (String s : currentHashSet) {
                if (m_droppedFiles.containsKey(s)) {
                    ArrayList<File> fileList = new ArrayList<>();
                    fileList.add(m_droppedFiles.get(s));
                    m_model.setFiles(fileList, key);
                }
            }

        }
        
        updateDropZoneInfo();
        
    }

    private void updateAssociations() {

        HashSet<String> totalMissingValues = new HashSet<String>();

        HashMap<Integer, HashSet<String>> missingValues = m_model.getMissingValues();
        
        Iterator<Integer> keyIterator = missingValues.keySet().iterator();
        while (keyIterator.hasNext()) {
            HashSet<String> currentHashSet = missingValues.get(keyIterator.next());
            for (String s : currentHashSet) {
                totalMissingValues.add(s);
            }
        }

        Iterator<String> it = m_droppedFiles.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            
            AssociationWrapper.AssociationType associationType;
            
            if (!totalMissingValues.contains(key)) {
                associationType = AssociationWrapper.AssociationType.NOT_ASSOCIATED;
            }else{
                associationType = AssociationWrapper.AssociationType.ASSOCIATED;
            }
            
            if(m_associations.containsKey(key)){
                if(m_associations.get(key).getAssociationType()==AssociationWrapper.AssociationType.NOT_ASSOCIATED){
                    m_associations.get(key).setAssociationType(associationType);
                }
            }else{
                m_associations.put(key, new AssociationWrapper(key, associationType));
            }
        }
    }
    
    public HashMap<String, AssociationWrapper> getAssociations(){
        return m_associations;
    }

    @Override
    public void removeAllSamples() {
        if (m_droppedFiles != null) {
            m_droppedFiles.clear();
        }
    }

    @Override
    public HashMap<String, File> getAllSamples() {
        if (m_droppedFiles != null) {
            return m_droppedFiles;
        } else {
            return null;
        }
    }

    @Override
    public void addSample(Object sample) {
        if (sample instanceof File) {
            if (!m_droppedFiles.containsKey(MiscellaneousUtils.getFileName(((File) sample).toString().toLowerCase(), suffix))) {
                m_droppedFiles.put(MiscellaneousUtils.getFileName(((File) sample).toString().toLowerCase(), suffix), (File) sample);
            }
        }
    }

    @Override
    public void removeSample(Object key) {
        if (m_droppedFiles != null) {
            m_droppedFiles.remove(key);
        }
    }
    
    public XICDropZoneInfo getDropZoneInfo(){
        return m_info;
    }
    
    public void updateDropZoneInfo(){
        if(m_info!=null){
            updateAssociations();
            m_info.updateInfo(getAssociations());
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
                updateTable();
            }
            
        }
    }

    @Override
    public void clearDropZone() {
        m_droppedFiles.clear();
        m_associations.clear();
        m_info.clearInfo();
    }
}
