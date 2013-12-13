package fr.proline.studio.pattern;

import java.util.ArrayList;

/**
 * Group of parameters
 * @author JM235353
 */
public class GroupParameter  {
    
    private ArrayList<DataParameter> m_parameterList = new ArrayList<>(1);
    
    public void addParameter(Class c, boolean isList) {
        m_parameterList.add(new DataParameter(c, isList));
    }
    
    public void addParameter(Class c, boolean isList, boolean isCompulsory) {
        m_parameterList.add(new DataParameter(c, isList, isCompulsory));
    }

    public ArrayList<DataParameter> getParameterList() {
        return m_parameterList;
    }
    
    public boolean isDataDependant(Class dataType) {
        int nbParameters  = m_parameterList.size();
        for (int i=0;i<nbParameters;i++) {
            DataParameter inParameter = m_parameterList.get(i);
            if (inParameter.equalsData(dataType)) {
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
