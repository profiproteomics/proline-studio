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
package fr.proline.mzscope.mzdb;

import fr.proline.mzscope.model.IPeakel;
import fr.profi.mzdb.model.Peakel;
import fr.proline.mzscope.model.IRawFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptor that convert a fr.profi.mzdb.model.Peakel object to IFeature implementation.
 *
 * @author CB205360
 */
public class MzdbPeakelWrapper implements IPeakel {

  final private static Logger logger = LoggerFactory.getLogger(MzdbFeatureWrapper.class);

  private final Peakel peakel;
  //TODO set to final as soon as setRawFile will remove
  private IRawFile rawFile;
  private final int msLevel;
  private double parentMz;

  public MzdbPeakelWrapper(Peakel mzdbPeakel, IRawFile rawfile) {
    this(mzdbPeakel, rawfile, 1);
  }

  public MzdbPeakelWrapper(Peakel mzdbPeakel, IRawFile rawfile, double parentMz) {
    this(mzdbPeakel, rawfile, 2);
    this.parentMz = parentMz;
  }

  public MzdbPeakelWrapper(Peakel mzdbPeakel, IRawFile rawfile, int msLevel) {
    this.peakel = mzdbPeakel;
    this.rawFile = rawfile;
    this.msLevel = msLevel;
  }

  @Override
  public float getArea() {
    return peakel.getArea();
  }

  @Override
  public int getScanCount() {
    return peakel.getSpectrumIds().length;
  }

  @Override
  public double getMz() {
    return peakel.getMz();
  }

  @Override
  public float getElutionTime() {
    return peakel.getElutionTime();
  }

  @Override
  public float getDuration() {
    return peakel.calcDuration();
  }

  @Override
  public float getApexIntensity() {
    return peakel.getApexIntensity();
  }

  @Override
  public float getFirstElutionTime() {
    return peakel.getFirstElutionTime();
  }

  @Override
  public float getLastElutionTime() {
    return peakel.getLastElutionTime();
  }

  @Override
  public IRawFile getRawFile() {
    return rawFile;
  }

  @Override
  public void setRawFile(IRawFile rawfile) {
    this.rawFile = rawfile;
  }

  @Override
  public int getMsLevel() {
    return msLevel;
  }

  @Override
  public Peakel getPeakel() {
    return peakel;
  }

  @Override
  public double getParentMz() {
    return parentMz;
  }
}
