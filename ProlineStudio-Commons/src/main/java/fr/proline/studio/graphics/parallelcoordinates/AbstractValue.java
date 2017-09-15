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
    
    public AbstractValue(ValueTypeEnum type, int rowIndex) {
        m_type = type;
        m_rowIndex = rowIndex;
    }
    
    public int getRowIndex() {
        return m_rowIndex;
    }

    
}
