package fr.proline.studio.python.data;

import org.python.core.PyObject;

/**
 *
 * @author JM235353
 */
public abstract class Col extends PyObject {
    
    public abstract Object getValueAt(int row);

    public abstract int getRowCount();
}
