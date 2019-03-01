/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author AK249877
 */
public class FilesTransferable implements Transferable, Serializable {

    public final static DataFlavor Files_FLAVOR = new DataFlavor(FilesTransferable.class, "Drag and drop Files Selection");

    private static final DataFlavor[] DATA_FLAVORS = {Files_FLAVOR};

    private final ArrayList<File> m_selectedFiles;

    public FilesTransferable(ArrayList<File> selectedFiles) {
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
