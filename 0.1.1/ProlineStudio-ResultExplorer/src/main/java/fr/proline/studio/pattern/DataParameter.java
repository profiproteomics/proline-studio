package fr.proline.studio.pattern;

/**
 *
 * @author JM235353
 */
public class DataParameter {

    private Class c;
    private boolean isList;

    public DataParameter(Class c, boolean isList) {
        this.c = c;
        this.isList = isList;
    }

    @Override
    public boolean equals(Object p) {
        if (p instanceof DataParameter) {
            return c.equals(((DataParameter)p).c);
        }
        return false;
    }
    
    public boolean equalsData(Class dataC) {
        return c.equals(dataC);
    }

    public boolean isCompatibleWithOutParameter(DataParameter outParameter) {
        if (!c.equals(outParameter.c)) {
            return false;
        }
        if (isList && !outParameter.isList) {
            return false;
        }
        return true;

    }
    
    @Override
    public int hashCode() {
        return c.hashCode();
    }
    
}