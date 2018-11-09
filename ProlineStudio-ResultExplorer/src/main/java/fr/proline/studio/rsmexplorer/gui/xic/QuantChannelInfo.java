package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * map quantChannel info and map info, especilaly to maintain the same colors between the maps and the quantChannels
 * @author MB243701
 */
public class QuantChannelInfo {
    
    private DQuantitationChannel[] m_quantChannels = null;
    private DDataset m_dataset = null;
    
    public QuantChannelInfo(DDataset dataset) {
        this.m_dataset = dataset;
        List<DQuantitationChannel> listQuantChannel = new ArrayList();
        if (m_dataset.getMasterQuantitationChannels() != null && !m_dataset.getMasterQuantitationChannels().isEmpty()) {
            DMasterQuantitationChannel masterChannel = m_dataset.getMasterQuantitationChannels().get(0);
            listQuantChannel = masterChannel.getQuantitationChannels();
        }
        this.m_quantChannels = new DQuantitationChannel[listQuantChannel.size()];
        listQuantChannel.toArray(m_quantChannels);
        
    }

    public DQuantitationChannel[] getQuantChannels() {
        return m_quantChannels;
    }

    public void setQuantChannels(DQuantitationChannel[] quantChannels) {
        this.m_quantChannels = quantChannels;
    }
    
    
    public String getRsmHtmlColor(int quantChIndex) {
        return  CyclicColorPalette.getHTMLColor(quantChIndex);
    }
    
    
    public Color getMapColor (Long mapId) {
        for(int i=0; i<m_quantChannels.length; i++) {
            if (compareMap( m_quantChannels[i], mapId)) {
                return CyclicColorPalette.getColor(i);
            }
        }
        return CyclicColorPalette.getColor(m_quantChannels.length);
    }
    
    public String  getMapHtmlColor (Long mapId) {
        for(int i=0; i<m_quantChannels.length; i++) {
            if (compareMap( m_quantChannels[i], mapId)) {
                return CyclicColorPalette.getHTMLColor(i);
            }
        }
        return CyclicColorPalette.getHTMLColor(m_quantChannels.length);
    }
    
    public String getMapTitle (Long mapId) {
        for (DQuantitationChannel m_quantChannel : m_quantChannels) {
            if (compareMap( m_quantChannel, mapId)) {
                return m_quantChannel.getName();
            }
        }
        return null;
    }
    
    
    public DQuantitationChannel getQuantChannelForMap(long mapId) {
        for (DQuantitationChannel quantChannel : m_quantChannels) {
            if (compareMap( quantChannel, mapId)) {
                return quantChannel;
            }
        }
        return null;
    }

    public DDataset getDataset() {
        return m_dataset;
    }
    
    
    private boolean compareMap(DQuantitationChannel m_quantChannel, Long mapId) {
        Long lcmsRawMapId = m_quantChannel.getLcmsRawMapId();
        Long lcmsMapId = m_quantChannel.getLcmsMapId();
        if (lcmsRawMapId != null && lcmsRawMapId.equals(mapId)) {
            return true;
        }
        if (lcmsMapId != null && lcmsMapId.equals(mapId)) {
            return true;
        }
        return false;
    }
    
}
