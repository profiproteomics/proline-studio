package fr.proline.studio.python.math;

import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.python.core.PyTuple;

/**
 *
 * @author JM235353
 */
public class Stats {
    
    public static ColData ttd(PyTuple p1, PyTuple p2) throws MathException {
        
        
        
        int nbRow = ((ColRef) p1.get(0)).getRowCount();
        
        ArrayList<Double> ttdArray = new ArrayList(nbRow);
        
        for (int row=0;row<nbRow;row++) {
            DescriptiveStatistics ds1 = _toDescriptiveStatistics(p1, row);
            DescriptiveStatistics ds2 = _toDescriptiveStatistics(p2, row);


            // calculate ttd
            double m1 = ds1.getMean();
            double m2 = ds2.getMean();
            double ttd = (Double.isNaN(m1) ? 0.0 : m1) - (Double.isNaN(m2) ? 0.0 : m2);
            ttd = Math.max(ttd, -15.0);
            ttd = Math.min(ttd, 15.0);
            ttdArray.add(ttd);
        }
        
        return new ColData(ttdArray, null);
    }
    
    public static ColData pvalue(PyTuple p1, PyTuple p2) throws MathException {

        int nbRow = ((ColRef) p1.get(0)).getRowCount();

        ArrayList<Double> resArray = new ArrayList(nbRow);

        for (int row = 0; row < nbRow; row++) {
            DescriptiveStatistics ds1 = _toDescriptiveStatistics(p1, row);
            DescriptiveStatistics ds2 = _toDescriptiveStatistics(p2, row);

            TTest tTest = new TTestImpl();

            // calculate pvalue
            double pvalue;
            try {
                pvalue = -Math.log10(tTest.tTest(ds1.getValues(), ds2.getValues()));
                if (Double.isInfinite(pvalue) || Double.isNaN(pvalue)) {
                    pvalue = -1.0;
                }
            } catch (IllegalArgumentException ex) {
                pvalue = -1.0;
            } catch (MathException ex) {
                pvalue = -1.0;
            }

            resArray.add(pvalue);
        }

        return new ColData(resArray, null);
    }
    
    private static DescriptiveStatistics _toDescriptiveStatistics(PyTuple p, int row) {
        final double LOG2 = Math.log(2.0d);

        DescriptiveStatistics m = new DescriptiveStatistics();

        Object[] objArray = p.getArray();
        int nb = objArray.length;
        for (int i = 0; i < nb; i++) {
            ColRef c = ((ColRef) objArray[i]);
            Object o = c.getValueAt(row);
            if (o instanceof LazyData) {
                o = ((LazyData) o).getData();
            }
            if ((o != null) && (o instanceof Number)) {
                double d = ((Number) o).doubleValue();
                if ((d == d) && (d > 0.0f)) {
                    m.addValue(Math.log(d) / LOG2);
                }
            }

        }
        
        return m;
    }

}
