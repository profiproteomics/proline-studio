/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
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
