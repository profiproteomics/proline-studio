package fr.proline.studio.python.math;

import fr.proline.studio.python.data.Col;
import fr.proline.studio.python.data.ColDoubleData;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.PythonImage;
import fr.proline.studio.python.data.Table;
import org.apache.commons.math.MathException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;


/**
 *
 * @author JM235353
 */
public class Stats {

    public static ColDoubleData abs(Col values) {
        return StatsImplementation.abs(values);
    }
    
    public static PyFloat mean(Col values) {
        return StatsImplementation.mean(values);
    }
    
    public static PyFloat std(Col values) {
        return StatsImplementation.std(values);
    }
    
    public static ColDoubleData log2(Col values) {
        return StatsImplementation.log2(values);
    }
    
    public static Table log2(Table t, PyTuple pcols) {
        return StatsImplementation.log2(t, pcols);
    }
    
    public static Table log2(Table t, ColRef column) {
        return StatsImplementation.log2(t, column);
    }
    
    public static ColDoubleData log10(Col values) {
        return StatsImplementation.log10(values);
    }

    public static Table log10(Table t, PyTuple pcols) {
        return StatsImplementation.log10(t, pcols);
    }

    public static Table log10(Table t, ColRef column) {
        return StatsImplementation.log10(t, column);
    }
    
    
    public static PyObject adjustP(Col pvalues) throws Exception {
        return adjustP(pvalues, new PyFloat(1));
    }

    public static PyObject adjustP(Col pvalues, PyFloat pi0Method) throws Exception {
        return adjustP(pvalues, pi0Method, new PyFloat(0.05), new PyInteger(20), new PyFloat(0.05));
    }

    public static PyObject adjustP(Col pvalues, PyString pi0Method) throws Exception {
        return adjustP(pvalues, pi0Method, new PyFloat(0.05), new PyInteger(20), new PyFloat(0.05));
    }

    public static PyObject adjustP(Col pvaluesCol, PyFloat pi0Method, PyFloat alpha, PyInteger nbins, PyFloat pz) throws Exception {
        String pi0Parameter = "pi0.method=" + pi0Method.toString();
        return StatsRImplementation.adjustP(pvaluesCol, pi0Parameter, alpha, nbins, pz);
    }

    public static PyObject adjustP(Col pvaluesCol, PyString pi0Method, PyFloat alpha, PyInteger nbins, PyFloat pz) throws Exception {
        String pi0Parameter = "pi0.method=\"" + pi0Method.toString() + "\"";
        return StatsRImplementation.adjustP(pvaluesCol, pi0Parameter, alpha, nbins, pz);
    }

    public static Table bbinomial(PyTuple p) throws Exception {
        return StatsRImplementation.bbinomial(p);
    }


    public static PythonImage boxPlot(PyTuple p, PyTuple labels) throws Exception {
        return StatsRImplementation.boxPlot(p, labels);
    }

    public static PythonImage densityPlot(PyTuple p, PyTuple labels) throws Exception {
        return StatsRImplementation.densityPlot(p, labels);
    }


    public static PythonImage varianceDistPlot(PyTuple p, PyTuple labels) throws Exception {
        return StatsRImplementation.varianceDistPlot(p, labels);
    }

    
    public static PyObject calibrationPlot(Col pvalues) throws Exception {
        return calibrationPlot(pvalues, new PyString("pounds"));
    }

    public static PyObject calibrationPlot(Col pvalues, PyString pi0Method) throws Exception {
        return calibrationPlot(pvalues, pi0Method, new PyInteger(20), new PyFloat(0.05));
    }

    public static PyObject calibrationPlot(Col pvaluesCol, PyFloat pi0Method, PyInteger nbins, PyFloat pz) throws Exception {
        
        // numeric value for pi0Method
        Double numericValue = pi0Method.getValue();
        PyString pi0MethodString = new PyString(String.valueOf(numericValue));
        return StatsRImplementation.calibrationPlot(pvaluesCol, pi0MethodString, nbins, pz);
    }
    
    public static PyObject calibrationPlot(Col pvaluesCol, PyString pi0Method, PyInteger nbins, PyFloat pz) throws Exception {
        return StatsRImplementation.calibrationPlot(pvaluesCol, pi0Method, nbins, pz);
    }
    
