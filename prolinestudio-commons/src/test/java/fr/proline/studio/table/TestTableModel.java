package fr.proline.studio.table;

import org.junit.Test;

import java.util.Arrays;


public class TestTableModel {

    public static Column PROTEIN_ID = new Column("Id", Long.class, 0);
    public static Column PROTEIN_NAME = new Column("Protein", String.class, 1);
    public static Column PROTEIN_DESCRIPTION = new Column("Description", String.class, 2);

    public  Column[] columns = Column.pack(this.getClass());

  public void getValueAt(int row, int col) {
    PROTEIN_DESCRIPTION.getIndex();
    String v;

    if (columns[col] == PROTEIN_ID) {
      v = "toto";
    }

 }

  public int getColumnCount() {
    return columns.length;
  }

  public String getColumnName(int col) {
    return columns[col].getName();
  }

  public Class getColumnClass(int col) {
    return columns[col].getColumnClass();
  }


  public static void main(String[] args) {
    TestTableModel model = new TestTableModel();
    for (Column c : model.columns) {
      System.out.println("column : "+c.getName()+", "+c.getIndex());
    }
  }
}
