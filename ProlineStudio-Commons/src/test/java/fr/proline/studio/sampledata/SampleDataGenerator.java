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
         Object[] item = {random.nextGaussian(), random.nextBoolean(), random.nextInt(200), random.nextGaussian() * 10.0, random.nextInt(300), k};
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
