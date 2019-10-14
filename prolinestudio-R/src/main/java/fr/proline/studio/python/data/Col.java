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
package fr.proline.studio.python.data;

import fr.proline.studio.python.util.Conversion;
import java.util.ArrayList;
import org.python.core.Py;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Python object corresponding to the column of a Table Model
 * It is the base class of ColDoubleData, ColBooleanData and ColRef
 * 
 * @author JM235353
 */
public abstract class Col extends PyObject {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    protected String m_columnName = null;
    
    protected String m_tooltip = null;
    
    protected Table m_table = null;
    
    public Col(Table table) {
        m_table = table;
    }
    
    public abstract Object getValueAt(int row);
    
    public abstract void setValuetAt(int row, Object o);

    public abstract int getRowCount();

    public Table getTable() {
        return m_table;
    }
    
    public void setColumnName(String columnName) {
        m_columnName = columnName;
    }
    
    public String getColumnName() {
        return m_columnName;
    }
    
    public void setTooltip(String tooltip) {
        m_tooltip = tooltip;
    }

    public String getTooltip() {
        return m_tooltip;
    }
    
    public String getExportColumnName() {
        return m_columnName;
    }
    
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
            setValuetAt( ((PyInteger)key).getValue() , value);
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
        return applyBinaryDoubleOp(this, right, new IBinaryOperator() {
            @Override
            public double compute(double leftD, double rightD) {
                return leftD + rightD;
            }
        });
    }
    
    @Override
    public PyObject __radd__(PyObject left) {
        return __add__(left);
    }
    
    
    @Override
    public PyObject __pow__(PyObject right) {
        return applyBinaryDoubleOp(this, right, new IBinaryOperator() {
            @Override
            public double compute(double leftD, double rightD) {
                return Math.pow(leftD,rightD);
            }
        });
    }
    
    @Override
    public PyObject __rpow__(PyObject left) {
        return applyBinaryDoubleOp(left, this, new IBinaryOperator() {
            @Override
            public double compute(double leftD, double rightD) {
                return Math.pow(leftD,rightD);
            }
        });
    }

    @Override
    public PyObject __neg__() {
        return applyBinaryDoubleOp(new PyInteger(0), this, new IBinaryOperator() {
            @Override
            public double compute(double leftD, double rightD) {
                return leftD - rightD;
            }
        });
    }
    
    
    @Override
    public PyObject __sub__(PyObject right) {
        return applyBinaryDoubleOp(this, right, new IBinaryOperator() {
            @Override
            public double compute(double leftD, double rightD) {
                return leftD - rightD;
            }
        });
    }
    
    @Override
    public PyObject __rsub__(PyObject left) {
        return applyBinaryDoubleOp(left, this, new IBinaryOperator() {
            @Override
            public double compute(double leftD, double rightD) {
                return leftD - rightD;
            }
        });
    }
    
    @Override
    public PyObject __mul__(PyObject right) {
        return applyBinaryDoubleOp(this, right, new IBinaryOperator() {
            @Override
            public double compute(double leftD, double rightD) {
                return leftD * rightD;
            }
        });
    }
    
    @Override
    public PyObject __rmul__(PyObject left) {
        return __mul__(left);
    }
    
    @Override
    public PyObject __div__(PyObject right) {
        return applyBinaryDoubleOp(this, right, new IBinaryOperator() {
            @Override
            public double compute(double leftD, double rightD) {
                return leftD / rightD;
            }
        });
    }
    
    @Override
    public PyObject __rdiv__(PyObject left) {
        return applyBinaryDoubleOp(left, this, new IBinaryOperator() {
            @Override
            public double compute(double leftD, double rightD) {
                return leftD / rightD;
            }
        });
    }
    
    @Override
    public PyObject __gt__(PyObject right) {
        return applyComparatorOp(this, right, new IComparator() {
            @Override
            public boolean compare(double leftD, double rightD) {
                return leftD > rightD;
            }
            @Override
            public boolean compareNullity(Object o1, Object o2) { return Boolean.FALSE; }
        });
    }
     
    @Override
     public PyObject __ge__(PyObject right) {
        return applyComparatorOp(this, right, new IComparator() {
            @Override
            public boolean compare(double leftD, double rightD) {
                return leftD >= rightD;
            }
            @Override
            public boolean compareNullity(Object o1, Object o2) { return Boolean.FALSE; }
        });
    }
     
    @Override
    public PyObject __lt__(PyObject right) {
        return applyComparatorOp(this, right, new IComparator() {
            @Override
            public boolean compare(double leftD, double rightD) {
                return leftD < rightD;
            }
            @Override
            public boolean compareNullity(Object o1, Object o2) { return Boolean.FALSE; }
        });
    }

    @Override
    public PyObject __le__(PyObject right) {
        return applyComparatorOp(this, right, new IComparator() {
            @Override
            public boolean compare(double leftD, double rightD) {
                return leftD <= rightD;
            }
            @Override
            public boolean compareNullity(Object o1, Object o2) { return Boolean.FALSE; }

        });
    }
    
    @Override
    public PyObject __eq__(PyObject right) { 
        return applyComparatorOp(this, right, new IComparator() {
            @Override
            public boolean compare(double leftD, double rightD) {
                return leftD == rightD;
            }
            @Override
            public boolean compareNullity(Object o1, Object o2) { return (o1 == null) && (o2==null); }
        });
    }
    
    @Override
    public PyObject __ne__(PyObject right) {
            
        return applyComparatorOp(this, right, new IComparator() {
            @Override
            public boolean compare(double leftD, double rightD) {
                return leftD != rightD;
            }
            @Override
            public boolean compareNullity(Object o1, Object o2) { return (o1!= null) || (o2 != null); }
        });
    }
    
    @Override
    public PyObject __invert__() {

        int nb = __len__();

        ArrayList<Boolean> resultArray = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            Object o = getValueAt(i);

            if (!(o instanceof Boolean)) {
                throw Py.TypeError("Col with non boolean values");
            }
            boolean b = !((Boolean) o);
            resultArray.add(b);
        }
        return new ColBooleanData(m_table, resultArray, null);
    }

    
    @Override
    public PyObject __and__(PyObject right) {
        if (right instanceof ColBooleanData) {

            int nb = __len__();
            if (right.__len__() != nb) {
                throw Py.TypeError("Tried to do 'and' with Columns with different sizes");
            }

            ArrayList<Boolean> resultArray = new ArrayList<>(nb);
            Col rightCol = (Col) right;
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                Object o2 = rightCol.getValueAt(i);
                if ((o1 == null) || (o2 == null)) {
                    resultArray.add(Boolean.FALSE);
                    continue;
                }
                if (!(o1 instanceof Boolean) || !(o2 instanceof Boolean)) {
                    throw Py.TypeError("Col with non boolean values");
                }
                boolean b = ((Boolean) o1) && ((Boolean) o2);
                resultArray.add(b);
            }

            return new ColBooleanData(m_table, resultArray, null);
        }
        
        throw Py.TypeError("Type Mismatch for + " + right.getClass().getName());
    }
    
    @Override
    public PyObject __or__(PyObject right) {
        if (right instanceof ColBooleanData) {

            int nb = __len__();
            if (right.__len__() != nb) {
                throw Py.TypeError("Tried to do 'and' with Columns with different sizes");
            }

            ArrayList<Boolean> resultArray = new ArrayList<>(nb);
            Col rightCol = (Col) right;
            for (int i = 0; i < nb; i++) {
                Object o1 = getValueAt(i);
                Object o2 = rightCol.getValueAt(i);
                if ((o1 == null) || (o2 == null)) {
                    if ((o1 != null) && (o1 instanceof Boolean)) {
                        resultArray.add(((Boolean) o1));
                        continue;
                    }
                    if ((o2 != null) && (o2 instanceof Boolean)) {
                        resultArray.add(((Boolean) o2));
                        continue;
                    }
                    resultArray.add(Boolean.FALSE);
                    continue;
                }
                if (!(o1 instanceof Boolean) || !(o2 instanceof Boolean)) {
                    throw Py.TypeError("Col with non boolean values");
                }
                boolean b = ((Boolean) o1) || ((Boolean) o2);
                resultArray.add(b);
            }

            return new ColBooleanData(m_table, resultArray, null);
        }
        
        throw Py.TypeError("Type Mismatch for + " + right.getClass().getName());
    }

     
    private PyObject applyComparatorOp(Col left, PyObject right, IComparator comparator) {
        int nb = left.__len__();        
        if (right instanceof Col) {
            if (right.__len__() != nb) {
                throw Py.TypeError("Tried to compare Columns with different sizes");
            }

            ArrayList<Boolean> resultArray = new ArrayList<>(nb);
            Col rightCol = (Col) right;
            for (int i = 0; i < nb; i++) {
                Object o1 = left.getValueAt(i);
                Object o2 = rightCol.getValueAt(i);
                if ((o1 == null) || (o2 == null)) {
                        boolean b = comparator.compareNullity(o1, o2);
                        resultArray.add(b);
                        continue;
                }
                if (!(o1 instanceof Number) || !(o2 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                boolean b = comparator.compare(((Number) o1).doubleValue(), ((Number) o2).doubleValue());
                resultArray.add(b);
            }

            return new ColBooleanData(m_table, resultArray, null);
        } else if (right instanceof PyInteger) {

            int v = ((PyInteger) right).getValue();
            ArrayList<Boolean> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = left.getValueAt(i);
                if (o1 == null) {
                    resultArray.add(Boolean.FALSE);
                    continue;
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                boolean b = comparator.compare(((Number) o1).doubleValue(), v);
                resultArray.add(b);
            }
            return new ColBooleanData(m_table, resultArray, null);
        } else if (right instanceof PyFloat) {

            double v = ((PyFloat) right).getValue();
            ArrayList<Boolean> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = left.getValueAt(i);
                if (o1 == null) {
                    resultArray.add(Boolean.FALSE);
                    continue;
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                boolean b = comparator.compare(((Number) o1).doubleValue(), v);
                resultArray.add(b);
            }
            return new ColBooleanData(m_table, resultArray, null);
        }
        throw Py.TypeError("Type Mismatch for + " + right.getClass().getName());    
    }
    
    private PyObject applyBinaryDoubleOp(PyObject left, PyObject right, IBinaryOperator operator) {
        if ((right instanceof Col) && (left instanceof Col)) {
            int nb = left.__len__();
            if (right.__len__() != nb) {
                throw Py.TypeError("Tried to add Columns with different sizes");
            }
            
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            Col rightCol = (Col) right;
            Col leftCol = (Col) left;
            for (int i=0;i<nb;i++) {
                Object o1 = leftCol.getValueAt(i);
                Object o2 = rightCol.getValueAt(i);
                if ((o1 == null) || (o2 == null)) {
                    resultArray.add(Double.NaN);
                    continue;
                }
                if (!(o1 instanceof Number) || !(o2 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = operator.compute(((Number) o1).doubleValue(),((Number) o2).doubleValue());
                resultArray.add(d);
            }
            
            return new ColDoubleData(m_table, resultArray, null);
        } else if (right instanceof PyInteger) {
            
            int v = ((PyInteger) right).getValue();
            
            int nb = left.__len__();
            Col leftCol = (Col) left;
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = leftCol.getValueAt(i);
                if (o1 == null) {
                    resultArray.add(Double.NaN);
                    continue;
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = operator.compute(((Number) o1).doubleValue(), v);
                resultArray.add(d);
            }
            return new ColDoubleData(m_table, resultArray, null);
        } else if (right instanceof PyFloat) {
            
            double v = ((PyFloat) right).getValue();
            int nb = left.__len__();
            Col leftCol = (Col) left;
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = leftCol.getValueAt(i);
                if (o1 == null) {
                    resultArray.add(Double.NaN);
                    continue;
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = operator.compute(((Number) o1).doubleValue(),v);
                resultArray.add(d);
            }
            return new ColDoubleData(m_table, resultArray, null);
        } else if (left instanceof PyInteger) {
            
            int v = ((PyInteger) left).getValue();
            
            int nb = right.__len__();
            Col rightCol = (Col) right;
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = rightCol.getValueAt(i);
                if (o1 == null) {
                    resultArray.add(Double.NaN);
                    continue;
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = operator.compute(v, ((Number) o1).doubleValue());
                resultArray.add(d);
            }
            return new ColDoubleData(m_table, resultArray, null);
        } else if (left instanceof PyFloat) {
            
            double v = ((PyFloat) left).getValue();
            int nb = right.__len__();
            Col rightCol = (Col) right;
            ArrayList<Double> resultArray = new ArrayList<>(nb);
            for (int i = 0; i < nb; i++) {
                Object o1 = rightCol.getValueAt(i);
                if (o1 == null) {
                    resultArray.add(Double.NaN);
                    continue;
                }
                if (!(o1 instanceof Number)) {
                    throw Py.TypeError("Col with non numeric values");
                }
                double d = operator.compute(v, ((Number) o1).doubleValue());
                resultArray.add(d);
            }
            return new ColDoubleData(m_table, resultArray, null);
        }
        throw Py.TypeError("Type Mismatch for + "+right.getClass().getName());
    } 
    
    public abstract Class getColumnClass();
}


interface IBinaryOperator {
    public double compute(double leftD, double rightD);
}

interface IComparator {
    public boolean compare(double leftD, double rightD);
    public boolean compareNullity(Object o1, Object o2);
}