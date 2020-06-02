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
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author JM235353
 */
public class DataBoxFrozenCopy extends AbstractDataBox {
    
    // class as parameterType, ParameterSubtypeEnum, parameter
    private HashMap<Class, HashMap<ParameterSubtypeEnum, Object>> m_frozenDataMap = new HashMap<>();
    
    public DataBoxFrozenCopy(AbstractDataBox srcDataBox) {
        super(srcDataBox.getType(), srcDataBox.getStyle());
        
        AvailableParameters avalaibleParameters = new AvailableParameters(srcDataBox);
        
        HashMap<DataParameter, Integer> parametersMap = avalaibleParameters.getParametersMap();
        Iterator<DataParameter> it = parametersMap.keySet().iterator();
        while (it.hasNext()) {
            DataParameter parameter = it.next();
            Class c = parameter.getParameterClass();
            ParameterSubtypeEnum subtype = parameter.getSubtype();
            HashMap<ParameterSubtypeEnum, Object> map = m_frozenDataMap.get(c);
            if (map == null) {
                map = new HashMap<>();
                map.put(subtype, srcDataBox.getData(c, subtype));
                m_frozenDataMap.put(c, map);
            }

        }
        
        Class specificClass =  ResultSet.class;
        ResultSet rset = (ResultSet) srcDataBox.getData(specificClass, null);
        if (rset == null) {
            DDataset dataset = (DDataset) srcDataBox.getData(DDataset.class, null);
            if (dataset!= null) {
                rset = dataset.getResultSet();
            }
        }
        if (rset == null) {
            ResultSummary rsm = (ResultSummary) srcDataBox.getData(specificClass, null);
            if (rsm != null) {
                rset = rsm.getResultSet();
            }
        }
        if (rset != null) {
            HashMap<ParameterSubtypeEnum, Object> map = m_frozenDataMap.get(specificClass);
            if (map == null) {
                map = new HashMap<>();
                map.put(null, rset);
                m_frozenDataMap.put(specificClass, map);
            }
        }
        
        specificClass =  ResultSummary.class;
        ResultSummary rsm = (ResultSummary) srcDataBox.getData(specificClass);
        if (rsm == null) {
            DDataset dataset = (DDataset) srcDataBox.getData(DDataset.class);
            if (dataset!= null) {
                rsm = dataset.getResultSummary();
            }
        }
        if (rsm != null) {
            HashMap<ParameterSubtypeEnum, Object> map = m_frozenDataMap.get(specificClass);
            if (map == null) {
                map = new HashMap<>();
                map.put(null, rsm);
                m_frozenDataMap.put(specificClass, map);
            }
        }
        
        // register out parameters
        ParameterList outParameter = new ParameterList();
        Iterator<Class> itClass = m_frozenDataMap.keySet().iterator();
        while (itClass.hasNext()) {
            Class c = itClass.next();
            outParameter.addParameter(c);
        }
        registerOutParameter(outParameter);

        
        
    }

    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        HashMap<ParameterSubtypeEnum, Object> map = m_frozenDataMap.get(parameterType);
        if (map == null) {
            return null;
        }
        return map.get(parameterSubtype);
    }
    
    @Override
    public void createPanel() {
        return; //JPM.TODO
    }

    @Override
    public void dataChanged() {
        // nothing to do, can not happen for a frozen copy
    }
}
