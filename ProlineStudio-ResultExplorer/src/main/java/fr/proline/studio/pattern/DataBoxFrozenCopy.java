package fr.proline.studio.pattern;

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
