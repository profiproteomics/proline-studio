package fr.proline.studio.utils;

/**
 * LazyData can wrap any sort of data which will be loaded later.
 */
public class LazyData implements Comparable<LazyData> {

    private Comparable m_data = null;

    public LazyData() {
    }

    public void setData(Comparable data) {
        m_data = data;
    }

    public Object getData() {
        return m_data;
    }

    @Override
    public int compareTo(LazyData o) {
        if (m_data == null) {
            if (o.m_data == null) {
                return 0;
            } else {
                return -1;
            }
        }
        if (o.m_data == null) {
            return 1;
        }

        return m_data.compareTo(o.m_data);
    }
    
    @Override
    public String toString() {
        if (m_data == null) 
            return super.toString();
        return m_data.toString();
    }
}