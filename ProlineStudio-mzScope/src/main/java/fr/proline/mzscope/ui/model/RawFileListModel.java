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
