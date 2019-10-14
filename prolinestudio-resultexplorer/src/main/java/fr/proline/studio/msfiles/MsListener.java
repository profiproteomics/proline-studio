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

import java.util.ArrayList;

/**
 *
 * @author AK249877
 */
public interface MsListener {  
    
    public void conversionPerformed(ArrayList<MsListenerConverterParameter> list);
    
    public void uploadPerformed(ArrayList<MsListenerParameter> list);
    
    public void downloadPerformed(ArrayList<MsListenerDownloadParameter> list);
    
    public void exportPerformed(ArrayList<MsListenerParameter> list);
    
    public void verificationPerformed(ArrayList<MsListenerParameter> list);
    
    public void entryStateUpdated(ArrayList<MsListenerEntryUpdateParameter> list);
    
}
