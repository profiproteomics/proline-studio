/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import java.util.ArrayList;
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
    private HashSet<DataParameter> inParameters = new HashSet<DataParameter>();
    private ArrayList<DataParameter> outParameters = new ArrayList<DataParameter>();
    
    protected String name;
    
    private DataBoxLayout layout = DataBoxLayout.VERTICAL;
    
    protected AbstractDataBox nextDataBox = null;
    protected AbstractDataBox previousDataBox = null;
    
    protected void registerInParameter(DataParameter parameter) {
        inParameters.add(parameter);
    }
    
    protected void registerOutParameter(DataParameter parameter)  {
        outParameters.add(parameter);
    }
    
    public boolean isDataDependant(Class dataType) {
        Iterator<DataParameter> it = inParameters.iterator();
        while (it.hasNext()) {
            DataParameter parameter = it.next();
            if (parameter.isDataDependant(dataType)) {
                return true;
            }
        }
        return false;
    }
  
    
    public boolean isCompatible(AbstractDataBox nextDataBox) {
        
        Iterator<DataParameter> it = nextDataBox.inParameters.iterator();
        
        while (it.hasNext()) {
            DataParameter parameter = it.next();
            
            if (parameter.isCompatibleWithOutParameter(outParameters)) {
                return true;
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
    
    public abstract void dataChanged(Class dataType);
    
    public Object getData(boolean getArray, Class parameterType) {
        if (previousDataBox != null) {
            return previousDataBox.getData(getArray, parameterType);
        }
        return null;
    }
    
    public void setEntryData(Object data) {
        throw new UnsupportedOperationException();
    }
    
    public void propagateDataChanged(Class dataType) {
        if (nextDataBox != null) {
            if (nextDataBox.isDataDependant(dataType)) {
                nextDataBox.dataChanged(dataType);
            }
            nextDataBox.propagateDataChanged(dataType);
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
