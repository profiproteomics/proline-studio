package fr.proline.studio.graphics;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Keep a list of ColorInformation for colors of a graphic
 * 
 * @author JM235353
 */
public class GraphicColors {
    
   private final ArrayList<ColorInformation> m_colorInformationList = new ArrayList<>();
    
   public GraphicColors() {
       
   }
   
   public void addColorInformation(HashSet<Long> ids, ColorOrGradient colorDefinition) {
       ColorInformation colorInformation = new ColorInformation(ids, colorDefinition);
       m_colorInformationList.add(colorInformation);
   }
   
   public void clear() {
       m_colorInformationList.clear();
   }
   
   public ColorOrGradient getColorDefinition(Long id) {
       int nb = m_colorInformationList.size();
       for (int i=0;i<nb;i++) {
           ColorOrGradient c = m_colorInformationList.get(i).getColorDefinition(id);
           if (c != null) {
               return c;
           }
       }
       return null; // should never happen
   }
   
   private class ColorInformation  {
       
       private HashSet<Long> m_ids;
       
       private ColorOrGradient m_colorDefinition;
       
       public ColorInformation(HashSet<Long> ids, ColorOrGradient colorDefinition) {
           m_ids = ids;
       }
       
       public ColorOrGradient getColorDefinition(Long id) {
           if ((m_ids == null) || (m_ids.contains(id)))  {
               return m_colorDefinition;
           }
           return null;
       }
       
   }
}
