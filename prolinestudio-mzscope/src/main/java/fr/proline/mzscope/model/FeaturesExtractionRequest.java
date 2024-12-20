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
package fr.proline.mzscope.model;

/**
 * @author CB205360
 */
public class FeaturesExtractionRequest extends ExtractionRequest {

  public static final boolean REMOVE_BASELINE = false;
  public static final boolean USE_SMOOTHING = true;
  public static final int MIN_PEAKS_COUNT = 5;
  public static final int MIN_MAX_DISTANCE = 3;
  public static final float MIN_MAX_RATIO = 0.75f;
  public static final float INTENSITY_PERCENTILE = 0.9f;
  public static final int MAX_CONSECUTIVE_GAPS = 3;
  public static final float COELUTION_RT_TOLERANCE = 40;

  public enum ExtractionMethod {
    EXTRACT_MS2_FEATURES, DETECT_PEAKELS, DETECT_FEATURES
  }

  public static class Builder<T extends Builder<T>> extends ExtractionRequest.Builder<T> {

    ExtractionMethod extractionMethod;
    boolean removeBaseline = REMOVE_BASELINE;
    boolean useSmoothing = USE_SMOOTHING;
    int minPeaksCount = MIN_PEAKS_COUNT;
    int minmaxDistanceThreshold = MIN_MAX_DISTANCE;
    int maxConsecutiveGaps = MAX_CONSECUTIVE_GAPS;
    float maxIntensityRelativeThreshold = MIN_MAX_RATIO;
    float intensityPercentile = INTENSITY_PERCENTILE;

    @Override
    public T setMzTolPPM(float mzTolPPM) {
      this.mzTolPPM = mzTolPPM;
      return self();
    }

    @Override
    public T setMz(double mz) {
      this.mz = mz;
      this.maxMz = (mz + mz * mzTolPPM / 1e6f);
      this.minMz = (mz - mz * mzTolPPM / 1e6f);
      return self();
    }

    @Override
    public T setMaxMz(double maxMz) {
      this.maxMz = maxMz;
      return self();
    }

    @Override
    public T setMinMz(double minMz) {
      this.minMz = minMz;
      return self();
    }

    public T setExtractionMethod(ExtractionMethod extractionMethod) {
      this.extractionMethod = extractionMethod;
      return self();
    }

    public T setRemoveBaseline(boolean removeBaseline) {
      this.removeBaseline = removeBaseline;
      return self();
    }

    public T setUseSmoothing(boolean useSmoothing) {
      this.useSmoothing = useSmoothing;
      return self();
    }

    public T setMinPeaksCount(int minPeaksCount) {
      this.minPeaksCount = minPeaksCount;
      return self();
    }

    public T setMinmaxDistanceThreshold(int minmaxDistanceThreshold) {
      this.minmaxDistanceThreshold = minmaxDistanceThreshold;
      return self();
    }

    public T setIntensityPercentile(float intensityPercentile) {
      this.intensityPercentile = intensityPercentile;
      return self();
    }

    public T setMaxIntensityRelativeThreshold(float maxIntensityRelativeThreshold) {
      this.maxIntensityRelativeThreshold = maxIntensityRelativeThreshold;
      return self();
    }

    public T setMaxConsecutiveGaps(int maxConsecutiveGaps) {
      this.maxConsecutiveGaps = maxConsecutiveGaps;
      return self();
    }

    public FeaturesExtractionRequest build() {
      return new FeaturesExtractionRequest(this);
    }
  }

  @SuppressWarnings("rawtypes")
  public static Builder<?> builder() {
    return new Builder();
  }

  private ExtractionMethod extractionMethod;
  private boolean removeBaseline;
  private boolean useSmoothing;
  private int minPeaksCount;
  private int minmaxDistanceThreshold;
  private float maxIntensityRelativeThreshold;
  private float intensityPercentile;
  private int maxConsecutiveGaps;
  private float coelutionRtTolerance;

  protected FeaturesExtractionRequest(Builder builder) {
    super(builder);
    this.extractionMethod = builder.extractionMethod;
    this.removeBaseline = builder.removeBaseline;
    this.useSmoothing = builder.useSmoothing;
    this.minPeaksCount = builder.minPeaksCount;
    this.minmaxDistanceThreshold = builder.minmaxDistanceThreshold;
    this.maxIntensityRelativeThreshold = builder.maxIntensityRelativeThreshold;
    this.intensityPercentile = builder.intensityPercentile;
    this.maxConsecutiveGaps = builder.maxConsecutiveGaps;
    this.coelutionRtTolerance = COELUTION_RT_TOLERANCE;
  }

  public ExtractionMethod getExtractionMethod() {
    return extractionMethod;
  }

  public boolean isRemoveBaseline() {
    return removeBaseline;
  }

  public boolean isUseSmoothing() {
    return useSmoothing;
  }

  public int getMinPeaksCount() {
    return minPeaksCount;
  }

  public int getMinmaxDistanceThreshold() {
    return minmaxDistanceThreshold;
  }

  public float getMaxIntensityRelativeThreshold() {
    return maxIntensityRelativeThreshold;
  }

  public float getIntensityPercentile() {
    return intensityPercentile;
  }

  public int getMaxConsecutiveGaps() {
    return maxConsecutiveGaps;
  }

  public float getCoelutionRtTolerance() {
    return coelutionRtTolerance;
  }

 
  public String getExtractionParamsString() {
    StringBuilder sb = new StringBuilder();
    String em = "";
    switch (getExtractionMethod()) {
      case EXTRACT_MS2_FEATURES: {
        em = "Extract MS2 Features";
        break;
      }
      case DETECT_PEAKELS: {
        em = "Detect Peakels";
        break;
      }
      case DETECT_FEATURES: {
        em = "Detect Features";
        break;
      }
    }
    sb.append("<html>");
    sb.append(em);
    sb.append(": <br/>");
    sb.append("m/z tolerance (ppm): ");
    sb.append(Float.toString(getMzTolPPM()));
    sb.append("<br/>");
    sb.append("rt coelution tolerance (s): ");
    sb.append(Float.toString(getCoelutionRtTolerance()));
    sb.append("<br/>");
    if (isRemoveBaseline()) {
      sb.append("Use Peakels baseline remover <br/>");
    }
    if (Double.compare(getMinMz(), getMaxMz()) == 0 && Double.compare(getMinMz(), 0.0) == 0) {
      sb.append("no m/z bounds");
    } else if (Double.compare(getMz(), 0) != 0) {
      sb.append("at m/z: ");
      sb.append(getMz());
    } else {
      sb.append("m/z bounds: ");
      sb.append(getMinMz());
      sb.append(" - ");
      sb.append(getMaxMz());
    }
    sb.append("</html>");

    return sb.toString();
  }

}
