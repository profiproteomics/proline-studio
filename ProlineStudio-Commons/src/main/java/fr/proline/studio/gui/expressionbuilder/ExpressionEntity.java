package fr.proline.studio.gui.expressionbuilder;

/**
 *
 * @author JM235353
 */
public class ExpressionEntity {

    private String m_name;
    private String m_nameDisplayed;
    private String m_code;

    public ExpressionEntity(String name, String nameDisplayed, String code) {
        m_name = name;
        m_nameDisplayed = nameDisplayed;
        m_code = code;
    }

    public String getName() {
        return m_name;
    }
    
    public String getNameDisplayed() {
        return m_nameDisplayed;
    }

    public String getCode() {
        return m_code;
    }

}
