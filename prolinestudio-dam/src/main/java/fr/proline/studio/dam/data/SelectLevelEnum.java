package fr.proline.studio.dam.data;

import java.util.HashMap;

public enum SelectLevelEnum {
  DESELECTED_MANUAL(0, "Invalidated manually"),
  DESELECTED_AUTO(1, "Invalidated automatically"),
  SELECTED_AUTO(2, "Validated automatically"),
  SELECTED_MANUAL(3, "Validated Manual"),
  UNKNOWN(-1, "Invalid (not quantified)"),
  RESET_AUTO(-2, "Reset auto");

  private int _intValue;
  private String _description;
  private static HashMap<Integer, SelectLevelEnum> map = new HashMap<>();

  SelectLevelEnum(int value, String description) {
    this._intValue = value;
    this._description = description;
  }

  static {
    for (SelectLevelEnum status : SelectLevelEnum.values()) {
      map.put(status._intValue, status);
    }
  }

  public boolean isSelected() {
    return this.equals(SELECTED_AUTO) || this.equals(SELECTED_MANUAL);
  }

  public boolean isDeselected() {
    return this.equals(DESELECTED_AUTO) || this.equals(DESELECTED_MANUAL);
  }

  public int getIntValue() {
    return _intValue;
  }

  public String getDescription() {
    return _description;
  }

  public static SelectLevelEnum valueOf(int status) {
    return map.get(status);
  }

}
