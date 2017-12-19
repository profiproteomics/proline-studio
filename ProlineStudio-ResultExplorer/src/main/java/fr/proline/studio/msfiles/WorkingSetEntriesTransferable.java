/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author AK249877
 */
public class WorkingSetEntriesTransferable implements Transferable, Serializable {

    public final static DataFlavor WorkingSetEntries_FLAVOR = new DataFlavor(WorkingSetEntriesTransferable.class, "Drag and drop Entries Selection");

    private static final DataFlavor[] DATA_FLAVORS = {WorkingSetEntries_FLAVOR};

    private final ArrayList<WorkingSetEntry> m_selectedEntries;

    public WorkingSetEntriesTransferable(ArrayList<WorkingSetEntry> selectedFiles) {
        m_selectedEntries = selectedFiles;
    }

    public ArrayList<WorkingSetEntry> getFiles() {
        return m_selectedEntries;
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
