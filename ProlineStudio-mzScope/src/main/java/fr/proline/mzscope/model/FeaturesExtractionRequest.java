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
   
   
   public String getExtractionParamsString(){
       StringBuilder sb = new StringBuilder();
       String em = "";
       switch (getExtractionMethod()) {
           case EXTRACT_MS2_FEATURES: {
               em = "Extract MS2 Features";
               break;
           }
           case DETECT_PEAKELS: {
               em = "Detect Peakels";
               break;
           }
           case DETECT_FEATURES: {
               em = "Detect Features";
               break;
           }
       }
       sb.append("<html>");
       sb.append(em);
       sb.append(": <br/>");
       sb.append("m/z tolerance (ppm): ");
       sb.append(Float.toString(mzTolPPM));
       sb.append("<br/>");
       if (isRemoveBaseline()){
           sb.append("Use Peakels baseline remover <br/>");
       }
       if (Double.compare(getMinMz(),getMaxMz() ) == 0 && Double.compare(getMinMz(), 0.0) == 0){
           sb.append("no m/z bounds");
       }else if (Double.compare(getMz(), 0) != 0){
           sb.append("at m/z: ");
           sb.append(getMz());
       }else{
           sb.append("m/z bounds: ");
           sb.append(getMinMz());
           sb.append(" - ");
           sb.append(getMaxMz());
       }
       sb.append("</html>");
       
       return sb.toString();
   }
   
}
