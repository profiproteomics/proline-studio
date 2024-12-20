/* 
 * Copyright (C) 2019
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

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.ui.dialog.ExtractionParamsDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Annotate a chromatogram by finding the peakel matching the requested m/z and rt. If not found
 * The charge state is used to test if a peakel matching a putative second isotope could be found.
 */
public class PeakelAnnotator implements IAnnotator {

  Map<IRawFile, PeakelAnnotatorImpl> annotators = null;
  FeaturesExtractionRequest extractionParams = null;

  public PeakelAnnotator() {

  }

  @Override
  public AnnotatedChromatogram annotate(IRawFile rawFile, IChromatogram chromatogram, ExtractionRequest request, Integer expectedCharge) {
    if (annotators == null) {
      ExtractionParamsDialog dialog = new ExtractionParamsDialog(null, true, false);
      dialog.setExtractionParamsTitle("Detect Peakels Parameters");
      dialog.showExtractionParamsDialog();
      FeaturesExtractionRequest.Builder builder = dialog.getExtractionParams();
      if (builder == null) {
        return new AnnotatedChromatogram(chromatogram, null);
      } else {
        builder.setExtractionMethod(FeaturesExtractionRequest.ExtractionMethod.DETECT_PEAKELS);
        extractionParams =  builder.build();
        annotators = new HashMap<>();
      }
    }
    if (! annotators.containsKey(rawFile)) {
      annotators.put(rawFile, new PeakelAnnotatorImpl(rawFile, extractionParams));
    }
    return annotators.get(rawFile).annotate(chromatogram, request, expectedCharge);
  }
}

class PeakelAnnotatorImpl {

  private static double AVERAGE_ISOTOPE_MASS_DIFF = 1.0027;

  private final FeaturesExtractionRequest params;
  private IRawFile rawFile;
  private Map<Integer, List<IPeakel>> featuresByNominalMass;

  public PeakelAnnotatorImpl(IRawFile rawFile, FeaturesExtractionRequest params) {
    this.params = params;
    this.rawFile = rawFile;
  }

  private Map<Integer, List<IPeakel>> getPeakels() {
    if (featuresByNominalMass == null) {
      if (params != null) {
        List<IPeakel> list = rawFile.extractPeakels(params);
        featuresByNominalMass = list.stream().collect(Collectors.groupingBy(f -> Integer.valueOf((int) f.getMz()), Collectors.toList()));
      }
    }

    return featuresByNominalMass;
  }

  public AnnotatedChromatogram annotate(IChromatogram chromatogram, ExtractionRequest request, Integer expectedCharge) {
    List<IPeakel> peakels = getPeakels().get((int)request.getMz());

    if (peakels == null)
      return chromatogram == null ? null : new AnnotatedChromatogram(chromatogram, null);

    IPeakel peakel = peakels.stream().filter(f -> {
              double tolDa = f.getMz()*params.getMzTolPPM()/1e6;
              double upperTimeLimit = rawFile.getSpectrumElutionTime(rawFile.getNextSpectrumId(rawFile.getNextSpectrumId(rawFile.getSpectrumId(f.getLastElutionTime()), 1), 1));
              double lowerTimeLimit = rawFile.getSpectrumElutionTime(rawFile.getPreviousSpectrumId(rawFile.getSpectrumId(f.getFirstElutionTime()), 1));
              return (request.getElutionTime() * 60.0 >= lowerTimeLimit && request.getElutionTime()*60.0 <= upperTimeLimit && Math.abs(f.getMz() - request.getMz()) < tolDa);
            }
    ).findFirst().orElse(null);
    if (peakel == null) {
      double secondIsotopeMz = request.getMz() + AVERAGE_ISOTOPE_MASS_DIFF/expectedCharge;
      peakels = getPeakels().get((int)secondIsotopeMz);
      if (peakels != null) {
         peakel = peakels.stream().filter(f -> {
                double tolDa = f.getMz()*params.getMzTolPPM()/1e6;
                double upperTimeLimit = rawFile.getSpectrumElutionTime(rawFile.getNextSpectrumId(rawFile.getNextSpectrumId(rawFile.getSpectrumId(f.getLastElutionTime()), 1), 1));
                double lowerTimeLimit = rawFile.getSpectrumElutionTime(rawFile.getPreviousSpectrumId(rawFile.getSpectrumId(f.getFirstElutionTime()), 1));
                return (request.getElutionTime() * 60.0 >= lowerTimeLimit && request.getElutionTime()*60.0 <= upperTimeLimit && Math.abs(f.getMz() - secondIsotopeMz) < tolDa);
              }
            ).findFirst().orElse(null);
      }  else {
          peakel = null;
      }
    }
    return new AnnotatedChromatogram(chromatogram, peakel);
  }
}

