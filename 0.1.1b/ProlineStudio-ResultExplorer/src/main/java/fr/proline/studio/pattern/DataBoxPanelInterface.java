package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;

/**
 * All panels which are linked to a databox must implement this interface
 * @author JM235353
 */
public interface DataBoxPanelInterface extends SplittedPanelContainer.UserActions {
    
    public void setDataBox(AbstractDataBox dataBox);
    
    public void setLoading(int id);
    
    public void setLoaded(int id);
}
