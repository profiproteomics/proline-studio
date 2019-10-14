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
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

/**
 *
 * @author AK249877
 */
public class AssociationWrapper {
    
    public enum AssociationType {

        ASSOCIATED, NOT_ASSOCIATED
    }
    
    private AssociationType m_associationType;
    private final String m_path;
    
    public AssociationWrapper(String path, AssociationType associationType){
        m_path = path;
        m_associationType = associationType;
    }
    
    public String getPath(){
        return m_path;
    }
    
    public AssociationType getAssociationType(){
        return m_associationType;
    }
    
    public void setAssociationType(AssociationType associationType){
        m_associationType = associationType;
    }
    
}
