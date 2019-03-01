package fr.proline.studio.python.util;

import fr.proline.studio.table.LazyData;
import org.python.core.Py;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyLong;
import org.python.core.PyObject;

/**
 *
 * Converstion from java classes to Python classes, or from Python classes to java classes
 * 
 * @author JM235353
 */
public class Conversion {
    
    public static PyObject convertToPyObject(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
        }
        if (data instanceof Double) {
            return new PyFloat(((Double)data).doubleValue());
        }
        if (data instanceof Float) {
            return new PyFloat(((Float)data).floatValue());
        }
        if (data instanceof Integer) {
            return new PyInteger(((Integer)data).intValue());
        }
        if (data instanceof Long) {
            return new PyLong(((Long)data).longValue());
        }
        if (data instanceof PyObject) {
            return (PyObject) data;
        }
        throw Py.TypeError("Can not convert "+data.getClass().getName()+" to PyObject");
    }
    
    public static Number convertToJavaNumber(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof PyInteger) {
            return Integer.valueOf(((PyInteger) data).getValue());
        }
        if (data instanceof PyFloat) {
            return Double.valueOf(((PyFloat) data).getValue());
        }
        if (data instanceof Number) {
            return ((Number)data);
        }
        throw Py.TypeError("Can not convert "+data.getClass().getName()+" to Number");
    }
}
