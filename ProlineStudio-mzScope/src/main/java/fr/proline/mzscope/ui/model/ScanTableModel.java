package fr.proline.mzscope.ui.model;

import fr.proline.mzscope.model.Spectrum;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.graphics.PlotInformation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 * data for scan
 * @author MB243701
 */
public class ScanTableModel extends AbstractTableModel implements CompareDataInterface{
    private static final String[] m_columnNames = {"Masses", "Intensities", "Retention Time", "MS Level"};
    
    public static final int COLTYPE_SCAN_MASS = 0;
    public static final int COLTYPE_SCAN_INTENSITIES = 1;
    public static final int COLTYPE_SCAN_RETENTION_TIME = 2;
    public static final int COLTYPE_SCAN_MSLEVEL = 3;
    
    private Spectrum scan;
    
    private String m_modelName;
    
    private Color scanColor;
    
    public ScanTableModel(Spectrum scan) {
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

    @Override
    public long row2UniqueId(int rowIndex) {
        return rowIndex;
    }
    
    @Override
    public int uniqueId2Row(long id) {
        return (int) id;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public Object getValue(Class c) {
        return null;
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addSingleValue(Object v) {
        return;
    }

    @Override
    public Object getSingleValue(Class c) {
        return null;
    }
}
