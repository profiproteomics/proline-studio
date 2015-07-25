/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.model;

import fr.profi.mzdb.MzDbReader;

/**
 *
 * @author CB205360
 */
public class Ms1ExtractionRequest extends ExtractionRequest {

   public static class Builder<T extends Builder<T>> extends ExtractionRequest.Builder<T> {

      float mzTolPPM = 10.0f;
      double mz = -1.0;
      MzDbReader.XicMethod method = MzDbReader.XicMethod.MAX;

      public T setMzTolPPM(float mzTolPPM) {
         this.mzTolPPM = mzTolPPM;
         if (mz > 0.0) {
          maxMz = (mz + mz * mzTolPPM / 1e6f);
          minMz = (mz - mz * mzTolPPM / 1e6f);
         }
         return self();
      }

      public T setMz(double mz) {
         this.mz = mz;
         maxMz = (mz + mz * mzTolPPM / 1e6f);
         minMz = (mz - mz * mzTolPPM / 1e6f);
         return self();
      }

      @Override
      public T setMaxMz(double maxMz) {
         super.setMaxMz(maxMz); 
         if (getMinMz() > 0.0) {
            mz = (maxMz + minMz)/2.0;
            mzTolPPM = (float) (1e6 * (maxMz - minMz) / (2*mz));
         }
         return self();
      }

      @Override
      public T setMinMz(double minMz) {
         super.setMinMz(minMz);
         if (getMaxMz() > 0.0) {
            mz = (maxMz + minMz)/2.0;
            mzTolPPM = (float) (1e6 * (maxMz - minMz) / (2*mz));
         }
         return self();
      }

      
      public T setMethod(MzDbReader.XicMethod method) {
         this.method = method;
         return self();
      }
      
      public Ms1ExtractionRequest build() {
         return new Ms1ExtractionRequest(this);
      }
      
   }

   @SuppressWarnings("rawtypes")
   public static Builder<?> builder() {
      return new Builder();
   }
   
   private final float mzTolPPM;
   private final double mz;
   private final MzDbReader.XicMethod method;

   protected Ms1ExtractionRequest(Builder builder) {
      super(builder);
      this.mzTolPPM = builder.mzTolPPM;
      this.mz = builder.mz;
      this.method = builder.method;
   }

   public float getMzTolPPM() {
      return mzTolPPM;
   }

   public double getMz() {
      return mz;
   }

   public MzDbReader.XicMethod getMethod() {
      return method;
   }
   
   
}
