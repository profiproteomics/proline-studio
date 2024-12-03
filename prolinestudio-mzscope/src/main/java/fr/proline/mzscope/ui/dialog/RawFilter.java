package fr.proline.mzscope.ui.dialog;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class RawFilter extends FileFilter {

  @Override
  public boolean accept(File file) {
    // Allow only directories, or files with ".txt" extension
    return file.isDirectory() || file.getAbsolutePath().toLowerCase().endsWith(".raw");
  }

  @Override
  public String getDescription() {
    // This description will be displayed in the dialog,
    // hard-coded = ugly, should be done via I18N
    return "Raw file (*.raw)";
  }
}
