package fr.proline.studio.export.model;

import fr.proline.studio.export.ExportExcelSheet;
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
