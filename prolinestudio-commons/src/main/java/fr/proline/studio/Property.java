package fr.proline.studio;

public class Property {

    protected String m_key;

    protected String m_propName;
    protected Class m_class;
    protected Object m_value;

    public Property() {

    }
    public Property(String propNameKey, String propName, Class c, Object value) {
        m_key = propNameKey;
        m_class = c;
        m_propName = propName;
        m_value = value;
    }

    public String getKey() {
        return m_key;
    }
}
