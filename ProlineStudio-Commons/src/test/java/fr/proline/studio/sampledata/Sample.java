/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.sampledata;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.PlotInformation;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableModel;

/**
 *
 * @author CB205360
 */
public class Sample implements CompareDataInterface {

   private TableModel model; 
   
   public Sample(int size) {
      List<Object[]> sample = SampleDataGenerator.generate(size);
      String[] col = { "xValue", "isA", "anInt", "yValue", "anotherInt", "id"};
      model = new SampleTableModel(sample, col);
   }

   @Override
   public int getRowCount() {
      return model.getRowCount();
   }

   @Override
   public int getColumnCount() {
      return model.getColumnCount();
   }

   @Override
   public String getDataColumnIdentifier(int columnIndex) {
      return model.getColumnName(columnIndex);
   }

   @Override
   public Class getDataColumnClass(int columnIndex) {
      return model.getColumnClass(columnIndex);
   }

   @Override
   public Object getDataValueAt(int rowIndex, int columnIndex) {
      return model.getValueAt(rowIndex, columnIndex);
   }

   @Override
   public int[] getKeysColumn() {
      int[] key = { 5 }; 
      return key;
   }

   @Override
   public int getInfoColumn() {
      return 2;
   }

   @Override
   public void setName(String name) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public String getName() {
      return "Sample dataset";
   }

   @Override
   public Map<String, Object> getExternalData() {
      return null;
   }

   @Override
   public PlotInformation getPlotInformation() {
      return null;
   }

    @Override
    public long row2UniqueId(int rowIndex) {
        return rowIndex;
    }
    
    @Override
    public int uniqueId2Row(long id) {
        return (int) id;
    }

}
