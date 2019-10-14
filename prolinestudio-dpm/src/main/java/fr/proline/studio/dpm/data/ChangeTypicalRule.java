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
package fr.proline.studio.dpm.data;

/**
 * Pattern rule for Typical Proteins
 * @author VD225637
 */
public class ChangeTypicalRule {
       
    private String m_rulePattern = null;
    private Boolean m_onAccession = null;

    public ChangeTypicalRule(String pattern, Boolean applyOnAccession) {        
        this.m_rulePattern = pattern;
        this.m_onAccession = applyOnAccession;
    }

    public Boolean getApplyOnAccession() {
        return m_onAccession;
    }

    public void setApplyOnAccession(Boolean applyOnAccession) {
        this.m_onAccession = applyOnAccession;
    }

    public String getRulePattern() {
        return m_rulePattern;
    }

    public void setRulePattern(String pattern) {
        this.m_rulePattern = pattern;
    }
    
}
