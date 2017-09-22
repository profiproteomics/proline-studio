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
    
    public void setValue(Number n) {
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
        
        double v1 = m_n.doubleValue();
        double v2 = ((NumberValue) o).m_n.doubleValue();
        if (Double.isNaN(v1)) {
            if (Double.isNaN(v2)) {
                return 0;
            } else {
                return -1;
            }
        } else if (Double.isNaN(v2)) {
            return 1;
        }
        
        double delta =  v1 - v2;
        if (delta >0) {
            return 1;
        } else if (delta < 0) {
            return -1;
        }
        return 0;
    }
    
    @Override
    public boolean isNan() {
        return Double.isNaN(doubleValue());
    }
    
}
