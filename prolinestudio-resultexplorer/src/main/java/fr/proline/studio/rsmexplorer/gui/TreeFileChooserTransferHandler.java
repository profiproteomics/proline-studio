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

import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.msfiles.MzdbUploadBatch;
import fr.proline.studio.msfiles.MzdbUploadSettings;
import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;

import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.openide.util.Exceptions;

/**
 * Class used for Drag and Drop of nodes
 *
 * @author JM235353
 */
public class TreeFileChooserTransferHandler extends TransferHandler {

    private HashMap<JComponent, JComponent> m_components;

    public TreeFileChooserTransferHandler() {
        m_components = new HashMap<JComponent, JComponent>();
    }

    public void addComponent(JComponent component) {
        m_components.put(component, component);
    }

    public void clearHighlights() {
        Iterator<JComponent> it = m_components.keySet().iterator();
        while (it.hasNext()) {
            JComponent key = it.next();
            key.setBackground(null);
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {

        if (c instanceof TreeFileChooser) {
            TreeFileChooser tree = (TreeFileChooser) c;

            ArrayList<File> selectedFiles = tree.getSelectedFiles();
            if (selectedFiles.isEmpty()) {
                return null;
            }

            return new FilesTransferable(selectedFiles, FilesTransferable.SourceFileSystem.SOURCE_SERVE_FILE_SYSTEM);

        }
        return null;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        clearHighlights();
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);

        if (support.isDataFlavorSupported(FilesTransferable.Files_FLAVOR)) {
            
            
            
            DropLocation dropLocation = support.getDropLocation();
            if (dropLocation instanceof JTable.DropLocation) {
                return true;
            } else if (m_components.containsKey((JComponent) support.getComponent())) {
                m_components.get((JComponent) support.getComponent()).setBackground(Color.WHITE);
                return true;
            } else if (dropLocation instanceof JTree.DropLocation) {
                try {
                    FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
                    if (transferable.getSource() == FilesTransferable.SourceFileSystem.SOURCE_LOCAL_FILE_SYSTEM) {

                        JTree.DropLocation treeDropLocation = (JTree.DropLocation) support.getDropLocation();
                        TreePath treePath = treeDropLocation.getPath();

                        DropInfo dropInfo = checkServer(treePath);
                        if (dropInfo == null) {
                            return false;
                        }
                        
                        ArrayList<File> transferredFiles = transferable.getFiles();
                        
                        ArrayList<File> compatibleTransferredFiles = dropInfo.filterCompatibleFiles(transferredFiles);
                        if (compatibleTransferredFiles.isEmpty()) {
                            return false;
                        }
                        
                        return true;
                    } else {
                        return false;
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    return false;
                }
            }
        }

        clearHighlights();
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {

        if (support.getComponent() instanceof JTable) {

            try {
                JTable table = (JTable) support.getComponent();
                JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();

                int index = table.convertRowIndexToModel(dl.getRow());

                int max = table.getModel().getRowCount();
                if (index < 0 || index > max) {
                    return false;
                }

                FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
                ArrayList<File> transferredFiles = transferable.getFiles();

                ArrayList<Integer> indices = new ArrayList<>();
                for (int i = 0; i < transferredFiles.size(); i++) {
                    indices.add(index + i);
                }

                TreeFileChooserTableModelInterface model = (TreeFileChooserTableModelInterface) table.getModel();

                if (!model.canSetFiles(indices)) {
                    JOptionPane.showMessageDialog(null, "An mzDB file is already linked to this dataset. Operation is cancelled.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                if (model.shouldConfirmCorruptFiles(indices)) {
                    int reply = JOptionPane.showConfirmDialog(null, "Previous quantitation has been run with existing mzDB file.\n Are you sure you want to overwrite this link.", "Warning", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.NO_OPTION) {
                        return false;
                    }
                }

                model.setFiles(transferredFiles, index);

            } catch (Exception e) {
                //should not happen
                return false;
            }
        } else if (support.getComponent() instanceof DropZoneInterface) {
            try {

                DropZoneInterface dropZone = (DropZoneInterface) support.getComponent();

                FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
                ArrayList<File> transferredFiles = transferable.getFiles();

                ArrayList<File> samples = new ArrayList<File>();

                for (int i = 0; i < transferredFiles.size(); i++) {

                    if (transferredFiles.get(i).isFile()) {
                        samples.add(transferredFiles.get(i));
                    } else {
                        File[] listOfFiles = transferredFiles.get(i).listFiles();
                        for (File file : listOfFiles) {
                            if (file.isFile()) {
                                samples.add(file);
                            }
                        }

                    }
                }

                dropZone.addSamples(samples);

            } catch (Exception e) {
                //should not happen
                return false;
            }

            return false;
        } else if (support.getComponent() instanceof TreeFileChooser) {

            try {

                FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
                ArrayList<File> transferredFiles = transferable.getFiles();

                JTree.DropLocation treeDropLocation = (JTree.DropLocation) support.getDropLocation();
                TreePath dropPath = treeDropLocation.getPath();

                return treeTransfer(transferredFiles, dropPath);

            } catch (UnsupportedFlavorException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        return true;

    }

    public boolean treeTransfer(ArrayList<File> transferredFiles, TreePath dropPath) {
        DropInfo dropInfo = checkServer(dropPath);
        if (dropInfo == null) {
            return false;
        }
        ArrayList<File> compatibleTransferredFiles = dropInfo.filterCompatibleFiles(transferredFiles);
        if (compatibleTransferredFiles.isEmpty()) {
            return false;
        }

        String parentLabel = dropInfo.getParentLabel();
        String destination = dropInfo.getDestination();

        //Here we must do a small modification so that mzdb is deleted if it is a result of a conversion drag!
        MzdbUploadSettings uploadSettings = new MzdbUploadSettings(false, parentLabel, destination);

        //Here prepare mzdb samples HashMap!
        HashMap<File, MzdbUploadSettings> uploadSamples = new HashMap<File, MzdbUploadSettings>();
        for (int i = 0; i < transferredFiles.size(); i++) {
            if (compatibleTransferredFiles.get(i).isFile()) {
                uploadSamples.put(compatibleTransferredFiles.get(i), uploadSettings);
            } else {
                File[] listOfFiles = compatibleTransferredFiles.get(i).listFiles();
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        uploadSamples.put(file, uploadSettings);
                    }
                }
            }
        }

        //Uploading Task
        MzdbUploadBatch uploadBatch = new MzdbUploadBatch(uploadSamples, dropPath);
        Thread thread = new Thread(uploadBatch);
        thread.start();

        return true;
    }
    
    private DropInfo checkServer(TreePath treePath) {

        // Check that the path if a directory
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        Object data = node.getUserObject();
        if (data instanceof IconData) {
            Object extraData = ((IconData) data).getObject();
            if (extraData instanceof TreeFileChooserPanel.FileNode) {
                File f = ((TreeFileChooserPanel.FileNode) extraData).getFile();
                if (! f.isDirectory()) {
                    return null;
                }
            }
        }

        String textTreePath = treePath.toString();

        textTreePath = textTreePath.replaceAll("\\[", "").replaceAll("\\]", "");

        String[] pathNodes = textTreePath.split(",");

        for (int i = 0; i < pathNodes.length; i++) {
            pathNodes[i] = pathNodes[i].trim();
        }
        
        DropInfo dropInfo = lookForLabels(pathNodes, RootInfo.TYPE_MZDB_FILES);
        if (dropInfo != null) {
            return dropInfo;
        }
        
        dropInfo = lookForLabels(pathNodes, RootInfo.TYPE_RAW_FILES);
        if (dropInfo != null) {
            return dropInfo;
        }
        
        dropInfo = lookForLabels(pathNodes, RootInfo.TYPE_RESULT_FILES);
        return dropInfo;

        
    }
    
    private DropInfo lookForLabels(String[] pathNodes, String rootInfoType) {
        ArrayList<String> labels = ServerFileSystemView.getServerFileSystemView().getLabels(rootInfoType);
        String parentLabel = null;
        int parentIndex = -1;

        for (int i = 0; i < pathNodes.length; i++) {

            for (int j = 0; j < labels.size(); j++) {
                String node = pathNodes[i];
                String label = labels.get(j);

                if (node.compareToIgnoreCase(label) == 0) {
                    parentLabel = pathNodes[i];
                    parentIndex = i;
                    break;
                }
            }

        }

        if (parentLabel == null || parentIndex == -1) {
            return null;
        }
        
        StringBuilder destinationBuilder = new StringBuilder();

        for (int i = parentIndex + 1; i < pathNodes.length; i++) {
            destinationBuilder.append(File.separator).append(pathNodes[i]);
        }
        destinationBuilder.append(File.separator);

        String destination = destinationBuilder.toString();
        
        return new DropInfo(parentLabel, parentIndex, destination, rootInfoType);
    }
    
    private class DropInfo {
        
        private String m_parentLabel;
        private int m_parentIndex;
        private String m_destination;
        private String m_rootInfoType;
        
        public DropInfo(String parentLabel, int parentIndex, String destination, String rootInfoType) {
            m_parentLabel = parentLabel;
            m_parentIndex = parentIndex;
            m_destination = destination;
            m_rootInfoType = rootInfoType;
        }
        
        public String getParentLabel() {
            return m_parentLabel;
        }
        
        public int getParentIndex() {
            return m_parentIndex;
        }
        
        public String getDestination() {
            return m_destination;
        }
        
        public ArrayList<File> filterCompatibleFiles(ArrayList<File> files) {
            
            String allowedExtension = null;
            if (m_rootInfoType.equals(RootInfo.TYPE_MZDB_FILES)) {
                allowedExtension = ".mzdb";
            } else if (m_rootInfoType.equals(RootInfo.TYPE_RAW_FILES)) {
                allowedExtension = ".raw";
            } else if (m_rootInfoType.equals(RootInfo.TYPE_RESULT_FILES)) {
                allowedExtension = ".dat";
            } 
            
            
            ArrayList<File> compatibleFiles = new ArrayList<>(files.size());
            for (File f : files) {
                if (f.getAbsolutePath().toLowerCase().endsWith(allowedExtension)) {
                    compatibleFiles.add(f);
                }
            }
            return compatibleFiles;
        }
    }
    
}
