package fr.proline.studio.dam.tasks.data.ptm;

import java.util.ArrayList;
import java.util.List;

public class ComparableList extends ArrayList implements Comparable<List> {
    @Override
    public int compareTo(List o) {
        if (o == null) return -1;
        if ((this == o) || (this.isEmpty() && o.isEmpty())) return 0;
        if (this.isEmpty()) return -1;
        if (o.isEmpty()) return 1;
        int comp = 0;
        int index = 0;
        while (index < Math.min(this.size(), o.size()) && comp == 0) {
            comp = ((Comparable)this.get(index)).compareTo(o.get(index));
            index++;
        }

        return comp == 0 ? ((Comparable)this.size()).compareTo(o.size()) : comp;
    }
}
