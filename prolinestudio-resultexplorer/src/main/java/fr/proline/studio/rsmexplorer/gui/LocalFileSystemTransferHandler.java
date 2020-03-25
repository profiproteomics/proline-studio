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

import fr.proline.studio.dpm.task.jms.DownloadProcessedFileTask;
import fr.proline.studio.msfiles.MzdbDownloadBatch;
import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.openide.util.Exceptions;

/**
 *
 * @author AK249877
 */
public class LocalFileSystemTransferHandler extends TransferHandler {

    public LocalFileSystemTransferHandler() {
        ;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {

        ArrayList<File> transferableFiles = new ArrayList<File>();

        if (c instanceof JTree) {
            JTree tree = (JTree) c;

            TreePath[] paths = tree.getSelectionPaths();
            for (TreePath path : paths) {
                
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                File file = (File) node.getUserObject();
                
                String url = file.getAbsolutePath().toLowerCase();
                
                if (url.endsWith(".mzdb") || url.endsWith(".raw") || url.endsWith(".wiff") || url.endsWith(".dat")) {
                    transferableFiles.add(file);
                }
            }

            if (transferableFiles.isEmpty()) {
                return null;
            }

            return new FilesTransferable(transferableFiles, FilesTransferable.SourceFileSystem.SOURCE_LOCAL_FILE_SYSTEM);

        }
        return null;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        //clearHighlights();
        ;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);

        if (support.isDataFlavorSupported(FilesTransferable.Files_FLAVOR)) {
            DropLocation dropLocation = support.getDropLocation();
            if (dropLocation instanceof JTree.DropLocation) {
                
                try {
                    FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
                    if (transferable.getSource() == FilesTransferable.SourceFileSystem.SOURCE_LOCAL_FILE_SYSTEM) {
                        return false;
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    return false;
                }
                
                TreePath dropPath = ((JTree.DropLocation) support.getDropLocation()).getPath();
                File dropFile = pathToFile(MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().getSelectedRoot(), dropPath);
                if (!dropFile.isDirectory()) {
                    return false;
                }
 
                return true;
            }else if(support.getComponent() instanceof TreeFileChooserPanel){
                return true;
            }
        }
        
        
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        
        try {
            
            FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
            ArrayList<File> filesToTransfer = transferable.getFiles();
            
            if (support.getComponent() instanceof JTree) {
                
                JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
                
                TreePath dropPath = dropLocation.getPath();
                
                MzdbDownloadBatch downloadBatch = new MzdbDownloadBatch(filesToTransfer, dropPath, MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().getSelectedRoot());
                Thread downloadThread = new Thread(downloadBatch);
                downloadThread.start();

                return true;
                
            } else if (support.getComponent() instanceof TreeFileChooserPanel) {
                return true;
            }
            
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;

    }

    private File pathToFile(String root, TreePath pathToExpand) {
        
        StringBuilder localURL = new StringBuilder();
        localURL.append(root);
        
        Object elements[] = pathToExpand.getPath();
        for (int i = 0, n = elements.length; i < n; i++) {
            localURL.append(elements[i]).append("\\");
        }

        File f = new File(localURL.toString());
        return f;
    }
}
