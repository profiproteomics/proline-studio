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
package fr.proline.studio.export.model;

import fr.proline.studio.export.ExportExcelSheetField;

/**
 *
 * @author Karine XUE, @cea
 */
public class CustomExportTableModel extends javax.swing.table.DefaultTableModel {

    private Object[][] m_object = new Object[][]{};
    private String[] m_columnName = new String[]{
        "Internal field name", "Displayed field name (editable)", "Export"
    };

    public CustomExportTableModel() {
        super();
        super.setDataVector(m_object, m_columnName);
    }

    private Class[] types = new Class[]{
        java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
    };

    private boolean[] canEdit = new boolean[]{
        false, true, true
    };

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit[columnIndex];
    }
    
    public void addRow(ExportExcelSheetField field ) {
        Object[] rowArray = new Object[3];
        rowArray[0] = field.id;
        rowArray[1] = field.title;
        rowArray[2] = field.default_displayed;
        this.addRow(rowArray);
    }

    public void addRow(ExportExcelSheetField field, boolean isFieldSelected) {
        Object[] rowArray = new Object[3];
        rowArray[0] = field.id;
        rowArray[1] = field.title;
        rowArray[2] = isFieldSelected;
        this.addRow(rowArray);
    }
}
