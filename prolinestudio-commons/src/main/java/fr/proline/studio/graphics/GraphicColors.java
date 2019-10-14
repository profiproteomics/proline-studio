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
