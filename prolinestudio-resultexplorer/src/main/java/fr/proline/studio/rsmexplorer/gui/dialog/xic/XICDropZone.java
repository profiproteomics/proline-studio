/* 
 * Copyright (C) 2019
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
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.gui.DropZoneInterface;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserTransferHandler;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.SelectRawFilesPanel.FlatDesignTableModel;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.HelpUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 *
 * @author AK249877
 */
public class XICDropZone extends JPanel implements DropZoneInterface {
    /**
     * Hashmap key File name, value File.
     */
    private final HashMap<String, File> m_droppedFiles;
    private FlatDesignTableModel m_model;
    private final List<String> suffix; // {".raw", ".mzdb", ".wiff"};
    private XICDropZoneInfo m_info;
    private final TreeFileChooserTransferHandler m_transferHandler;
    private final HashMap<String, AssociationWrapper> m_associations;

    public XICDropZone(TreeFileChooserTransferHandler transferHandler) {
        m_transferHandler = transferHandler;
        
        m_droppedFiles = new HashMap<>();
        m_associations = new HashMap<>();
        suffix = new ArrayList<>();
        suffix.add("mzdb");
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

    //Ne faudrait-il pas un updateRow que updateTable => update complet a chaque ligne ? 
    /**
     *  first, for all run, which missing association, we get m_model.getPotentialFilenamesForMissings()
     *  for each run missing fileName, find between m_droppedFiles, if there are a File has the same name of PotentialFilenamesForMissings
     *  if found, updateDropZoneInfo 
     */  
    public void updateTable() {
        if (m_model == null) {
            return;
        }
        
        m_model.updatePotentialsListForMissings();
        List<String> associatedFileList = new ArrayList<>();
        
        HashMap<Integer, HashSet<String>> potentialFileNamesForMissings = m_model.getPotentialFilenamesForMissings();
        Iterator<Integer> missingIndexIterator = potentialFileNamesForMissings.keySet().iterator();
        while (missingIndexIterator.hasNext()) {

            Integer nodeIndex = missingIndexIterator.next();

            HashSet<String> potentialFileNames = potentialFileNamesForMissings.get(nodeIndex);

            for (String potentialFileName : potentialFileNames) {
                //Get the first dropped file which at least start with the same string as Potential RawFile FileName
                Optional<String> foundedFile = m_droppedFiles.keySet().stream().filter(fileName ->fileName.toLowerCase().startsWith(potentialFileName)).findFirst();
                if(foundedFile.isPresent()){
                    String associedtDropFile = foundedFile.get();
                    associatedFileList.add(associedtDropFile);
                    ArrayList<File> fileList = new ArrayList<>();
                    fileList.add(m_droppedFiles.get(associedtDropFile));
                    m_model.setFiles(fileList, nodeIndex);
                }
            }
        }
        
        updateDropZoneInfo(associatedFileList );        
    }

    /**
     * Update de DropZoneInfo using new associated fileList
     * @param associatedFileList 
     */
    private void updateAssociations(List<String> associatedFileList) {

//        HashSet<String> totalMissingValues = new HashSet<>();
//
//        HashMap<Integer, HashSet<String>> potentialFileNamesForMissings = m_model.getPotentialFilenamesForMissings();
//        
//        Iterator<HashSet<String>> potentialFilenames = potentialFileNamesForMissings.values().iterator();
//        while(potentialFilenames.hasNext()){
//            HashSet<String> nextPotentialFilenames = potentialFilenames.next();
//            for (String filename : nextPotentialFilenames) {
//                totalMissingValues.add(filename);
//            }
//        }

        Iterator<String> droppedFilenameIt = m_droppedFiles.keySet().iterator();
        while (droppedFilenameIt.hasNext()) {
            String droppedFilename = droppedFilenameIt.next();
            
            AssociationWrapper.AssociationType associationType;
            if(associatedFileList.contains(droppedFilename))
                associationType = AssociationWrapper.AssociationType.ASSOCIATED;
            else
                associationType = AssociationWrapper.AssociationType.NOT_ASSOCIATED;

            AssociationWrapper assocWraper = m_associations.getOrDefault(droppedFilename, new AssociationWrapper(droppedFilename, associationType));
            if(assocWraper.getAssociationType()== AssociationWrapper.AssociationType.NOT_ASSOCIATED)
                assocWraper.setAssociationType(associationType);           
            m_associations.put(droppedFilename, assocWraper);
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

    /**
     * sample is a File, if not exist before, put in m_droppedFiles, fileName without suffix->File
     * @param sample: must be a File object
     */
    @Override
    public void addSample(Object sample) {
        if (sample instanceof File && suffix.contains(FilenameUtils.getExtension(((File) sample).getName()).toLowerCase()) ) {
            String name = FilenameUtils.getBaseName(((File) sample).getName().toLowerCase());
            if (!m_droppedFiles.containsKey(name)) {
                m_droppedFiles.put(name, (File) sample);
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
    
    /**
     * Update de DropZoneInfo using new associated fileList
     * @param associatedFileList 
     */
    public void updateDropZoneInfo(List<String> associatedFileList ){
        if(m_info!=null){
            updateAssociations(associatedFileList);
            m_info.updateInfo(getAssociations());
        }
    }

    /**
     * call addSample for each File in paramter, puis updateTable()
     * @param o , must be an ArrayList of File
     */
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
