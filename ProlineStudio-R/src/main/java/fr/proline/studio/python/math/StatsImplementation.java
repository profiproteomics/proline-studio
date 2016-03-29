package fr.proline.studio.python.math;


import fr.proline.studio.python.data.Col;
import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.ExprTableModel;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.model.QuantiFilterModel;

import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyTuple;


/**
 *
 * @author JM235353
 */
public class StatsImplementation {

    
    public static ColData log(Col values) {

        int nbRow = values.getRowCount();
        
        ArrayList<Double> resultArray = new ArrayList<>(nbRow);

        for (int i = 0; i < nbRow; i++) {
            Object o = values.getValueAt(i);
            if (o instanceof LazyData) {
                o = ((LazyData) o).getData();
            }
            double d;
            if ((o != null) && (o instanceof Number)) {
                d = ((Number) o).doubleValue();
                if ((d != d) || (d<=0)) {
                    d =  Double.NaN;
                } else {
                    d = StrictMath.log(d);
                }
            } else {
                d = Double.NaN;
            }
            resultArray.add(d);

            
        }
        
        return new ColData(values.getTable(), resultArray, "log("+values.getExportColumnName()+")");
    }
    
    public static Table log(Table sourceTable, PyTuple pcols) {

        ExprTableModel model = new ExprTableModel(sourceTable.getModel());
        Table resTable = new Table(model);
        HashMap<Integer, Col> modifiedColumns = new HashMap<>();
        
        Object[] objArray = pcols.getArray();
        int nb = objArray.length;
        for (int i = 0; i < nb; i++) {
            ColRef c = ((ColRef) objArray[i]);
            ColData cLogged = log(c);
            modifiedColumns.put(c.getModelCol(), cLogged);
        }
        
        model.modifyColumnValues(modifiedColumns);

        return resTable;
    }
    
    public static Table log(Table sourceTable, ColRef column) {

        ExprTableModel model = new ExprTableModel(sourceTable.getModel());
        Table resTable = new Table(model);
        HashMap<Integer, Col> modifiedColumns = new HashMap<>();
        
        ColData cLogged = log(column);
        modifiedColumns.put(column.getModelCol(), cLogged);

        
        model.modifyColumnValues(modifiedColumns);

        return resTable;
    }
    
    public static ColData ttd(PyTuple p1, PyTuple p2) throws MathException {

        Table t = ((ColRef) p1.get(0)).getTable();

        int nbRow = ((ColRef) p1.get(0)).getRowCount();

        ArrayList<Double> ttdArray = new ArrayList(nbRow);

        for (int row = 0; row < nbRow; row++) {
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

        return new ColData(t, ttdArray, null);
    }

    public static ColData pvalue(PyTuple p1, PyTuple p2) throws MathException {

        Table t = ((ColRef) p1.get(0)).getTable();

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
                    pvalue = Double.NaN;
                } else {
                    // avoid -0.0
                    pvalue += 0.0;
                }
            } catch (IllegalArgumentException ex) {
                pvalue = Double.NaN;
            } catch (MathException ex) {
                pvalue = Double.NaN;
            }

            resArray.add(pvalue);
        }

        return new ColData(t, resArray, null);
    }

    public static Table quantifilter(PyTuple p1, PyTuple p2, PyTuple p3, Table t, PyInteger option, PyInteger threshold) throws MathException {

        ColRef[] cols = (p3!=null) ? StatsUtil.colTupleToColArray(p1, p2, p3) : StatsUtil.colTupleToColArray(p1, p2);
        
        int nbGroup1 = p1.size();
        int nbGroup2 = p2.size();
        int nbGroup3 = (p3 == null) ? 0 : p3.size();
        int nbTotal = nbGroup1+nbGroup2+nbGroup3;
        int[] groupIndex = new int[nbTotal];
        int[] colsIndex = new int[nbTotal];
        for (int i=0;i<nbGroup1;i++) {
            groupIndex[i] = 1;
            colsIndex[i] = cols[i].getModelCol();
        }
        for (int i=nbGroup1;i<nbGroup1+nbGroup2;i++) {
            groupIndex[i] = 2;
            colsIndex[i] = cols[i].getModelCol();
        }
        for (int i=nbGroup1+nbGroup2;i<nbGroup1+nbGroup2+nbGroup3;i++) {
            groupIndex[i] = 3;
            colsIndex[i] = cols[i].getModelCol();
        }
        

        
        QuantiFilterModel quantiFilterModelModel = new QuantiFilterModel(t.getModel(), colsIndex, groupIndex, option.getValue(), threshold.getValue());
        quantiFilterModelModel.filter();
        return new Table(quantiFilterModelModel);
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
