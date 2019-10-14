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
