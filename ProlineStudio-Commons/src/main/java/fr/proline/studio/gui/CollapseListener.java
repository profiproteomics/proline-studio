package fr.proline.studio.gui;

/**
 * event thrown when collapsing or expanding the collapsePanel
 * @author MB243701
 */

public interface CollapseListener {
    
    /**
     * the panel's status has changed from 'expand' or 'collapse' state
     */
    public void collapse();
}
