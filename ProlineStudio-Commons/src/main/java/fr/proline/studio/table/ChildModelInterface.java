package fr.proline.studio.table;

import javax.swing.event.TableModelListener;

/**
 *
 * @author JM235353
 */
public interface ChildModelInterface extends GlobalTableModelInterface, TableModelListener {
    public void setParentModel(GlobalTableModelInterface parentModel);
    
    public GlobalTableModelInterface getParentModel();
}
