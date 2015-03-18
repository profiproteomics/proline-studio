/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.proline.mzscope.ui;

import fr.profi.mzdb.model.Feature;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * table model for features / peaks
 * @author CB205360
 */
public class FeaturesTableModel extends AbstractTableModel {
    
   public static enum Columns { MZ_COL, ET_COL, DURATION_COL, APEX_INT_COL, AREA_COL, SCAN_COUNT_COL };

    private List<Feature> features = new ArrayList<Feature>();

    public void setFeatures(List<Feature> features) {
        this.features = features;
        fireTableDataChanged();
    }

   @Override
    public int getRowCount() {
        return features.size();
    }

   @Override
    public int getColumnCount() {
        return Columns.values().length;
    }

   @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(Columns.values()[columnIndex]) {
            case MZ_COL: return features.get(rowIndex).getMz();
            case ET_COL: return features.get(rowIndex).getElutionTime()/60.0;
            case DURATION_COL: return features.get(rowIndex).getBasePeakel().calcDuration()/60.0;
            case APEX_INT_COL: return features.get(rowIndex).getBasePeakel().getApexIntensity();
            case AREA_COL: return features.get(rowIndex).getArea();
            case SCAN_COUNT_COL: return features.get(rowIndex).getMs1Count();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        switch(Columns.values()[column]) {
            case MZ_COL: return "m/z";
            case ET_COL: return "elution";
            case DURATION_COL: return "duration";
            case APEX_INT_COL: return "apex Int.";
            case AREA_COL: return "area";
            case SCAN_COUNT_COL: return "MS count";
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
         switch(Columns.values()[columnIndex]) {
            case MZ_COL: return Double.class;
            case ET_COL: return Float.class;
            case DURATION_COL: return Float.class;
            case APEX_INT_COL: return Float.class;
            case AREA_COL: return Float.class;
            case SCAN_COUNT_COL: return Integer.class;
        }
        return Object.class;
    }
    
    
    
}
