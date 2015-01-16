package fr.proline.studio.comparedata;

import java.awt.Color;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class StatsModel implements CompareDataInterface {

    private final CompareDataInterface m_sourceDataInterface;
    private final int m_colSelected;

    private String m_modelName;

    public StatsModel(CompareDataInterface sourceDataInterface, int colSelected) {
        m_sourceDataInterface = sourceDataInterface;
        m_colSelected = colSelected;
    }

    @Override
    public int getRowCount() {
        return m_sourceDataInterface.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_sourceDataInterface.getDataColumnIdentifier(m_colSelected);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return m_sourceDataInterface.getDataColumnClass(m_colSelected);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return m_sourceDataInterface.getDataValueAt(rowIndex, m_colSelected);
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

    public double getValue(int columnIndex) {
        Object value = getDataValueAt(columnIndex, 0);
        return value == null ? Double.NaN : ((Number) value).doubleValue(); //CBy TODO a revoir ?
    }

    /*
    public double sum() {

        double s = 0;
        int nb = getRowCount();
        for (int i = 0; i < nb; i++) {
            s += getValue(i);
        }

        return s;
    }

    public double mean() {
        return sum() / getRowCount();
    }

    public double variance() {

        double v = 0;
        double mean = mean();
        int nb = getRowCount();
        for (int i = 0; i < nb; i++) {
            double diff = getValue(i) - mean;
            v += diff * diff;
        }
        return v / nb;
    }
    
    public double standardDeviation() {
        return Math.sqrt(variance());
    }
    
    */

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
        return m_sourceDataInterface.getInfoColumn();
    }
    
    @Override
    public Map<String, Object> getExternalData() {
        return m_sourceDataInterface.getExternalData();
    }
    
    
    @Override
    public Color getPlotColor() {
        return m_sourceDataInterface.getPlotColor();
    }
    
    @Override
    public String getPlotTitle() {
        return m_sourceDataInterface.getPlotTitle();
    }
    
}
