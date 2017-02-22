package fr.proline.mzscope.ui.model;

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
   private float fragmentMzPPMTolerance;
   
   private MzScopePreferences() {
      mzPPMTolerance = 10.0f;
      fragmentMzPPMTolerance = 50.0f;
   }

   public float getMzPPMTolerance() {
      return mzPPMTolerance;
   }
   
   public void setMzPPMTolerance(float ppm) {
      this.mzPPMTolerance = ppm;
   }

    public float getFragmentMzPPMTolerance() {
        return fragmentMzPPMTolerance;
    }

    public void setFragmentMzPPMTolerance(float fragmentMzPPMTolerance) {
        this.fragmentMzPPMTolerance = fragmentMzPPMTolerance;
    }
}
