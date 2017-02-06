package fr.proline.studio.parameter;

import java.util.ArrayList;
import javax.swing.JComponent;

/**
 *
 * @author JM235353
 */
public class ValuesFromComponentParameter extends AbstractParameter {

    /*private Double m_minValue;
    private Double m_maxValue;
    private Double m_defaultValue;*/

    private ArrayList<AbstractLinkedParameters> m_linkedParametersList = null;
    
    private ComponentParameterInterface m_componentParameterInterface;
    
    public ValuesFromComponentParameter(String key, String name, ComponentParameterInterface componentParameterInterface) {
        super(key, name, Object.class, ComponentParameterInterface.class);

        m_componentParameterInterface = componentParameterInterface;
    }

    @Override
    public JComponent getComponent(Object value) {

        if (m_parameterComponent == null) {
           
            m_parameterComponent = m_componentParameterInterface.getComponent();
                
        }
        
        // no default value to set
        
        return m_parameterComponent;

    }
    
    @Override
    public void initDefault() {
        // no default value to set
    }

    @Override
    public ParameterError checkParameter() {
        
        if (!m_used && !m_compulsory) {
            return null;
        }
        
        return m_componentParameterInterface.checkParameter();
    }
    
    @Override
    public void setValue(String v) {
        // not used
    }
    

    @Override
    public String getStringValue() {
        return m_componentParameterInterface.getStringValue();
    }

    @Override
    public Object getObjectValue() {
        return m_componentParameterInterface.getObjectValue();
    }
 
    
    public void addLinkedParameters(final AbstractLinkedParameters linkedParameters) {

        if (m_linkedParametersList == null) {
            m_linkedParametersList = new ArrayList<>(1);
        }
        m_linkedParametersList.add(linkedParameters);
//linkedParameters.valueChanged(getStringValue(), getAssociatedObjectValue());
    }
    
    public void valueChanged(Object o) {
        if (m_linkedParametersList == null) {
            return;
        }
        for (AbstractLinkedParameters linkedParameter : m_linkedParametersList) {
            linkedParameter.valueChanged(getStringValue(), getObjectValue());
        }
    }
    
}
