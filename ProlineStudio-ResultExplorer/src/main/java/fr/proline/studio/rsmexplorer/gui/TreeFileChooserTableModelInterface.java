package fr.proline.studio.rsmexplorer.gui;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public interface TreeFileChooserTableModelInterface {
    public void setFiles(ArrayList<File> fileList, int rowIndex);
    public boolean shouldConfirmCorruptFiles(ArrayList<Integer> indices);
    public boolean canSetFiles(ArrayList<Integer> indices);
}
