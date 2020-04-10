package fr.proline.studio.table.renderer;

import fr.proline.studio.utils.DataFormat;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class CollectionRenderer implements TableCellRenderer {

  private TableCellRenderer m_defaultRenderer;
  private int m_digits = -1;

  public CollectionRenderer(TableCellRenderer defaultRenderer, int digits) {
    m_defaultRenderer = defaultRenderer;
    m_digits = digits;
  }

  public CollectionRenderer(TableCellRenderer defaultRenderer) {
    m_defaultRenderer = defaultRenderer;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    String formatedValue = "";

   if (value instanceof Collection) {
      Collection<Object> l = (Collection) value;
      formatedValue = l.stream().map(v -> m_digits < 0 ? v.toString() : DataFormat.format(((Number)v).floatValue(), m_digits)).collect(Collectors.joining(";"));
    } else if (value.getClass().isArray()) {
      formatedValue = Arrays.stream((Object[])value).map(v -> m_digits < 0 ? v.toString() : DataFormat.format(((Number)v).floatValue(), m_digits)).collect(Collectors.joining(";"));
    }
    return m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);
  }
}
