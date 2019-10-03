/*
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.table;

import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ExpansionTableModel;
import fr.proline.studio.extendedtablemodel.ImportedDataTableModel;
import fr.proline.studio.sampledata.Sample;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.openide.util.Exceptions;

/**
 *
 * @author CB205360
 */
public class CompoundTableDemo extends JFrame {

    public CompoundTableDemo(String filepath) {
        super("Compound Table demo");
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(getTablePane(filepath), BorderLayout.CENTER);
        setSize(450, 350);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    protected JComponent getTablePane(String filepath) {
        JScrollPane scroll = new javax.swing.JScrollPane();
        DecoratedTable table = new DecoratedTable() {

            @Override
            public TablePopupMenu initPopupMenu() {
                return new TablePopupMenu();
            }

            @Override
            public void prepostPopupMenu() {
            }

        };

        ImportedDataTableModel model1 = new ImportedDataTableModel();
        ImportedDataTableModel.loadFile(model1, filepath, ',', true, false);
        ImportedDataTableModel model2 = new ImportedDataTableModel();
        ImportedDataTableModel.loadFile(model2, filepath, ',', true, false);
        ExpansionTableModel model = new ExpansionTableModel(model1, model2);
        
        table.setModel(model);
        scroll.setViewportView(table);

        return scroll;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {

                    String filepath = "C:/Users/CB205360/Documents/Classeur1.csv";
                    File file = new File(filepath);
                    if (file.exists()) {
                      CompoundTableDemo plot = new CompoundTableDemo(filepath);
                      plot.setVisible(true);
                    } else {
                      System.out.println(".run() fail: File "+filepath+" does not exists");
                    }
                }
            });
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
