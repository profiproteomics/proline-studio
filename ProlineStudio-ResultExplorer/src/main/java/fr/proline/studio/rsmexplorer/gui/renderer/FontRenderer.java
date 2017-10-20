package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.rsmexplorer.gui.xic.FeatureTableModel;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer to display a cell in italic -- for now specific for FeatureTableModel -- see if it can be used elsewhere
 * @author JM235353
 */
public class FontRenderer implements TableCellRenderer  {
    
    private TableCellRenderer m_defaultRenderer;
    
    public FontRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }
    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = m_defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if ((((CompoundTableModel) table.getModel()).getBaseModel()) instanceof FeatureTableModel) {
            FeatureTableModel tableModel= (FeatureTableModel)(((CompoundTableModel) table.getModel()).getBaseModel());
            if (tableModel.isInItalic(table.convertRowIndexToModel(row), table.convertColumnIndexToModel(column))) {
                //c.setBackground(Color.LIGHT_GRAY);
                Font font = c.getFont();
                c.setFont(font.deriveFont(Font.ITALIC));
            }
        }
        return c;

    }
}
