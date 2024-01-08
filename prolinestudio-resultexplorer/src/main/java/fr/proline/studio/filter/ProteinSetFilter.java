package fr.proline.studio.filter;

import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProteinSetFilter extends  StringFilter {

    public ProteinSetFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
    }

    @Override
    public Filter cloneFilter4Search() {
        ProteinSetFilter clone = new ProteinSetFilter(m_variableName, m_convertValueInterface, m_modelColumn);
        clone.m_optionList = new String[1];
        clone.m_optionList[0] = OPTION_EQUAL;
        //clone.m_optionList[1] = OPTION_IN; search has not multi chose
        //next attributs  have not been created
        clone.reset();
        setValuesForClone(clone);
        clone.m_filterText = m_filterText;
        return clone;
    }

    @Override
    public boolean filter(Object v1, Object v2) {
        DProteinSet value = (DProteinSet) v1;
        List<String> acc = Arrays.asList(value.getSameSubSetNames());

        boolean found = false;
        switch (m_selectItem) {
            case OPTION_EQUAL:
                if (m_filterText.length() == 0) {
                    found = true;
                } else {
                    for (String protAcc : acc) {
                        Matcher matcher = m_searchPattern.matcher(protAcc);
                        if (matcher.matches()) {
                            found = true;
                            break;
                        }
                    }
                }
                break;
            case OPTION_IN:
                if (m_filterAreaText.length() == 0) {
                    found = true;
                } else {
                    found = false;
                    for (String protAcc : acc) {
                        for (Pattern rx : m_searchPatternList) {
                            if (rx.matcher(protAcc).matches()) {
                                found = true;
                                break;
                            }
                        }
                        if(found)
                            break;
                    }
                }
                break;
        }
        return found;
    }

}
