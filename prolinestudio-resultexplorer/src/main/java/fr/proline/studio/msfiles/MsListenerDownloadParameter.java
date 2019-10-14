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

import java.io.File;

/**
 *
 * @author AK249877
 */
public class MsListenerDownloadParameter {
    
    private final File m_destinationFile, m_remoteFile;
    private final boolean m_success;
    
    public MsListenerDownloadParameter(File destinationFile, File remoteFile,boolean success){
        m_destinationFile = destinationFile;
        m_remoteFile = remoteFile;
        m_success = success;
    }
    
    public File getDestinationFile(){
        return m_destinationFile;
    }
    
    public File getRemoteFile(){
        return m_remoteFile;
    }
    
    public boolean wasSuccessful(){
        return m_success;
    }
    
}
