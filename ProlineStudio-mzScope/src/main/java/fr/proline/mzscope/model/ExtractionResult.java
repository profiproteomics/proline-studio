/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
   private List<Pair<Chromatogram, Object>> chromatograms;
   private Status status = Status.NONE;

   public ExtractionResult(MsnExtractionRequest request) {
      this.request = request;
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

   public Status getStatus() {
      return status;
   }

   public void setStatus(Status status) {
      this.status = status;
   }
   
   public boolean addChromatogram(Chromatogram chromato) {
      if (chromatograms == null) {
         chromatograms = new ArrayList<>();
      }
      return chromatograms.add(new MutablePair<>(chromato, null));
   }

   public List<Chromatogram> getChromatograms() {
      List<Chromatogram> result = new ArrayList<>(chromatograms.size());
      for( Pair<Chromatogram, Object>  p : chromatograms) {
         result.add(p.getKey());
      }
      return result;
   }
   
   public MsnExtractionRequest getRequest() {
      return request;
   }
   
}
