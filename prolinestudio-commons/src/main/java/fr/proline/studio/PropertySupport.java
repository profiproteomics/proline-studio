package fr.proline.studio;

import java.lang.reflect.InvocationTargetException;

public class PropertySupport {


    public static abstract class ReadOnly extends Property {



        public ReadOnly(String propNameKey, Class c, String propName, Object value) {
            m_key = propNameKey;
            m_class = c;
            m_propName = propName;
            m_value = value;
        }
        public abstract Object getValue() throws InvocationTargetException;
    }
}
