/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

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
                
                if (url.endsWith(".mzdb") || url.endsWith(".raw") || url.endsWith(".wiff")) {
                    transferableFiles.add(file);
                }
            }

            if (transferableFiles.isEmpty()) {
                return null;
            }

            return new FilesTransferable(transferableFiles);

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
            ArrayList<File> files = transferable.getFiles();
            
            if (support.getComponent() instanceof JTree) {
                
                JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
                
                TreePath dropPath = dropLocation.getPath();
                
                MzdbDownloadBatch downloadBatch = new MzdbDownloadBatch(files, dropPath, MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().getSelectedRoot());
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

}
