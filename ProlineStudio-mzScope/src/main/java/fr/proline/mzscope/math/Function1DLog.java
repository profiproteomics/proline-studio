/**
 *
 */
package fr.proline.mzscope.math;

/**
 * Linear function 'x+b'
 *
 * @author JeT
 */
public class Function1DLog implements Function1D {

    private double base;

    /**
     * @param a
     * @param b
     */
    public Function1DLog(double base) {
	super();
	this.base = base;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.math.Function1D#f(double)
     */
    @Override
    public double eval(double x) {
	return Math.log(x) / Math.log(this.base);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "log base " + this.base + "(x)";
    }

}
