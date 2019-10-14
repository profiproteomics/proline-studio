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
package fr.proline.studio.extendedtablemodel;

import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.LongFilter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;
import au.com.bytecode.opencsv.CSVReader;

/**
 *
 * @author CB205360
 */
 public class ImportedDataTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

        private String[] m_columnNames = { "No Data" };
        private Class[] m_columTypes = { String.class };
        private Object[][] m_data = null;
        private String m_modelName = null;

        public ImportedDataTableModel() {
            m_data = new Object[1][1];
            m_data[0][0] = "";
        }
        public ImportedDataTableModel(String[] columnNames, Class[] columTypes, Object[][] data) {
            setData(columnNames, columTypes, data);
        }
        
        public final void setData(String[] columnNames, Class[] columTypes, Object[][] data) {
            m_columnNames = columnNames;
            m_columTypes = columTypes;
            m_data = data;
            fireTableStructureChanged();
        }
        
        @Override
        public int getRowCount() {
            if (m_data == null) {
                return 0;
            }
            return m_data.length;
        }

        @Override
        public int getColumnCount() {
            if (m_columnNames == null) {
                return 0;
            }
           return m_columnNames.length;
        }
        
        @Override
        public Class getColumnClass(int column) {
            return m_columTypes[column];
        }

        @Override
        public String getColumnName(int column) {
            return m_columnNames[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return m_data[rowIndex][columnIndex];
        }

        @Override
        public String getToolTipForHeader(int col) {
            return null;
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            if (m_columTypes[col] == String.class) {
                return new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
            } else if  (m_columTypes[col] == Long.class) {
                return TableDefaultRendererManager.getDefaultRenderer(Long.class);
            } else if  (m_columTypes[col] == Double.class) {
                return new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true);
            }  
            return null;
        }

        @Override
        public GlobalTableModelInterface getFrozzenModel() {
            return this;
        }

        @Override
        public Long getTaskId() {
            return -1L;
        }

        @Override
        public LazyData getLazyData(int row, int col) {
            return null;
        }

        @Override
        public void givePriorityTo(Long taskId, int row, int col) {
        }

        @Override
        public void sortingChanged(int col) {
        }

        @Override
        public int getSubTaskId(int col) {
            return -1;
        }

        @Override
        public String getDataColumnIdentifier(int columnIndex) {
            return m_columnNames[columnIndex];
        }

        @Override
        public Class getDataColumnClass(int columnIndex) {
            return m_columTypes[columnIndex];
        }

        @Override
        public Object getDataValueAt(int rowIndex, int columnIndex) {
            return getValueAt(rowIndex, columnIndex);
        }

        @Override
        public int[] getKeysColumn() {
            return null;
        }

        @Override
        public void setName(String name) {
            m_modelName = name;
        }

        @Override
        public String getName() {
            return m_modelName;
        }

        @Override
        public Map<String, Object> getExternalData() {
            return null;
        }

        @Override
        public PlotInformation getPlotInformation() {
            return null;
        }

        @Override
        public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
            
            int nbCols = getColumnCount();
            for (int i = 0; i < nbCols; i++) {
                Class c = getColumnClass(i);
                if (c.equals(Double.class)) {
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null,i ));
                } else if (c.equals(Long.class)) {
                    filtersMap.put(i, new LongFilter(getColumnName(i), null, i));
                } else if (c.equals(String.class)) {
                    filtersMap.put(i, new StringDiffFilter(getColumnName(i), null, i));
                }
            }
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public int getLoadingPercentage() {
            return 100;
        }

        @Override
        public PlotType getBestPlotType() {
            return PlotType.SCATTER_PLOT;
        }

        @Override
        public int[] getBestColIndex(PlotType plotType) {
            return null;
        }


        @Override
        public String getExportRowCell(int row, int col) {
            return ExportModelUtilities.getExportRowCell(this, row, col);
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return null;
        }
        
        @Override
        public String getExportColumnName(int col) {
            return m_columnNames[col];
        }

        @Override
        public int getInfoColumn() {
            return 0;
        }

        @Override
        public ArrayList<ExtraDataType> getExtraDataTypes() {
            return null;
        }

        @Override
        public Object getValue(Class c) {
            return getSingleValue(c);
        }

        @Override
        public Object getRowValue(Class c, int row) {
            return null;
        }
        
        @Override
        public Object getColValue(Class c, int col) {
            return null;
        }
        
        public static Exception loadFile(ImportedDataTableModel model, String filePath, char separator, boolean header, boolean testFile) {
        
        File f = new File(filePath);
        if (!f.exists()) {
            model.setData(null, null, null);
        }
        
        try {
            CSVReader reader = new CSVReader(new FileReader(filePath), separator);

            
            
        // read column headers
        String[] headerLine = null;
        int nbColumns = 0;
        int line = 1;
        if (header) {
            headerLine = reader.readNext();
            if (headerLine == null) {
                model.setData(null, null, null);
                throw new IOException("No Data found in File");
            }
            nbColumns = headerLine.length;
            line = 2;
        }
        
        if (headerLine == null) {
            headerLine = new String[nbColumns];
            for (int i = 0; i < nbColumns; i++) {
                headerLine[i] = String.valueOf(i + 1);
            }
        }

        // read lines, skip empty ones and check the number of columns
        ArrayList<String[]> allDataLines = new ArrayList<>();
        String[] dataLine;
        while ((dataLine = reader.readNext()) != null) {

            int nbColumsCur = dataLine.length;
            if (nbColumsCur == 0) {
                // continue : empty line
            } else if (nbColumns == 0) {
                nbColumns = nbColumsCur;
            }

            if (nbColumns != nbColumsCur) {
                model.setData(null, null, null);
                return new IOException("Number of columns differ in file at line " + line);
            }

            allDataLines.add(dataLine);

            line++;

            if ((testFile) && (line >= 7)) {
                // we read only the first lines of the file
                break;
                
            }
        }



        // check the type of columns
        int nbRows = allDataLines.size();
        Class[] columTypes = new Class[nbColumns];
        for (int col = 0; col < nbColumns; col++) {

            boolean canBeLong = true;
            boolean canBeDouble = true;
            for (int row = 0; row < nbRows; row++) {
                String data = allDataLines.get(row)[col];
                if (canBeLong) {
                    try {
                        Long.parseLong(data);
                    } catch (NumberFormatException nfe) {
                        canBeLong = false;
                    }
                }
                if ((!canBeLong) && (canBeDouble)) {
                    try {
                        Double.parseDouble(data);
                    } catch (NumberFormatException nfe) {
                        canBeDouble = false;
                        break;
                    }
                }
            }
            if (canBeLong) {
                columTypes[col] = Long.class;
            } else if (canBeDouble) {
                columTypes[col] = Double.class;
            } else {
                columTypes[col] = String.class;
            }
        }

        // create the model
        Object[][] data = new Object[nbRows][nbColumns];
        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbColumns; col++) {
                String value = allDataLines.get(row)[col];
                if (columTypes[col].equals(Long.class)) {
                    data[row][col] = Long.parseLong(value);
                } else if (columTypes[col].equals(Double.class)) {
                    data[row][col] = Double.parseDouble(value);
                } else {
                    data[row][col] = value;
                }

            }
        }

        
        model.setData(headerLine, columTypes, data);
        model.setName(f.getName().substring(0, f.getName().lastIndexOf('.')));

        } catch (Exception e) {
            model.setData(null, null, null);
            return e;
        }
        
        return null;
    }
    }