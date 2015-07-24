package fr.proline.studio.sampledata;

import fr.proline.studio.table.DecoratedTableModelInterface;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author CB205360
 */
class SampleTableModel extends AbstractTableModel implements DecoratedTableModelInterface {

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

   @Override
   public String getToolTipForHeader(int col) {
      return columns[col];
   }

   @Override
   public String getTootlTipValue(int row, int col) {
      return columns[col];
   }

   @Override
   public TableCellRenderer getRenderer(int col) {
      return null;
   }

}
