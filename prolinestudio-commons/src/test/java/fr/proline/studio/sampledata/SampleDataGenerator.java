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
package fr.proline.studio.sampledata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleDataGenerator {

   public static List<Object[]> generate(int count) {
      List<Object[]> data = new ArrayList<Object[]>();
      Random random = new Random();
      for (int k = 0; k < count; k++) {
         Object[] item = {random.nextGaussian(), random.nextBoolean(), random.nextInt(200), random.nextGaussian() * 10000.0, random.nextInt(300), k};
         data.add(item);
      }
      return data;
   }
   
      public static List<Object[]> generate2DGaussian(int count, double sigma, double h, double noiseLevel) {
      Random random = new Random();
      List<Object[]> data = new ArrayList<Object[]>();
      int m = count / 2;
      for (int k = 0; k < count; k++) {
         Object[] item = {k, 1.0*k, h*Math.exp(-(k - m)*(k - m)/(2* sigma * sigma))+random.nextGaussian()*noiseLevel};
         data.add(item);
      }
      return data;
   }

   public static List<Object[]> generate(int count, Class[] classes) {
      List<Object[]> data = new ArrayList<Object[]>();
      Random random = new Random();
      for (int k = 0; k < count; k++) {
         Object[] item = new Object[classes.length];
         for (int i = 0; i < classes.length; i++) {
            if (classes[i].equals(Double.class))
               item[i] = random.nextGaussian(); 
            else if (classes[i].equals(Boolean.class)) 
               item[i] = random.nextBoolean(); 
            else if (classes[i].equals(Integer.class)) 
               item[i] = random.nextInt(200);
         }
         data.add(item);
      }
      return data;
   }

   public static void writeToFile(List<Object[]> data, String[] columns, File file) {

      try {
         FileWriter writer = new FileWriter(file);
         writer.write(Integer.toString(columns.length));
         writer.write('\n');
         for (String c : columns) {
            writer.write(c);
            writer.write('\n');
         }
         for (Object[] item : data) {
            for (int k = 0; k < item.length; k++) {
               writer.write(item[k].toString());
               if (k < (item.length - 1)) {
                  writer.write(' ');
               }
            }
            writer.write('\n');
         }
         writer.flush();
         writer.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}
