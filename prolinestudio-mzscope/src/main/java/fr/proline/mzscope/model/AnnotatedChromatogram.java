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
