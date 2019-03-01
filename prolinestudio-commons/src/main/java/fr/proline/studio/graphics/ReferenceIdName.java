package fr.proline.studio.graphics;

/**
 *
 * @author JM235353
 */
public class ReferenceIdName {

    private final String m_name;
    private final int m_columnIndex;

    public ReferenceIdName(String name, int columnIndex) {
        m_name = name;
        m_columnIndex = columnIndex;
    }

    public int getColumnIndex() {
        return m_columnIndex;
    }

    @Override
    public String toString() {
        return m_name;
    }
}
