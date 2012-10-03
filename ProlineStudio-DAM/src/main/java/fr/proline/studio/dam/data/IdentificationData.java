package fr.proline.studio.dam.data;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.Identification;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadIdentificationFractionTask;
import java.util.List;

/**
 * Correspond to an Identification in UDS DB
 * @author JM235353
 */
public class IdentificationData extends AbstractData {
 
    Identification identification;
    
    public IdentificationData(Identification identification) {
        dataType = DataTypes.IDENTIFICATION;
        
        this.identification = identification;
        
    }
    
    @Override
    public String getName() {
        if (identification == null) {
            return "";
        } else {
            return identification.getName();
        }
    }
 
    @Override
    public void loadImpl(AbstractDatabaseCallback callback, List<AbstractData> list) {
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadIdentificationFractionTask(callback, identification, list));



    }
    
    
    
}
