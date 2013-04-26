/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class DataParameter  {
    
    private ArrayList<Parameter> parameterList = new ArrayList<Parameter>(1);
    
    public void addParameter(Class c, boolean isList) {
        parameterList.add(new Parameter(c, isList));
    }

    public boolean isDataDependant(Class dataType) {
        int nbParameters  = parameterList.size();
        for (int i=0;i<nbParameters;i++) {
            Parameter inParameter = parameterList.get(i);
            if (inParameter.equalsData(dataType)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isCompatibleWithOutParameter(ArrayList<DataParameter> outParameterList) {
        int nbInParameters  = parameterList.size();
        int nbOutParameters = outParameterList.size();
        
        for (int i=0;i<nbInParameters;i++) {
            Parameter inParameter = parameterList.get(i);
            
            
            boolean compatibleOutParameterFound = false;
            for (int j=0;j<nbOutParameters;j++) {
                
                DataParameter dataOutParameter = outParameterList.get(j);
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
    
    private boolean isCompatibleWithInParameter(Parameter inParameter) {
        int nbOutParameters  = parameterList.size();
        for (int i=0;i<nbOutParameters;i++) {
            Parameter outParameter = parameterList.get(i);
            if (inParameter.isCompatibleWithOutParameter(outParameter)) {
                return true;
            }
        }
        return false;
    }
    
    private class Parameter {

        private Class c;
        private boolean isList;

        private Parameter(Class c, boolean isList) {
            this.c = c;
            this.isList = isList;
        }
        
        private boolean equalsData(Class dataC) {
            return c.equals(dataC);
        }
        
        private boolean isCompatibleWithOutParameter(Parameter outParameter) {
            if (!c.equals(outParameter.c)) {
                return false;
            }
            if (isList && !outParameter.isList) {
                return false;
            }
            return true;

        }
        
    }

}
