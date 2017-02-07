package fr.proline.studio.sampledata;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.graphics.PlotInformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableModel;

/**
 *
 * @author CB205360
 */
public class Gaussian2DSample implements CompareDataInterface {

   private TableModel model; 
   
   public Gaussian2DSample(int size) {
      this(size, 1.0, 0.0);
   }

   public Gaussian2DSample(int size, double noiseLevel) {
      this(size, 1.0, noiseLevel);
   }
   public Gaussian2DSample(int size, double h, double noiseLevel) {
      List<Object[]> sample = SampleDataGenerator.generate2DGaussian(size, size/8, h, noiseLevel);
      String[] col = { "id", "xValue", "yValue"};
      model = new SampleTableModel(sample, col);
   }

   public TableModel getTableModel() {
      return model;
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
      int[] key = { 0 }; 
      return key;
   }

   @Override
   public int getInfoColumn() {
      return 0;
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

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValue(Class c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getRowValue(Class c, int row) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getColValue(Class c, int col) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addSingleValue(Object v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getSingleValue(Class c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
