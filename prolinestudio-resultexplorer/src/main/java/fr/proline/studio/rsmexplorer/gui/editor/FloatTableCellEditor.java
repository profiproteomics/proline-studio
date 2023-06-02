package fr.proline.studio.rsmexplorer.gui.editor;

import fr.proline.studio.utils.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.EventObject;

public class FloatTableCellEditor extends DefaultCellEditor  {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private int m_digits = 2;


    public FloatTableCellEditor(int digits) {
        super(new JTextField());
        editorComponent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ((JTextField)e.getComponent()).selectAll();
            }

        });
        this.m_digits = digits;
    }

    @Override
    public Object getCellEditorValue() {
        return super.getCellEditorValue();
    }

    @Override
    public boolean stopCellEditing() {
        try {

            Float.valueOf(getCellEditorValue().toString());
            fireEditingStopped();
            return true;

        } catch (NumberFormatException nfe) {
            String fVal = getCellEditorValue().toString().replace(',','.'); // try changing separator
            try {
                Float.valueOf(fVal);
                delegate.setValue(fVal);
                fireEditingStopped();
                return true;
            }catch (NumberFormatException nfe2) {

                return false;
            }
        }
    }


    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Float f = (Float) value;
        String formatedValue;
        if ((f == null) || (f.isNaN())) {
            formatedValue = "";
        } else {
            formatedValue = DataFormat.format(f.floatValue(), m_digits);
        }
        return  super.getTableCellEditorComponent(table, formatedValue, isSelected, row,column);
    }

}

