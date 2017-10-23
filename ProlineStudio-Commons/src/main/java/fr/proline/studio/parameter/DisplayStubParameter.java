package fr.proline.studio.parameter;

import javax.swing.JComponent;

/**
 * Parameter used only to display extra info in a Parameter Panel
 * @author JM235353
 */
public class DisplayStubParameter extends AbstractParameter {

    private JComponent m_displayComponent = null;
    
    public DisplayStubParameter(String name, JComponent displayComponent) {
        super(null, name, null, displayComponent.getClass());
        m_displayComponent = displayComponent;
    }
    
    
    @Override
    public JComponent getComponent(Object value) {
        return m_displayComponent;
    }

    @Override
    public void initDefault() {
        // nothing to do
    }

    @Override
    public ParameterError checkParameter() {
        // nothing to do
        return null;
    }

    @Override
    public String getStringValue() {
        // nothing to do
        return null;
    }

    @Override
    public Object getObjectValue() {
        // nothing to do
        return null;
    }

    @Override
    public void setValue(String v) {
        // nothing to do
    }
    
    @Override
    public LabelVisibility showLabel() {
        return LabelVisibility.AS_BORDER_TITLE;
    }
}
