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
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.uds.FragmentationRule;
import fr.proline.core.orm.uds.FragmentationRuleSet;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.core.orm.uds.SpectrumTitleParsingRule;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.HashSet;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseAdminTask extends AbstractDatabaseTask {

    private long m_peaklistSoftwareId = -1;
    private String m_name; //For Peaklist Software & Fragmentation Rule Sets
    
    //Variables used for Peaklist Software
    private String m_version;
    private SpectrumTitleParsingRule m_spectrumTitleParsingRule = null;

    //Variables used for Fragmentation Rule Sets
    private List<FragmentationRule> m_fragmentationRules = null;
    // Task attributes
    private int m_action;
    private final static int ADD_PEAKLIST_SOFTWARE = 0;
    private final static int MODIFY_PEAKLIST_SOFTWARE = 1;
    private final static int ADD_FRAGMENTATION_RULE_SET = 2;
//    private final static int MODIFY_FRAGMENTATION_RULE_SET = 3;

    public DatabaseAdminTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }

    /**
     * Load PeakList Path for Rset
     */
    public void initAddPeakListSoftware(String name, String version, SpectrumTitleParsingRule spectrumTitleParsingRule) {
        setTaskInfo(new TaskInfo(" Add PeakList Software " + name, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_name = name;
        m_version = version;
        m_spectrumTitleParsingRule = spectrumTitleParsingRule;
        m_action = ADD_PEAKLIST_SOFTWARE;
    }

    public void initModifyPeakListSoftware(long id, String name, String version) {
        setTaskInfo(new TaskInfo(" Modify PeakList Software " + name, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_peaklistSoftwareId = id;
        m_name = name;
        m_version = version;
        m_action = MODIFY_PEAKLIST_SOFTWARE;
    }
    
    public void initAddFragmentationRuleSet(String name, List<FragmentationRule> fragmentationRules) {
        setTaskInfo(new TaskInfo(" Add Fragmentation Rule Set " + name, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_name = name;
        m_fragmentationRules = fragmentationRules;
        m_action = ADD_FRAGMENTATION_RULE_SET;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

    @Override
    public boolean fetchData() {
        switch (m_action) {
            case ADD_PEAKLIST_SOFTWARE:
                return addPeaklistSoftware();
            case MODIFY_PEAKLIST_SOFTWARE:
                return modifyPeaklistSoftware();
            case ADD_FRAGMENTATION_RULE_SET:
                return addFragmentationRuleSet();
        }
        return false;
    }
    
    public boolean addFragmentationRuleSet() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        
        try {
            entityManagerUDS.getTransaction().begin();
            
            FragmentationRuleSet frs = new FragmentationRuleSet();
            frs.setName(m_name);
            frs.setFragmentationRules(new HashSet(m_fragmentationRules));
               
            entityManagerUDS.persist(frs);
            reloadFragmentationRuleSets(entityManagerUDS);
            
            entityManagerUDS.getTransaction().commit();
            
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();        
        }   
        return true;
    }

    public boolean addPeaklistSoftware() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

        try {

            entityManagerUDS.getTransaction().begin();
            
            entityManagerUDS.persist(m_spectrumTitleParsingRule);
            
            PeaklistSoftware peaklistSoftware = new PeaklistSoftware();
            peaklistSoftware.setName(m_name);
            peaklistSoftware.setVersion(m_version);
            peaklistSoftware.setSpecTitleParsingRule(m_spectrumTitleParsingRule);
            
            entityManagerUDS.persist(peaklistSoftware);
            
            reloadPeakListSoftwares(entityManagerUDS);
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {

            entityManagerUDS.close();

        }

        return true;
    }

    public boolean modifyPeaklistSoftware() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

        try {

            entityManagerUDS.getTransaction().begin();

            PeaklistSoftware peaklistSoftware = entityManagerUDS.find(PeaklistSoftware.class, m_peaklistSoftwareId);

            peaklistSoftware.setName(m_name);
            peaklistSoftware.setVersion(m_version);
            
            reloadPeakListSoftwares(entityManagerUDS);
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {

            entityManagerUDS.close();

        }

        return true;
    }
    
    private void reloadPeakListSoftwares(EntityManager entityManagerUDS) {
        // Load All peaklist softwares

        TypedQuery<PeaklistSoftware> peaklistSoftwareQuery = entityManagerUDS.createQuery("SELECT p FROM fr.proline.core.orm.uds.PeaklistSoftware p ORDER BY p.name ASC", PeaklistSoftware.class);
        List<PeaklistSoftware> peaklistSoftwareList = peaklistSoftwareQuery.getResultList();
        DatabaseDataManager.getDatabaseDataManager().setPeaklistSofwares(peaklistSoftwareList);

    }
    
      private void reloadFragmentationRuleSets(EntityManager entityManagerUDS) {
        // Load All peaklist softwares

        TypedQuery<FragmentationRuleSet> fragRuleSetQuery = entityManagerUDS.createQuery("SELECT frs FROM fr.proline.core.orm.uds.FragmentationRuleSet frs ORDER BY frs.name ASC", FragmentationRuleSet.class);
        List<FragmentationRuleSet> frsList = fragRuleSetQuery.getResultList();
         frsList.stream().forEach(frs -> { 
                frs.getFragmentationRules().stream().forEach(fr -> fr.getDescription()); // init lazy collection
            });
        DatabaseDataManager.getDatabaseDataManager().setFragmentationRuleSets(frsList);
    }

}
