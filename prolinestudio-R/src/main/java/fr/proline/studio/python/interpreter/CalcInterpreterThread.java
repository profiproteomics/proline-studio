/* 
 * Copyright (C) 2019
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
package fr.proline.studio.python.interpreter;

import fr.proline.studio.python.data.ColBooleanData;
import fr.proline.studio.python.data.ColDoubleData;
import fr.proline.studio.python.data.PythonImage;
import fr.proline.studio.python.data.Table;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;
import org.slf4j.LoggerFactory;

/**
 *
 * Thread in charge to interpret python expressions
 * 
 * @author JM235353
 */
public class CalcInterpreterThread extends Thread {

    private static CalcInterpreterThread m_instance;

    private final LinkedList<CalcInterpreterTask> m_actions;

    private CalcInterpreterThread() {
        super("CalcInterpreterThread"); // useful for debugging

        m_actions = new LinkedList<>();

    }

    public static CalcInterpreterThread getCalcInterpreterThread() {
        if (m_instance == null) {
            m_instance = new CalcInterpreterThread();
            m_instance.start();
        }
        return m_instance;
    }

    public final void addTask(CalcInterpreterTask task) {

        //TaskInfoManager.getTaskInfoManager().add(task.getTaskInfo());
        // action is queued
        synchronized (this) {
            m_actions.add(task);
            notifyAll();
        }
    }
    
    /**
     * Main loop of the thread
     */
    @Override
    public void run() {
        try {
            while (true) {
                CalcInterpreterTask task = null;
                synchronized (this) {

                    while (true) {

                        // look for a task to be done
                        if (!m_actions.isEmpty()) {
                            task = m_actions.poll();
                            break;
                        }
                        wait();
                    }
                    notifyAll();
                }

                PythonInterpreter interpreter = new PythonInterpreter();

                try {
                    String code = task.getCode();
                    ResultVariable[] parameters = task.getParameters();
                    
                    interpreter.exec("from fr.proline.studio.python.data import Col");
                    interpreter.exec("from fr.proline.studio.python.data import Table");
                    interpreter.exec("from fr.proline.studio.python.math import Stats");
                    interpreter.exec("import math");
                    if (parameters != null) {
                        for (ResultVariable parameter : parameters) {
                            interpreter.set(parameter.getName(), parameter.getValue());
                        }
                    }
                    interpreter.exec(code);

                     ArrayList<ResultVariable> resultVariableArray = new ArrayList<>();

                    PyStringMap locals = (PyStringMap) interpreter.getLocals();
                    PyList keysList = locals.keys();
                    int nbKeys = keysList.size();
                    for (int i = 0; i < nbKeys; i++) {
                        PyObject key = keysList.__getitem__(i);
                        PyObject value = locals.get(key);
                        if (value instanceof ColDoubleData) {
                            ColDoubleData col = (ColDoubleData) value;
                            String columnName = col.getColumnName();
                            if ((columnName == null) || (columnName.isEmpty())) {
                                col.setColumnName(key.toString());
                            }
                        }
                        if (value instanceof ColBooleanData) {
                            ColBooleanData col = (ColBooleanData) value;
                            String columnName = col.getColumnName();
                            if ((columnName == null) || (columnName.isEmpty())) {
                                col.setColumnName(key.toString());
                            }
                        }
                        if ((value instanceof ColDoubleData) || (value instanceof ColBooleanData) || (value instanceof PyInteger) || (value instanceof PyFloat) || (value instanceof Table) || (value instanceof PythonImage)) {

                            resultVariableArray.add(new ResultVariable(key.toString(), value));
                        }
                    }

                    runCallback(task, resultVariableArray, null);

                } catch (Throwable e) {
                    int lineError = -1;
                    StackTraceElement[] stackTraceArray = e.getStackTrace();
                    String error = null;
                    for (int i = 0; i < stackTraceArray.length; i++) {
                        if (error == null) {
                            String lineCode = stackTraceArray[i].toString();
                            if ((lineCode != null) && (!lineCode.isEmpty())) {
                                error = lineCode;
                                lineError = stackTraceArray[i].getLineNumber();
                            }
                        } else if (lineError == -1) {
                            lineError = stackTraceArray[i].getLineNumber();
                        } else {
                            break;
                        }
                    }
                    if (error == null) {
                        error = "Executing Error";
                    }
                    
                    
                    
                    runCallback(task, null, new CalcError(e, error, lineError));
                }
            }
        } catch (Throwable t) {
            LoggerFactory.getLogger("ProlineStudio.R").error("Unexpected exception in main loop of CalcInterpreterThread", t);
            m_instance = null; // reset thread
        }

    }
    
    private void runCallback(final CalcInterpreterTask task, final ArrayList<ResultVariable> resultVariableArray, final CalcError error) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                task.getCallback().run(resultVariableArray, error);
                //getTaskInfo().setFinished(success, m_taskError, false);
            }
        });
    }

}
