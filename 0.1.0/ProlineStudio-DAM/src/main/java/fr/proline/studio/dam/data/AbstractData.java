package fr.proline.studio.dam.data;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import java.util.List;

/**
 * Must be extended by classes used as User Data in Result Explorer Nodes
 *
 * @author JM235353
 */
public abstract class AbstractData {

    public enum DataTypes {

        MAIN,
        PROJECT,
        DATA_SET,
        //IDENTIFICATION,
        //IDENTIFICATION_FRACTION,
        //RESULT_SET,
        //RESULT_SUMMARY,
        ALL_IMPORTED
    }
    protected String name;
    protected DataTypes dataType;
    // by default, any data can have children when corresponding node is expanded
    protected boolean hasChildren = true;

    /**
     * Returns if the corresponding Node will have Children when it will be
     * expanded
     *
     * @return
     */
    public boolean hasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
    
    /**
     * This method is called from a thread which is not the AWT and can wait for
     * the answer. So we use a Semaphore to synchronize and wait the result from
     * the AccessDatabaseThread
     *
     * @param list
     */
    public abstract void load(AbstractDatabaseCallback callback, List<AbstractData> list);

    /**
     * Subclasses must overriden this method to give the name of the data
     *
     * @return
     */
    public abstract String getName();


    public DataTypes getDataType() {
        return dataType;
    }
}
