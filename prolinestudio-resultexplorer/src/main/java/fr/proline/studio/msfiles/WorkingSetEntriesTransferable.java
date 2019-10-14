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

    private final ArrayList<TransferableEntryWrapper> m_selectedEntries;

    public WorkingSetEntriesTransferable(ArrayList<TransferableEntryWrapper> selectedEntries) {
        m_selectedEntries = selectedEntries;
    }

    public ArrayList<TransferableEntryWrapper> getEntries() {
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
