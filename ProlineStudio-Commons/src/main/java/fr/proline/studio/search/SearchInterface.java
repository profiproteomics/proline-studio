
package fr.proline.studio.search;

import fr.proline.studio.filter.Filter;


/**
 * Interface to be implemented to be able to do a search through the SearchToggleButton/AdvancesSearchFloatingPanel
 * @author JM235353
 */
public interface SearchInterface {
    public int search(Filter f, boolean newSearch);
}
