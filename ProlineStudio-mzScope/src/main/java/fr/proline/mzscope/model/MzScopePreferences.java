package fr.proline.mzscope.model;

/**
 *
 * @author CB205360
 */
public class MzScopePreferences {
   
   public static MzScopePreferences getInstance() {
      return instance;
   }
   
   private static MzScopePreferences instance = new MzScopePreferences();
   
   private float mzPPMTolerance; 
   
   private MzScopePreferences() {
      mzPPMTolerance = 5.0f;
   }

   public float getMzPPMTolerance() {
      return mzPPMTolerance;
   }

   public void setMzPPMTolerance(float ppm) {
      this.mzPPMTolerance = ppm;
   }
}
