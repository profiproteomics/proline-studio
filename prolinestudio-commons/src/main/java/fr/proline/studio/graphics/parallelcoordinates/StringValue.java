package fr.proline.studio.graphics.parallelcoordinates;

/**
 * Encapsulation of values of type String to be displayed in ParallelCoordinates
 * 
 * @author JM235353
 */
public class StringValue extends AbstractValue {
    
    private String m_s;
    
    public StringValue(String s, int rowIndex) {
        super(ValueTypeEnum.STRING, rowIndex);
        
        m_s = s;
    }
    
    @Override
    public String toString() {
        return m_s;
    }

    @Override
    public int compareTo(AbstractValue o) {
        return m_s.compareTo(((StringValue) o).m_s);
    }
}
