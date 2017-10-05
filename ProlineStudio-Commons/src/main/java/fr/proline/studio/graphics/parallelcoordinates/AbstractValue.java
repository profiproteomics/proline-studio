package fr.proline.studio.graphics.parallelcoordinates;

/**
 *
 * @author JM235353
 */
public abstract class AbstractValue implements Comparable<AbstractValue> {

    protected enum ValueTypeEnum {
        STRING,
        NUMBER
    }
    
    protected ValueTypeEnum m_type;
    protected int m_rowIndex;
    protected int m_index;
    
    public AbstractValue(ValueTypeEnum type, int rowIndex) {
        m_type = type;
        m_rowIndex = rowIndex;
    }
    
    public void setIndex(int index) {
        m_index = index;
    }
    
    public int getRowIndex() {
        return m_rowIndex;
    }
    
    public int getIndex() {
        return m_index;
    }

    public boolean isNan() {
        return false;
    }
    
}
