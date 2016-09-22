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
    
    private HashMap<Class, HashMap<Boolean, Object>> m_frozenDataMap = new HashMap<>();
    
    public DataBoxFrozenCopy(AbstractDataBox srcDataBox) {
        super(DataboxType.DataBoxMSQueriesForRSM, srcDataBox.getStyle());
        
        AvailableParameters avalaibleParameters = new AvailableParameters(srcDataBox);
        
        HashMap<DataParameter, Integer> parametersMap = avalaibleParameters.getParametersMap();
        Iterator<DataParameter> it = parametersMap.keySet().iterator();
        while (it.hasNext()) {
            DataParameter parameter = it.next();
            Class c = parameter.getParameterClass();
            HashMap<Boolean, Object> map = m_frozenDataMap.get(c);
            if (map == null) {
                map = new HashMap<>();
                map.put(true, srcDataBox.getData(true, c));
                map.put(false, srcDataBox.getData(false, c));
                m_frozenDataMap.put(c, map);
            }

        }
        
        Class specificClass =  ResultSet.class;
        ResultSet rset = (ResultSet) srcDataBox.getData(false, specificClass);
        if (rset == null) {
            DDataset dataset = (DDataset) srcDataBox.getData(false, DDataset.class);
            if (dataset!= null) {
                rset = dataset.getResultSet();
            }
        }
        if (rset != null) {
            HashMap<Boolean, Object> map = m_frozenDataMap.get(specificClass);
            if (map == null) {
                map = new HashMap<>();
                map.put(false, rset);
                m_frozenDataMap.put(specificClass, map);
            }
        }
        
        specificClass =  ResultSummary.class;
        ResultSummary rsm = (ResultSummary) srcDataBox.getData(false, specificClass);
        if (rsm == null) {
            DDataset dataset = (DDataset) srcDataBox.getData(false, DDataset.class);
            if (dataset!= null) {
                rsm = dataset.getResultSummary();
            }
        }
        if (rsm != null) {
            HashMap<Boolean, Object> map = m_frozenDataMap.get(specificClass);
            if (map == null) {
                map = new HashMap<>();
                map.put(false, rsm);
                m_frozenDataMap.put(specificClass, map);
            }
        }
        
        // register out parameters
        Iterator<Class> itClass = m_frozenDataMap.keySet().iterator();
        while (itClass.hasNext()) {
            Class c = itClass.next();
            GroupParameter outParameter = new GroupParameter();
            outParameter.addParameter(c, false);
            registerOutParameter(outParameter);
        }

        
        
    }

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        HashMap<Boolean, Object> map = m_frozenDataMap.get(parameterType);
        if (map == null) {
            return null;
        }
        return map.get(getArray);
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
