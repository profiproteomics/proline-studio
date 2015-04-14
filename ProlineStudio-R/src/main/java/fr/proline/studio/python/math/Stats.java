package fr.proline.studio.python.math;

import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.table.LazyData;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.python.core.Py;
import org.python.core.PyTuple;
import org.rosuda.REngine.REXPGenericVector;

/**
 *
 * @author JM235353
 */
public class Stats {
    
    public static ColData bbinomial(PyTuple p1, PyTuple p2) throws Exception {
        
        RServerManager serverR = RServerManager.getRServerManager();
        boolean RStarted = serverR.startRProcessWithRetry();
        if (!RStarted) {
            throw Py.RuntimeError("Server R not found");
        }
        
        serverR.connect();

        
        int nbRow = ((ColRef) p1.get(0)).getRowCount();
        
        Object[] objArray1 = p1.getArray();
        int nb1 = objArray1.length;
        Object[] objArray2 = p2.getArray();
        int nb2 = objArray2.length;
        
        ColRef[] cols = new ColRef[nb1+nb2];
        for (int i = 0; i < nb1; i++) {
            cols[i] = ((ColRef) objArray1[i]);
        }
        for (int i = 0; i < nb2; i++) {
            cols[nb1+i] = ((ColRef) objArray2[i]);
        }
        
        Table t = cols[0].getTable();
        
        File tempFile = File.createTempFile("binomial", "csv");
        tempFile.deleteOnExit();
        FileWriter fw = new FileWriter(tempFile);
        
        
        for (int i = 0; i < nbRow; i++) {
            for (int j=0;j<cols.length;j++) {
                ColRef c = cols[j];
                Object o = c.getValueAt(i);
                if (o instanceof LazyData) {
                    o = ((LazyData) o).getData();
                }
                double d;
                if ((o != null) && (o instanceof Number)) {
                    d = ((Number) o).doubleValue();
                    if (d != d) {
                        d = 0; // NaN values
                    }
                } else {
                    d = 0;
                }
                fw.write(String.valueOf(d));
                if (j<cols.length-1) {
                    fw.write(';');
                }
            }
            fw.write('\n');

        }
        
        fw.close();
        
        ColData c = _bbinomialR(t, tempFile, nb1, nb2);
        
        tempFile.delete();
        
        return c;
    }
    
    private static ColData _bbinomialR(Table t, File f, int nbCols1, int nbCols2) throws Exception {
                
        int nbCols = nbCols1+nbCols2;
        
        RServerManager serverR = RServerManager.getRServerManager();

        String path = f.getCanonicalPath().replaceAll("\\\\", "/");
        
        // write file
        
        serverR.parseAndEval("library(ibb)"); // , lib.loc='D:\\LibrairiesR')
        
        
        String cmdReadCSV = "testbinomial<-read.delim('"+path+"',header=F, sep=';')";
        serverR.parseAndEval(cmdReadCSV);

        String cmdBB = "resbinomial<-bb.test(x=testbinomial[,1:"+nbCols+"], tx=colSums(testbinomial[,1:"+nbCols+"]), group=c(rep('C1',"+nbCols1+"),rep('C2',"+nbCols2+")),n.threads=0)";
        REXPGenericVector resVector = (REXPGenericVector) serverR.parseAndEval(cmdBB);

        Object o = resVector.asNativeJavaObject();
        HashMap map = (HashMap) o;
        double[] values = (double[]) map.keySet().toArray()[0];

        ArrayList<Double> resArray = new ArrayList<>(values.length);
        for (int i=0;i<values.length;i++) {
            resArray.add(values[i]);
        }
        
        return new ColData(t, resArray, null);

    }
    
    
    
    
    
    public static ColData ttd(PyTuple p1, PyTuple p2) throws MathException {
        
        
        Table t = ((ColRef) p1.get(0)).getTable();
        
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
                    pvalue = -1.0;
                }
            } catch (IllegalArgumentException ex) {
                pvalue = -1.0;
            } catch (MathException ex) {
                pvalue = -1.0;
            }

            resArray.add(pvalue);
        }

        return new ColData(t, resArray, null);
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
