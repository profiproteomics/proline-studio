/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Scan;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.graphics.PlotInformation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 * data for scan
 * @author MB243701
 */
public class ScanModel extends AbstractTableModel implements CrossSelectionInterface, CompareDataInterface{
    private static final String[] m_columnNames = {"Masses", "Intensities", "Retention Time", "MS Level"};
    
    public static final int COLTYPE_SCAN_MASS = 0;
    public static final int COLTYPE_SCAN_INTENSITIES = 1;
    public static final int COLTYPE_SCAN_RETENTION_TIME = 2;
    public static final int COLTYPE_SCAN_MSLEVEL = 3;
    
    private Scan scan;
    
    private String m_modelName;
    
    private Color scanColor;
    
    public ScanModel(Scan scan) {
        this.scan = scan;
    }
    
    @Override
    public int getRowCount() {
        return scan.getMasses().length;
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }
    
    @Override
    public String getColumnName(int col) {
        return m_columnNames[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
       
        switch (columnIndex) {
            case COLTYPE_SCAN_MASS: {
                return scan.getMasses()[rowIndex];
            }
            case COLTYPE_SCAN_INTENSITIES:{
                return scan.getIntensities()[rowIndex];
            }
            case COLTYPE_SCAN_RETENTION_TIME:{
                return scan.getRetentionTime() / 60;
            }
            case COLTYPE_SCAN_MSLEVEL:{
                return scan.getMsLevel();
            }
        }
        return null; // should never happen
    }

    @Override
    public void select(ArrayList<Integer> rows) {
        
    }

    @Override
    public ArrayList<Integer> getSelection() {
        return new ArrayList();
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_SCAN_MASS: {
                return Double.class;
            }
            case COLTYPE_SCAN_INTENSITIES:{
                return Float.class;
            }
            case COLTYPE_SCAN_RETENTION_TIME:{
                return Float.class;
            }
            case COLTYPE_SCAN_MSLEVEL:{
                return Integer.class;
            }
        }
        return null; // should never happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return getValueAt(rowIndex, columnIndex);
    }

   @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_SCAN_MASS};
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_SCAN_RETENTION_TIME;
    }

    @Override
    public void setName(String name) {
        m_modelName = name;
    }

    @Override
    public String getName() {
        return m_modelName;
    }

    @Override
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
       PlotInformation plotInformation = new PlotInformation();
       plotInformation.setPlotColor(scanColor);
        return plotInformation;
    }
    
    public void setColor(Color c){
        this.scanColor = c;
    }
}
