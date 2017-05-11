package fr.proline.studio.filter;

import fr.proline.studio.table.LazyData;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 * Filter base class
 * @author JM235353
 */
public abstract class Filter {


    
    protected ArrayList<Integer> m_valueKeys = null;
    protected HashMap<Integer, Component> m_components = null;
    
    protected String m_variableName;
    protected boolean m_used;
    protected boolean m_defined;

    private int m_index = -1;
    
    protected final int m_modelColumn;
    
    protected final int m_extraModelColumn;
    
    protected final ConvertValueInterface m_convertValueInterface;
    
    
    public Filter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        this(variableName, convertValueInterface, modelColumn, -1);
    }
    public Filter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn, int extraModelColumn) {
        m_variableName = variableName;
        m_convertValueInterface = convertValueInterface;
        m_modelColumn = modelColumn;
        m_extraModelColumn = extraModelColumn;
    }
    
    public void setIndex(int index){
        m_index = index;
    }
    
    public int getIndex(){
        return m_index;
    }
    
    public abstract boolean filter(Object v1, Object v2);
    
    public abstract Filter cloneFilter();
    
    public void setValuesForClone(Filter clone) {
        clone.m_used = m_used;
        clone.m_defined = m_defined;
        clone.m_valueKeys = m_valueKeys;
    }
    
    public int getModelColumn() {
        return m_modelColumn;
    }
    
    public int getExtraModelColumn() {
        return m_extraModelColumn;
    }
    
    public Object convertValue(Object o) {
        if (o instanceof LazyData) {
            o = ((LazyData) o).getData();
        }

        if (m_convertValueInterface == null) {
            return o;
        }
        return m_convertValueInterface.convertValue(o);
    }

    
    public abstract FilterStatus checkValues();
    
    
    public void registerDefinedAsUsed() {
        m_used = m_defined;
    }
    
    /**
     * @return if the filter has been changed since the last time
     */
    public abstract boolean registerValues();
    

    public ArrayList<Integer> getValueKeys() {
        return m_valueKeys;
    }

    public String getName() {
        return m_variableName;
    }
    
    public boolean isDefined() {
        return m_defined;
    }
    
    public void setDefined(boolean defined) {
        m_defined = defined;
    }

    
    public boolean isUsed() {
        return m_used;
    }
   
    public void setUsed(boolean used) {
        m_used = used;
    }

    
    @Override
    public String toString() {
        return m_variableName;
    }
    
    
    public void registerComponent(Integer key, Component c) {
        if (m_components == null) {
            m_components = new HashMap<>();
        }
        m_components.put(key, c);
    }
    
    public Component getComponent(Integer key) {
        if (m_components == null) {
            return null;
        }
        return m_components.get(key);
    }
    
    public void clearComponents() {
        if (m_components == null) {
            return;
        }
        m_components.clear();
    }
    
    public abstract void createComponents(JPanel p, GridBagConstraints c);
    
    public abstract void reset();
}
