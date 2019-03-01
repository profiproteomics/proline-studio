/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.model;

import fr.profi.mzdb.XicMethod;

/**
 *
 * @author CB205360
 */
public class MsnExtractionRequest extends ExtractionRequest {

   public static class Builder<T extends Builder<T>> extends ExtractionRequest.Builder<T> {

      float mzTolPPM = 10.0f;
      float fragmentMzTolPPM = 50.0f;
      
      XicMethod method = XicMethod.MAX;

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
      
      @Override
      public T setFragmentMz(double mz){
          fragmentMz = mz;
          fragmentMaxMz = (fragmentMz + fragmentMz * fragmentMzTolPPM / 1e6f);
          fragmentMinMz = (fragmentMz - fragmentMz * fragmentMzTolPPM / 1e6f);
          super.setMsLevel(2);
          return self();
      }

            @Override
      public T setFragmentMaxMz(double maxMz) {
         super.setFragmentMaxMz(maxMz); 
         if (getFragmentMinMz() > 0.0) {
            fragmentMz = (fragmentMaxMz + fragmentMinMz)/2.0;
            fragmentMzTolPPM = (float) (1e6 * (fragmentMaxMz - fragmentMinMz) / (2*fragmentMz));
            super.setMsLevel(2);
         }
         return self();
      }

      @Override
      public T setFragmentMinMz(double minMz) {
         super.setFragmentMinMz(minMz);
         if (getFragmentMaxMz() > 0.0) {
            fragmentMz = (fragmentMaxMz + fragmentMinMz)/2.0;
            fragmentMzTolPPM = (float) (1e6 * (fragmentMaxMz - fragmentMinMz) / (2*fragmentMz));
            super.setMsLevel(2);
         }
         return self();
      }
      
     public T setFragmentMzTolPPM(float mzTolPPM) {
         fragmentMzTolPPM = mzTolPPM;
         if (fragmentMz > 0.0) {
          fragmentMaxMz = (fragmentMz + fragmentMz * fragmentMzTolPPM / 1e6f);
          fragmentMinMz = (fragmentMz - fragmentMz * fragmentMzTolPPM / 1e6f);
         }
         return self();
      }
     
      public T setMethod(XicMethod method) {
         this.method = method;
         return self();
      }
      
      public MsnExtractionRequest build() {
         return new MsnExtractionRequest(this);
      }
      
   }

   @SuppressWarnings("rawtypes")
   public static Builder<?> builder() {
      return new Builder();
   }
   
   private final float mzTolPPM;
   private final float fragmentMzTolPPM;
   private final XicMethod method;

   protected MsnExtractionRequest(Builder builder) {
      super(builder);
      this.mzTolPPM = builder.mzTolPPM;
      this.fragmentMzTolPPM = builder.fragmentMzTolPPM;
      this.method = builder.method;
   }

   public float getMzTolPPM() {
      return mzTolPPM;
   }

   public float getFragmentMzTolPPM() {
      return fragmentMzTolPPM;
   }
      
   public XicMethod getMethod() {
      return method;
   }
      
}
