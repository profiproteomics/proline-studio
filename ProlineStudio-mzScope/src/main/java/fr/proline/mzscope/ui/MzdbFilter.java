/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author MB243701
 */
 public class MzdbFilter extends FileFilter {

        @Override
        public boolean accept(File file) {
            // Allow only directories, or files with ".txt" extension
            return file.isDirectory() || file.getAbsolutePath().toLowerCase().endsWith(".mzdb");
        }

        @Override
        public String getDescription() {
            // This description will be displayed in the dialog,
            // hard-coded = ugly, should be done via I18N
            return "Mzdb file (*.mzdb)";
        }

    }
