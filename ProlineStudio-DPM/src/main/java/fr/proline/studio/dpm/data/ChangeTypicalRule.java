/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.data;

/**
 *
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