    public static ColDoubleData pvalue(PyTuple p) throws MathException {
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        return pvalue(pArray[0], pArray[1]);
    }
    public static ColDoubleData pvalue(PyTuple p1, PyTuple p2) throws MathException {
        return StatsImplementation.pvalue(p1, p2);
    }

    public static Table quantifilter(PyTuple p, Table t, PyInteger option, PyInteger threshold) throws MathException {
        return StatsImplementation.quantifilter(p, t, option, threshold, false);
    }

    public static Table quantifilterReversed(PyTuple p, Table t, PyInteger option, PyInteger threshold) throws MathException {
        return StatsImplementation.quantifilter(p, t, option, threshold, true);
    }
    
    public static Table normalize(PyTuple p, PyTuple labels, PyString normalizeFamily, PyString normalizeOption) throws Exception {
        return StatsRImplementation.normalize(p, labels, normalizeFamily, normalizeOption);
    }
    
    public static Table mvimputation(PyTuple p1, PyString method) throws Exception {
        return StatsRImplementation.mvimputation(p1, method);
    }
    public static Table mvimputation(PyTuple p1, PyString method, PyFloat percentilePercentage, PyInteger addImputationColumns) throws Exception {
        return StatsImplementation.mvimputationPercentile(p1, percentilePercentage, addImputationColumns);
    }

    
    public static Table diffanalysis(PyTuple p, PyTuple labels, PyString diffAnalysisType) throws Exception {
        return StatsRImplementation.diffanalysis(p, labels, diffAnalysisType);
    }
    
    public static ColDoubleData ttd(PyTuple p) throws MathException {
        PyTuple[] pArray = StatsUtil.colTupleToTuplesArray(p);
        return ttd(pArray[0], pArray[1]);
    }
    public static ColDoubleData ttd(PyTuple p1, PyTuple p2) throws MathException {
        return StatsImplementation.ttd(p1, p2);
    }
    
    public static PyObject computeFDR(Col pvalues, Col logFC, PyFloat pvalueThreshold, PyFloat logFCThreshold) throws Exception {
        return computeFDR(pvalues, logFC, pvalueThreshold, logFCThreshold, new PyFloat(1));
    }

    public static PyObject computeFDR(Col pvalues, Col logFC, PyFloat pvalueThreshold, PyFloat logFCThreshold, PyFloat pi0Method) throws Exception {
        return computeFDR(pvalues, logFC, pvalueThreshold, logFCThreshold, pi0Method, new PyFloat(0.05), new PyInteger(20), new PyFloat(0.05));
    }

    public static PyObject computeFDR(Col pvalues, Col logFC, PyFloat pvalueThreshold, PyFloat logFCThreshold, PyString pi0Method) throws Exception {
        return computeFDR(pvalues, logFC, pvalueThreshold, logFCThreshold, pi0Method, new PyFloat(0.05), new PyInteger(20), new PyFloat(0.05));
    }

    public static PyObject computeFDR(Col pvalues, Col logFC, PyFloat pvalueThreshold, PyFloat logFCThreshold, PyFloat pi0Method, PyFloat alpha, PyInteger nbins, PyFloat pz) throws Exception {
        String pi0Parameter = "pi0Method=" + pi0Method.toString();
        return StatsRImplementation.computeFDR(pvalues, logFC, pvalueThreshold, logFCThreshold, pi0Parameter, alpha, nbins, pz);
    }

    public static PyObject computeFDR(Col pvalues, Col logFC, PyFloat pvalueThreshold, PyFloat logFCThreshold, PyString pi0Method, PyFloat alpha, PyInteger nbins, PyFloat pz) throws Exception {
        String pi0Parameter = "pi0Method=\"" + pi0Method.toString() + "\"";
        return StatsRImplementation.computeFDR(pvalues, logFC, pvalueThreshold, logFCThreshold, pi0Parameter, alpha, nbins, pz);
    }
    
    public static Table differentialProteins(Col pvalues, Col logFC, PyFloat pvalueThreshold, PyFloat logFCThreshold) throws Exception {

        return StatsImplementation.differentialProteins(pvalues, logFC, pvalueThreshold, logFCThreshold);
    }
    
    /*public void Testdapar() throws Exception {
        RServerManager serverR = RServerManager.getRServerManager();
        serverR.parseAndEval("library(DAPAR)");
    }*/

}
