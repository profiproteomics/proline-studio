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
