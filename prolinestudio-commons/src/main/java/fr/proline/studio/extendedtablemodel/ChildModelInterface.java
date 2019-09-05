package fr.proline.studio.extendedtablemodel;

import javax.swing.event.TableModelListener;

/**
 * A ChildModelInterface can be put in a CompoundTableModel which is a model which group different model together.
 * (for instance when we want to add a filterModel to any data model loaded from the database)
 * @author JM235353
 */
public interface ChildModelInterface extends GlobalTableModelInterface, TableModelListener {

    public void setParentModel(GlobalTableModelInterface parentModel);
    public GlobalTableModelInterface getParentModel();
}
