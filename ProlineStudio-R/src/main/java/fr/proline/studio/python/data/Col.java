package fr.proline.studio.python.data;

import fr.proline.studio.python.util.Conversion;
import java.util.ArrayList;
import org.python.core.Py;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;

/**
 *
 * @author JM235353
 */
public abstract class Col extends PyObject {
    
    public abstract Object getValueAt(int row);
    
    public abstract void setValuetAt(int row, Object o);

    public abstract int getRowCount();
    
    public abstract String getColumnName();
    
    public abstract Col mutable();

    /**
     * __finditem__ is useful for [] call
     * @param key
     * @return 
     */
    @Override
    public PyObject __finditem__(PyObject key) {
        if (key instanceof PyInteger) {
            Object o = getValueAt(((PyInteger) key).getValue());
            return Conversion.convertToPyObject(o);
        }
        throw Py.TypeError("Unexpected Type Found "+key.getClass().getName());
    }
    
    @Override
    public void __setitem__(PyObject key, PyObject value) {
        if (key instanceof PyInteger) {
            setValuetAt( ((PyInteger)key).getValue() , key);
            return;
        }
        throw Py.TypeError("Unexpected Type Found "+key.getClass().getName());
    }
    
    @Override
    public int __len__() {
        return getRowCount();
    }
    
    @Override
    public PyObject __add__(PyObject right) {
        if (right instanceof Col) {
            
            int nb = __len__();
            if (right.__len__() != nb) {
                throw Py.TypeError("Tried to add Columns with different sizes");
            }
            
            
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            Col rightCol = (Col) right;
            for (int i=0;i<nb;i++) {
                Object o1 = getValueAt(i);
                Object o2 = rightCol.getValueAt(i);
                if ((o1 == null) || (o2 == null)) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number) || !(o2 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o1).doubleValue()+((Number) o2).doubleValue();
                resultArray.add(d);
            }
            
            return new ColData(resultArray, getColumnName()+'+'+rightCol.getColumnName());
        } else if (right instanceof PyInteger) {
            
            int v = ((PyInteger) right).getValue();
            
            int nb = __len__();
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                if (o1 == null) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o1).doubleValue()+v;
                resultArray.add(d);
            }
            return new ColData(resultArray, getColumnName()+'+'+String.valueOf(v));
        } else if (right instanceof PyFloat) {
            
            double v = ((PyFloat) right).getValue();
            
            int nb = __len__();
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                if (o1 == null) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o1).doubleValue()+v;
                resultArray.add(d);
            }
            return new ColData(resultArray, getColumnName()+'+'+String.valueOf(v));
        }
        throw Py.TypeError("Type Mismatch for + "+right.getClass().getName());
    }
    
    @Override
    public PyObject __radd__(PyObject left) {
        return __add__(left);
    }
    
    @Override
    public PyObject __sub__(PyObject right) {
        if (right instanceof Col) {
            
            int nb = __len__();
            if (right.__len__() != nb) {
                throw Py.TypeError("Tried to sub Columns with different sizes");
            }
  
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            Col rightCol = (Col) right;
            for (int i=0;i<nb;i++) {
                Object o1 = getValueAt(i);
                Object o2 = rightCol.getValueAt(i);
                if ((o1 == null) || (o2 == null)) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number) || !(o2 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o1).doubleValue()-((Number) o2).doubleValue();
                resultArray.add(d);
            }
            
            return new ColData(resultArray, getColumnName()+'-'+rightCol.getColumnName());
        } else if (right instanceof PyInteger) {
            
            int v = ((PyInteger) right).getValue();
            
            int nb = __len__();
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                if (o1 == null) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o1).doubleValue()-v;
                resultArray.add(d);
            }
            return new ColData(resultArray, getColumnName()+'-'+String.valueOf(v));
        } else if (right instanceof PyFloat) {
            
            double v = ((PyFloat) right).getValue();
            
            int nb = __len__();
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                if (o1 == null) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o1).doubleValue()-v;
                resultArray.add(d);
            }
            return new ColData(resultArray, getColumnName()+'-'+String.valueOf(v));
        }
        throw Py.TypeError("Type Mismatch for + "+right.getClass().getName());
    }
    
    @Override
    public PyObject __rsub__(PyObject left) {
        if (left instanceof Col) {
            
            int nb = __len__();
            if (left.__len__() != nb) {
                throw Py.TypeError("Tried to sub Columns with different sizes");
            }
  
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            Col leftCol = (Col) left;
            for (int i=0;i<nb;i++) {
                Object o1 = getValueAt(i);
                Object o2 = leftCol.getValueAt(i);
                if ((o1 == null) || (o2 == null)) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number) || !(o2 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o2).doubleValue()-((Number) o1).doubleValue();
                resultArray.add(d);
            }
            
            return new ColData(resultArray, leftCol.getColumnName()+'-'+getColumnName());
        } else if (left instanceof PyInteger) {
            
            int v = ((PyInteger) left).getValue();
            
            int nb = __len__();
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                if (o1 == null) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = v-((Number) o1).doubleValue();
                resultArray.add(d);
            }
            return new ColData(resultArray, String.valueOf(v)+'-'+getColumnName());
        } else if (left instanceof PyFloat) {
            
            double v = ((PyFloat) left).getValue();
            
            int nb = __len__();
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                if (o1 == null) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = v-((Number) o1).doubleValue();
                resultArray.add(d);
            }
            return new ColData(resultArray, String.valueOf(v)+'-'+getColumnName());
        }
        throw Py.TypeError("Type Mismatch for + "+left.getClass().getName());
    }
    
    @Override
    public PyObject __mul__(PyObject right) {
        if (right instanceof Col) {

            int nb = __len__();
            if (right.__len__() != nb) {
                throw Py.TypeError("Tried to mul Columns with different sizes");
            }

            ArrayList<Double> resultArray = new ArrayList<>(nb);
            Col rightCol = (Col) right;
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                Object o2 = rightCol.getValueAt(i);
                if ((o1 == null) || (o2 == null)) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number) || !(o2 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o1).doubleValue() * ((Number) o2).doubleValue();
                resultArray.add(d);
            }

            return new ColData(resultArray, getColumnName() + '*' + rightCol.getColumnName());
        } else if (right instanceof PyInteger) {

            int v = ((PyInteger) right).getValue();

            int nb = __len__();
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                if (o1 == null) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o1).doubleValue() * v;
                resultArray.add(d);
            }
            return new ColData(resultArray, getColumnName() + '*' + String.valueOf(v));
        } else if (right instanceof PyFloat) {

            double v = ((PyFloat) right).getValue();

            int nb = __len__();
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                if (o1 == null) {
                    throw Py.TypeError("Col with null values");
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = ((Number) o1).doubleValue() * v;
                resultArray.add(d);
            }
            return new ColData(resultArray, getColumnName() + '*' + String.valueOf(v));
        }
        throw Py.TypeError("Type Mismatch for + " + right.getClass().getName());
    }
    
    @Override
    public PyObject __rmul__(PyObject left) {
        return __mul__(left);
    }
}
