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
public class MzdbUploadSettings {
    
    private final boolean m_deleteMzdb;
    private final String m_mountLabel;
    private final String m_destination;
    
    public MzdbUploadSettings(boolean deleteMzdb, String mountLabel, String destination){
        m_deleteMzdb = deleteMzdb;
        m_mountLabel = mountLabel;
        m_destination = destination;
    }
    
    public boolean getDeleteMzdb(){
        return m_deleteMzdb;
    }
    
    public String getMountingPointPath(){
        return m_mountLabel;
    }
    
    public String getDestination(){
        return m_destination;
    }
    
}
