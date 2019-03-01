package fr.proline.studio.filter;


/**
 *
 * @author JM235353
 */
public class FilterMirroredTableModel extends FilterTableModel {
    
    private FilterTableModel m_filterSrcModel = null;
    
    public FilterMirroredTableModel(FilterTableModel srcModel) {
        super(srcModel.getTableModelSource());
        
        m_filterSrcModel = new FilterTableModel(m_tableModelSource);
        m_filterSrcModel.setFilters(srcModel.getFilters());
    }
    
    @Override
    public boolean filter(int row) {
        
        boolean filter = m_filterSrcModel.filter(row);
        
        return !filter;
    }
    
    /*@Override
    public boolean filter() {
        
    }*/
    
}
