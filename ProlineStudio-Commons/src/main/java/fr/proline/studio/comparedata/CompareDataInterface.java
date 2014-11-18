package fr.proline.studio.comparedata;

/**
 *
 * @author JM235353
 */
public interface CompareDataInterface {
    public int getRowCount();
    public int getColumnCount();
    public String getDataColumnIdentifier(int columnIndex);
    public Class getDataColumnClass(int columnIndex);
    public Object getDataValueAt(int rowIndex, int columnIndex);
    public int[] getKeysColumn();
    public void setName(String name);
    public String getName();
    
}
