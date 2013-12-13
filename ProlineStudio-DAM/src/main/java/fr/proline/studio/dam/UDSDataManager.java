package fr.proline.studio.dam;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.InstrumentConfiguration;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.core.orm.uds.UserAccount;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Static reference of the instruments of the current UDS
 * @author jm235353
 */
public class UDSDataManager  {
    
    private static UDSDataManager m_singleton = null;
    
    private InstrumentConfiguration[] m_instruments;
    private PeaklistSoftware[] m_peaklistSoftwares;
    private UserAccount[] m_projectUsers;
    private UserAccount m_projectUser;
    private String m_jdbcURL;
    private String m_jdbcDriver;
    
    private HashMap<Aggregation.ChildNature, Aggregation> m_aggregationMap = null;
    
    private UDSDataManager() {
    }
    
    public static UDSDataManager getUDSDataManager() {
        if (m_singleton == null) {
            m_singleton = new UDSDataManager();
        }
        return m_singleton;
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
    
    public void setProjectUser(UserAccount projectUser) {
        m_projectUser = projectUser;
    }
    
    public UserAccount getProjectUser() {
        return m_projectUser;
    }
    
    public String getProjectUserName() {
        return m_projectUser.getLogin();
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

}
