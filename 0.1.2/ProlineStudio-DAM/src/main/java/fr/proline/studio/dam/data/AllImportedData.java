package fr.proline.studio.dam.data;

import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import java.util.List;

/**
 *  Data for All Imported Node
 * @author JM235353
 */
public class AllImportedData extends AbstractData {

    public AllImportedData() {
        m_dataType = AbstractData.DataTypes.ALL_IMPORTED;
    }
    
    
    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        
    }

    @Override
    public String getName() {
        return "All Imported";
    }
    
    @Override
    public boolean hasChildren() {
        return false;
    }
    
}
