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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.task.jms.FilterProteinSetsTask;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterList;

import javax.swing.*;

/**
 *
 * @author VD225637
 */
public class FilterProteinSetPanel extends JPanel {

    public static AbstractParameter[] createProteinSetFilterParameters(String keyPrefix, ParameterList parameterList) {
      
        AbstractParameter[] parameters = new AbstractParameter[FilterProteinSetsTask.Filter.values().length + 1];
        parameters[0] = null;
        int index = 1;
        for (FilterProteinSetsTask.Filter filter : FilterProteinSetsTask.Filter.values()) {
            String filterKey = keyPrefix + filter.key;
            if (filter == FilterProteinSetsTask.Filter.SCORE) {
                parameters[index] = new DoubleParameter(filterKey, filter.name, new JTextField(6), new Double(10), new Double(1), null);
                parameters[index].setAssociatedData(">=");
            }  else {
                parameters[index] = new IntegerParameter(filterKey, filter.name, new JTextField(6), new Integer(1), new Integer(1), null);
                parameters[index].setAssociatedData(">=");
            }

            AbstractParameter p = parameters[index];
            if (p != null) {
                p.setUsed(false);
                p.setCompulsory(false);
                parameterList.add(p);
            }
            index++;
        }
        
        return parameters;
    }

}
