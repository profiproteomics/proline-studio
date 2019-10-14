/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
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
