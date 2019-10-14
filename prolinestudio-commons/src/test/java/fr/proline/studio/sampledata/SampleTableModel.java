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

import fr.proline.studio.table.DecoratedTableModelInterface;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author CB205360
 */
public class SampleTableModel extends AbstractTableModel implements DecoratedTableModelInterface {

   private List<Object[]> sample;
   private Class[] classes;
   private String[] columns;

   /**
    * 
    * @param sample, an Array list of object[]
    * @param columns , list of column name
    */
   public SampleTableModel(List<Object[]> sample, String[] columns) {
      this.sample = sample;
      this.columns = columns;
      int k = 0;
      classes = new Class[sample.get(0).length];
      for (Object item : sample.get(0)) {
         classes[k++] = item.getClass();
      }
   }

   public int getRowCount() {
      return sample.size();
   }

   public int getColumnCount() {
      return sample.get(0).length;
   }

   public Object getValueAt(int rowIndex, int columnIndex) {
      return sample.get(rowIndex)[columnIndex];
   }

   public void setDataValueAt(Object value, int rowIndex, int columnIndex) {
       sample.get(rowIndex)[columnIndex] = value;
   }
   
     
   @Override
   public String getColumnName(int column) {
      return columns[column];
   }

   @Override
   public Class<?> getColumnClass(int columnIndex) {
      // TODO Auto-generated method stub
      return classes[columnIndex];
   }

   @Override
   public String getToolTipForHeader(int col) {
      return columns[col];
   }

   @Override
   public String getTootlTipValue(int row, int col) {
      return columns[col];
   }

   @Override
   public TableCellRenderer getRenderer(int row, int col) {
      return null;
   }

}
