package fr.proline.mzscope.processing;

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.ui.dialog.ExtractionParamsDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PeakelAnnotator implements IAnnotator {

  Map<IRawFile, PeakelAnnotatorImpl> annotators = null;
  FeaturesExtractionRequest extractionParams = null;

  public PeakelAnnotator() {

  }

  @Override
  public AnnotatedChromatogram annotate(IRawFile rawFile, IChromatogram chromatogram, MsnExtractionRequest request, Integer expectedCharge) {
    if (annotators == null) {
      ExtractionParamsDialog dialog = new ExtractionParamsDialog(null, true, false);
      dialog.setExtractionParamsTitle("Extract Features Parameters");
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
  private Map<Integer, List<IFeature>> featuresByNominalMass;

  public PeakelAnnotatorImpl(IRawFile rawFile, FeaturesExtractionRequest params) {
    this.params = params;
    this.rawFile = rawFile;
  }

  private Map<Integer, List<IFeature>> getPeakels() {
    if (featuresByNominalMass == null) {
      if (params != null) {
        List<IFeature> list = rawFile.extractFeatures(params);
        featuresByNominalMass = list.stream().collect(Collectors.groupingBy(f -> Integer.valueOf((int) f.getMz()), Collectors.toList()));
      }
    }

    return featuresByNominalMass;
  }

  public AnnotatedChromatogram annotate(IChromatogram chromatogram, MsnExtractionRequest request, Integer expectedCharge) {
    List<IFeature> features = getPeakels().get((int)request.getMz());

    if (features == null)
      return chromatogram == null ? null : new AnnotatedChromatogram(chromatogram, null);

    IFeature feature = features.stream().filter(f -> {
              double tolDa = f.getMz()*params.getMzTolPPM()/1e6;
              double upperTimeLimit = rawFile.getSpectrumElutionTime(rawFile.getNextSpectrumId(rawFile.getNextSpectrumId(rawFile.getSpectrumId(f.getLastElutionTime()), 1), 1));
              double lowerTimeLimit = rawFile.getSpectrumElutionTime(rawFile.getPreviousSpectrumId(rawFile.getSpectrumId(f.getFirstElutionTime()), 1));
              return (request.getElutionTime() * 60.0 >= lowerTimeLimit && request.getElutionTime()*60.0 <= upperTimeLimit && Math.abs(f.getMz() - request.getMz()) < tolDa);
            }
    ).findFirst().orElse(null);
    if (feature == null) {
      double secondIsotopeMz = request.getMz() + AVERAGE_ISOTOPE_MASS_DIFF/expectedCharge;
      features = getPeakels().get((int)secondIsotopeMz);
      feature = features.stream().filter(f -> {
                double tolDa = f.getMz()*params.getMzTolPPM()/1e6;
                double upperTimeLimit = rawFile.getSpectrumElutionTime(rawFile.getNextSpectrumId(rawFile.getNextSpectrumId(rawFile.getSpectrumId(f.getLastElutionTime()), 1), 1));
                double lowerTimeLimit = rawFile.getSpectrumElutionTime(rawFile.getPreviousSpectrumId(rawFile.getSpectrumId(f.getFirstElutionTime()), 1));
                return (request.getElutionTime() * 60.0 >= lowerTimeLimit && request.getElutionTime()*60.0 <= upperTimeLimit && Math.abs(f.getMz() - secondIsotopeMz) < tolDa);
              }
      ).findFirst().orElse(null);
    }
    return new AnnotatedChromatogram(chromatogram, feature);
  }
}

