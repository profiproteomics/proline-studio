/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.gui;

import fr.proline.core.orm.uds.Dataset;
import javax.swing.Action;

/**
 *
 * @author CB205360
 */
public interface DatasetAction extends Action {
    
     public void updateEnabled(Dataset[] selectedNodes);
     
     public void actionPerformed(Dataset[] selectedNodes, int x, int y);

}
