package fr.proline.studio.graphics;

import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public interface CrossSelectionInterface {

    public void select(ArrayList<Long> uniqueIds);
    public ArrayList<Long> getSelection();
}
