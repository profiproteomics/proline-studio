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

import java.util.*;

/**
 *  Container for available parameters of a DataBox
 * 
 * @author JM235353
 */
public class AvailableParameters {
    

    private TreeMap<DataParameter, Integer> m_availableParametersMap;
    
    public AvailableParameters(AbstractDataBox box) {
        
        initAvailableParameters(box);
    }
    
    public Map<DataParameter, Integer> getParametersMap() {
        return m_availableParametersMap;
    }
    
    private void initAvailableParameters(AbstractDataBox box) {
        m_availableParametersMap = new TreeMap<>(new Comparator<DataParameter>() {
            @Override
            public int compare(DataParameter o1, DataParameter o2) {
                if(o1 == null && o2 == null)
                    return 0;
                if(o1 == null)
                    return  1;
                if(o2 == null)
                    return  1;
                if (o1.equalsData(o2))
                    return 0;
                if(o1.equals(o2))
                    return (o1.getSubtype().compareTo(o2.getSubtype()));
                return (o1.getParameterClass().toString().compareTo(o2.getParameterClass().toString()));
            }
        });
        initAvailableParameters(box, 0);
    }
    private void initAvailableParameters(AbstractDataBox box, Integer depth) {
    
        ParameterList outParameters = box.getOutParameters();

        ArrayList<DataParameter> parameterList = outParameters.getParameterList();
        for (int j = 0; j < parameterList.size(); j++) {
            m_availableParametersMap.put(parameterList.get(j), depth);
        }
        
        AbstractDataBox previousDataBox = box.m_previousDataBox;
        if (previousDataBox != null) {
            initAvailableParameters(previousDataBox, depth+1);
        }
    }
    
    
    public double calculateParameterCompatibilityDistance(AbstractDataBox box, Class compulsoryInParameterClass) {
        
        ParameterList inParameters = box.getInParameters();
        
        boolean foundCompulsoryInParameter = (compulsoryInParameterClass == null);
        double minAverageDistance = -1;

        ArrayList<DataParameter> parameterList = inParameters.getParameterList();
        int distanceCur = 0;
        for (int i = 0; i < parameterList.size(); i++) {

            DataParameter parameter = parameterList.get(i);
            if ((!foundCompulsoryInParameter) && (compulsoryInParameterClass != null)) {
                Class parameterClass = parameter.getParameterClass();
                foundCompulsoryInParameter = parameterClass.equals(compulsoryInParameterClass);
            }

            if (!parameter.isCompulsory()) {
                continue;
            }
            Integer distanceCurI = m_availableParametersMap.get(parameter);
            if (distanceCurI == null) {
                distanceCur = -1;
                break;
            }
            distanceCur += distanceCurI;
        }

        if (distanceCur >= 0) {

            double averageDistanceCur = ((double) distanceCur) / parameterList.size();

            if (minAverageDistance < 0) {
                minAverageDistance = averageDistanceCur;
            } else if (minAverageDistance > averageDistanceCur) {
                minAverageDistance = averageDistanceCur;
            }
        }


        if (!foundCompulsoryInParameter) {
            return -1;
        }
        
        return minAverageDistance;

    }
    

    
}
