package fr.proline.studio.gui;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public interface TreeFileChooserTableModelInterface {
    public void setFiles(ArrayList<File> fileList, int rowIndex);
}
