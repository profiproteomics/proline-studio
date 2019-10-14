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

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class ExtractionResult {

   final private static Logger logger = LoggerFactory.getLogger(ExtractionResult.class);

   public enum Status {
      NONE, REQUESTED, DONE
   };

   private MsnExtractionRequest request;
   private Integer expectedCharge;
   private Map<IRawFile, AnnotatedChromatogram> chromatograms;
   private Status status = Status.NONE;

   public ExtractionResult(MsnExtractionRequest request, Integer expectedCharge) {
      this.request = request;
      this.expectedCharge = expectedCharge;
   }
   
   public float getMzTolPPM() {
      return request.getMzTolPPM();
   }

   public double getMz() {
      return request.getMz();
   }

   public double getMinMz() {
      return request.getMinMz();
   }

   public double getMaxMz() {
      return request.getMaxMz();
   }

   public float getElutionTimeLowerBound() {
      return request.getElutionTimeLowerBound();
   }

   public float getElutionTimeUpperBound() {
      return request.getElutionTimeUpperBound();
   }

   public float getElutionTime() {
      return request.getElutionTime();
   }

   public Integer getExpectedCharge() {
      return expectedCharge;
   }

   public Status getStatus() {
      return status;
   }

   public void setStatus(Status status) {
      this.status = status;
   }
   
   public boolean addChromatogram(IRawFile rawFile, AnnotatedChromatogram chromato) {
      if (chromatograms == null) {
         chromatograms = new HashMap<>();
      }
      chromatograms.put(rawFile, chromato);
      return true;
   }

   public Map<IRawFile, IChromatogram> getChromatogramsMap() {
      Map<IRawFile, IChromatogram> result = new HashMap<>();
      for (Map.Entry<IRawFile, AnnotatedChromatogram> entry : chromatograms.entrySet()) {
         result.put(entry.getKey(), entry.getValue());
      }
      return result;
   }

   public IChromatogram getChromatogram(IRawFile rawFile) {
      return ((chromatograms != null) && (chromatograms.containsKey(rawFile))) ? chromatograms.get(rawFile) : null;
   }

   public MsnExtractionRequest getRequest() {
      return request;
   }
   
}
