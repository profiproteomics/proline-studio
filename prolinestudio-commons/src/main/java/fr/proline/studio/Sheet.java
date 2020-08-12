package fr.proline.studio;

import java.util.HashMap;
import java.util.HashSet;

public class Sheet {

    private HashSet<Set> m_set = new HashSet<>();

    public Sheet() {


    }

    public static Sheet createDefault() {
        return new Sheet();
    }

    public void put(Set set) {
        m_set.add(set);
    }

    public static Set createPropertiesSet() {
        return new Set();
    }

    public static class Set {
        private String m_name;
        private String m_displayName;

        private HashMap<String, Property> m_map = new HashMap<>();

        public Set() {

        }

        public static Sheet createDefault() {
            return new Sheet();
        }

        public void put(Property property) {
            m_map.put(property.getKey(), property);
        }

        public void setName(String name) {
            m_name = name;
        }
        public void setDisplayName(String displayName) {
            m_displayName = displayName;
        }

    }
}
