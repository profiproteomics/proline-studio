package fr.proline.studio.table;

import fr.proline.studio.filter.*;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;

import javax.swing.table.TableCellRenderer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Column {

  public static TableCellRenderer DOUBLE_5DIGITS_RIGHT = new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 5);
  public static TableCellRenderer DOUBLE_2DIGITS_RIGHT = new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 2);
  public static TableCellRenderer STRING_LEFT = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
  private String name;
  private String tooltip;
  private Class type;
  private int index;
  private boolean isLazy;
  private Filter filter;
  private TableCellRenderer renderer;

  public Column(String name, String tooltip, Class type, int index, TableCellRenderer renderer) {
    this.name = name;
    this.tooltip = tooltip;
    this.type = type;
    this.index = index;
    this.isLazy = false;
    this.renderer = renderer;
  }

  public Column(String name, String tooltip, Class type, int index) {
    this(name, tooltip, type, index, null);
  }

  public Column(String name, Class type, int index) {
    this(name, name, type, index);
  }

  public static Column[] pack(Class modelClass) {
    System.out.println("supplied class = " + modelClass);

    Field[] declaredFields = modelClass.getFields();
    List<Column> columns = new ArrayList<Column>();
    for (Field field : declaredFields) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType().equals(Column.class)) {
        System.out.println("field = " + field.getName());
        try {
          Column value = (Column) field.get(null);
          System.out.println("column value = " + value);
          columns.add(value);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    columns.sort(Comparator.comparingInt(Column::getIndex));
    return columns.toArray(new Column[0]);
  }

  public String getName() {
    return name;
  }

  public Class getColumnClass() {
    return isLazy ? LazyData.class : type;
  }

  public Class getDataColumnClass() {
    return type;
  }

  public int getIndex() {
    return index;
  }

  public String getTooltip() {
    return tooltip;
  }

  public Filter getDefaultFilter() {
      if (Boolean.class.isAssignableFrom(type)) {
        return new BooleanFilter(getName(), null, getIndex());
      } else if (String.class.isAssignableFrom(type)) {
        return new StringDiffFilter(getName(), null, getIndex());
      } else if (Double.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type)) {
        return new DoubleFilter(getName(), null, getIndex());
      } else if (Integer.class.isAssignableFrom(type)) {
        return new IntegerFilter(getName(), null, getIndex());
      } else if (Long.class.isAssignableFrom(type)) {
        return new LongFilter(getName(), null, getIndex());
      }
      return null;
  }

  public Filter getFilter() {
    if (filter == null) {
      filter = getDefaultFilter();
    }
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public TableCellRenderer getRenderer() {
    return renderer;
  }
}
