package fr.proline.studio.parameter;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author jm235353
 */
public abstract class AbstractLinkedParameters {
    
    private ParameterList m_parameterList = null;
    
    public AbstractLinkedParameters(ParameterList parameterList) {
        m_parameterList = parameterList;
    }
    
    public ParameterList getParameterList() {
        return m_parameterList;
    }
    
    public abstract void valueChanged(String value);
    
    protected void showParameter(AbstractParameter parameter, boolean show) {
        JComponent comp = parameter.getComponent(null);
        comp.setVisible(show);
        JLabel l = getParameterList().getAssociatedLabel(comp);
        if (l != null) {
            l.setVisible(show);
        }
    }
    
    protected void updataParameterListPanel() {
        JPanel p = getParameterList().getPanel();
        
        JDialog dialog = getParent(p);
        if (dialog != null) {
            if (dialog instanceof DefaultDialog) {
                ((DefaultDialog) dialog).repack();
            } else {
                dialog.pack();
            }
        } else if (p != null) {
            Container c = p.getParent();
            if (c != null) {
                c.revalidate();
            }
        }
    }
    
    private JDialog getParent(JComponent c) {
        Container container = c.getParent();
        while (true) {
            if (container == null) {
                return null;
            }
            if (container instanceof JDialog) {
                return (JDialog) container;
            }
            container = container.getParent();
        }
    }
    
}
