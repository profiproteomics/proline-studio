package fr.proline.studio.markerbar;

public interface MarkerComponentInterface {

    public int getRowCount();

    public int getFirstVisibleRow();

    public int getLastVisibleRow();

    public void scrollToVisible(int row);

    public int getRowYStart(int row);

    public int getRowYStop(int row);
    
    public int getRow(int y);
    
    public int convertRowIndexToModel(int rowIndex);
    
    public int convertRowIndexToView(int rowIndex);
}
