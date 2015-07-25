/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author CB205360
 */
public class ExtractionRequest {

   public static class Builder<T extends Builder<T>> {

      double minMz = 0.0;
      double maxMz = 0.0;
      float elutionTime = -1.0f;
      float elutionTimeLowerBound = -1.0f;
      float elutionTimeUpperBound = -1.0f;

      @SuppressWarnings("unchecked")  // Smell 1
      protected T self() {
         return (T) this;            // Unchecked cast!
      }

      public T setMinMz(double minMz) {
         this.minMz = minMz;
         return self();
      }

      public T setMaxMz(double maxMz) {
         this.maxMz = maxMz;
         return self();
      }

      public T setElutionTimeLowerBound(float startRT) {
         this.elutionTimeLowerBound = startRT;
         return self();
      }

      public T setElutionTimeUpperBound(float stopRT) {
         this.elutionTimeUpperBound = stopRT;
         return self();
      }

      public double getMinMz() {
         return minMz;
      }

      public double getMaxMz() {
         return maxMz;
      }
      
      public ExtractionRequest build() {
            return new ExtractionRequest(this);
      }
   }

   private final double minMz;
   private final double maxMz;
   // values in seconds !! 
   private final float elutionTimeLowerBound;
   private final float elutionTimeUpperBound;
   private final float elutionTime;

   protected ExtractionRequest(Builder builder) {
      this.maxMz = builder.maxMz;
      this.minMz = builder.minMz;
      this.elutionTimeLowerBound = builder.elutionTimeLowerBound;
      this.elutionTimeUpperBound = builder.elutionTimeUpperBound;
      this.elutionTime = builder.elutionTime;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this);
   }

   @SuppressWarnings("rawtypes")       // Smell 2
   public static Builder<?> builder() {
      return new Builder();           // Raw type - no type argument!
   }

   public double getMinMz() {
      return minMz;
   }

   public double getMaxMz() {
      return maxMz;
   }

   public float getElutionTimeLowerBound() {
      return elutionTimeLowerBound;
   }

   public float getElutionTimeUpperBound() {
      return elutionTimeUpperBound;
   }

   public float getElutionTime() {
      return elutionTime;
   }

   

}
