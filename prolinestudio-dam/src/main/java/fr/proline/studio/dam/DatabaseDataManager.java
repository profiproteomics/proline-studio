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
package fr.proline.studio.dam;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.FragmentationRule;
import fr.proline.core.orm.uds.FragmentationRuleSet;
import fr.proline.core.orm.uds.InstrumentConfiguration;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.SpectrumTitleParsingRule;
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.module.seq.DatabaseAccess;
import fr.proline.repository.IDatabaseConnector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;

/**
 * Static reference of the several UDS values : project, instruments ....
 * @author jm235353
 */
public class DatabaseDataManager  {
    
    private static DatabaseDataManager m_singleton = null;
    
    private InstrumentConfiguration[] m_instruments;
    private FragmentationRuleSet[] m_fragmentationRuleSets;
    private FragmentationRule[] m_fragmentationRules;
    private PeaklistSoftware[] m_peaklistSoftwares;
    private UserAccount[] m_projectUsers;
    private UserAccount m_loggedUser;
    private String m_jdbcURL;
    private String m_jdbcDriver;
    private HashMap<Object, Object> m_serverConnectionProperties;
    
    private HashMap<Aggregation.ChildNature, Aggregation> m_aggregationMap = null;
    
    private DatabaseDataManager() {
    }
    
    public static DatabaseDataManager getDatabaseDataManager() {
        if (m_singleton == null) {
            m_singleton = new DatabaseDataManager();
        }
        return m_singleton;
    }
    
    public void setServerConnectionProperties(HashMap<Object, Object> connProperties){
        this.m_serverConnectionProperties = connProperties;
    }
    
    public void setIntruments(List<InstrumentConfiguration> l) {
        m_instruments = l.toArray(new InstrumentConfiguration[l.size()]);
    }
    
    public InstrumentConfiguration[] getInstrumentsArray() {
        return m_instruments;
    }
    
    public InstrumentConfiguration[] getInstrumentsWithNullArray() {
        
        int length = m_instruments.length;
        InstrumentConfiguration[] instrumentsWithNull = new InstrumentConfiguration[length+1];
        instrumentsWithNull[0] = null;
        System.arraycopy(m_instruments, 0, instrumentsWithNull, 1, length);
        return instrumentsWithNull;
    }
    
    public void setPeaklistSofwares(List<PeaklistSoftware> l) {
        m_peaklistSoftwares = l.toArray(new PeaklistSoftware[l.size()]);
    }
    
    public PeaklistSoftware[] getPeaklistSoftwaresArray() {
        return m_peaklistSoftwares;
    }
    
    public void getParsingRules(HashSet<String> rawFileIdentifierSet, HashSet<String> firstCycleSet, HashSet<String> lastCycleSet, HashSet<String> firstScanSet, HashSet<String> lastScanSet, HashSet<String> firstTimeSet, HashSet<String> lastTimeSet) {

        for (PeaklistSoftware peaklistSoftware : m_peaklistSoftwares) {
            
            SpectrumTitleParsingRule parsingRule = peaklistSoftware.getSpecTitleParsingRule();
            
            String rule;
            
            rule = parsingRule.getRawFileIdentifier();
            if ((rule != null) && (!rule.isEmpty())) {
                rawFileIdentifierSet.add(rule);
            }
            
            rule = parsingRule.getFirstCycle();
            if ((rule != null) && (!rule.isEmpty())) {
                firstCycleSet.add(rule);
            }
            
            rule = parsingRule.getLastCycle();
            if ((rule != null) && (!rule.isEmpty())) {
                lastCycleSet.add(rule);
            }
            
            rule = parsingRule.getFirstScan();
            if ((rule != null) && (!rule.isEmpty())) {
                firstScanSet.add(rule);
            }
            
            rule = parsingRule.getLastScan();
            if ((rule != null) && (!rule.isEmpty())) {
                lastScanSet.add(rule);
            }
            
            rule = parsingRule.getFirstTime();
            if ((rule != null) && (!rule.isEmpty())) {
                firstTimeSet.add(rule);
            }
      
            rule = parsingRule.getLastTime();
            if ((rule != null) && (!rule.isEmpty())) {
                lastTimeSet.add(rule);
            }

        }

    }
    
    public PeaklistSoftware[] getPeaklistSoftwaresWithNullArray() {
        
        int length = m_peaklistSoftwares.length;
        PeaklistSoftware[] peaklistSoftwaresWithNull = new PeaklistSoftware[length+1];
        peaklistSoftwaresWithNull[0] = null;
        System.arraycopy(m_peaklistSoftwares, 0, peaklistSoftwaresWithNull, 1, length);
        return peaklistSoftwaresWithNull;
    }
    
