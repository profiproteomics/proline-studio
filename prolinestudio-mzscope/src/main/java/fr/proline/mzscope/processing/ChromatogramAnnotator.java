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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ChromatogramAnnotator implements IAnnotator {

  final private static Logger logger = LoggerFactory.getLogger(ChromatogramAnnotator.class);

  public ChromatogramAnnotator() {

  }

  @Override
  public AnnotatedChromatogram annotate(IRawFile rawFile, IChromatogram chromatogram, MsnExtractionRequest request, Integer expectedCharge) {

    try {
      int maxConsecutiveGaps = 3;

      double[] refTime = rawFile.getElutionTimes(request.getMsLevel());

      double[] intensities = chromatogram.getIntensities();
      double[] time = chromatogram.getTime();
      int rtIndex = ~Arrays.binarySearch(refTime, request.getElutionTime());
      int cRtIndex = ~Arrays.binarySearch(time, request.getElutionTime());
      int cIndex = cRtIndex;

      if (rtIndex != -1) {
        // search for signal before and after rtIndex
        int consecutiveGaps = 0;
        int cEndIdx = cIndex;
        int cStartIdx = cIndex;
        int startIdx = rtIndex;
        int endIdx = rtIndex;

        double maxIntensity = -1.0;

        for (int k = rtIndex; (k < refTime.length) && (consecutiveGaps <= maxConsecutiveGaps); k++) {
          if (Math.abs(refTime[k] - time[cIndex]) < 0.01) {
            if (intensities[cIndex] > 0) {
              consecutiveGaps = 0;
              cEndIdx = cIndex;
              endIdx = k;
              maxIntensity = Math.max(maxIntensity, intensities[cIndex]);
            } else {
              consecutiveGaps++;
            }
            cIndex++;
          } else {
            consecutiveGaps++;
          }
        }

        consecutiveGaps = 0;
        cIndex = cRtIndex;
        for (int k = rtIndex; (k > 0) && (consecutiveGaps <= maxConsecutiveGaps); k--) {
          if (Math.abs(refTime[k] - time[cIndex]) < 0.01) {
            if (intensities[cIndex] > 0) {
              consecutiveGaps = 0;
              cStartIdx = cIndex;
              startIdx = k;
              maxIntensity = Math.max(maxIntensity, intensities[cIndex]);
            } else {
              consecutiveGaps++;
            }
            cIndex--;
          } else {
            consecutiveGaps++;
          }
        }

  //TODO : redetermine elution-time as apex time instead of requested retention time
          BasePeakel feature = new BasePeakel(
                  (chromatogram.getMaxMz() + chromatogram.getMinMz()) / 2.0,
                  (float) time[cRtIndex]*60.0f,
                  (float) time[cStartIdx]*60.0f,
                  (float) time[cEndIdx]*60.0f,
                  rawFile,
                  request.getMsLevel());
          feature.setApexIntensity((float) maxIntensity);
          feature.setMs1Count(endIdx - startIdx + 1);
          return new AnnotatedChromatogram(chromatogram, feature);
      }
    } catch (Exception e) {
      logger.error("Annotation failed", e);
    }
    return new AnnotatedChromatogram(chromatogram, null);
  }
}
