package fr.proline.studio.parameter;

/**
 * Base class to convert a Parameter to a String
 * @author JM235353
 */
public abstract class AbstractParameterToString<E> {
    
    public abstract String toString(E o);
}
