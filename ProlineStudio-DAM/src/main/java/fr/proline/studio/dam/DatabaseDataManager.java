package fr.proline.studio.dam;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.InstrumentConfiguration;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.module.seq.DatabaseAccess;
import fr.proline.repository.IDatabaseConnector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Static reference of the several UDS values : project, instruments ....
 * @author jm235353
 */
public class DatabaseDataManager  {
    
    private static DatabaseDataManager m_singleton = null;
    
    private InstrumentConfiguration[] m_instruments;
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
    
    public PeaklistSoftware[] getPeaklistSoftwaresWithNullArray() {
        
        int length = m_peaklistSoftwares.length;
        PeaklistSoftware[] peaklistSoftwaresWithNull = new PeaklistSoftware[length+1];
        peaklistSoftwaresWithNull[0] = null;
        System.arraycopy(m_peaklistSoftwares, 0, peaklistSoftwaresWithNull, 1, length);
        return peaklistSoftwaresWithNull;
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
                IDatabaseConnector seqDatabaseConnector = null;
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
    

}
