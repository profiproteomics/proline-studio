/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.parameter;

import javax.swing.JComboBox;
import javax.swing.JComponent;


/**
 *
 * @author JM235353
 */
public class ObjectParameter<E> extends AbstractParameter {

    private E[] objects;
    private int defaultIndex;

    public ObjectParameter(String key, String name, E[] objects, int defaultIndex) {
        super(key, name, Integer.class, JComboBox.class);
        this.objects = objects;
        if ((defaultIndex<0) || (defaultIndex>=objects.length)) {
            defaultIndex = 0;
        }
        this.defaultIndex = defaultIndex;
    }
    
    @Override
    public JComponent getComponent(Object value) {
        
        if (graphicalType.equals(JComboBox.class)) {
            JComboBox combobox = new JComboBox(objects);
            if (value != null) {
                combobox.setSelectedItem(value);
            } else {
                combobox.setSelectedIndex(defaultIndex);
            }
            parameterComponent = combobox;
            return combobox;
        }

        return null; // should not happen
    }

    @Override
    public void initDefault() {
        ((JComboBox) parameterComponent).setSelectedIndex(defaultIndex);
    }

    @Override
    public ParameterError checkParameter() {
        return null;
    }
    
}
