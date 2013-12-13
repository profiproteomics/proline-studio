package fr.proline.studio.dam;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Aggregation.ChildNature;
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
    
    private static UDSDataManager singleton = null;
    
    private InstrumentConfiguration[] instruments;
    private PeaklistSoftware[] peaklistSoftwares;
    private UserAccount[] projectUsers;
    private UserAccount projectUser;
    private String jdbcURL;
    private String jdbcDriver;
    
    private HashMap<Aggregation.ChildNature, Aggregation> aggregationMap = null;
    
    private UDSDataManager() {
    }
    
    public static UDSDataManager getUDSDataManager() {
        if (singleton == null) {
            singleton = new UDSDataManager();
        }
        return singleton;
    }
    
    public void setIntruments(List<InstrumentConfiguration> l) {
        instruments = l.toArray(new InstrumentConfiguration[l.size()]);
    }
    
    public InstrumentConfiguration[] getInstrumentsArray() {
        return instruments;
    }
    
    public InstrumentConfiguration[] getInstrumentsWithNullArray() {
        
        int length = instruments.length;
        InstrumentConfiguration[] instrumentsWithNull = new InstrumentConfiguration[length+1];
        instrumentsWithNull[0] = null;
        System.arraycopy(instruments, 0, instrumentsWithNull, 1, length);
        return instrumentsWithNull;
    }
    
    public void setPeaklistSofwares(List<PeaklistSoftware> l) {
        peaklistSoftwares = l.toArray(new PeaklistSoftware[l.size()]);
    }
    
    public PeaklistSoftware[] getPeaklistSoftwaresArray() {
        return peaklistSoftwares;
    }
    
    public PeaklistSoftware[] getPeaklistSoftwaresWithNullArray() {
        
        int length = peaklistSoftwares.length;
        PeaklistSoftware[] peaklistSoftwaresWithNull = new PeaklistSoftware[length+1];
        peaklistSoftwaresWithNull[0] = null;
        System.arraycopy(peaklistSoftwares, 0, peaklistSoftwaresWithNull, 1, length);
        return peaklistSoftwaresWithNull;
    }
    
    public void setAggregationList(List<Aggregation> l) {
        
        aggregationMap = new HashMap<>();
        
        Iterator<Aggregation> it = l.iterator();
        while (it.hasNext()) {
            Aggregation aggregation = it.next();
            aggregationMap.put(aggregation.getChildNature(), aggregation);
        }
    }
    
    public Aggregation getAggregation(Aggregation.ChildNature childNature) {
        return aggregationMap.get(childNature);
    }
    
    
    public void setProjectUsers(List<UserAccount> l) {
        projectUsers = l.toArray(new UserAccount[l.size()]);
    }
    
    public UserAccount[] getProjectUsersArray() {
        return projectUsers;
    }
    
    public void setProjectUser(UserAccount projectUser) {
        this.projectUser = projectUser;
    }
    
    public UserAccount getProjectUser() {
        return projectUser;
    }
    
    public String getProjectUserName() {
        return projectUser.getLogin();
    }

    public void setUdsJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }
    public String getUdsJdbcDriver() {
        return jdbcDriver;
    }

    public void setUdsJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    public String getUdsJdbcURL() {
        return  jdbcURL;
    }

}
