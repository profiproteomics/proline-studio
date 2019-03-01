/**
 *
 */
package fr.proline.mzscope.math;

/**
 * Linear function 'ax+b'
 *
 * @author JeT
 */
public class Function1DLinear implements Function1D {

    private double a, b;

    /**
     * @param a
     * @param b
     */
    public Function1DLinear(double a, double b) {
	super();
	this.a = a;
	this.b = b;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.math.Function1D#f(double)
     */
    @Override
    public double eval(double x) {
	return (this.a * x) + this.b;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return this.a + ".x+" + this.b;
    }

}
