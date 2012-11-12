/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author JM235353
 */
public abstract class AbstractDataBox {
   
    public enum DataBoxLayout {
      VERTICAL,
      HORIZONTAL,
      TABBED
    };
    
    // Panel corresponding to this box
    protected DataBoxPanelInterface panel;
    
    // In and out Parameters Registration
    private HashMap<Class, HashSet<Class>> inParametersMap = new HashMap<Class, HashSet<Class>>();
    private HashMap<Class, HashSet<Class>> outParametersMap = new HashMap<Class, HashSet<Class>>();
    
    protected String name;
    
    private DataBoxLayout layout = DataBoxLayout.VERTICAL;
    
    protected AbstractDataBox nextDataBox = null;
    protected AbstractDataBox previousDataBox = null;
    
    protected void registerInParameterType(Class arrayParameterType, Class parameterType) {
        registerParameter(inParametersMap, arrayParameterType, parameterType);
    }
    
    protected void registerOutParameterType(Class arrayParameterType, Class parameterType) {
        registerParameter(outParametersMap, arrayParameterType, parameterType);
    }
    
    private void registerParameter(HashMap<Class, HashSet<Class>> map, Class arrayParameterType, Class parameterType) {
        HashSet<Class> subMap = map.get(parameterType);
        if (subMap == null) {
            subMap = new HashSet<Class>();
            map.put(parameterType, subMap);
        }
        subMap.add(arrayParameterType);
    }
    
    private boolean hasParameter(HashMap<Class, HashSet<Class>> map, Class arrayParameterType, Class parameterType) {
        HashSet<Class> subMap = map.get(parameterType);
        if (subMap == null) {
            return false;
        }
        return subMap.contains(arrayParameterType);
    }
    
    
    public boolean isCompatible(AbstractDataBox nextDataBox) {
        Iterator<Class> it = nextDataBox.inParametersMap.keySet().iterator();
        while (it.hasNext()) {
            Class parameterType = it.next();
            HashSet<Class> arrayParameterTypeSet = nextDataBox.inParametersMap.get(parameterType);
            Iterator<Class> itArray = arrayParameterTypeSet.iterator();
            while (itArray.hasNext()) {
                Class arrayParameterType = itArray.next();
                if (hasParameter(outParametersMap, arrayParameterType, parameterType)) {
                    return true;
                }
            }
        }
        if (previousDataBox != null) {
            return previousDataBox.isCompatible(nextDataBox);
        }
        return false;
    }
    
    public void setNextDataBox(AbstractDataBox nextDataBox) {
        this.nextDataBox = nextDataBox;
        nextDataBox.previousDataBox = this;
    }
    
    public abstract void createPanel();
    
    public abstract void dataChanged(AbstractDataBox srcDataBox);
    
    public Object getData(Class arrayParameterType, Class parameterType) {
        if (previousDataBox != null) {
            return previousDataBox.getData(arrayParameterType, parameterType);
        }
        return null;
    }
    
    public void setEntryData(Object data) {
        throw new UnsupportedOperationException();
    }
    
    public void propagateDataChanged() {
        if (nextDataBox != null) {
            nextDataBox.dataChanged(this);
        }
    }
    
    public DataBoxPanelInterface getPanel() {
        return panel;
    }
    
    public void setLayout(DataBoxLayout layout) {
        this.layout = layout;
    }
    
    public DataBoxLayout getLayout() {
        return layout;
    }
    
    public String getName() {
        return name;
    }
}
