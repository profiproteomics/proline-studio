package fr.proline.studio.dam;

import fr.proline.core.orm.uds.Instrument;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.core.orm.uds.UserAccount;
import java.util.List;

/**
 * Static reference of the instruments of the current UDS
 * @author jm235353
 */
public class UDSDataManager  {
    
    private static UDSDataManager singleton = null;
    
    private Instrument[] instruments;
    private PeaklistSoftware[] peaklistSoftwares;
    private UserAccount[] projectUsers;
    private UserAccount projectUser;
    
    private UDSDataManager() {
    }
    
    public static UDSDataManager getUDSDataManager() {
        if (singleton == null) {
            singleton = new UDSDataManager();
        }
        return singleton;
    }
    
    public void setIntruments(List<Instrument> l) {
        instruments = l.toArray(new Instrument[l.size()]);
    }
    
    public Instrument[] getInstrumentsArray() {
        return instruments;
    }
    
    public void setPeaklistSofwares(List<PeaklistSoftware> l) {
        peaklistSoftwares = l.toArray(new PeaklistSoftware[l.size()]);
    }
    
    public PeaklistSoftware[] getPeaklistSoftwaresArray() {
        return peaklistSoftwares;
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
    

    
}
