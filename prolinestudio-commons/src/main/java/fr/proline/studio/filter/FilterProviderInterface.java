package fr.proline.studio.filter;


import java.util.LinkedHashMap;

/**
 * Interface to extend for models which wants to enable filtering on some columns
 * @author JM235353
 */
public interface FilterProviderInterface {
    
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap);
    
}
