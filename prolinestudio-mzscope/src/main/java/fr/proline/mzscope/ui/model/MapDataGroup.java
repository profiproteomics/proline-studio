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
package fr.proline.mzscope.ui.model;

import fr.proline.studio.table.DataGroup;
import java.awt.Color;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author CB205360
 */
public class MapDataGroup extends DataGroup {

    private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###,###.0");
    private static DecimalFormat SCIENTIFIC_FORMAT = new DecimalFormat("0.00E0");
    private static DecimalFormat PRECISION_FORMAT = new DecimalFormat("###,###,##0.0000");
    private static DateFormat MEDIUM_DATEFORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
    
    private List<List<GroupObject>> values;
    private List<GroupObject> rows;
    private Color color;

    
    public MapDataGroup(List<Map<String, Object>> maps, Color color, String name, int rowStart) {
        super(name, rowStart);
        rows = extractKeys(maps);
        values = new ArrayList<>();
        for (GroupObject row : rows) {
            List<GroupObject> rowValues = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                Object value = map.get(row.stringForRendering());
                // convert Object value to String
                rowValues.add(new GroupObject(convertToString(value), this));
            }
            values.add(rowValues);
        }
        this.color = color;
    }

    private List<GroupObject> extractKeys(List<Map<String, Object>> maps) {
        List<GroupObject> keys = new ArrayList<>();
        Set<String> dictionnary = new HashSet<String>();
        for (Map<String, Object> map : maps) {
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (!dictionnary.contains(e.getKey())) {
                    keys.add(new GroupObject(e.getKey(), this));
                    dictionnary.add(e.getKey());
                }
            }
        }
        
        return keys;
    }
        
    @Override
    public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {
        return values.get(rowIndex).get(columnIndex);
    }

    @Override
    public GroupObject getGroupNameAt(int rowIndex) {
        return rows.get(rowIndex);
    }

    @Override
    public Color getGroupColor(int row) {
        return color;
    }

    @Override
    public int getRowCountImpl() {
        return rows.size();
    }

    private static String convertToString(Object value) {
        if (value == null) return "";
        if (String.class.isAssignableFrom(value.getClass())) return (String) value;
        if (Integer.class.isAssignableFrom(value.getClass())) return String.valueOf(((Integer)value));
        if (Number.class.isAssignableFrom(value.getClass())) {
            double v = ((Number)value).doubleValue();
            DecimalFormat format = DECIMAL_FORMAT;
            if (v <= 1.0) {
                format = PRECISION_FORMAT;
            } else if (v > 1e7) {
                format = SCIENTIFIC_FORMAT;
            }
            return format.format(v);
        }
        
        if (Date.class.isAssignableFrom(value.getClass())) return MEDIUM_DATEFORMAT.format((Date)value);
        
        return value.toString();
    }
    
}
