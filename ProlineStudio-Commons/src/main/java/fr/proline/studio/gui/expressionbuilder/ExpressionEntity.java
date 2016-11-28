package fr.proline.studio.gui.expressionbuilder;

/**
 *
 * @author JM235353
 */
public class ExpressionEntity {

    private String m_name;
    private String m_code;
    private Class m_type;

    public ExpressionEntity(String name, String code, Class type) {
        m_name = name;
        m_code = code;
        m_type = type;
    }

    public String getName() {
        return m_name;
    }

    public String getCode() {
        return m_code;
    }

}
