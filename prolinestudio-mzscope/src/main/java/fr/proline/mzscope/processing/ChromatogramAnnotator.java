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

import fr.profi.mzdb.model.SpectrumHeader;
import fr.proline.mzscope.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Int;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChromatogramAnnotator implements IAnnotator {

  final private static Logger logger = LoggerFactory.getLogger(ChromatogramAnnotator.class);
  public static final double EPSILON_TIME = 0.01;

  public ChromatogramAnnotator() {

  }

  @Override
  public AnnotatedChromatogram annotate(IRawFile rawFile, IChromatogram chromatogram, ExtractionRequest request, Integer expectedCharge) {

    try {

      if (chromatogram == null)
        return null;

      final int maxConsecutiveGaps = 3;
      double[] globalTime = getElutionTimes(rawFile, request);

      double[] chromatogramIntensities = chromatogram.getIntensities();
      double[] chromatogramTime = chromatogram.getTime();
      int globalRtIndex = ~Arrays.binarySearch(globalTime, request.getElutionTime());
      int chromatogramRtIndex = ~Arrays.binarySearch(chromatogramTime, request.getElutionTime());
      chromatogramRtIndex = (chromatogramRtIndex < chromatogramTime.length) ? chromatogramRtIndex : chromatogramTime.length - 1;
      int chromatogramIndex = chromatogramRtIndex;
      List<Integer> peakelIndexes = new ArrayList<>(chromatogramTime.length);

      if (globalRtIndex != -1) {
        // search for signal before and after rtIndex
        int consecutiveGaps = 0;
        int cEndIdx = chromatogramIndex;
        int cStartIdx = chromatogramIndex;
        int startIdx = globalRtIndex;
        int endIdx = globalRtIndex;

        double maxIntensity = -1.0;

        for (int k = globalRtIndex; (k < globalTime.length) && (chromatogramIndex < chromatogramTime.length) && (consecutiveGaps <= maxConsecutiveGaps); k++) {
          if (Math.abs(globalTime[k] - chromatogramTime[chromatogramIndex]) < EPSILON_TIME) {
            if (chromatogramIntensities[chromatogramIndex] > 0) {
              consecutiveGaps = 0;
              cEndIdx = chromatogramIndex;
              endIdx = k;
              maxIntensity = Math.max(maxIntensity, chromatogramIntensities[chromatogramIndex]);
              peakelIndexes.add(chromatogramIndex);
            } else {
              consecutiveGaps++;
            }
            chromatogramIndex++;
          } else {
            consecutiveGaps++;
          }
        }

        consecutiveGaps = 0;
        chromatogramIndex = chromatogramRtIndex;
        for (int k = globalRtIndex; (k >= 0) && (chromatogramIndex >= 0) && (consecutiveGaps <= maxConsecutiveGaps); k--) {
          if (Math.abs(globalTime[k] - chromatogramTime[chromatogramIndex]) < EPSILON_TIME) {
            if (chromatogramIntensities[chromatogramIndex] > 0) {
              consecutiveGaps = 0;
              cStartIdx = chromatogramIndex;
              startIdx = k;
              maxIntensity = Math.max(maxIntensity, chromatogramIntensities[chromatogramIndex]);
              peakelIndexes.add(chromatogramIndex);
            } else {
              consecutiveGaps++;
            }
            chromatogramIndex--;
          } else {
            consecutiveGaps++;
          }
        }

        double area = -1.0;
        if (!peakelIndexes.isEmpty()) {
          peakelIndexes.sort(Integer::compareTo);
          area = 0.0;
          for (int k = 1; k < peakelIndexes.size(); k++) {
            int prevIdx = peakelIndexes.get(k - 1);
            int currIdx = peakelIndexes.get(k);
            area += (chromatogramIntensities[prevIdx] + chromatogramIntensities[currIdx]) * (chromatogramTime[currIdx] - chromatogramTime[prevIdx]) / 2.0;
          }
          if (startIdx > 1) {
            int firstIdx = peakelIndexes.get(0);
            area += chromatogramIntensities[firstIdx] * (chromatogramTime[firstIdx] - globalTime[startIdx - 1]) / 2.0;
          }
          if (endIdx < globalTime.length - 2) {
            int lastIdx = peakelIndexes.get(peakelIndexes.size() - 1);
            area += chromatogramIntensities[lastIdx] * (globalTime[endIdx + 1] - chromatogramTime[lastIdx]) / 2.0;
          }
        }
  //TODO : redetermine elution time as apex time instead of requested retention time
          BasePeakel peakel = new BasePeakel(
                  (chromatogram.getMaxMz() + chromatogram.getMinMz()) / 2.0,
                  (float) chromatogramTime[chromatogramRtIndex]*60.0f,
                  (float) chromatogramTime[cStartIdx]*60.0f,
                  (float) chromatogramTime[cEndIdx]*60.0f,
                  rawFile,
                  request.getMsLevel());
          if (maxIntensity > 0)
            peakel.setApexIntensity((float) maxIntensity);
          if (area > 0)
            peakel.setArea((float)area);
          if ((endIdx-startIdx) > 0)
            peakel.setScanCount(endIdx - startIdx + 1);
          if (request.getMsLevel() > 1) {
            peakel.setParentMz(request.getMz());
          }
          return new AnnotatedChromatogram(chromatogram, peakel);
      }
    } catch (Exception e) {
      logger.error("Annotation failed", e);
    }
    return new AnnotatedChromatogram(chromatogram, null);
  }

  private double[] getElutionTimes(IRawFile rawFile, ExtractionRequest request) {
    if (!rawFile.isDIAFile() || request.getMsLevel() == 1)
      return rawFile.getElutionTimes(request.getMsLevel());

    final Map<SpectrumHeader, IsolationWindow> selection = rawFile.getIsolationWindowByMs2Headers().entrySet().stream()
            .filter(e -> e.getValue().contains(request.getMz()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return selection.keySet().stream().mapToDouble(h -> h.getElutionTime()/60.0).sorted().toArray();
  }
}
