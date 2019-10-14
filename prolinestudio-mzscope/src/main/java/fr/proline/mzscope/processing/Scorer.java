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
package fr.proline.mzscope.processing;

import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.model.SpectrumData;
import scala.Tuple2;

/**
 *
 * @author CB205360
 */
public interface Scorer {
    public  Tuple2<Double, TheoreticalIsotopePattern> score(SpectrumData currentSpectrum, double intialMz, int shift, int charge, double ppmTol);
}