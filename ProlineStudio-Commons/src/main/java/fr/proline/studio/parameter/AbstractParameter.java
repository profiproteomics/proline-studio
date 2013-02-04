package fr.proline.studio.parameter;

import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author jm235353
 */
public abstract class AbstractParameter {

    protected String key;
    protected String name;
    protected Class type;
    protected Class graphicalType;
    protected JComponent parameterComponent = null;

    protected AbstractParameter(String key, String name, Class type, Class graphicalType) {
        this.key = key;
        this.name = name;
        this.type = type;
        this.graphicalType = graphicalType;
    }

    public String getName() {
        return name;
    }

    public abstract JComponent getComponent();
}
