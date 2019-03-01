package fr.proline.studio.filter;

import java.awt.Component;

/**
 * Filter Status used to display to the user an error when
 * a filter is incorrectly set
 * @author JM235353
 */
public class FilterStatus {
    
    private String m_error;
    private Component m_c;
    
    
    public FilterStatus(String error, Component c) {
        m_error = error;
        m_c = c;
    }
    
    public String getError() {
        return m_error;
    }
    
    public Component getComponent() {
        return m_c;
    }
}
