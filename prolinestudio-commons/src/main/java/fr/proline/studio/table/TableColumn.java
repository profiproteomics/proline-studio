package fr.proline.studio.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TableColumn {

  private int position;
  private String name;
  private String tooltip;
  private Class type;
  private boolean isLazy = false;

  public static class Builder {

    List<TableColumn> columns = new ArrayList<>();

    public TableColumn.Builder add(int position, String name, String tooltip, Class type, boolean isLazy) {
      columns.add(new TableColumn(position, name, tooltip, type, isLazy));
      return this;
    }

    public TableColumn.Builder add(int position, String name, String tooltip, Class type) {
      return add(position, name, tooltip, type, false);
    }

    public TableColumn[] build() {
      columns.sort(Comparator.comparing(TableColumn::getPosition));
      // verify non null positions
      for(int k = 0; k < columns.size(); k++) {
        assert(columns.get(k).getPosition() == k);
      }
      return columns.toArray(new TableColumn[0]);
    }

  }

  public TableColumn(int position, String name, String tooltip, Class type, boolean isLazy) {
    this(name, tooltip, type);
    this.position = position;
    this.isLazy = isLazy;
  }

  public TableColumn(String name, String tooltip, Class type) {
    this.name = name;
    this.tooltip = tooltip;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public String getTooltip() {
    return tooltip;
  }

  public int  getPosition() {
    return position;
  }

  public Class getType() {
    if (isLazy)
      return LazyData.class;
    return type;
  }

}
