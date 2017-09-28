/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.utils;

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