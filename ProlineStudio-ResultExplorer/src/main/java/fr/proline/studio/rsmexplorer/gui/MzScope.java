/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.mzscope.MzdbInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MB243701
 */
public class MzScope implements MzScopeInterface{
    private File file;
    private List<File> listFile;
    private int action;

    public MzScope(int action, File file) {
        super();
        this.action = action;
        this.file = file;
    }
    
    public MzScope(int action, List<File>  listFile) {
        super();
        this.action = action;
        this.listFile = listFile;
    }
    
    @Override
    public List<MzdbInfo> getMzdbInfo() {
        List<MzdbInfo> mzdbInfos = new ArrayList();
        if (this.listFile != null){
            MzdbInfo info  = new MzdbInfo(action, listFile);
             mzdbInfos.add(info); 
        }else if (file != null){
            MzdbInfo info  = new MzdbInfo(action, file);
            mzdbInfos.add(info); 
        }
        return mzdbInfos;
    }
    
}
