package fr.proline.studio.gui;

import fr.proline.core.orm.uds.dto.DDataset;
import javax.swing.Action;

/**
 *
 * @author CB205360
 */
public interface DatasetAction extends Action {
    
     public void updateEnabled(DDataset[] selectedNodes);
     
     public void actionPerformed(DDataset[] selectedNodes, int x, int y);

}
