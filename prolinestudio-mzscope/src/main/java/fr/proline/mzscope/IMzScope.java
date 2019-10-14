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
package fr.proline.mzscope;

import java.io.File;
import java.util.List;

/**
 * mzscope interface
 * @author MB243701
 */
public interface IMzScope {
    /**
     * open the specified file, and extract at the moz specified value
     * @param file
     * @param moz 
     * @param elutionTime 
     * @param firstScanTime 
     * @param lastScanTime 
     */
    public abstract void openRawAndExtract(File file, double moz, double elutionTime, double firstScanTime, double lastScanTime);
    
    
    /**
     * open the specified file
     * @param file 
     * @param b 
     */
    public abstract void openRaw(File file, boolean b);
    
    
    /**
     * open multi files
     * @param files 
     * @param b 
     */
    public abstract void openRaw(List<File> files, boolean b);
    
    /**
     * launch the detectPeeakels dialog for 1 file
     * @param file 
     */
    public abstract void detectPeakels(File file);
    
    /**
     * launch the detectPeakels dialog for all selected files
     * @param fileList 
     */
    public abstract void detectPeakels(List<File> fileList);
    
}
