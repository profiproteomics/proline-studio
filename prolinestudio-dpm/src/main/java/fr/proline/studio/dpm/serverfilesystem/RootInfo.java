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
package fr.proline.studio.dpm.serverfilesystem;

/**
 *
 * @author JM235353
 */
public class RootInfo {
    
    public static final String TYPE_RESULT_FILES = "result_files";
    public static final String TYPE_RAW_FILES = "raw_files";
    public static final String TYPE_MZDB_FILES = "mzdb_files";

            
    private String m_label;
    private String m_type;
    
    public RootInfo(String label, String type) {
        m_label = label;
        m_type = type;
    }
    
    public String getLabel() {
        return m_label;
    }
    
    public String getType() {
        return m_type;
    }
    
}
