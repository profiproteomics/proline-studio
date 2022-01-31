/*
 * Copyright (C) 2019
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
