package fr.proline.studio.filter;

/**
 * Interface used for filtering to convert a value (for instance a Peptide) to another value (for instance a String)
 * which can be filtered
 * @author JM235353
 */
public interface ConvertValueInterface {
    public Object convertValue(Object o);
}
