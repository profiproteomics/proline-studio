package fr.proline.studio.python.math;

import fr.proline.studio.graphics.PlotType;
import static fr.proline.studio.graphics.PlotType.SCATTER_PLOT;
import fr.proline.studio.python.data.Col;
import fr.proline.studio.python.data.ColDoubleData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.model.ExprTableModel;
import fr.proline.studio.python.data.PythonImage;
import fr.proline.studio.python.data.Table;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.types.LogInfo;
import fr.proline.studio.types.LogRatio;
import fr.proline.studio.types.PValue;
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
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.RList;

/**
 *
 * Implementation with a call to R Server of some statistics calculations
 * 
 * @author JM235353
 */
public class StatsRImplementation {

    private static final String LIB_PROSTAR = "DAPAR";
    private static final String LIB_CP4P = "cp4p";
    private static final double LOG2 = StrictMath.log(2);
    
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
            double d = asDoubleValue(pvaluesCol, i);
            fw.write(String.valueOf(d));
            fw.write('\n');

        }

        fw.close();

        ColDoubleData c = _adjustP(t, tempFile, pi0Parameter, alpha, nbins, pz);

        tempFile.delete();

        return c;

    }

    private static double asDoubleValue(Col pvaluesCol, int i) {
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
        return d;
    }

    private static ColDoubleData _adjustP(Table t, File f, String pi0Parameter, PyFloat alpha, PyInteger nbins, PyFloat pz) throws Exception {

        RServerManager serverR = RServerManager.getRServerManager();

        String path = f.getCanonicalPath().replaceAll("\\\\", "/");

        // write file
        serverR.parseAndEval("library(" + LIB_CP4P + ")");

        String cmdReadCSV = "testadjustp<-read.delim('" + path + "',header=F, sep=';')";
        serverR.parseAndEval(cmdReadCSV);

        String cmdBB1 = "resadjustp<-adjust.p(p=testadjustp[,1:1], " + pi0Parameter + ", alpha=" + alpha.toString() + ", nbins=" + nbins.toString() + ", pz=" + pz.toString() + ")";
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
        for (int i = 0; i < values.length; i++) {
            resArray.add(values[i]);
        }

        return new ColDoubleData(t, resArray, null);

    }

    public static Table bbinomial(PyTuple p) throws Exception {

        // needs R for this calculation
        StatsUtil.startRServer();

        // PyTuple to Col Array
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        ColRef[] cols = StatsUtil.colTupleToColArray(pArray);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, false, false, false);

        // do the calculation
        Table t = cols[0].getTable();
        int nb1 = pArray[0].size();
        int nb2 = pArray[1].size();
        int nb3 = (pArray.length<=2) ? 0 : pArray[2].size();
        
        Table resTable = _bbinomialR(t, matrixTempFile, nb1, nb2, nb3, pArray, cols);
        
        // delete temp files
        matrixTempFile.delete();
        

        return resTable;
    }

    private static Table _bbinomialR(Table t, File matrixTempFile, int nbCols1, int nbCols2, int nbCols3, PyTuple[] pArray, ColRef[] cols) throws Exception {
        
        final boolean hasBest = (pArray.length<=2); // bbinomial on two groups
        final int colPValue = t.getModel().getColumnCount() + 1;
        final int colLogFC = colPValue + 1;
        ExprTableModel model = new ExprTableModel(t.getModel()) {
            @Override
            public int[] getBestColIndex(PlotType plotType) {
                if ((plotType == SCATTER_PLOT) && (hasBest)) {

                    int[] cols = new int[2];
                    cols[0] = colLogFC;
                    cols[1] = colPValue;
                    return cols;
                }
                return super.getBestColIndex(plotType);
            }

            
            @Override
            public PlotType getBestPlotType() {
                return PlotType.SCATTER_PLOT;
            }
        };
        
        Table resTable = new Table(model);
        
        // load library to do the calculation
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library(ibb)");

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, false);

        // do calculation
        int nbCols = nbCols1 + nbCols2 + nbCols3;
        String forCol3 = "";
        if (nbCols3 > 0) {
            forCol3 = ",rep('C3'," + nbCols3 + ")";
        }
        String cmdBB = "resbinomial<-bb.test(x=" + StatsUtil.MATRIX_VARIABLE + "[,1:" + nbCols + "], tx=colSums(" + StatsUtil.MATRIX_VARIABLE + "[,1:" + nbCols + "]), group=c(rep('C1'," + nbCols1 + "),rep('C2'," + nbCols2 + ")" + forCol3 + "),n.threads=-2)";
        REXPGenericVector resVector = (REXPGenericVector) serverR.parseAndEval(cmdBB);

        Object o = resVector.asNativeJavaObject();
        HashMap map = (HashMap) o;
        double[] values = (double[]) map.keySet().toArray()[0];

        int nbRows = values.length;

        ArrayList<Double> valuesForCol = new ArrayList<>(nbRows);

        for (int i = 0; i < nbRows; i++) {
            valuesForCol.add(values[i]);
        }

        String colName = "bbinomial PValue";
        ColDoubleData pvalueCol = new ColDoubleData(resTable, valuesForCol, colName);
        ColDoubleData log10PvalueCol = StatsImplementation.log10(pvalueCol);
        ColDoubleData minusLog10PvalueCol = StatsImplementation.neg(log10PvalueCol);

        model.addExtraColumn(pvalueCol, ExprTableModel.DOUBLE_RENDERER);
        model.addExtraColumnInfo(model.getColumnCount() - 1, new PValue());
        model.addExtraColumn(minusLog10PvalueCol, ExprTableModel.DOUBLE_RENDERER);
        model.addExtraColumnInfo(model.getColumnCount() - 1, new PValue());
        model.addExtraColumnInfo(model.getColumnCount() - 1, new LogInfo(LogInfo.LogState.LOG10));

        // if bbinomial is calculated for 2 groups, automatically compute a log Ratio column.  
        if (pArray.length<=2) {
            nbRows = t.getModel().getRowCount(); 
            
            ArrayList<Double> valuesForCol2 = new ArrayList<>(nbRows);
            for (int row = 0; row < nbRows; row++) {
                double n = 0.0;
                int c = 0;
                for (; c < nbCols1; c++) {
                    n += asDoubleValue(cols[c], row) +1;
                }
                double d = 0.0;
                for (; c < (nbCols1+nbCols2); c++) {
                    d += asDoubleValue(cols[c], row) +1;
                }
                valuesForCol2.add(StrictMath.log((n/nbCols1)/(d/nbCols2))/LOG2); 
            }

            colName = "log Ratio";
            model.addExtraColumn(new ColDoubleData(resTable, valuesForCol2, colName), ExprTableModel.DOUBLE_RENDERER);
            model.addExtraColumnInfo(model.getColumnCount()-1, new LogRatio());
        
        }
        
        
        return resTable;

    }

    public static PythonImage boxPlot(PyTuple p, PyTuple labels) throws Exception {

        // needs R for this calculation
        StatsUtil.startRServer();

        // PyTuple to Col Array
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        ColRef[] cols = StatsUtil.colTupleToColArray(pArray);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, true, false, true);

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
        serverR.parseAndEval("library(" + LIB_PROSTAR + ")");

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, true);

        // create image
        return StatsUtil.createImage(imageTempFile, "boxPlotD(" + StatsUtil.MATRIX_VARIABLE + ",dataForXAxis=data.frame(Label=" + StatsUtil.stringTupleToRVector(columnsName) + "),labels=" + StatsUtil.stringTupleToRVector(labels) + ")");

    }

    public static PyObject calibrationPlot(Col pvaluesCol, PyString pi0Method, PyInteger nbins, PyFloat pz) throws Exception {

        // needs R for this calculation
        StatsUtil.startRServer();

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
        serverR.parseAndEval("library(" + LIB_CP4P + ")");

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, false);

        // create image
        String pi0Parameter;
        String pi0String = pi0Method.toString();
        try {
            Double.parseDouble(pi0String);
            pi0Parameter = "pi0.method=" + pi0Method.toString() + "";  // pi0 is a numeric value for numeric method
        } catch (NumberFormatException nfe) {
            pi0Parameter = "pi0.method=\"" + pi0Method.toString() + "\""; // pi0 is a method
        }

        String cmd = "calibration.plot(p=" + StatsUtil.MATRIX_VARIABLE + "[,1:1], " + pi0Parameter + ", nbins=" + nbins.toString() + ", pz=" + pz.toString() + ")";
        return StatsUtil.createImage(imageTempFile, cmd);

    }

    public static PythonImage densityPlot(PyTuple p, PyTuple labels) throws Exception {

        // needs R for this calculation
        StatsUtil.startRServer();

        // PyTuple to Col Array
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        ColRef[] cols = StatsUtil.colTupleToColArray(pArray);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, true, false, true);

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
        serverR.parseAndEval("library(" + LIB_PROSTAR + ")");

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, true);

        // create image
        return StatsUtil.createImage(imageTempFile, "densityPlotD(" + StatsUtil.MATRIX_VARIABLE + ",labels=" + StatsUtil.stringTupleToRVector(labels) + ")");

    }

    public static PythonImage varianceDistPlot(PyTuple p, PyTuple labels) throws Exception {

        // needs R for this calculation
        StatsUtil.startRServer();

        // PyTuple to Col Array
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        ColRef[] cols = StatsUtil.colTupleToColArray(pArray);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, true, false, true);

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
        serverR.parseAndEval("library(" + LIB_PROSTAR + ")");

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, true);

        // create image
        return StatsUtil.createImage(imageTempFile, "varianceDistD(" + StatsUtil.MATRIX_VARIABLE + ",labels=" + StatsUtil.stringTupleToRVector(labels) + ")");

    }

    public static Table normalize(PyTuple p, PyTuple labels, PyString normalizeFamily, PyString normalizeOption) throws Exception {

        // needs R for this calculation
        StatsUtil.startRServer();

        // PyTuple to Col Array
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        ColRef[] cols = StatsUtil.colTupleToColArray(pArray);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, false, false, true);

        // do the calculation
        Table t = cols[0].getTable();
        Table resTable = _normalizeR(t, matrixTempFile, labels, cols, normalizeFamily, normalizeOption);

        // delete temp files
        matrixTempFile.delete();

        return resTable;
    }

    private static Table _normalizeR(Table t, File matrixTempFile, PyTuple labels, ColRef[] cols, PyString normalizeFamily, PyString normalizeOption) throws Exception {

        // load library to do the calculation
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library(" + LIB_PROSTAR + ")");

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, false);

        String cmdBB = "normalizeD(" + StatsUtil.MATRIX_VARIABLE + "," + StatsUtil.stringTupleToRVector(labels) + ",\"" + normalizeFamily + "\",\"" + normalizeOption + "\")";

        Object res = serverR.parseAndEval(cmdBB);

        ExprTableModel model = new ExprTableModel(t.getModel());
        Table resTable = new Table(model);
        HashMap<Integer, Col> modifiedColumns = new HashMap<>();

        if (res instanceof REXPDouble) {
            REXPDouble matrixDouble = (REXPDouble) res;
            double[][] d = matrixDouble.asDoubleMatrix();

            int nbRows = d.length;
            int nbCols = cols.length;

            ArrayList<ArrayList<Double>> valuesForCol = new ArrayList<>();
            for (int j = 0; j < nbCols; j++) {
                valuesForCol.add(new ArrayList<>());
            }
            for (int i = 0; i < nbRows; i++) {
                for (int j = 0; j < nbCols; j++) {
                    valuesForCol.get(j).add(d[i][j]);
                }
            }

            for (int j = 0; j < nbCols; j++) {
                modifiedColumns.put(cols[j].getModelCol(), new ColDoubleData(resTable, valuesForCol.get(j), "norm(" + cols[j].getExportColumnName() + ")"));
            }

        } else {
            REXPGenericVector matrix = (REXPGenericVector) res;

            RList list = (RList) matrix.asList();
            for (int i = 0; i < list.size(); i++) {
                REXPDouble resDoubleArray = (REXPDouble) list.get(i);
                double[] d = resDoubleArray.asDoubles();
                ArrayList<Double> values = new ArrayList<>();
                for (int j = 0; j < d.length; j++) {
                    values.add(d[j]);
                }
                modifiedColumns.put(cols[i].getModelCol(), new ColDoubleData(resTable, values, "norm(" + cols[i].getExportColumnName() + ")"));
            }

            

        }
        
        model.modifyColumnValues(modifiedColumns, null);

        return resTable;

    }

    public static Table mvimputation(PyTuple p, PyString method) throws Exception {

        // needs R for this calculation
        StatsUtil.startRServer();

        // PyTuple to Col Array
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        ColRef[] cols = StatsUtil.colTupleToColArray(pArray);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, false, false, true);

        // do the calculation
        Table t = cols[0].getTable();
        Table resTable = _mvimputationR(t, matrixTempFile, cols, method);

        // delete temp files
        matrixTempFile.delete();

        return resTable;
    }

    private static Table _mvimputationR(Table t, File matrixTempFile, ColRef[] cols, PyString method) throws Exception {

        // load library to do the calculation
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library(" + LIB_PROSTAR + ")");

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, false);

        String cmdBB = "mvImputation(" + StatsUtil.MATRIX_VARIABLE + ",\"" + method + "\")";

        Object res = serverR.parseAndEval(cmdBB);

        ExprTableModel model = new ExprTableModel(t.getModel());
        Table resTable = new Table(model);
        HashMap<Integer, Col> modifiedColumns = new HashMap<>();

        if (res instanceof REXPDouble) {
            REXPDouble matrixDouble = (REXPDouble) res;
            double[][] d = matrixDouble.asDoubleMatrix();

            int nbRows = d.length;
            int nbCols = cols.length;

            ArrayList<ArrayList<Double>> valuesForCol = new ArrayList<>();
            for (int j = 0; j < nbCols; j++) {
                valuesForCol.add(new ArrayList<>());
            }
            for (int i = 0; i < nbRows; i++) {
                for (int j = 0; j < nbCols; j++) {
                    valuesForCol.get(j).add(d[i][j]);
                }
            }

            for (int j = 0; j < nbCols; j++) {
                modifiedColumns.put(cols[j].getModelCol(), new ColDoubleData(resTable, valuesForCol.get(j), "mvi(" + cols[j].getExportColumnName() + ")"));
            }

        } else {
            REXPGenericVector matrix = (REXPGenericVector) res;

            RList list = (RList) matrix.asList();
            for (int i = 0; i < list.size(); i++) {
                REXPDouble resDoubleArray = (REXPDouble) list.get(i);
                double[] d = resDoubleArray.asDoubles();
                ArrayList<Double> values = new ArrayList<>();
                for (int j = 0; j < d.length; j++) {
                    values.add(d[j]);
                }
                modifiedColumns.put(cols[i].getModelCol(), new ColDoubleData(resTable, values, "mvi(" + cols[i].getExportColumnName() + ")"));
            }

            

        }
        
        model.modifyColumnValues(modifiedColumns, null);

        return resTable;

    }

    public static Table diffanalysis(PyTuple p, PyTuple labels, PyString diffAnalysisType) throws Exception {

        // needs R for this calculation
        StatsUtil.startRServer();

        // PyTuple to Col Array
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        ColRef[] cols = StatsUtil.colTupleToColArray(pArray);

        // Create a temp file with a matrix containing cols data
        File matrixTempFile = StatsUtil.columnsToMatrixTempFile(cols, false, false, true);

        // do the calculation
        Table t = cols[0].getTable();
        Table resTable = _diffanalysisR(t, matrixTempFile, labels, cols, diffAnalysisType);

        // delete temp files
        matrixTempFile.delete();

        return resTable;
    }

    private static Table _diffanalysisR(Table t, File matrixTempFile, PyTuple labels, ColRef[] cols, PyString diffAnalysisType) throws Exception {

        // Prepare Model
        final int colPValue = t.getModel().getColumnCount() + 1;
        final int colLogFC = colPValue + 1;
        ExprTableModel model = new ExprTableModel(t.getModel()) {
            @Override
            public int[] getBestColIndex(PlotType plotType) {
                if (plotType == SCATTER_PLOT) {
                    int[] cols = new int[2];
                    cols[0] = colLogFC;
                    cols[1] = colPValue;
                    return cols;
                }
                return super.getBestColIndex(plotType);
            }

            
            @Override
            public PlotType getBestPlotType() {
                return PlotType.SCATTER_PLOT;
            }
        };
        Table resTable = new Table(model);

        // load library to do the calculation
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library(" + LIB_PROSTAR + ")");
        serverR.parseAndEval("library(limma)");

        // read Matrix Data
        StatsUtil.readMatrixData(matrixTempFile, false);

        
        String diffAnalysisTypeString = diffAnalysisType.toString();
        if (diffAnalysisTypeString.compareTo("Limma") == 0) {

            int nbValues = labels.getArray().length;

            String cmdBB = "limmaObject <- limmaCompleteTest(" + StatsUtil.MATRIX_VARIABLE + "," + StatsUtil.RVectorToRFactor(StatsUtil.stringTupleToRVector(labels)) + "," + "factor(1:" + nbValues + ")" + "," + "factor(1:" + nbValues + ")" + ")";
            serverR.parseAndEval(cmdBB);

            // order limma result according to index
            cmdBB = "limmaObject<-limmaObject[order(as.numeric(rownames(limmaObject))),,drop=FALSE]";
            serverR.parseAndEval(cmdBB);
            
            Object resPValue = serverR.parseAndEval("limmaObject$P_Value");
            Object resLogFC = serverR.parseAndEval("limmaObject$logFC");
   
            double[] pvaluesArray =  ((REXPDouble) resPValue).asDoubles();
            double[] logFCArray =  ((REXPDouble) resLogFC).asDoubles();

            int nbRows = pvaluesArray.length;

            ArrayList<Double> valuesForCol = new ArrayList<>(nbRows);

            for (int i = 0; i < nbRows; i++) {
                valuesForCol.add(pvaluesArray[i]);
            }

            String colName = diffAnalysisTypeString + " PValue";
            ColDoubleData pvalueCol = new ColDoubleData(resTable, valuesForCol, colName);
            ColDoubleData log10PvalueCol = StatsImplementation.log10(pvalueCol);
            ColDoubleData minusLog10PvalueCol = StatsImplementation.neg(log10PvalueCol);
            model.addExtraColumn(pvalueCol, ExprTableModel.DOUBLE_RENDERER);
            model.addExtraColumnInfo(model.getColumnCount()-1, new PValue());
            model.addExtraColumn(minusLog10PvalueCol, ExprTableModel.DOUBLE_RENDERER);
            model.addExtraColumnInfo(model.getColumnCount()-1, new PValue());
            model.addExtraColumnInfo(model.getColumnCount()-1, new LogInfo(LogInfo.LogState.LOG10));

            valuesForCol = new ArrayList<>(nbRows);
            for (int i = 0; i < nbRows; i++) {
                valuesForCol.add(-logFCArray[i]); // minus to logFC to be consistent with WELCH
            }

            colName = diffAnalysisTypeString + " log Ratio";
            model.addExtraColumn(new ColDoubleData(resTable, valuesForCol, colName), ExprTableModel.DOUBLE_RENDERER);
            model.addExtraColumnInfo(model.getColumnCount()-1, new LogRatio());
         

        } else if (diffAnalysisTypeString.compareTo("Welch") == 0) {
            String cmdBB = "diffAnaWelch(" + StatsUtil.MATRIX_VARIABLE + "," + StatsUtil.stringTupleToRVector(labels) + ",\"" + "group0" + "\",\"" + "group1" + "\")";

            Object res = serverR.parseAndEval(cmdBB);

            if (res instanceof REXPDouble) {
                REXPDouble matrixDouble = (REXPDouble) res;
                double[][] d = matrixDouble.asDoubleMatrix();

                int nbRows = d.length;
                int nbCols = cols.length;

                ArrayList<ArrayList<Double>> valuesForCol = new ArrayList<>();
                for (int j = 0; j < nbCols; j++) {
                    valuesForCol.add(new ArrayList<>());
                }
                for (int i = 0; i < nbRows; i++) {
                    for (int j = 0; j < nbCols; j++) {
                        valuesForCol.get(j).add(d[i][j]);
                    }
                }

                for (int j = 0; j < nbCols; j++) {

                    if (j == 0) {
                        String colName = diffAnalysisTypeString + " PValue";
                        ColDoubleData pvalueCol = new ColDoubleData(resTable, valuesForCol.get(j), colName);
                        ColDoubleData log10PvalueCol = StatsImplementation.log10(pvalueCol);
                        ColDoubleData minusLog10PvalueCol = StatsImplementation.neg(log10PvalueCol);
                        model.addExtraColumn(pvalueCol, ExprTableModel.DOUBLE_RENDERER);
                        model.addExtraColumnInfo(model.getColumnCount()-1, new PValue());
                        model.addExtraColumn(minusLog10PvalueCol, ExprTableModel.DOUBLE_RENDERER);
                        model.addExtraColumnInfo(model.getColumnCount()-1, new PValue());
                        model.addExtraColumnInfo(model.getColumnCount()-1, new LogInfo(LogInfo.LogState.LOG10));
                    } else if (j == 1) {
                        String colName = diffAnalysisTypeString + " log Ratio";
                        model.addExtraColumn(new ColDoubleData(resTable, valuesForCol.get(j), colName), ExprTableModel.DOUBLE_RENDERER);
                        model.addExtraColumnInfo(model.getColumnCount()-1, new LogRatio());
                    }

                }

            } else {
                REXPGenericVector matrix = (REXPGenericVector) res;

                RList list = (RList) matrix.asList();
                for (int i = 0; i < list.size(); i++) {
                    REXPDouble resDoubleArray = (REXPDouble) list.get(i);
                    double[] d = resDoubleArray.asDoubles();
                    ArrayList<Double> values = new ArrayList<>();
                    for (int j = 0; j < d.length; j++) {
                        values.add(d[j]);
                    }

                    if (i == 0) {
                        String colName = diffAnalysisTypeString + " PValue";
                        Object colExtraInfo = new PValue();
                        ColDoubleData pvalueCol = new ColDoubleData(resTable, values, colName);
                        ColDoubleData log10PvalueCol = StatsImplementation.log10(pvalueCol);
                        ColDoubleData minusLog10PvalueCol = StatsImplementation.neg(log10PvalueCol);
                        model.addExtraColumn(pvalueCol, ExprTableModel.DOUBLE_RENDERER);
                        model.addExtraColumnInfo(model.getColumnCount()-1, colExtraInfo);
                        model.addExtraColumn(minusLog10PvalueCol, ExprTableModel.DOUBLE_RENDERER);
                        model.addExtraColumnInfo(model.getColumnCount()-1, colExtraInfo);
                        model.addExtraColumnInfo(model.getColumnCount()-1, new LogInfo(LogInfo.LogState.LOG10));
                    } else if (i == 1) {
                        String colName = diffAnalysisTypeString + " log Ratio";
                        Object colExtraInfo = new LogRatio();
                        model.addExtraColumn(new ColDoubleData(resTable, values, colName), ExprTableModel.DOUBLE_RENDERER);
                        model.addExtraColumnInfo(model.getColumnCount()-1, colExtraInfo);
                    }

                }

            }
        }

        return resTable;

    }
    
    
    public static PyObject computeFDR(Col pvaluesCol, Col logFCCol, PyFloat minusLogPvalueThreshold, PyFloat logFCThreshold, String pi0Parameter, PyFloat alpha, PyInteger nbins, PyFloat pz) throws Exception {

        RServerManager serverR = RServerManager.getRServerManager();
        boolean RStarted = serverR.startRProcessWithRetry();
        if (!RStarted) {
            throw Py.RuntimeError("Server R not found");
        }

        serverR.connect();

        int nbRow = pvaluesCol.getRowCount();

        File tempFile = File.createTempFile("computeFDR", "csv");
        tempFile.deleteOnExit();
        FileWriter fw = new FileWriter(tempFile);

        for (int i = 0; i < nbRow; i++) {
            double pvalueDouble = asDoubleValue(pvaluesCol, i);
            double logFCDouble = asDoubleValue(logFCCol, i);
            fw.write(String.valueOf(pvalueDouble));
            fw.write(';');
            fw.write(String.valueOf(logFCDouble));
            fw.write('\n');

        }

        fw.close();

        PyFloat d = _computeFDR(tempFile, minusLogPvalueThreshold, logFCThreshold, pi0Parameter, alpha, nbins, pz);

        tempFile.delete();

        return d;

    }

    private static PyFloat _computeFDR(File f, PyFloat minusLogPvalueThreshold, PyFloat logFCThreshold, String pi0Parameter, PyFloat alpha, PyInteger nbins, PyFloat pz) throws Exception {

         // !!!!!! Pvalue parameter in diffAnaComputeFDR is not a Pvalue in fact but -log10(PValue) !!!!!
        double pValue = minusLogPvalueThreshold.getValue(); // Math.pow(10, -minusLogPvalueThreshold.getValue());
        
        RServerManager serverR = RServerManager.getRServerManager();

        String path = f.getCanonicalPath().replaceAll("\\\\", "/");

        // write file
        serverR.parseAndEval("library(" + LIB_PROSTAR + ")");


        String cmdReadCSV = "computeFDRValues<-read.delim('" + path + "',header=F, sep=';', col.names=c(\"P_Value\",\"logFC\"))";
        serverR.parseAndEval(cmdReadCSV);

        String cmdBB1 = "computedFDR<-diffAnaComputeFDR(computeFDRValues, " +pValue+","+logFCThreshold.toString()+"," + pi0Parameter  + ")";
        
        REXPDouble fdr = (REXPDouble) serverR.parseAndEval(cmdBB1);

    
        return new PyFloat(fdr.asDouble()*100); // convert to percentage


    }

}
