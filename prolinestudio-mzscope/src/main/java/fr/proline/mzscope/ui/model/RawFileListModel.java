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
package fr.proline.mzscope.ui.model;

import fr.proline.mzscope.model.IRawFile;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author VD225637
 */
public class RawFileListModel extends  AbstractListModel<IRawFile> {
   
    private List<IRawFile> rawFiles = new ArrayList<>();
    
    public RawFileListModel() {
    }

    public IRawFile[] getFiles() {
        return rawFiles.toArray(new IRawFile[0]);
    }

    @Override
    public IRawFile getElementAt(int index) {
        if (index >=0 && index < getSize()) {
            return rawFiles.get(index);
        }
        return null;
    }

    @Override
    public int getSize() {
        return rawFiles.size();
    }
    
    public boolean add(IRawFile f) {
        if (!rawFiles.contains(f)) {
            rawFiles.add(f);
            fireContentsChanged(this, rawFiles.size() - 2, rawFiles.size() - 1);
            return true;
        }
        return false;
    }
    
    public void removeAllFiles() {
        rawFiles = new ArrayList<>();
        fireContentsChanged(this, 0, getSize()-1);
    }
    
    public boolean removeFile(IRawFile f) {
        if (rawFiles.contains(f)) {
            rawFiles.remove(f);
            fireContentsChanged(this, 0, getSize()-1);
            return true;
        }
        return false;
    }
}
