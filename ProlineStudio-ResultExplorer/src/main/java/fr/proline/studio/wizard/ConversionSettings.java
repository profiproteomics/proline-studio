/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

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
