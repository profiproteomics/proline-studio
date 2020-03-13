/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.graphics.core;

/**
 *
 * @author JM235353
 */
public interface PlotToolbarListenerInterface {

    public enum BUTTONS {
        GRID,
        EXPORT_SELECTION,
        IMPORT_SELECTION
    }

    public void stateModified(BUTTONS b);

    public void enable(BUTTONS b, boolean v);
}
