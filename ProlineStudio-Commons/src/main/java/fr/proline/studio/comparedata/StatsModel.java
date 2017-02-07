package fr.proline.studio.comparedata;

import fr.proline.studio.graphics.PlotInformation;
import java.util.ArrayList;
import java.util.Map;

/**
 * Model used to give mean, variance... used for the histogram display
 * @author JM235353
 */
public class StatsModel implements CompareDataInterface {

    private final CompareDataInterface m_src;
    private final int m_colSelected;

    private String m_modelName;

    public StatsModel(CompareDataInterface sourceDataInterface, int colSelected) {
        m_src = sourceDataInterface;
        m_colSelected = colSelected;
    }

    @Override
    public int getRowCount() {
        return m_src.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_src.getDataColumnIdentifier(m_colSelected);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return m_src.getDataColumnClass(m_colSelected);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return m_src.getDataValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        return null;
    }

    @Override
    public void setName(String name) {
        m_modelName = name;
    }

    @Override
    public String getName() {
        return m_modelName;
    }

    public double getValue(int rowIndex) {
        Object value = getDataValueAt(rowIndex, m_colSelected);
        return value == null ? Double.NaN : ((Number) value).doubleValue(); //CBy TODO a revoir ?
    }


    public double sumNaN() {

        double s = 0;
        int nb = getRowCount();
        for (int i = 0; i < nb; i++) {
            double d = getValue(i);
            if (d != d) {
                continue;
            }
            s += d;
        }

        return s;
    }

    public double meanNaN() {
        int nb = getRowCount();
        int countWithoutNaN = 0;
        for (int i = 0; i < nb; i++) {
            double d = getValue(i);
            if (d != d) {
                continue;
            }
            countWithoutNaN++;
        }
        return sumNaN() / countWithoutNaN;
    }

    public double varianceNaN() {
        double v = 0;
        double mean = meanNaN();
        int nb = getRowCount();
        for (int i = 0; i < nb; i++) {
            double d = getValue(i);
            if (d != d) {
                continue;
            }
            double diff = d - mean;
            v += diff * diff;
        }
        return v / nb;
    }

    

    public double standardDeviationNaN() {
        return Math.sqrt(varianceNaN());
    }

    @Override
    public int getInfoColumn() {
        return m_src.getInfoColumn();
    }
    
    @Override
    public Map<String, Object> getExternalData() {
        return m_src.getExternalData();
    }
    
    
    @Override
    public PlotInformation getPlotInformation() {
        return m_src.getPlotInformation();
    }

    @Override
    public long row2UniqueId(int rowIndex) {
        return m_src.row2UniqueId(rowIndex);
    }
    
    @Override
    public int uniqueId2Row(long id) {
        return m_src.uniqueId2Row(id);
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return m_src.getExtraDataTypes();
    }

    @Override
    public Object getValue(Class c) {
        return m_src.getValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return m_src.getRowValue(c, row);
    }

    @Override
    public Object getColValue(Class c, int col) {
        return m_src.getColValue(c, col);
    }

    @Override
    public void addSingleValue(Object v) {
        // not used
    }

    @Override
    public Object getSingleValue(Class c) {
        return m_src.getSingleValue(c);
    }
    
}
