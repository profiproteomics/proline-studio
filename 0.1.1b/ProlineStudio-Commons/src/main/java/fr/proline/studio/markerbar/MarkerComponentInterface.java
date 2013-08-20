package fr.proline.studio.markerbar;

/**
 * Interface which must be extended by any Component included in a MarkerContainerPanel
 * @author JM235353
 */
public interface MarkerComponentInterface {

    public int getRowCount();

    public int getFirstVisibleRow();

    public int getLastVisibleRow();

    public void scrollToVisible(int row);

    public int getRowYStart(int row);

    public int getRowYStop(int row);
    
    public int getRowInModel(int y);
    
    public int convertRowIndexToModel(int rowIndex);
    
    public int convertRowIndexToView(int rowIndex);
    
    public void addViewChangeListerner(ViewChangeListener listener);
    
    public void removeViewChangeListener(ViewChangeListener listener);
    
    public void dispatchViewChange();
    
}
