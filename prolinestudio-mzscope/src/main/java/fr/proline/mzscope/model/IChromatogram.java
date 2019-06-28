package fr.proline.mzscope.model;

public interface IChromatogram {
  double getMaxIntensity();

  String getRawFilename();

  String getTitle();

  double getMinMz();

  double getMaxMz();

  double[] getTime();

  double[] getIntensities();

  double getElutionStartTime();

  double getElutionEndTime();
}
