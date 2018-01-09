/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
public class WorkingSetEntriesTransferHandler extends TransferHandler {

    public WorkingSetEntriesTransferHandler() {
        ;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {

        ArrayList<TransferableEntryWrapper> transferable = new ArrayList<TransferableEntryWrapper>();

        if (c instanceof JTree) {
            JTree tree = (JTree) c;

            TreePath[] paths = tree.getSelectionPaths();
            for (TreePath path : paths) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                Object userObject = node.getUserObject();

                if (userObject instanceof WorkingSetEntry) {
                    transferable.add(new TransferableEntryWrapper((WorkingSetEntry) userObject, path.getParentPath()));
                }

            }

            if (transferable.isEmpty()) {
                return null;
            }

            return new WorkingSetEntriesTransferable(transferable);

        }
        return null;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        ;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);

        if (support.isDataFlavorSupported(WorkingSetEntriesTransferable.WorkingSetEntries_FLAVOR)) {
            if (support.getDropLocation() instanceof JTree.DropLocation) {

                JTree.DropLocation location = (JTree.DropLocation) support.getDropLocation();
                TreePath path = location.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof WorkingSet) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {

        try {

            WorkingSetEntriesTransferable transferable = (WorkingSetEntriesTransferable) support.getTransferable().getTransferData(WorkingSetEntriesTransferable.WorkingSetEntries_FLAVOR);

            if (support.getComponent() instanceof JTree) {

                ArrayList<TransferableEntryWrapper> entriesWrappers = transferable.getEntries();

                JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();

                TreePath dropPath = dropLocation.getPath();

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) dropPath.getLastPathComponent();

                if (node.getUserObject() instanceof WorkingSet) {
                    WorkingSet set = (WorkingSet) node.getUserObject();

                        for (int i = 0; i < entriesWrappers.size(); i++) {
                            if(set.addEntry(WorkingSetView.getWorkingSetView().getModel().getEntiesObjects().get(entriesWrappers.get(i).getEntry().getPath()))){
                                
                                DefaultMutableTreeNode sourceNode = (DefaultMutableTreeNode) entriesWrappers.get(i).getSource().getLastPathComponent();
                                if(sourceNode.getUserObject() instanceof WorkingSet){
                                    WorkingSet sourceSet = (WorkingSet) sourceNode.getUserObject();
                                    sourceSet.removeEntry(entriesWrappers.get(i).getEntry().getPath());
                                }
                                
                                WorkingSetView.getWorkingSetView().expand(dropPath);
                                WorkingSetView.getWorkingSetView().reloadAndSave();

                            }
                        }
                    
                }

                return true;

            }

        } catch (UnsupportedFlavorException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;

    }

}
