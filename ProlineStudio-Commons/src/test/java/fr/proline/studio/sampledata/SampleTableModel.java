/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.sampledata;

import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author CB205360
 */
class SampleTableModel extends AbstractTableModel {

   private List<Object[]> sample;
   private Class[] classes;
   private String[] columns;

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

   @Override
   public String getColumnName(int column) {
      return columns[column];
   }

   @Override
   public Class<?> getColumnClass(int columnIndex) {
      // TODO Auto-generated method stub
      return classes[columnIndex];
   }

}
