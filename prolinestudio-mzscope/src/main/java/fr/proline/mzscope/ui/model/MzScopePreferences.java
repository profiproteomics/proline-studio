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
