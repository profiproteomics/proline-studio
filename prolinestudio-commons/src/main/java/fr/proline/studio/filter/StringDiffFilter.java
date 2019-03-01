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
