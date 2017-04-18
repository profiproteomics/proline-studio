/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

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
                String url = path.getLastPathComponent().toString().toLowerCase();
                if (url.endsWith(".mzdb") || url.endsWith(".raw")) {
                    transferableFiles.add(new File(url));
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

        if (support.getComponent() instanceof JTree) {

            //JTree tree = (JTree) support.getComponent();
            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();

            TreePath path = dropLocation.getPath();

            //int dropRow = tree.getRowForPath(path);

        } else if (support.getComponent() instanceof TreeFileChooserPanel) {
            return true;
        }

        return true;

    }

}
