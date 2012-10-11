package fr.proline.studio.utils;

/**
 * LazyData can wrap any sort of data which will be loaded later.
 */
public class LazyData implements Comparable<LazyData> {

    private Comparable data = null;

    public LazyData() {
    }

    public void setData(Comparable data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    @Override
    public int compareTo(LazyData o) {
        if (data == null) {
            if (o.data == null) {
                return 0;
            } else {
                return -1;
            }
        }
        if (o.data == null) {
            return 1;
        }

        return data.compareTo(o.data);


    }
}