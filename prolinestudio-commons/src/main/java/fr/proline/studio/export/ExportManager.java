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
package fr.proline.studio.export;

import fr.proline.studio.gui.DefaultDialog;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * Class used to do a local export of a table
 *
 * @author JM235353
 */
public class ExportManager {

    private final JTable m_table;

    private ExportManager m_exportManager = null;
    private Exception m_exportException = null;
    
    
    public ExportManager(JTable table) {
        m_table = table;
        m_exportManager = this;
    }

    public ExportWorker getTask(ExporterInterface exporter, String name, String filePath, boolean decorated) {
        
        // get cols from the table
        int nbCol = m_table.getColumnCount();
        int[] colsInModel = new int[nbCol];
        for (int i = 0; i < nbCol; i++) {
            int colInModel = m_table.convertColumnIndexToModel(i);
            colsInModel[i] = colInModel;
        }

        // get rows from the table
        int nbRow = m_table.getRowCount();
        int[] rowsInModel = new int[nbRow];
        for (int i = 0; i < nbRow; i++) {
            int rowInModel = m_table.convertRowIndexToModel(i);
            rowsInModel[i] = rowInModel;
        }

        // retrieve model
        TableModel model = m_table.getModel();
        
        return new ExportWorker(exporter, name, filePath, decorated, model, colsInModel, rowsInModel);
    }
    
    public void setException(Exception e) {
        m_exportException = e;
    }

    public Exception getException() {
        return m_exportException;
    }

    public class ExportWorker extends DefaultDialog.ProgressTask {

        private final ExporterInterface m_exporter;
        private final String m_name;
        private final String m_filePath;
        private final boolean m_decorated;
        
        private final TableModel m_model;
        private final int[] m_colsInModel;
        private final int[] m_rowsInModel;
        
        

        public ExportWorker(ExporterInterface exporter, String name, String filePath, boolean decorated, TableModel model, int[] colsInModel, int[] rowsInModel) {
            m_exporter = exporter;
            m_name = name;
            m_filePath = filePath;
            m_decorated = decorated;
            m_model = model;
            m_colsInModel = colsInModel;
            m_rowsInModel = rowsInModel;
        }

        @Override
        protected Object doInBackground() throws Exception {
            
            try {
                m_exporter.start(m_filePath);
                m_exporter.startSheet(m_name);

                m_exporter.setDecorated(m_decorated);
 

                ExportModelInterface exportInterface = (m_model instanceof ExportModelInterface) ? (ExportModelInterface) m_model : null;
                
                int nbCol = m_colsInModel.length;
                int nbRow = m_rowsInModel.length;
                
                // headers
                if (exportInterface != null) {
                    m_exporter.startRow();
                    for (int j = 0; j < nbCol; j++) {
                        String colName = exportInterface.getExportColumnName(m_colsInModel[j]);
                        m_exporter.addCell(colName, null);
                    }
                } else {
                    m_exporter.startRow();
                    for (int j = 0; j < nbCol; j++) {
                        String colName = m_model.getColumnName(m_colsInModel[j]);
                        m_exporter.addCell(colName, null);
                    }
                }

                // all rows
                

                int lastPercentage = 0;
                int percentage;
                for (int j = 0; j < nbRow; j++) {
                    int row = m_rowsInModel[j];
                    if (exportInterface != null) {
                        m_exporter.startRow();
                        for (int i = 0; i < nbCol; i++) {
                            int col = m_colsInModel[i];
                            String text = exportInterface.getExportRowCell(row, col);
                             ArrayList<ExportFontData> stringFonts = null;
                            if (text == null) {
                                Object o = ((ExtendedTableModelInterface) m_model).getDataValueAt(row, col);
                                if (o != null) {
                                    text = o.toString();
                                }
                            } else {
                                stringFonts = exportInterface.getExportFonts(row, col);
                            }
                            m_exporter.addCell(text, stringFonts);
                        }
                    } else if (m_model instanceof ExtendedTableModelInterface) {
                        m_exporter.startRow();
                        for (int i = 0; i < nbCol; i++) {
                            int col = m_colsInModel[i];
                            String text = null;
                            Object o = ((ExtendedTableModelInterface) m_model).getDataValueAt(row, col);
                            if (o != null) {
                                text = o.toString();
                            }
                            
                            m_exporter.addCell(text, null);
                        }
                    } else {
                        m_exporter.startRow();
                        for (int i = 0; i < nbCol; i++) {
                            int col = m_colsInModel[i];
                            String text = null;
                            Object o = m_model.getValueAt(row, col);
                            if (o != null) {
                                text = o.toString();
                            }
                            
                            m_exporter.addCell(text, null);
                        }
                    }

                    percentage = (int) Math.round((((double) (j + 1)) / nbRow) * 100);
                    if (percentage > lastPercentage) {
                        setProgress(percentage);
                        lastPercentage = percentage;
                    }
                }

            } catch (Exception e) {
                m_exportManager.setException(e);
            }
            
            setProgress(getMaxValue());

            // end fild
            m_exporter.end();

            return null;
        }

        @Override
        public int getMinValue() {
            return 0;
        }

        @Override
        public int getMaxValue() {
            return 100;
        }
    }
}
