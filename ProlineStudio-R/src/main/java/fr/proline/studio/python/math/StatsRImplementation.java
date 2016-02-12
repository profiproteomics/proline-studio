package fr.proline.studio.python.math;

import fr.proline.studio.python.data.Col;
import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.PythonImage;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.table.LazyData;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.python.core.Py;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.rosuda.REngine.REXPGenericVector;

/**
 *
 * @author JM235353
 */
public class StatsRImplementation {

    private static final String LIB_PROSTAR = "DAPAR"; 
    private static final String LIB_CP4P = "cp4p"; 
    
    public static PyObject adjustP(Col pvaluesCol, String pi0Parameter, PyFloat alpha, PyInteger nbins, PyFloat pz) throws Exception {

        RServerManager serverR = RServerManager.getRServerManager();
        boolean RStarted = serverR.startRProcessWithRetry();
        if (!RStarted) {
            throw Py.RuntimeError("Server R not found");
        }
        
        serverR.connect();

        
        int nbRow = pvaluesCol.getRowCount();

        
        Table t = pvaluesCol.getTable();
        
        File tempFile = File.createTempFile("adjustp", "csv");
        tempFile.deleteOnExit();
        FileWriter fw = new FileWriter(tempFile);
        
        
        for (int i = 0; i < nbRow; i++) {
            Object o = pvaluesCol.getValueAt(i);
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
            fw.write('\n');

        }
        
        fw.close();
        
        ColData c = _adjustP(t, tempFile, pi0Parameter, alpha, nbins, pz);
        
        tempFile.delete();
        
        return c;
   
    }
    
    private static ColData _adjustP(Table t, File f, String pi0Parameter, PyFloat alpha, PyInteger nbins, PyFloat pz) throws Exception {

        RServerManager serverR = RServerManager.getRServerManager();

        String path = f.getCanonicalPath().replaceAll("\\\\", "/");
        
        // write file
        
        serverR.parseAndEval("library("+LIB_CP4P+")"); 
        
        
        String cmdReadCSV = "testadjustp<-read.delim('"+path+"',header=F, sep=';')";
        serverR.parseAndEval(cmdReadCSV);

        String cmdBB1 = "resadjustp<-adjust.p(p=testadjustp[,1:1], "+pi0Parameter+", alpha="+alpha.toString()+", nbins="+nbins.toString()+", pz="+pz.toString()+")";
        String cmdBB2 = "adjp<-resadjustp$adjp";
        serverR.parseAndEval(cmdBB1);
        REXPGenericVector resVector = (REXPGenericVector) serverR.parseAndEval(cmdBB2);

        
        
        Object o = resVector.asNativeJavaObject();
        HashMap map = (HashMap) o;
        
        double[] values = null;
        Set keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object value = map.get(key);
            if (value.toString().compareTo("adjusted.p") == 0) {
                values = (double[]) key;
                break;
            }
        }
        if (values == null) {
            throw new Exception("Data not correctly parsed");
        }

        ArrayList<Double> resArray = new ArrayList<>(values.length);
        for (int i=0;i<values.length;i++) {
            resArray.add(values[i]);
        }
        
