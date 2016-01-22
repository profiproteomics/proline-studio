package fr.proline.studio.python.math;

import fr.proline.studio.python.data.Col;
import fr.proline.studio.python.data.ColData;
import fr.proline.studio.python.data.PythonImage;

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

    public static ColData bbinomial(PyTuple p1, PyTuple p2) throws Exception {
        return StatsRImplementation.bbinomial(p1, p2);
    }

    public static PythonImage boxPlot(PyTuple p) throws Exception {
        return StatsRImplementation.boxPlot(p);
    }

    public static PyObject calibrationPlot(Col pvalues) throws Exception {
        return calibrationPlot(pvalues, new PyString("pounds"));
    }

    public static PyObject calibrationPlot(Col pvalues, PyString pi0Method) throws Exception {
        return calibrationPlot(pvalues, pi0Method, new PyInteger(20), new PyFloat(0.05));
    }

    public static PyObject calibrationPlot(Col pvaluesCol, PyString pi0Method, PyInteger nbins, PyFloat pz) throws Exception {
        return StatsRImplementation.calibrationPlot(pvaluesCol, pi0Method, nbins, pz);
    }
    
    public static PythonImage densityPlot(PyTuple p) throws Exception {
        return StatsRImplementation.densityPlot(p);
    }

    public static ColData pvalue(PyTuple p1, PyTuple p2) throws MathException {
        return StatsImplementation.pvalue(p1, p2);
    }

    public static ColData ttd(PyTuple p1, PyTuple p2) throws MathException {
        return StatsImplementation.ttd(p1, p2);
    }
    
    public static PythonImage varianceDistPlot(PyTuple p) throws Exception {
        return StatsRImplementation.varianceDistPlot(p);
    }

}