    public void setFragmentationRules(List<FragmentationRule> l) {
        m_fragmentationRules = l.toArray(new FragmentationRule[l.size()]);
    }
    
    public FragmentationRule[] getFragmentationRulesArray() {
        return m_fragmentationRules;
    }
      
    public void setFragmentationRuleSets(List<FragmentationRuleSet> l) {
        m_fragmentationRuleSets = l.toArray(new FragmentationRuleSet[l.size()]);
    }
    
    public FragmentationRuleSet[] getFragmentationRuleSetsArray() {
        return m_fragmentationRuleSets;
    }
    
    public FragmentationRuleSet[] getFragmentationRuleSetsWithNullArray() {        
        int length = m_fragmentationRuleSets.length;
        FragmentationRuleSet[] fragRuleSetsWithNull = new FragmentationRuleSet[length+1];
        fragRuleSetsWithNull[0] = null;
        System.arraycopy(m_fragmentationRuleSets, 0, fragRuleSetsWithNull, 1, length);
        return fragRuleSetsWithNull;
    }
    
    public FragmentationRuleSet getFragmentationRuleSet(Long frsId){
        if(frsId == null)
            return null;
        for(FragmentationRuleSet frs : m_fragmentationRuleSets){
            if(frs.getId() == frsId)
                return frs;
        }
        return null;
    }
    
    public void setAggregationList(List<Aggregation> l) {
        
        m_aggregationMap = new HashMap<>();
        
        Iterator<Aggregation> it = l.iterator();
        while (it.hasNext()) {
            Aggregation aggregation = it.next();
            m_aggregationMap.put(aggregation.getChildNature(), aggregation);
        }
    }
    
    public Aggregation getAggregation(Aggregation.ChildNature childNature) {
        return m_aggregationMap.get(childNature);
    }
    
    
    public void setProjectUsers(List<UserAccount> l) {
        m_projectUsers = l.toArray(new UserAccount[l.size()]);
    }
    
    public UserAccount[] getProjectUsersArray() {
        return m_projectUsers;
    }
    
    public void setLoggedUser(UserAccount loggedUser) {
        m_loggedUser = loggedUser;
    }
    
    public UserAccount getLoggedUser() {
        return m_loggedUser;
    }
    
    
    public String getLoggedUserName() {
        return m_loggedUser.getLogin();
    }

    public boolean ownProject(Project p) {
        if (p== null) {
            return false;
        }
        return (p.getOwner().getId() == m_loggedUser.getId());
    }
    
    public void setUdsJdbcDriver(String jdbcDriver) {
        m_jdbcDriver = jdbcDriver;
    }
    public String getUdsJdbcDriver() {
        return m_jdbcDriver;
    }

    public void setUdsJdbcURL(String jdbcURL) {
        m_jdbcURL = jdbcURL;
    }

    public String getUdsJdbcURL() {
        return  m_jdbcURL;
    }
    
    public boolean isSeqDatabaseExists() {
        
        if (m_checkDatabaseExists == null) {
        
            try {
                IDatabaseConnector seqDatabaseConnector;
                if(m_serverConnectionProperties != null && !m_serverConnectionProperties.isEmpty())
                    seqDatabaseConnector = DatabaseAccess.getSEQDatabaseConnector(false,m_serverConnectionProperties);
                else
                    seqDatabaseConnector =DatabaseAccess.getSEQDatabaseConnector(false);
                
                if (seqDatabaseConnector == null) {
                    m_checkDatabaseExists = Boolean.FALSE;
                } else {
                    m_checkDatabaseExists = Boolean.TRUE;
                }
            } catch (Throwable e) {
                m_checkDatabaseExists = Boolean.FALSE;
            }
        }

        return m_checkDatabaseExists;
    }
    private Boolean m_checkDatabaseExists = null;
    
    public static boolean isAdmin(UserAccount userAccount) {
        boolean isAdmin = false;
        try {
            Map<String, Object> map = userAccount.getSerializedPropertiesAsMap();
            if (map != null) {
                String userGroup = (String) map.get("user_group");
                if (userGroup != null) {
                    isAdmin = (userGroup.compareTo(UserAccount.UserGroupType.ADMIN.name()) == 0);
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return isAdmin;
    }

}
