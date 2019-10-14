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
package fr.proline.mzscope.model;

import fr.profi.mzdb.model.Peakel;

/**
 *
 * @author CB205360
 */
public interface IFeature {

   float getArea();

   float getApexIntensity();

   int getCharge();

   float getDuration();

   float getElutionTime();

   float getFirstElutionTime();

   float getLastElutionTime();

   int getScanCount();

   double getMz();

   int getPeakelsCount();
   
   IRawFile getRawFile();
   
   // TODO : to be removed as soon as ThreadedMzdbRawFile is removed
   void setRawFile(IRawFile rawfile);
   
   int getMsLevel();
   
   double getParentMz();
   
   Peakel[] getPeakels();

}
