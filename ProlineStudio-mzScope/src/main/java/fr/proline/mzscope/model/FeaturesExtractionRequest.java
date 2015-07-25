package fr.proline.mzscope.model;

/**
 *
 * @author CB205360
 */
public class FeaturesExtractionRequest extends ExtractionRequest {

   public enum ExtractionMethod {
      EXTRACT_MS2_FEATURES, DETECT_PEAKELS, DETECT_FEATURES
   };

   public static class Builder<T extends Builder<T>> extends ExtractionRequest.Builder<T> {

      float mzTolPPM = 10.0f;
      ExtractionMethod extractionMethod;
      boolean removeBaseline;

      public T setMzTolPPM(float mzTolPPM) {
         this.mzTolPPM = mzTolPPM;
         return self();
      }

      public T setExtractionMethod(ExtractionMethod extractionMethod) {
         this.extractionMethod = extractionMethod;
         return self();
      }

      public T setRemoveBaseline(boolean removeBaseline) {
         this.removeBaseline = removeBaseline;
         return self();
      }

      public float getMzTolPPM() {
         return mzTolPPM;
      }
      
      public FeaturesExtractionRequest build() {
            return new FeaturesExtractionRequest(this);
      }
   }

   @SuppressWarnings("rawtypes")
   public static Builder<?> builder() {
      return new Builder();
   }

   private ExtractionMethod extractionMethod;
   private  float mzTolPPM = 10.0f;
   // algorithm extraction configurable specificities
   private boolean removeBaseline;

   protected FeaturesExtractionRequest(Builder builder) {
      super(builder);
      this.mzTolPPM = builder.mzTolPPM;
      this.extractionMethod = builder.extractionMethod;
      this.removeBaseline = builder.removeBaseline;
   } 

   public ExtractionMethod getExtractionMethod() {
      return extractionMethod;
   }

   public float getMzTolPPM() {
      return mzTolPPM;
   }

   public boolean isRemoveBaseline() {
      return removeBaseline;
   }
   
   
}
