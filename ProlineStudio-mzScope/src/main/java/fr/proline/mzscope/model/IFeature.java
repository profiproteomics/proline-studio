/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.model;

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

   int getMs1Count();

   double getMz();

   int getPeakelsCount();
   
}
