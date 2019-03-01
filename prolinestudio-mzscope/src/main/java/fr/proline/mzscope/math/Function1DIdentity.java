/**
 *
 */
package fr.proline.mzscope.math;

/**
 * Linear function 'f(g(x))'
 *
 * @author JeT
 */
public class Function1DIdentity implements Function1D {

    /**
     */
    public Function1DIdentity() {
	super();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.math.Function1D#f(double)
     */
    @Override
    public double eval(double x) {
	return x;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "f(x) = x";
    }

}
