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

/**
 *
 * @author AK249877
 */
public class ConversionSettings {
    
    private final String m_converterPath, m_outputPath;
    private final boolean m_deleteRaw, m_uploadAfterConversion;
    private MzdbUploadSettings m_uploadSettings;
    
    public ConversionSettings(String converterPath, String outputPath, boolean deleteRaw, boolean uploadAfterConversion){
        m_converterPath = converterPath;
        m_outputPath = outputPath;
        m_deleteRaw = deleteRaw;
        m_uploadAfterConversion = uploadAfterConversion;
    }
    
    public String getConverterPath(){
        return m_converterPath;
    }
    
    public String getOutputPath(){
        return m_outputPath;
    }
    
    public boolean getDeleteRaw(){
        return m_deleteRaw;
    }
    
    public boolean getUploadAfterConversion(){
        return m_uploadAfterConversion;
    }
    
    public void setUploadSettings(MzdbUploadSettings settings){
        m_uploadSettings = settings;
    }
    
    public MzdbUploadSettings getUploadSettings(){
        return m_uploadSettings;
    }
    
}