        return new ColData(t, resArray, null);

    }
    
    public static ColData bbinomial(PyTuple p1, PyTuple p2, PyTuple p3) throws Exception {

        // needs R for this calculation
        StatsUtil.startRSever();

        // PyTuple to Col Array
        ColRef[] cols = (p3!=null) ? StatsUtil.colTupleToColArray(p1, p2, p3) : StatsUtil.colTupleToColArray(p1, p2);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, false);

        // do the calculation
        Table t = cols[0].getTable();
        int nb1 = p1.size();
        int nb2 = p2.size();
        int nb3 = (p3 == null) ? 0 : p3.size();
        ColData c = _bbinomialR(t, matrixTempFile, nb1, nb2, nb3);

        // delete temp files
        matrixTempFile.delete();

        return c;
    }

    private static ColData _bbinomialR(Table t, File matrixTempFile, int nbCols1, int nbCols2, int nbCols3) throws Exception {

        // load library to do the calculation
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library(ibb)");

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, false);

        // do calculation
        int nbCols = nbCols1 + nbCols2 + nbCols3;
        String forCol3 = "";
        if (nbCols3>0) {
            forCol3 = ",rep('C3'," + nbCols3 + ")";
        }
        String cmdBB = "resbinomial<-bb.test(x=" + StatsUtil.MATRIX_VARIABLE + "[,1:" + nbCols + "], tx=colSums(" + StatsUtil.MATRIX_VARIABLE + "[,1:" + nbCols + "]), group=c(rep('C1'," + nbCols1 + "),rep('C2'," + nbCols2 + ")"+forCol3+"),n.threads=0)";
        REXPGenericVector resVector = (REXPGenericVector) serverR.parseAndEval(cmdBB);

        Object o = resVector.asNativeJavaObject();
        HashMap map = (HashMap) o;
        double[] values = (double[]) map.keySet().toArray()[0];

        ArrayList<Double> resArray = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            resArray.add(values[i]);
        }

        return new ColData(t, resArray, null);

    }
    
    public static PythonImage boxPlot(PyTuple p1, PyTuple p2, PyTuple p3, PyTuple labels) throws Exception {

        // needs R for this calculation
        StatsUtil.startRSever();

        // PyTuple to Col Array
        ColRef[] cols = (p3!=null) ? StatsUtil.colTupleToColArray(p1, p2, p3) : StatsUtil.colTupleToColArray(p1, p2);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, true);

        // Create a temp file for the image
        File imageTempFile = StatsUtil.createImageTempFile();

        // do the boxplot in R
        PythonImage image = _boxPlotR(matrixTempFile, imageTempFile, StatsUtil.colNamesToTuple(cols), labels);

        // delete temp files
        matrixTempFile.delete();
        imageTempFile.delete();

        return image;

    }

    private static PythonImage _boxPlotR(File matrixTempFile, File imageTempFile, PyTuple columnsName, PyTuple labels) throws Exception {

        // load library to do the calculation
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library("+LIB_PROSTAR+")"); 

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, true);


        // create image
        return StatsUtil.createImage(imageTempFile, "boxPlotD(" + StatsUtil.MATRIX_VARIABLE + ",dataForXAxis=data.frame(Label="+StatsUtil.stringTupleToRVector(columnsName)+"),labels="+StatsUtil.stringTupleToRVector(labels)+")");

    }
    
    public static PyObject calibrationPlot(Col pvaluesCol, PyString pi0Method, PyInteger nbins, PyFloat pz) throws Exception {

        // needs R for this calculation
        StatsUtil.startRSever();

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnToMatrixTempFile(pvaluesCol);

        // Create a temp file for the image
        File imageTempFile = StatsUtil.createImageTempFile();

        // do the calibration plot in R
        PythonImage image = _calibrationPlot(matrixTempFile, imageTempFile, pi0Method, nbins, pz);
        
        // delete temp files
        matrixTempFile.delete();
        imageTempFile.delete();
        
        return image;
   
    }
 
    public static PythonImage _calibrationPlot(File matrixTempFile, File imageTempFile, PyString pi0Method, PyInteger nbins, PyFloat pz) throws Exception {

        // load library to do the calculation
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library("+LIB_CP4P+")"); 
    
        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, false);

        // create image
        String pi0Parameter = "pi0.method=\""+pi0Method.toString()+"\"";
        String cmd = "calibration.plot(p="+StatsUtil.MATRIX_VARIABLE+"[,1:1], "+pi0Parameter+", nbins="+nbins.toString()+", pz="+pz.toString()+")";
        return StatsUtil.createImage(imageTempFile, cmd);

    }
    
    public static PythonImage densityPlot(PyTuple p1, PyTuple p2, PyTuple p3, PyTuple labels) throws Exception {

        // needs R for this calculation
        StatsUtil.startRSever();

        // PyTuple to Col Array
        ColRef[] cols = (p3!=null) ? StatsUtil.colTupleToColArray(p1, p2, p3) : StatsUtil.colTupleToColArray(p1, p2);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, true);

        // Create a temp file for the image
        File imageTempFile = StatsUtil.createImageTempFile();

        // do the densityPlot in R
        PythonImage image = _densityPlotR(matrixTempFile, imageTempFile, labels);

        // delete temp files
        matrixTempFile.delete();
        imageTempFile.delete();

        return image;

    }

    private static PythonImage _densityPlotR(File matrixTempFile, File imageTempFile, PyTuple labels) throws Exception {

        // load library to do the calculation
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library("+LIB_PROSTAR+")"); 

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, true);

        // create image
        return StatsUtil.createImage(imageTempFile, "densityPlotD(" + StatsUtil.MATRIX_VARIABLE +",labels="+StatsUtil.stringTupleToRVector(labels)+")");


    }

    
    public static PythonImage varianceDistPlot(PyTuple p1, PyTuple p2, PyTuple p3, PyTuple labels) throws Exception {

        // needs R for this calculation
        StatsUtil.startRSever();

        // PyTuple to Col Array
        ColRef[] cols = (p3!=null) ? StatsUtil.colTupleToColArray(p1, p2, p3) : StatsUtil.colTupleToColArray(p1, p2);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, true);

        // Create a temp file for the image
        File imageTempFile = StatsUtil.createImageTempFile();

        // do the varianceDist Plot in R
        PythonImage image = _varianceDistPlot(matrixTempFile, imageTempFile, StatsUtil.colNamesToTuple(cols), labels);

        // delete temp files
        matrixTempFile.delete();
        imageTempFile.delete();

        return image;

    }
 
    

    private static PythonImage _varianceDistPlot(File matrixTempFile, File imageTempFile, PyTuple columnsName, PyTuple labels) throws Exception {

        // load library to do the calculation
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library("+LIB_PROSTAR+")"); 

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, true);

        // create image
        return StatsUtil.createImage(imageTempFile, "varianceDistD(" + StatsUtil.MATRIX_VARIABLE + ",labels="+StatsUtil.stringTupleToRVector(labels)+")");

    }

    
}
