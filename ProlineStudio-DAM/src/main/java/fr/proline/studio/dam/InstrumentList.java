package fr.proline.studio.dam;

import fr.proline.core.orm.uds.Instrument;
import java.util.List;

/**
 * Static reference of the instruments of the current UDS
 * @author jm235353
 */
public class InstrumentList  {
    
    private static InstrumentList singleton = null;
    
    private Instrument[] instruments;
    
    private InstrumentList() {
    }
    
    public static InstrumentList getInstrumentList() {
        if (singleton == null) {
            singleton = new InstrumentList();
        }
        return singleton;
    }
    
    public void setIntruments(List<Instrument> l) {
        instruments = l.toArray(new Instrument[l.size()]);
    }
    
    public Instrument[] getArray() {
        return instruments;
    }
    
    /*public Instrument get(int i) {
        return instruments[i];
    }
    
    public int size() {
        if (instruments == null) {
            return 0;
        }
        return instruments.length;
    }*/
    
    
    
}
