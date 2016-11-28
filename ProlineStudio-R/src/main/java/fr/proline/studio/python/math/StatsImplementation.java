package fr.proline.studio.python.math;


import fr.proline.studio.python.data.Col;
import fr.proline.studio.python.data.ColBooleanData;
import fr.proline.studio.python.data.ColDoubleData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.ExprTableModel;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.python.model.QuantiFilterModel;

import fr.proline.studio.table.LazyData;
import fr.proline.studio.types.LogInfo;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyTuple;


/**
 *
 * @author JM235353
 */
public class StatsImplementation {

    public static ColDoubleData neg(Col values) {

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
                d = -d;
            } else {
                d = Double.NaN;
            }
            resultArray.add(d);

        }

        return new ColDoubleData(values.getTable(), resultArray, "-" + values.getExportColumnName());
    }
    
    public static ColDoubleData log10(Col values) {
        return log(values, true);
    }
    public static ColDoubleData log2(Col values) {
        return log(values, false);
    }
    private static ColDoubleData log(Col values, boolean log10) {

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
                    d = (log10) ? StrictMath.log10(d) : StrictMath.log(d)/LOG2;
                }
            } else {
                d = Double.NaN;
            }
            resultArray.add(d);

            
        }
        
        String logfunction = (log10) ? "log10(" : "log2(";
        return new ColDoubleData(values.getTable(), resultArray, logfunction+values.getExportColumnName()+")");
    }
    private static final double LOG2 = StrictMath.log(2);
    
    public static Table log10(Table sourceTable, PyTuple pcols) {
        return log(sourceTable, pcols, true);
    }

    public static Table log2(Table sourceTable, PyTuple pcols) {
        return log(sourceTable, pcols, false);
    }
    private static Table log(Table sourceTable, PyTuple pcols, boolean log10) {

        ExprTableModel model = new ExprTableModel(sourceTable.getModel());
        Table resTable = new Table(model);
        HashMap<Integer, Col> modifiedColumns = new HashMap<>();
        HashMap<Integer, Object> modifiedColumnsExtraInfo = new HashMap<>();
        
        Object[] objArray = pcols.getArray();
        int nb = objArray.length;
        for (int i = 0; i < nb; i++) {
            ColRef c = ((ColRef) objArray[i]);
            ColDoubleData cLogged = log(c, log10);
            modifiedColumns.put(c.getModelCol(), cLogged);
            modifiedColumnsExtraInfo.put(c.getModelCol(), log10 ? new LogInfo(LogInfo.LogState.LOG10) : new LogInfo(LogInfo.LogState.LOG2));
        }
        
        model.modifyColumnValues(modifiedColumns, modifiedColumnsExtraInfo);

        return resTable;
    }
    
    public static Table log10(Table sourceTable, ColRef column) {
        return log(sourceTable, column, true);
    }

    public static Table log2(Table sourceTable, ColRef column) {
        return log(sourceTable, column, false);
    }
    public static Table log(Table sourceTable, ColRef column, boolean log10) {

        ExprTableModel model = new ExprTableModel(sourceTable.getModel());
        Table resTable = new Table(model);
        HashMap<Integer, Col> modifiedColumns = new HashMap<>();
        HashMap<Integer, Object> modifiedColumnsExtraInfo = new HashMap<>();
        
        ColDoubleData cLogged = log(column, log10);
        modifiedColumns.put(column.getModelCol(), cLogged);
        modifiedColumnsExtraInfo.put(column.getModelCol(), log10 ? new LogInfo(LogInfo.LogState.LOG10) : new LogInfo(LogInfo.LogState.LOG2));
        
        model.modifyColumnValues(modifiedColumns, modifiedColumnsExtraInfo);
        

        return resTable;
    }
    
    public static ColDoubleData ttd(PyTuple p1, PyTuple p2) throws MathException {

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

        return new ColDoubleData(t, ttdArray, null);
    }

    public static ColDoubleData pvalue(PyTuple p1, PyTuple p2) throws MathException {

        Table t = ((ColRef) p1.get(0)).getTable();

        int nbRow = ((ColRef) p1.get(0)).getRowCount();

        ArrayList<Double> resArray = new ArrayList(nbRow);

        for (int row = 0; row < nbRow; row++) {
            DescriptiveStatistics ds1 = _toDescriptiveStatistics(p1, row);
            DescriptiveStatistics ds2 = _toDescriptiveStatistics(p2, row);

            TTest tTest = new TTestImpl();

            // calculate Welch t-test pvalue
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

        return new ColDoubleData(t, resArray, null);
    }

    public static Table quantifilter(PyTuple p, Table t, PyInteger option, PyInteger threshold) throws MathException {
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        return quantifilter(pArray, t, option, threshold);
    }
    
    public static Table quantifilter(PyTuple[] pArray, Table t, PyInteger option, PyInteger threshold) throws MathException {

        ColRef[] cols = StatsUtil.colTupleToColArray(pArray);
        
        int size = pArray.length;
        int[] nbGroups = new int[size];
        int nbTotal = 0;
        for (int i=0;i<size;i++) {
            nbGroups[i] = pArray[i].size();
            nbTotal += nbGroups[i];
            
        }

        int[] groupIndex = new int[nbTotal];
        int[] colsIndex = new int[nbTotal];

        int start = 0;
        int end = 0;
        for (int i = 0; i < size; i++) {
            end += nbGroups[i];
            for (int j = start; j < end; j++) {
                groupIndex[j] = i+1;
                colsIndex[j] = cols[j].getModelCol();
            }
            start = end;
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
    
    public static Table differentialProteins(Col pvalues, Col logFC, PyFloat pvalueThreshold, PyFloat logFCThreshold) throws Exception {
        
        Table t = pvalues.getTable();
        
        ColBooleanData col = differentialProteinsCol(pvalues, logFC, pvalueThreshold, logFCThreshold);
        
        ExprTableModel model = new ExprTableModel(col, null, t.getModel());

        return new Table(model);
    }
    
    private static ColBooleanData differentialProteinsCol(Col pvalues, Col logFCs, PyFloat pvalueLogThreshold, PyFloat logFCThreshold) {

        int nbRow = pvalues.getRowCount();
        
        ArrayList<Boolean> resultArray = new ArrayList<>(nbRow);

        
        
        double pvalueThresholdDouble = StrictMath.pow(10, -pvalueLogThreshold.getValue());
        double test = -StrictMath.log10(pvalueThresholdDouble);
        double logFCThresholdDouble = logFCThreshold.getValue();
        
        for (int i = 0; i < nbRow; i++) {
            
            Object logFC = logFCs.getValueAt(i);
            if (logFC instanceof LazyData) {
                logFC = ((LazyData) logFC).getData();
            }
            double logFCDouble;
            if ((logFC != null) && (logFC instanceof Number)) {
                logFCDouble = ((Number) logFC).doubleValue();
            } else {
                logFCDouble = Double.NaN;
            }
            
            Object pvalue = pvalues.getValueAt(i);
            if (pvalue instanceof LazyData) {
                pvalue = ((LazyData) pvalue).getData();
            }
            double pvalueDouble;
            if ((pvalue != null) && (pvalue instanceof Number)) {
                pvalueDouble = ((Number) pvalue).doubleValue();
            } else {
                pvalueDouble = Double.NaN;
            }
            
            Boolean differentialProtein = ((pvalueDouble<=pvalueThresholdDouble) && ((logFCDouble>=logFCThresholdDouble) || (logFCDouble<=-logFCThresholdDouble)) );

            resultArray.add(differentialProtein);

            
        }

        return new ColBooleanData(pvalues.getTable(), resultArray, "Differential Proteins");
    }

}
