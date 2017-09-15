package fr.proline.studio.graphics.parallelcoordinates;

/**
 *
 * @author JM235353
 */
public class NumberValue extends AbstractValue {
    
    private Number m_n;
    
    public NumberValue(Number n, int rowIndex) {
        super(ValueTypeEnum.NUMBER, rowIndex);
        
        m_n = n;
    }
    
    @Override
    public String toString() {
        return m_n.toString();
    }
    
    public double doubleValue() {
        return m_n.doubleValue();
    }
    
    @Override
    public int compareTo(AbstractValue o) {
        double delta =  m_n.doubleValue() - ((NumberValue) o).m_n.doubleValue();
        if (delta >0) {
            return 1;
        } else if (delta < 0) {
            return -1;
        }
        return 0;
    }
}
