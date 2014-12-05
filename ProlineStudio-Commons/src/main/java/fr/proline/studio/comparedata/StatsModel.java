package fr.proline.studio.comparedata;

/**
 *
 * @author JM235353
 */
public class StatsModel implements CompareDataInterface {
    
    private CompareDataInterface m_sourceDataInterface;
    private int m_colSelected;
    
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
        return value == null ? Double.NaN : ((Number)value).doubleValue(); //CBy TODO a revoir ?
    }
    
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
    
}
