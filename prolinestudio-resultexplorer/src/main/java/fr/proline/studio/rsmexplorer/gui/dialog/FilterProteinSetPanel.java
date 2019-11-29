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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.task.jms.FilterRSMProtSetsTask;
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
      
        AbstractParameter[] parameters = new AbstractParameter[FilterRSMProtSetsTask.FILTER_KEYS.length + 1];
        parameters[0] = null;
        for (int index = 1; index <= FilterRSMProtSetsTask.FILTER_KEYS.length; index++) {
            String filterKey = keyPrefix + FilterRSMProtSetsTask.FILTER_KEYS[index - 1];
            if (filterKey.endsWith("SCORE")) {
                parameters[index] = new DoubleParameter(filterKey, FilterRSMProtSetsTask.FILTER_NAME[index - 1], new JTextField(6), new Double(10), new Double(1), null);
                parameters[index].setAssociatedData(">=");
            } if (filterKey.endsWith("BH_ADJUSTED_PVALUE")) { 
                parameters[index] = new DoubleParameter(filterKey, FilterRSMProtSetsTask.FILTER_NAME[index - 1], new JTextField(6), new Double(1.0), new Double(0), new Double(100));
                parameters[index].setAssociatedData("<=");
            } else {
                parameters[index] = new IntegerParameter(filterKey, FilterRSMProtSetsTask.FILTER_NAME[index - 1], new JTextField(6), new Integer(1), new Integer(1), null);
                parameters[index].setAssociatedData(">=");
            }

            AbstractParameter p = parameters[index];
            if (p != null) {
                p.setUsed(false);
                p.setCompulsory(false);
                parameterList.add(p);
            }
        }
        
        return parameters;
    }

}
