package fr.proline.studio.utils;

import java.util.ResourceBundle;

public class StudioResourceBundle {

  /**
   * Return a string for the given key from the resource bundle associated to specified class.
   * ResourceBundle for a class is the "Bundle" resource found in the class package.
   *
   * @param c The class to get the ResourceBundle for
   * @param key The key of the String to search for
   * @return String Value associated to the bundle/key couple or the key if no value found
   * @throws IllegalArgumentException if one of the parameter is null
   */
  public static String getMessage(Class c, String key){
    if(c == null || key==null) {
      throw new IllegalArgumentException("StudioResourceBundle: must specify a valid class and key.");
    }

    StringBuilder sb = new StringBuilder(c.getPackage().toString());
    sb.append(".Bundle");
    try {
      ResourceBundle rsc = ResourceBundle.getBundle(sb.toString());
      return ( rsc.getString(key));
    } catch (Exception e) {
      return  key;
    }
  }


}
