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
package fr.proline.studio.graphics;

import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * Base class for plots with
 *
 * @author JM235353
 */
public abstract class PlotXYAbstract extends PlotBaseAbstract {

    public static final String PLOT_PARAMETER_LIST_KEY = "Plots";
    
    public static final int LOG_SUPPRESS_VALUES = 0;
    public static final int LOG_REPLACE_VALUES = 1;
    public static final int DEFAULT_LOG_ALGO = 0;
    
    public static final String LOG_ALGO_KEY = "Log_Algo";
    public static final String LOG_ALGO_NAME = "Log Axis Algorithm";
    public static final String LOG_ALGO_OPTION1 = "Suppress Negative and Null values";
    public static final String LOG_ALGO_OPTION2 = "Replace Negative and Null Values";
    
    public static final String DEFAULT_LOG_REPLACE_VALUE_KEY = "Replacement_Value";
    public static final String DEFAULT_LOG_REPLACE_VALUE_NAME = "Replacement Value";
    
    public PlotXYAbstract(BasePlotPanel plotPanel, PlotType plotType, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface) {
        super(plotPanel, plotType, compareDataInterface, crossSelectionInterface);
    }

    @Override
    public String getEnumValueY(int index, boolean fromData, Axis axis) {
       return getEnumValueY(index, fromData);
    }

}
