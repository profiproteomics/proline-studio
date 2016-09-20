package fr.proline.studio.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *  
 * @author JM235353
 */
public class AvailableParameters {
    

    private HashMap<DataParameter, Integer> m_availableParametersMap;
    
    public AvailableParameters(AbstractDataBox box) {
        
        initAvailableParameters(box);
    }
    
    public HashMap<DataParameter, Integer> getParametersMap() {
        return m_availableParametersMap;
    }
    
    private void initAvailableParameters(AbstractDataBox box) {
        m_availableParametersMap = new HashMap<>();
        initAvailableParameters(box, 0);
    }
    private void initAvailableParameters(AbstractDataBox box, Integer depth) {
    
        ArrayList<GroupParameter> outParameters = box.getOutParameters();
        for (int i=0;i<outParameters.size();i++) {
            GroupParameter groupParameter = outParameters.get(i);
            ArrayList<DataParameter> parameterList = groupParameter.getParameterList();
            for (int j=0;j<parameterList.size();j++) {
                m_availableParametersMap.put(parameterList.get(j), depth);
            }
        }
        
        AbstractDataBox previousDataBox = box.m_previousDataBox;
        if (previousDataBox != null) {
            initAvailableParameters(previousDataBox, depth+1);
        }
    }
    
    
    public double calculateParameterCompatibilityDistance(AbstractDataBox box, Class compulsoryInParameterClass) {
        
        HashSet<GroupParameter> inParametersHashSet = box.getInParameters();
        
        boolean foundCompulsoryInParameter = (compulsoryInParameterClass == null);
        double minAverageDistance = -1;
        Iterator<GroupParameter> it = inParametersHashSet.iterator();
        while (it.hasNext()) {
            GroupParameter groupParameter = it.next();
            ArrayList<DataParameter> parameterList = groupParameter.getParameterList();
            int distanceCur = 0;
            for (int i=0;i<parameterList.size();i++) {
                
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
            
            if (distanceCur>=0) {
                
                double averageDistanceCur = ((double)distanceCur) / parameterList.size();
                
                if (minAverageDistance < 0) {
                    minAverageDistance = averageDistanceCur;
                } else if (minAverageDistance>averageDistanceCur) {
                    minAverageDistance = averageDistanceCur;
                }
            }
            
        }
        
        if (!foundCompulsoryInParameter) {
            return -1;
        }
        
        return minAverageDistance;

    }
    

    
}
