package fr.proline.studio.search;

import fr.proline.studio.filter.Filter;

/**
 * Interface to be implemented so a search can be applied by an external source
 * @author JM235353
 */
public interface ApplySearchInterface {

    public abstract void doSearch(Filter f, boolean firstSearch);

}
