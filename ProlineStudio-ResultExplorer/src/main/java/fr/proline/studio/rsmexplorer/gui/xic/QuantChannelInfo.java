package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.tasks.xic.MapAlignmentConverter;
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
    
    private List<MapAlignment> m_mapAlignments;
    private List<MapAlignment> m_allMapAlignments;
    List<MapAlignment> m_allMapAlignmentsRev;
    private List<ProcessedMap> m_allMaps;

    
    public QuantChannelInfo(DQuantitationChannel[] quantChannels) {
        this.m_quantChannels = quantChannels;
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
                return m_quantChannel.getResultFileName();
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

    public List<MapAlignment> getMapAlignments() {
        return m_mapAlignments;
    }

    public void setMapAlignments(List<MapAlignment> mapAlignments) {
        this.m_mapAlignments = mapAlignments;
    }

    public List<MapAlignment> getAllMapAlignments() {
        return m_allMapAlignments;
    }

    public void setAllMapAlignments(List<MapAlignment> allMapAlignments) {
        this.m_allMapAlignments = allMapAlignments;
        addReversedAlignment();
    }

    public List<ProcessedMap> getAllMaps() {
        return m_allMaps;
    }

    public void setAllMaps(List<ProcessedMap> allMaps) {
        this.m_allMaps = allMaps;
    }
    
    public void addReversedAlignment(){
        // add the reversed alignments
        m_allMapAlignmentsRev = new ArrayList();
        m_allMapAlignmentsRev.addAll(m_allMapAlignments);
        for (MapAlignment ma : m_allMapAlignments) {
            MapAlignment reversedMap = MapAlignmentConverter.getRevertedMapAlignment(ma);
            m_allMapAlignmentsRev.add(reversedMap);
        }
    }

    public List<MapAlignment> getAllMapAlignmentsRev() {
        return m_allMapAlignmentsRev;
    }
    
     
    
        
}
