package fr.proline.studio.search;

/**
 *
 * @author JM235353
 */
public abstract class AbstractSearch {
    
    protected SearchFloatingPanel m_searchPanel;
    
    public abstract void reinitSearch();
    public abstract void doSearch(String text);
    public void setFloatingPanel(SearchFloatingPanel searchPanel) {
        m_searchPanel = searchPanel;
    }
}
