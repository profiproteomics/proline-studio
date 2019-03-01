package fr.proline.studio.parameter;

import java.awt.event.ActionListener;
import javax.swing.JComponent;

/**
 * Interface to be used in coordination with ValuesFromComponentParameter
 * 
 * @author JM235353
 */
public interface ComponentParameterInterface extends ActionListener {
    
    public abstract JComponent getComponent();
    
    public abstract ParameterError checkParameter();
    
    public abstract String getStringValue();

    public abstract Object getObjectValue();

}
