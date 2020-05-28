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
package fr.proline.studio.pattern;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Group of parameters
 * @author JM235353
 */
public class GroupParameter  {
    
    private final ArrayList<DataParameter> m_parameterList = new ArrayList<>(1);
    
    public void addParameter(Class type) {
        m_parameterList.add(new DataParameter(type, ParameterSubtypeEnum.SINGLE_DATA));
    }
    
    public void addParameter(Class type, boolean isCompulsory) {
        m_parameterList.add(new DataParameter(type, ParameterSubtypeEnum.SINGLE_DATA, isCompulsory));
    }
    
    public void addParameter(Class type, ParameterSubtypeEnum subtype) {
        m_parameterList.add(new DataParameter(type, subtype));
    }
    
    public void addParameter(Class type, ParameterSubtypeEnum subtype, boolean isCompulsory) {
        m_parameterList.add(new DataParameter(type, subtype, isCompulsory));
    }

    public ArrayList<DataParameter> getParameterList() {
        return m_parameterList;
    }
    
    public void addParameter(GroupParameter groupParameter) {
        for (DataParameter dataParameter : groupParameter.getParameterList()) {
            m_parameterList.add(dataParameter);
        }
    }
    
    public boolean isDataDependant(Class dataType, ParameterSubtypeEnum subtype) {

        int nbParameters = m_parameterList.size();
        for (int i = 0; i < nbParameters; i++) {
            DataParameter parameter = m_parameterList.get(i);
            if (parameter.equalsData(dataType, subtype)) {
                return true;
            }
        }

        return false;
    }
    
    public boolean isCompatibleWithOutParameter(ArrayList<GroupParameter> outParameterList) {
        int nbInParameters  = m_parameterList.size();
        int nbOutParameters = outParameterList.size();
        
        for (int i=0;i<nbInParameters;i++) {
            DataParameter inParameter = m_parameterList.get(i);
            
            if (! inParameter.isCompulsory()) {
                continue;
            }
            
            boolean compatibleOutParameterFound = false;
            for (int j=0;j<nbOutParameters;j++) {
                
                GroupParameter dataOutParameter = outParameterList.get(j);
                if (dataOutParameter.isCompatibleWithInParameter(inParameter)) {
                    compatibleOutParameterFound = true;
                    break;
                }
            }
            if (!compatibleOutParameterFound) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isCompatibleWithInParameter(DataParameter inParameter) {
        int nbOutParameters  = m_parameterList.size();
        for (int i=0;i<nbOutParameters;i++) {
            DataParameter outParameter = m_parameterList.get(i);
            if (inParameter.isCompatibleWithOutParameter(outParameter)) {
                return true;
            }
        }
        return false;
    }
    


}
