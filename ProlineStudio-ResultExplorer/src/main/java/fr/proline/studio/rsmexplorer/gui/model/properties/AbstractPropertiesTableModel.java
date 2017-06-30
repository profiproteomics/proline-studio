package fr.proline.studio.rsmexplorer.gui.model.properties;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.table.PropertiesTableModel;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public abstract class AbstractPropertiesTableModel extends PropertiesTableModel {
    
    protected ArrayList<DDataset> m_datasetArrayList = null;
    protected ArrayList<Long> m_datasetIdArray = null;
    protected ArrayList<Long> m_projectIdArray = null;
    
    public abstract void setData(ArrayList<DDataset> datasetArrayList);
    
}
