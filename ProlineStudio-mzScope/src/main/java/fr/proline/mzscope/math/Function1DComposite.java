/**
 *
 */
package fr.proline.mzscope.math;

/**
 * Linear function 'f(g(x))'
 *
 * @author JeT
 */
public class Function1DComposite implements Function1D {

    private Function1D f, g;

    /**
     * @param f
     * @param g
     */
    public Function1DComposite(Function1D f, Function1D g) {
	super();
	this.f = f;
	this.g = g;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.math.Function1D#f(double)
     */
    @Override
    public double eval(double x) {
	return this.f.eval(this.g.eval(x));
    }

}
