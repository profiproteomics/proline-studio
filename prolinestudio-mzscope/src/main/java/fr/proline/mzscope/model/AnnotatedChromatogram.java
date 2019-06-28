package fr.proline.mzscope.model;

public class AnnotatedChromatogram implements IChromatogram {

  private IChromatogram chromatogram;
  private IFeature feature;

  public AnnotatedChromatogram(IChromatogram chromatogram, IFeature feature) {
    this.chromatogram = chromatogram;
    this.feature = feature;
  }

  public IFeature getAnnotation() {
    return feature;
  }

  @Override
  public double getMaxIntensity() {
    return chromatogram == null ? null : chromatogram.getMaxIntensity();
  }

  @Override
  public String getRawFilename() {
    return chromatogram == null ? null : chromatogram.getRawFilename();
  }

  @Override
  public String getTitle() {
    return chromatogram == null ? null : chromatogram.getTitle();
  }

  @Override
  public double getMinMz() {
    return chromatogram == null ? null : chromatogram.getMinMz();
  }

  @Override
  public double getMaxMz() {
    return chromatogram == null ? null : chromatogram.getMaxMz();
  }

  @Override
  public double[] getTime() {
    return chromatogram == null ? null : chromatogram.getTime();
  }

  @Override
  public double[] getIntensities() {
    return chromatogram == null ? null : chromatogram.getIntensities();
  }

  @Override
  public double getElutionStartTime() {
    return chromatogram == null ? null : chromatogram.getElutionStartTime();
  }

  @Override
  public double getElutionEndTime() {
    return chromatogram == null ? null : chromatogram.getElutionEndTime();
  }
}
