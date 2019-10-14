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
package fr.proline.studio.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter on String values with wildcards : * and ? The operator could be = or
 * !=, and allows the user to search on an empty string
 *
 * @author JM235353
 */
public class StringDiffFilter extends StringFilter {

    protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.Commons");

    public StringDiffFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
        m_optionList = new String[3];
        m_optionList[0] = OPTION_EQUAL;
        m_optionList[1] = OPTION_NOT;
        m_optionList[2] = OPTION_IN;
    }

    @Override
    public Filter cloneFilter4Search() {
        StringFilter clone = new StringDiffFilter(m_variableName, m_convertValueInterface, m_modelColumn);
        clone.m_optionList = new String[2];
        clone.m_optionList[0] = OPTION_EQUAL;
        clone.m_optionList[1] = OPTION_NOT;
        //clone.m_optionList[2] = OPTION_IN; search has not multi chose
        //next attributs  have not been created
        clone.reset();
        setValuesForClone(clone);
        clone.m_filterText = m_filterText;
        return clone;
    }

    /**
     * Filter action compare object v1/v2 with this filter valeur
     *
     * @param v1 object which need compare with the filter
     * @param v2, object which need compare with the filter
     * @return
     */
    @Override
    public boolean filter(Object v1, Object v2) {
        String value = (String) v1;
        boolean found = false;
        switch (m_selectItem) {
            case OPTION_EQUAL:
                if (m_filterText.length() == 0) {
                    return true;
                }
                Matcher matcher = m_searchPattern.matcher(value);
                found = matcher.matches();
                return found;
            case OPTION_NOT:
                if (m_filterText.length() == 0) {
                    return true;
                }
                matcher = m_searchPattern.matcher(value);
                found = matcher.matches();
                return !found;
            case OPTION_IN:
                if (m_filterAreaText.length() == 0) {
                    return true;
                }
                found = false;
                for (Pattern rx : m_searchPatternList) {
                    if (rx.matcher(value).matches()) {
                        return true; //out at the first matche
                    }
                }
                return false;
        }
        return found;
    }

}
