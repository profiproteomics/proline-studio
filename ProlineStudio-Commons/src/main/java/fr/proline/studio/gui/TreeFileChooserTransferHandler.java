package fr.proline.studio.gui;


import fr.proline.studio.table.DecoratedTableModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JTable;

import javax.swing.TransferHandler;

/**
 * Class used for Drag and Drop of nodes
 * @author JM235353
 */
public class TreeFileChooserTransferHandler extends TransferHandler {
    
    //private Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
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
            
            return new FilesTransferable(selectedFiles);

        }
        return null;
    }

    
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        support.setShowDropLocation(true);
        if (support.isDataFlavorSupported(FilesTransferable.Files_FLAVOR)) {

            // drop path
            DropLocation dropLocation = support.getDropLocation();
            if (dropLocation instanceof JTable.DropLocation) {
                return true;
            }
        }
        
        return false;
            
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {

        try {
            JTable table = (JTable) support.getComponent();
            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            
            int index = table.convertRowIndexToModel(dl.getRow());
            
            int max = table.getModel().getRowCount();
            if (index < 0 || index > max) {
                return false;
            }
            
            System.out.println("brekpoint");

            FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
            ArrayList<File> transferredFiles = transferable.getFiles();
            TreeFileChooserTableModelInterface model = (TreeFileChooserTableModelInterface) table.getModel();
            model.setFiles(transferredFiles, index);

        } catch (Exception e) {
            //should not happen
            return false;
        }

        return true;
    }

    
    public static class FilesTransferable implements Transferable, Serializable {

        public final static DataFlavor Files_FLAVOR = new DataFlavor(FilesTransferable.class, "Drag and drop Files Selection");

        private static final DataFlavor[] DATA_FLAVORS = {Files_FLAVOR};

        private final ArrayList<File> m_selectedFiles;
        
        public FilesTransferable( ArrayList<File> selectedFiles) {
            m_selectedFiles = selectedFiles;
        }
        
        public ArrayList<File> getFiles() {
            return m_selectedFiles;
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return this;
            }

            return null;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return DATA_FLAVORS;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return (DATA_FLAVORS[0].equals(flavor));
        }

    }

}
