package fr.proline.studio.rserver.data;

/**
 *
 * @author JM235353
 */
public class RExpression {
    
    private String m_RVariable = null;
    private String m_RExpression = null;

    
    public void setRVariable(String v) {
        m_RVariable = v;
    }

    public String getRVariable() {
        return m_RVariable;
    }

    public void setRExpression(String e) {
        m_RExpression = e;
    }

    public String getRExpression() {
        return m_RExpression;
    }
}
