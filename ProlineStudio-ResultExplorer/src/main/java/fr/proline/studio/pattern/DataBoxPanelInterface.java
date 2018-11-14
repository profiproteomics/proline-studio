package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;

/**
 * All panels which are linked to a databox must implement this interface
 * 3 abstract method are relative to task loading, loaded progress information
 *
 * @author JM235353
 */
public interface DataBoxPanelInterface extends SplittedPanelContainer.UserActions {

    public void addSingleValue(Object v);

    public void setDataBox(AbstractDataBox dataBox);

    public AbstractDataBox getDataBox();

    /**
     * set progress status
     * see setLoading(id, calculation =false) calculating = false
     *
     * @param id, numero de task
     */
    public void setLoading(int id);

    /**
     * set progress status
     * task=id begin with calculating state
     *
     * @param id
     * @param calculating, task = id, it's state of calculate
     */
    public void setLoading(int id, boolean calculating);

    /**
     * set progress status
     * task = id, finished
     *
     * @param id
     */
    public void setLoaded(int id);

}
