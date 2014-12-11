package fr.proline.studio.graphics;

import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public interface CrossSelectionInterface {

    public void select(ArrayList<Integer> rows);
    public ArrayList<Integer> getSelection();
}
