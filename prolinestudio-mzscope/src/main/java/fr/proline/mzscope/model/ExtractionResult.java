/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
