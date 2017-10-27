package fr.proline.studio.types;


/**
 * Class used to add information to a Table Model in the Data Analyzer
 * This class corresponds to the type of quantitation of a column
 * 
 * @author JM235353
 */
public class QuantitationType {
    
    public static final int ABUNDANCE = 0;
    public static final int RAW_ABUNDANCE = 1;
    public static final int BASIC_SC = 2;
    public static final int SPECIFIC_SC = 3;
    public static final int WEIGHTED_SC = 4;

    
    private static final int SIZE = 5; // <<<< get in sync

    private static QuantitationType[] m_array = null;
    
    private static final String[] m_names = { "Abundance", "Raw Abundance", "Basic SC", "Specific SC", "Weighted SC" };
    
    private final int m_type;
    
    private QuantitationType(int type) {
        m_type = type;
    }
    
    private static void initArray() {
        if (m_array == null) {
            m_array = new QuantitationType[SIZE];
            for (int i=0;i<SIZE;i++) {
                m_array[i] = new QuantitationType(i);
            }
        }
    }
    
    public static QuantitationType getQuantitationType(int type) {
        initArray();
        return m_array[type];
    }
    
    public int getType() {
        return m_type;
    }
    
    public String getName() {
        return m_names[m_type];
    }

    @Override
    public String toString() {
        return getName();
    }
    
}
